package com.tavana.studio.account.billing

import com.tavana.studio.account.SubscriptionTier

/**
 * Supported Iranian and international marketplace providers.
 */
enum class MarketplaceType(val id: String, val displayNameFa: String, val displayNameEn: String) {
    MYKET("myket", "مایکت (Myket)", "Myket"),
    BAZAAR("bazaar", "کافه‌بازار (Cafe Bazaar)", "Cafe Bazaar")
}

/**
 * Purchase lifecycle status state machine.
 */
enum class PurchaseStatus {
    IDLE,
    IN_PROGRESS,
    PENDING_MARKETPLACE,
    VERIFYING,
    ACTIVE,
    RESTORED,
    FAILED,
    CANCELLED
}

/**
 * Represents a configurable subscription plan offered in the marketplace.
 * Covers all 6 required durations: 1, 3, 6, 9, 12, 24 months.
 */
data class MarketplaceSubscriptionPlan(
    val planId: String,
    val durationMonths: Int,
    val tier: SubscriptionTier,
    val titleFa: String,
    val titleEn: String,
    val basePriceToman: Long,
    val discountPercent: Int = 0,
    val skuMyket: String,
    val skuBazaar: String,
    val badgeLabel: String? = null
) {
    val finalPriceToman: Long
        get() = (basePriceToman * (100 - discountPercent) / 100L).coerceAtLeast(0L)

    fun skuFor(type: MarketplaceType): String {
        return when (type) {
            MarketplaceType.MYKET -> skuMyket
            MarketplaceType.BAZAAR -> skuBazaar
        }
    }
}

/**
 * Standard catalog containing the 6 required durations:
 * 1, 3, 6, 9, 12, 24 months for PLUS and PRO tiers.
 */
object MarketplacePlanCatalog {

    val PLUS_PLANS: List<MarketplaceSubscriptionPlan> = listOf(
        MarketplaceSubscriptionPlan(
            planId = "tavana_plus_1m",
            durationMonths = 1,
            tier = SubscriptionTier.PLUS,
            titleFa = "اشتراک ۱ ماهه پلاس",
            titleEn = "1-Month Plus",
            basePriceToman = 89000L,
            discountPercent = 0,
            skuMyket = "sub_plus_1m",
            skuBazaar = "sub_plus_1m"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_plus_3m",
            durationMonths = 3,
            tier = SubscriptionTier.PLUS,
            titleFa = "اشتراک ۳ ماهه پلاس",
            titleEn = "3-Months Plus",
            basePriceToman = 267000L,
            discountPercent = 10,
            skuMyket = "sub_plus_3m",
            skuBazaar = "sub_plus_3m",
            badgeLabel = "۱۰٪ تخفیف"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_plus_6m",
            durationMonths = 6,
            tier = SubscriptionTier.PLUS,
            titleFa = "اشتراک ۶ ماهه پلاس",
            titleEn = "6-Months Plus",
            basePriceToman = 534000L,
            discountPercent = 20,
            skuMyket = "sub_plus_6m",
            skuBazaar = "sub_plus_6m",
            badgeLabel = "۲۰٪ تخفیف"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_plus_9m",
            durationMonths = 9,
            tier = SubscriptionTier.PLUS,
            titleFa = "اشتراک ۹ ماهه پلاس",
            titleEn = "9-Months Plus",
            basePriceToman = 801000L,
            discountPercent = 25,
            skuMyket = "sub_plus_9m",
            skuBazaar = "sub_plus_9m",
            badgeLabel = "۲۵٪ تخفیف"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_plus_12m",
            durationMonths = 12,
            tier = SubscriptionTier.PLUS,
            titleFa = "اشتراک ۱۲ ماهه (۱ ساله) پلاس",
            titleEn = "12-Months Plus (1 Year)",
            basePriceToman = 1068000L,
            discountPercent = 35,
            skuMyket = "sub_plus_12m",
            skuBazaar = "sub_plus_12m",
            badgeLabel = "محبوب‌ترین (۳۵٪ تخفیف)"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_plus_24m",
            durationMonths = 24,
            tier = SubscriptionTier.PLUS,
            titleFa = "اشتراک ۲۴ ماهه (۲ ساله) پلاس",
            titleEn = "24-Months Plus (2 Years)",
            basePriceToman = 2136000L,
            discountPercent = 50,
            skuMyket = "sub_plus_24m",
            skuBazaar = "sub_plus_24m",
            badgeLabel = "حداکثر صرفه‌جویی (۵۰٪ تخفیف)"
        )
    )

    val PRO_PLANS: List<MarketplaceSubscriptionPlan> = listOf(
        MarketplaceSubscriptionPlan(
            planId = "tavana_pro_1m",
            durationMonths = 1,
            tier = SubscriptionTier.PRO,
            titleFa = "اشتراک ۱ ماهه پرو استودیو",
            titleEn = "1-Month Pro Studio",
            basePriceToman = 179000L,
            discountPercent = 0,
            skuMyket = "sub_pro_1m",
            skuBazaar = "sub_pro_1m"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_pro_3m",
            durationMonths = 3,
            tier = SubscriptionTier.PRO,
            titleFa = "اشتراک ۳ ماهه پرو استودیو",
            titleEn = "3-Months Pro Studio",
            basePriceToman = 537000L,
            discountPercent = 15,
            skuMyket = "sub_pro_3m",
            skuBazaar = "sub_pro_3m",
            badgeLabel = "۱۵٪ تخفیف"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_pro_6m",
            durationMonths = 6,
            tier = SubscriptionTier.PRO,
            titleFa = "اشتراک ۶ ماهه پرو استودیو",
            titleEn = "6-Months Pro Studio",
            basePriceToman = 1074000L,
            discountPercent = 25,
            skuMyket = "sub_pro_6m",
            skuBazaar = "sub_pro_6m",
            badgeLabel = "۲۵٪ تخفیف"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_pro_9m",
            durationMonths = 9,
            tier = SubscriptionTier.PRO,
            titleFa = "اشتراک ۹ ماهه پرو استودیو",
            titleEn = "9-Months Pro Studio",
            basePriceToman = 1611000L,
            discountPercent = 30,
            skuMyket = "sub_pro_9m",
            skuBazaar = "sub_pro_9m",
            badgeLabel = "۳۰٪ تخفیف"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_pro_12m",
            durationMonths = 12,
            tier = SubscriptionTier.PRO,
            titleFa = "اشتراک ۱۲ ماهه (۱ ساله) پرو استودیو",
            titleEn = "12-Months Pro Studio (1 Year)",
            basePriceToman = 2148000L,
            discountPercent = 40,
            skuMyket = "sub_pro_12m",
            skuBazaar = "sub_pro_12m",
            badgeLabel = "پیشنهاد حرفه‌ای‌ها (۴۰٪ تخفیف)"
        ),
        MarketplaceSubscriptionPlan(
            planId = "tavana_pro_24m",
            durationMonths = 24,
            tier = SubscriptionTier.PRO,
            titleFa = "اشتراک ۲۴ ماهه (۲ ساله) پرو استودیو",
            titleEn = "24-Months Pro Studio (2 Years)",
            basePriceToman = 4296000L,
            discountPercent = 55,
            skuMyket = "sub_pro_24m",
            skuBazaar = "sub_pro_24m",
            badgeLabel = "ویژه مسترینگ (۵۵٪ تخفیف)"
        )
    )

    val ALL_PLANS: List<MarketplaceSubscriptionPlan> = PLUS_PLANS + PRO_PLANS

    fun findPlanById(planId: String): MarketplaceSubscriptionPlan? {
        return ALL_PLANS.firstOrNull { it.planId == planId }
    }
}

/**
 * Standard immutable record for a purchase retrieved from Marketplace.
 */
data class MarketplacePurchaseRecord(
    val orderId: String,
    val purchaseToken: String,
    val productId: String,
    val packageId: String,
    val marketplace: MarketplaceType,
    val purchaseTime: Long = System.currentTimeMillis(),
    val purchaseState: Int = 0, // 0 = Purchased
    val signature: String? = null,
    val developerPayload: String? = null
)

/**
 * Purchase flow result returned by Marketplace adapters.
 */
sealed class PurchaseFlowResult {
    data class Success(val record: MarketplacePurchaseRecord) : PurchaseFlowResult()
    object Cancelled : PurchaseFlowResult()
    data class Pending(val orderId: String, val message: String) : PurchaseFlowResult()
    data class Error(val errorMessage: String, val errorCode: Int? = null) : PurchaseFlowResult()
}

/**
 * Verification outcome from Secure Backend Gateway.
 */
sealed class VerificationResult {
    data class Valid(
        val plan: MarketplaceSubscriptionPlan,
        val durationDays: Int,
        val verifiedAt: Long = System.currentTimeMillis()
    ) : VerificationResult()

    data class Invalid(val reason: String) : VerificationResult()
}
