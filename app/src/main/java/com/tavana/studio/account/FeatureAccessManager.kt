package com.tavana.studio.account

/**
 * Access decision outcome from the feature monetization gate.
 */
sealed class FeatureAccessDecision {
    object GrantedUnlimited : FeatureAccessDecision()
    data class GrantedFreeAllowance(val remainingUses: Int, val maxAllowed: Int) : FeatureAccessDecision()
    data class CoinPaymentOption(
        val coinCost: Int,
        val userBalance: Int,
        val canAfford: Boolean,
        val requiredTier: SubscriptionTier,
        val reason: String
    ) : FeatureAccessDecision()
    data class UpgradeRequired(
        val requiredTier: SubscriptionTier,
        val reason: String
    ) : FeatureAccessDecision()
}

/**
 * Configuration for app monetization limits, AI costs, and referral rewards.
 * Managed centrally by Owner/Admin.
 */
data class AppMonetizationConfig(
    val freeMaxVocalRemovalUses: Int = 2,
    val freeMaxVocalRemovalDurationSeconds: Long = 240L, // 4 minutes
    val plusMaxVocalRemovalDurationSeconds: Long = 900L, // 15 minutes
    val aiCoachCoinCost: Int = 20,
    val aiJudgeCoinCost: Int = 15,
    val vocalRemovalCoinCost: Int = 25,
    val multiTrackExportCoinCost: Int = 15,
    val referralRefereeCoinsBonus: Int = 25,
    val referralRefereeDiscountPercent: Int = 15,
    val referralReferrerCoinsBonus: Int = 50,
    val isHeavyAiRestrictedOnFree: Boolean = true
)

/**
 * Evaluates feature access against tier entitlements, free allowances,
 * and coin wallet balances. Enforces AI cost protection.
 */
class FeatureAccessManager(
    private var config: AppMonetizationConfig = AppMonetizationConfig()
) {

    fun updateConfig(newConfig: AppMonetizationConfig) {
        config = newConfig
    }

    fun currentConfig(): AppMonetizationConfig = config

    /**
     * Evaluates access for a specific feature given user account state.
     */
    fun evaluateAccess(
        user: UserAccount,
        feature: FeatureKey,
        durationSeconds: Long = 0L
    ): FeatureAccessDecision {
        val tier = user.subscriptionTier

        return when (feature) {
            FeatureKey.AI_COACH -> evaluateAiCoachAccess(user, tier)
            FeatureKey.AI_JUDGE -> evaluateAiJudgeAccess(user, tier)
            FeatureKey.VOCAL_REMOVAL -> evaluateVocalRemovalAccess(user, tier, durationSeconds)
            FeatureKey.MULTI_TRACK_EXPORT -> evaluateMultiTrackExportAccess(user, tier)
            FeatureKey.ADVANCED_AUDIO_EFFECTS,
            FeatureKey.NOISE_REDUCTION -> evaluateAdvancedDspAccess(user, tier)
        }
    }

    private fun evaluateAiCoachAccess(user: UserAccount, tier: SubscriptionTier): FeatureAccessDecision {
        // PRO gets unlimited access to AI Coach
        if (tier == SubscriptionTier.PRO) {
            return FeatureAccessDecision.GrantedUnlimited
        }

        // PLUS or FREE users must use coins or upgrade to PRO (AI Cost Protection)
        val cost = config.aiCoachCoinCost
        val canAfford = user.coinsBalance >= cost
        val reason = if (tier == SubscriptionTier.FREE) {
            "مربی صوتی هوش مصنوعی شامل تحلیل پیشرفته ابری است. برای استفاده، $cost سکه پرداخت کنید یا اشتراک حرفه‌ای (PRO) تهیه نمایید."
        } else {
            "مربی صوتی با هوش مصنوعی پیشرفته مختص پلن PRO است. می‌توانید با $cost سکه از آن استفاده کنید."
        }

        return FeatureAccessDecision.CoinPaymentOption(
            coinCost = cost,
            userBalance = user.coinsBalance,
            canAfford = canAfford,
            requiredTier = SubscriptionTier.PRO,
            reason = reason
        )
    }

    private fun evaluateAiJudgeAccess(user: UserAccount, tier: SubscriptionTier): FeatureAccessDecision {
        if (tier == SubscriptionTier.PRO) {
            return FeatureAccessDecision.GrantedUnlimited
        }

        val cost = config.aiJudgeCoinCost
        val canAfford = user.coinsBalance >= cost
        val reason = "داوری هوشمند صوتی و تحلیل کوک/ریتم با هوش مصنوعی نیازمند $cost سکه یا اشتراک PRO است."

        return FeatureAccessDecision.CoinPaymentOption(
            coinCost = cost,
            userBalance = user.coinsBalance,
            canAfford = canAfford,
            requiredTier = SubscriptionTier.PRO,
            reason = reason
        )
    }

    private fun evaluateVocalRemovalAccess(
        user: UserAccount,
        tier: SubscriptionTier,
        durationSeconds: Long
    ): FeatureAccessDecision {
        // PLUS and PRO get unlimited vocal removal (up to max supported track length)
        if (tier == SubscriptionTier.PLUS || tier == SubscriptionTier.PRO) {
            return FeatureAccessDecision.GrantedUnlimited
        }

        // FREE Tier limits: max 2 uses, max 4 minutes (240s) per track
        if (durationSeconds > config.freeMaxVocalRemovalDurationSeconds) {
            val cost = config.vocalRemovalCoinCost
            return FeatureAccessDecision.CoinPaymentOption(
                coinCost = cost,
                userBalance = user.coinsBalance,
                canAfford = user.coinsBalance >= cost,
                requiredTier = SubscriptionTier.PLUS,
                reason = "در پلن رایگان، جداسازی صدای قطعات حداکثر ۴ دقیقه مجاز است. قطعه شما بیش از ۴ دقیقه است. می‌توانید با $cost سکه یا ارتقا به پلاس از آن استفاده کنید."
            )
        }

        if (user.vocalRemovalUsesCount < config.freeMaxVocalRemovalUses) {
            val remaining = config.freeMaxVocalRemovalUses - user.vocalRemovalUsesCount
            return FeatureAccessDecision.GrantedFreeAllowance(
                remainingUses = remaining,
                maxAllowed = config.freeMaxVocalRemovalUses
            )
        }

        // Exceeded free uses
        val cost = config.vocalRemovalCoinCost
        return FeatureAccessDecision.CoinPaymentOption(
            coinCost = cost,
            userBalance = user.coinsBalance,
            canAfford = user.coinsBalance >= cost,
            requiredTier = SubscriptionTier.PLUS,
            reason = "سهمیه رایگان حذف صدای خواننده (۲ بار) به پایان رسیده است. می‌توانید با $cost سکه یا ارتقا به پلن پلاس این عملیات را اجرا نمایید."
        )
    }

    private fun evaluateMultiTrackExportAccess(user: UserAccount, tier: SubscriptionTier): FeatureAccessDecision {
        if (tier == SubscriptionTier.PLUS || tier == SubscriptionTier.PRO) {
            return FeatureAccessDecision.GrantedUnlimited
        }

        val cost = config.multiTrackExportCoinCost
        return FeatureAccessDecision.CoinPaymentOption(
            coinCost = cost,
            userBalance = user.coinsBalance,
            canAfford = user.coinsBalance >= cost,
            requiredTier = SubscriptionTier.PLUS,
            reason = "خروجی مجزای هر لاین و میکس چندترکه مستلزم پلن PLUS یا پرداخت $cost سکه است."
        )
    }

    private fun evaluateAdvancedDspAccess(user: UserAccount, tier: SubscriptionTier): FeatureAccessDecision {
        if (tier == SubscriptionTier.PLUS || tier == SubscriptionTier.PRO) {
            return FeatureAccessDecision.GrantedUnlimited
        }

        return FeatureAccessDecision.UpgradeRequired(
            requiredTier = SubscriptionTier.PLUS,
            reason = "افکت‌های پیشرفته صوتی (کاهش نویز، ریورب استودیویی و تغییر گام بدون افت کیفیت) در پلن‌های پلاس و پرو فعال است."
        )
    }
}
