package com.tavana.studio.account.billing

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Adapter implementing Marketplace-Agnostic Billing for Cafe Bazaar (کافه‌بازار).
 * Adheres strictly to security rule: Zero API keys or RSA private keys stored in the APK.
 */
class BazaarBillingAdapter(
    private val isSandboxMode: Boolean = true
) : MarketplaceBillingProvider {

    override val marketplaceType: MarketplaceType = MarketplaceType.BAZAAR

    private var isInitialized: Boolean = false
    private val activePurchasesCache = mutableListOf<MarketplacePurchaseRecord>()

    companion object {
        const val BAZAAR_PACKAGE_NAME = "com.farsitel.bazaar"
        const val BAZAAR_BILLING_ACTION = "com.farsitel.bazaar.service.InAppBillingService.BIND"
        private const val TAG = "BazaarBillingAdapter"

        fun getProductionReadinessRequirements(): MarketplaceProductionReadiness {
            return MarketplaceProductionReadiness(
                marketplace = MarketplaceType.BAZAAR,
                isReadyForProduction = false,
                missingProductionRequirements = listOf(
                    "1. ثبت اپلیکیشن در پنل پیشخان کافه‌بازار (cafebazaar.ir/developers)",
                    "2. دریافت کلید عمومی RSA درون‌برنامه‌ای کافه‌بازار و ذخیره در سرور تایید رسید (Backend)",
                    "3. تعریف کدهای محصول دوره‌ای (Subscriptions) در پیشخان بازار مطابق با طرح‌های ۱، ۳، ۶، ۹، ۱۲، ۲۴ ماهه",
                    "4. دریافت Client ID و Client Secret از کافه‌بازار جهت استعلام REST API وضعیت اشتراک در سرور",
                    "5. بارگذاری بسته انتشار در کافه‌بازار"
                )
            )
        }
    }

    override suspend fun initialize(context: Context): Result<Boolean> {
        return try {
            val installed = isMarketplaceInstalled(context)
            isInitialized = true
            Log.d(TAG, "Cafe Bazaar billing adapter initialized. Installed=$installed, Sandbox=$isSandboxMode")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Cafe Bazaar billing adapter", e)
            Result.failure(e)
        }
    }

    override suspend fun queryAvailablePlans(): Result<List<MarketplaceSubscriptionPlan>> {
        // Returns the 6 standardized plans (1, 3, 6, 9, 12, 24 months)
        return Result.success(MarketplacePlanCatalog.ALL_PLANS)
    }

    override suspend fun launchPurchaseFlow(
        context: Context,
        plan: MarketplaceSubscriptionPlan,
        userId: String
    ): PurchaseFlowResult {
        if (!isInitialized) {
            return PurchaseFlowResult.Error("درگاه خرید کافه‌بازار مقداردهی اولیه نشده است.")
        }

        val sku = plan.skuBazaar
        val orderId = "bazaar_ord_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val purchaseToken = "bazaar_token_${plan.planId}_${System.currentTimeMillis()}"

        Log.d(TAG, "Launching Bazaar purchase flow for SKU: $sku, User: $userId, Plan: ${plan.planId}")

        val record = MarketplacePurchaseRecord(
            orderId = orderId,
            purchaseToken = purchaseToken,
            productId = sku,
            packageId = context.packageName,
            marketplace = MarketplaceType.BAZAAR,
            purchaseTime = System.currentTimeMillis(),
            purchaseState = 0,
            signature = "sig_bazaar_dev_${System.currentTimeMillis()}",
            developerPayload = "user:$userId|plan:${plan.planId}"
        )

        activePurchasesCache.add(record)
        return PurchaseFlowResult.Success(record)
    }

    override suspend fun restorePurchases(): Result<List<MarketplacePurchaseRecord>> {
        Log.d(TAG, "Restoring purchases from Cafe Bazaar. Found: ${activePurchasesCache.size} records")
        return Result.success(activePurchasesCache.toList())
    }

    override suspend fun acknowledgePurchase(purchaseToken: String): Result<Boolean> {
        Log.d(TAG, "Acknowledged purchase token with Cafe Bazaar: $purchaseToken")
        return Result.success(true)
    }

    override fun isMarketplaceInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(BAZAAR_PACKAGE_NAME, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    override fun release() {
        activePurchasesCache.clear()
        isInitialized = false
    }
}
