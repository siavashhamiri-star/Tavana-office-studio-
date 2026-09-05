package com.tavana.studio.account

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Result of attempting to redeem a referral code.
 */
sealed class ReferralRedeemResult {
    data class Success(
        val bonusCoins: Int,
        val discountPercent: Int,
        val message: String
    ) : ReferralRedeemResult()

    data class Error(val errorMessage: String) : ReferralRedeemResult()
}

/**
 * Manages referral code generation, validation, and anti-abuse mechanics.
 */
class ReferralManager(
    private val accountRepository: AccountRepository,
    private val configRepository: AdminConfigRepository = AdminConfigRepository()
) {

    /**
     * Attempts to apply a friend's referral code to the current user account.
     * Prevents:
     * - Self-referral
     * - Multiple referral redemptions on the same account
     * - Blank or invalid codes
     */
    fun redeemReferralCode(rawCode: String): ReferralRedeemResult {
        val code = rawCode.trim().uppercase()
        val currentUser = accountRepository.currentAccountSnapshot

        // 1. Validate non-empty
        if (code.isBlank() || code.length < 5) {
            return ReferralRedeemResult.Error("کد معرف نامعتبر است. لطفاً کد صحیح را وارد کنید.")
        }

        // 2. Prevent self-referral
        if (code == currentUser.referralCode.uppercase()) {
            return ReferralRedeemResult.Error("امکان استفاده از کد معرف خودتان وجود ندارد.")
        }

        // 3. Prevent duplicate referral application
        if (currentUser.referredByCode != null) {
            return ReferralRedeemResult.Error("شما قبلاً یک بار از کد معرف (${currentUser.referredByCode}) استفاده کرده‌اید.")
        }

        // 4. Calculate rewards from dynamic configuration
        val config = configRepository.currentConfig
        val bonusCoins = config.referralRefereeCoinsBonus
        val discount = config.referralRefereeDiscountPercent

        // 5. Apply reward to current user
        accountRepository.applyReferralSuccess(
            appliedCode = code,
            bonusCoins = bonusCoins,
            discountPercent = discount
        )

        return ReferralRedeemResult.Success(
            bonusCoins = bonusCoins,
            discountPercent = discount,
            message = "کد معرف با موفقیت اعمال شد! $bonusCoins سکه هدیه و $discount% تخفیف خرید اشتراک به شما تعلق گرفت."
        )
    }
}

/**
 * Manages administrative monetization configurations with Role-Based Access Control (RBAC).
 * Enforces zero-secret architecture: no admin secrets or master keys inside the APK.
 */
class AdminConfigRepository {

    private val _config = MutableStateFlow(AppMonetizationConfig())
    val config: StateFlow<AppMonetizationConfig> = _config.asStateFlow()

    val currentConfig: AppMonetizationConfig get() = _config.value

    /**
     * Updates monetization parameters if the user possesses ADMIN or OWNER role.
     */
    fun updateMonetizationConfig(
        user: UserAccount,
        newConfig: AppMonetizationConfig
    ): Result<AppMonetizationConfig> {
        if (!user.adminRole.hasAdminAccess) {
            return Result.failure(
                SecurityException("دسترسی غیرمجاز: برای تغییر پیکربندی سیستم، دسترسی سطح مدیر (ADMIN) یا مالک (OWNER) الزامی است.")
            )
        }

        _config.update { newConfig }
        return Result.success(newConfig)
    }
}
