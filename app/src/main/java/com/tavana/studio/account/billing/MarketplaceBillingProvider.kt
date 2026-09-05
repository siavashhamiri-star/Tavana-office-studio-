package com.tavana.studio.account.billing

import android.content.Context
import android.util.Log

/**
 * Provider-agnostic interface for in-app billing across Android marketplaces
 * (Myket, Cafe Bazaar, etc.).
 */
interface MarketplaceBillingProvider {

    val marketplaceType: MarketplaceType

    /**
     * Initializes the billing connection to the marketplace service (AIDL/InAppBillingService).
     */
    suspend fun initialize(context: Context): Result<Boolean>

    /**
     * Queries available subscription plans and their real-time localized pricing.
     */
    suspend fun queryAvailablePlans(): Result<List<MarketplaceSubscriptionPlan>>

    /**
     * Launches the native purchase intent/sheet for the selected plan.
     */
    suspend fun launchPurchaseFlow(
        context: Context,
        plan: MarketplaceSubscriptionPlan,
        userId: String
    ): PurchaseFlowResult

    /**
     * Restores previously completed active purchases from the marketplace cache/server.
     */
    suspend fun restorePurchases(): Result<List<MarketplacePurchaseRecord>>

    /**
     * Consumes or acknowledges an active purchase token with the marketplace.
     */
    suspend fun acknowledgePurchase(purchaseToken: String): Result<Boolean>

    /**
     * Checks if the required marketplace app/store is installed on this device.
     */
    fun isMarketplaceInstalled(context: Context): Boolean

    /**
     * Releases IPC bindings and listeners.
     */
    fun release()
}

/**
 * Technical specification of what is needed in production for each marketplace adapter.
 */
data class MarketplaceProductionReadiness(
    val marketplace: MarketplaceType,
    val isReadyForProduction: Boolean,
    val missingProductionRequirements: List<String>
)
