package com.tavana.studio.account.billing

import android.content.Context
import android.util.Log
import com.tavana.studio.account.AccountRepository
import com.tavana.studio.account.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Orchestrator for Marketplace-Agnostic Billing.
 * Enforces the unified lifecycle:
 * User → Select Plan → Marketplace Purchase → Purchase Verification → Entitlement → Premium Activation
 */
class MarketplaceBillingManager(
    private val accountRepository: AccountRepository,
    private val verificationService: PurchaseVerificationService = PurchaseVerificationService(),
    private val myketAdapter: MyketBillingAdapter = MyketBillingAdapter(),
    private val bazaarAdapter: BazaarBillingAdapter = BazaarBillingAdapter()
) {

    companion object {
        private const val TAG = "MarketplaceBillingManager"
    }

    private val _selectedProvider = MutableStateFlow(MarketplaceType.BAZAAR)
    val selectedProvider: StateFlow<MarketplaceType> = _selectedProvider.asStateFlow()

    private val _purchaseStatus = MutableStateFlow(PurchaseStatus.IDLE)
    val purchaseStatus: StateFlow<PurchaseStatus> = _purchaseStatus.asStateFlow()

    private val _activePlanUnderPurchase = MutableStateFlow<MarketplaceSubscriptionPlan?>(null)
    val activePlanUnderPurchase: StateFlow<MarketplaceSubscriptionPlan?> = _activePlanUnderPurchase.asStateFlow()

    private val _billingMessage = MutableStateFlow<String?>(null)
    val billingMessage: StateFlow<String?> = _billingMessage.asStateFlow()

    fun selectProvider(type: MarketplaceType) {
        _selectedProvider.value = type
    }

    fun currentProvider(): MarketplaceBillingProvider {
        return when (_selectedProvider.value) {
            MarketplaceType.MYKET -> myketAdapter
            MarketplaceType.BAZAAR -> bazaarAdapter
        }
    }

    suspend fun initialize(context: Context): Boolean {
        val bzOk = bazaarAdapter.initialize(context).isSuccess
        val myOk = myketAdapter.initialize(context).isSuccess
        return bzOk || myOk
    }

    /**
     * Executes the complete subscription purchase flow.
     * Flow:
     * User Selects Plan -> Marketplace Purchase Flow -> Secure Verification -> Entitlement -> Automatic Premium Activation
     */
    suspend fun purchasePlan(
        context: Context,
        plan: MarketplaceSubscriptionPlan,
        user: UserAccount
    ): PurchaseFlowResult {
        val provider = currentProvider()
        _activePlanUnderPurchase.value = plan
        _purchaseStatus.value = PurchaseStatus.IN_PROGRESS
        _billingMessage.value = "در حال انتقال به درگاه ${provider.marketplaceType.displayNameFa}..."

        Log.d(TAG, "Starting purchase for plan ${plan.planId} via ${provider.marketplaceType}")

        // 1. Launch Purchase Flow in Marketplace Adapter
        val flowResult = provider.launchPurchaseFlow(context, plan, user.uid)

        when (flowResult) {
            is PurchaseFlowResult.Success -> {
                // 2. Purchase Verification
                _purchaseStatus.value = PurchaseStatus.VERIFYING
                _billingMessage.value = "در حال بررسی و اعتبارسنجی رسید خرید در سرور امن..."
                val verifyRes = verificationService.verifyPurchase(flowResult.record)

                when (verifyRes) {
                    is VerificationResult.Valid -> {
                        // 3. Acknowledge with marketplace
                        provider.acknowledgePurchase(flowResult.record.purchaseToken)

                        // 4. Entitlement & Automatic Premium Activation in Central Account
                        accountRepository.activateMarketplaceSubscription(
                            tier = verifyRes.plan.tier,
                            durationDays = verifyRes.durationDays,
                            planId = verifyRes.plan.planId,
                            marketplace = flowResult.record.marketplace.id,
                            orderId = flowResult.record.orderId
                        )

                        _purchaseStatus.value = PurchaseStatus.ACTIVE
                        _billingMessage.value = "اشتراک ${verifyRes.plan.titleFa} با موفقیت فعال شد! دسترسی ویژه باز است."
                        return flowResult
                    }
                    is VerificationResult.Invalid -> {
                        _purchaseStatus.value = PurchaseStatus.FAILED
                        _billingMessage.value = "خطا در تایید رسید خرید: ${verifyRes.reason}"
                        return PurchaseFlowResult.Error(verifyRes.reason)
                    }
                }
            }
            is PurchaseFlowResult.Pending -> {
                _purchaseStatus.value = PurchaseStatus.PENDING_MARKETPLACE
                _billingMessage.value = "سفارش در وضعیت پرداخت درگاه قرار گرفت (${flowResult.orderId}). منتظر تایید باشید."
                return flowResult
            }
            is PurchaseFlowResult.Cancelled -> {
                _purchaseStatus.value = PurchaseStatus.CANCELLED
                _billingMessage.value = "عملیات خرید توسط کاربر لغو گردید."
                return flowResult
            }
            is PurchaseFlowResult.Error -> {
                _purchaseStatus.value = PurchaseStatus.FAILED
                _billingMessage.value = flowResult.errorMessage
                return flowResult
            }
        }
    }

    /**
     * Restores previously active purchases from the marketplace and activates entitlements.
     */
    suspend fun restorePurchases(context: Context, user: UserAccount): Result<Int> {
        val provider = currentProvider()
        _purchaseStatus.value = PurchaseStatus.VERIFYING
        _billingMessage.value = "در حال بازیابی خریدهای پیشین از ${provider.marketplaceType.displayNameFa}..."

        val restoreRes = provider.restorePurchases()
        if (restoreRes.isFailure) {
            _purchaseStatus.value = PurchaseStatus.FAILED
            _billingMessage.value = "خطا در برقراری ارتباط با مارکت جهت بازیابی خریدها."
            return Result.failure(restoreRes.exceptionOrNull() ?: Exception("Restore failed"))
        }

        val records = restoreRes.getOrNull().orEmpty()
        if (records.isEmpty()) {
            _purchaseStatus.value = PurchaseStatus.IDLE
            _billingMessage.value = "هیچ اشتراک فعالی برای این حساب در ${provider.marketplaceType.displayNameFa} یافت نشد."
            return Result.success(0)
        }

        var restoredCount = 0
        for (rec in records) {
            val verify = verificationService.verifyPurchase(rec)
            if (verify is VerificationResult.Valid) {
                accountRepository.activateMarketplaceSubscription(
                    tier = verify.plan.tier,
                    durationDays = verify.durationDays,
                    planId = verify.plan.planId,
                    marketplace = rec.marketplace.id,
                    orderId = rec.orderId
                )
                restoredCount++
            }
        }

        _purchaseStatus.value = PurchaseStatus.RESTORED
        _billingMessage.value = "$restoredCount اشتراک با موفقیت بازیابی و روی حساب فعال شد."
        return Result.success(restoredCount)
    }

    fun dismissBillingMessage() {
        _billingMessage.value = null
        if (_purchaseStatus.value != PurchaseStatus.ACTIVE && _purchaseStatus.value != PurchaseStatus.RESTORED) {
            _purchaseStatus.value = PurchaseStatus.IDLE
        }
    }

    fun getProductionRequirements(type: MarketplaceType): MarketplaceProductionReadiness {
        return when (type) {
            MarketplaceType.MYKET -> MyketBillingAdapter.getProductionReadinessRequirements()
            MarketplaceType.BAZAAR -> BazaarBillingAdapter.getProductionReadinessRequirements()
        }
    }
}
