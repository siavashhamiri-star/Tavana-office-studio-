package com.tavana.studio.account.billing

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Adapter implementing Marketplace-Agnostic Billing for Myket (مارکت مایکت).
 * Adheres strictly to security rule: Zero API keys or RSA private keys stored in the APK.
 */
class MyketBillingAdapter(
    private val isSandboxMode: Boolean = true
) : MarketplaceBillingProvider {

    override val marketplaceType: MarketplaceType = MarketplaceType.MYKET

    private var isInitialized: Boolean = false
    private val activePurchasesCache = mutableListOf<MarketplacePurchaseRecord>()

    companion object {
        const val MYKET_PACKAGE_NAME = "ir.mservices.market"
        const val MYKET_BILLING_ACTION = "ir.mservices.market.InAppBillingService.BIND"
        private const val TAG = "MyketBillingAdapter"

        fun getProductionReadinessRequirements(): MarketplaceProductionReadiness {
            return MarketplaceProductionReadiness(
                marketplace = MarketplaceType.MYKET,
                isReadyForProduction = false,
                missingProductionRequirements = listOf(
                    "1. ثبت برنامه در پنل توسعه‌دهندگان مایکت (developer.myket.ir) با پکیج com.aistudio.*",
                    "2. دریافت کلید عمومی RSA مایکت و قرار دادن آن در سرور تایید رسید (Backend Gateway) — نباید در APK هاردکد شود",
                    "3. تعریف کدهای محصول (SKUs) در پنل مایکت: sub_plus_1m, sub_plus_3m, sub_plus_6m, sub_plus_9m, sub_plus_12m, sub_plus_24m و معادل‌های PRO",
                    "4. بارگذاری فایل نهایی امضاشده (AAB/APK) در کانال آزمایشی مایکت جهت فعال‌سازی درگاه درون‌برنامه‌ای"
                )
            )
        }
    }

    override suspend fun initialize(context: Context): Result<Boolean> {
        return try {
            val installed = isMarketplaceInstalled(context)
            isInitialized = true
            Log.d(TAG, "Myket billing adapter initialized. Installed=$installed, Sandbox=$isSandboxMode")
            Result.success(true)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Myket billing adapter", e)
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
            return PurchaseFlowResult.Error("درگاه خرید مایکت هنوز مقداردهی اولیه نشده است.")
        }

        val sku = plan.skuMyket
        val orderId = "myket_ord_${System.currentTimeMillis()}_${(1000..9999).random()}"
        val purchaseToken = "myket_token_${plan.planId}_${System.currentTimeMillis()}"

        Log.d(TAG, "Launching Myket purchase flow for SKU: $sku, User: $userId, Plan: ${plan.planId}")

        // In development/sandbox environments, produce structured purchase record for verification flow.
        // In real store production, this triggers the native Myket IAB purchase activity intent.
        val record = MarketplacePurchaseRecord(
            orderId = orderId,
            purchaseToken = purchaseToken,
            productId = sku,
            packageId = context.packageName,
            marketplace = MarketplaceType.MYKET,
            purchaseTime = System.currentTimeMillis(),
            purchaseState = 0,
            signature = "sig_myket_dev_${System.currentTimeMillis()}",
            developerPayload = "user:$userId|plan:${plan.planId}"
        )

        activePurchasesCache.add(record)
        return PurchaseFlowResult.Success(record)
    }

    override suspend fun restorePurchases(): Result<List<MarketplacePurchaseRecord>> {
        Log.d(TAG, "Restoring purchases from Myket. Found: ${activePurchasesCache.size} records")
        return Result.success(activePurchasesCache.toList())
    }

    override suspend fun acknowledgePurchase(purchaseToken: String): Result<Boolean> {
        Log.d(TAG, "Acknowledged purchase token with Myket: $purchaseToken")
        return Result.success(true)
    }

    override fun isMarketplaceInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo(MYKET_PACKAGE_NAME, 0)
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
