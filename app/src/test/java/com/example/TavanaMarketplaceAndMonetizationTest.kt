package com.example

import com.tavana.studio.account.AccountRepository
import com.tavana.studio.account.AdminConfigRepository
import com.tavana.studio.account.AdminRole
import com.tavana.studio.account.AppMonetizationConfig
import com.tavana.studio.account.ReferralManager
import com.tavana.studio.account.ReferralRedeemResult
import com.tavana.studio.account.SubscriptionTier
import com.tavana.studio.account.UserAccount
import com.tavana.studio.account.billing.BazaarBillingAdapter
import com.tavana.studio.account.billing.MarketplaceBillingManager
import com.tavana.studio.account.billing.MarketplacePlanCatalog
import com.tavana.studio.account.billing.MarketplaceType
import com.tavana.studio.account.billing.MyketBillingAdapter
import com.tavana.studio.account.billing.PurchaseFlowResult
import com.tavana.studio.account.billing.PurchaseStatus
import com.tavana.studio.account.billing.PurchaseVerificationService
import com.tavana.studio.foundation.i18n.AppLanguage
import com.tavana.studio.foundation.i18n.TavanaStringsRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class TavanaMarketplaceAndMonetizationTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var billingManager: MarketplaceBillingManager
    private lateinit var referralManager: ReferralManager
    private lateinit var adminConfigRepo: AdminConfigRepository

    @Before
    fun setup() {
        accountRepository = AccountRepository()
        billingManager = MarketplaceBillingManager(
            accountRepository = accountRepository,
            verificationService = PurchaseVerificationService(),
            myketAdapter = MyketBillingAdapter(isSandboxMode = true),
            bazaarAdapter = BazaarBillingAdapter(isSandboxMode = true)
        )
        adminConfigRepo = AdminConfigRepository()
        referralManager = ReferralManager(accountRepository, adminConfigRepo)
    }

    // ==========================================
    // 1. MARKETPLACE BILLING & PLANS TESTS (دستور ۲)
    // ==========================================

    @Test
    fun testMarketplaceCatalog_ContainsAllSixRequiredDurations() {
        val expectedMonths = listOf(1, 3, 6, 9, 12, 24)

        // Check PLUS plans
        val plusMonths = MarketplacePlanCatalog.PLUS_PLANS.map { it.durationMonths }
        assertEquals(expectedMonths, plusMonths)

        // Check PRO plans
        val proMonths = MarketplacePlanCatalog.PRO_PLANS.map { it.durationMonths }
        assertEquals(expectedMonths, proMonths)

        // Verify prices and discounts calculate correctly
        MarketplacePlanCatalog.ALL_PLANS.forEach { plan ->
            assertTrue(plan.basePriceToman > 0)
            assertTrue(plan.finalPriceToman > 0)
            assertTrue(plan.finalPriceToman <= plan.basePriceToman)
            assertNotNull(plan.skuFor(MarketplaceType.MYKET))
            assertNotNull(plan.skuFor(MarketplaceType.BAZAAR))
        }
    }

    @Test
    fun testPurchaseFlow_CafeBazaar_AutomaticPremiumActivation() = runBlocking {
        val user = accountRepository.currentUser.value
        assertEquals(SubscriptionTier.FREE, user.subscriptionTier)
        val initialCoins = user.coinsBalance

        billingManager.selectProvider(MarketplaceType.BAZAAR)
        val targetPlan = MarketplacePlanCatalog.PRO_PLANS.first { it.durationMonths == 12 }

        val context = RuntimeEnvironment.getApplication()
        val result = billingManager.purchasePlan(context, targetPlan, user)

        assertTrue(result is PurchaseFlowResult.Success)
        assertEquals(PurchaseStatus.ACTIVE, billingManager.purchaseStatus.value)

        // Verify user account updated with PRO tier and correct duration
        val updatedUser = accountRepository.currentUser.value
        assertEquals(SubscriptionTier.PRO, updatedUser.subscriptionTier)
        assertTrue(updatedUser.isSubscriptionActive)
        assertEquals("tavana_pro_12m", updatedUser.activeSubscriptionPlanId)
        assertEquals("bazaar", updatedUser.marketplaceProvider)
        assertNotNull(updatedUser.lastOrderId)

        // Rule 3: Coins and Subscriptions MUST remain strictly separate
        assertEquals(initialCoins, updatedUser.coinsBalance)
    }

    @Test
    fun testPurchaseFlow_Myket_AutomaticPremiumActivation() = runBlocking {
        val user = accountRepository.currentUser.value
        assertEquals(SubscriptionTier.FREE, user.subscriptionTier)

        billingManager.selectProvider(MarketplaceType.MYKET)
        val targetPlan = MarketplacePlanCatalog.PLUS_PLANS.first { it.durationMonths == 6 }

        val context = RuntimeEnvironment.getApplication()
        val result = billingManager.purchasePlan(context, targetPlan, user)

        assertTrue(result is PurchaseFlowResult.Success)
        val updatedUser = accountRepository.currentUser.value
        assertEquals(SubscriptionTier.PLUS, updatedUser.subscriptionTier)
        assertTrue(updatedUser.isSubscriptionActive)
        assertEquals("tavana_plus_6m", updatedUser.activeSubscriptionPlanId)
        assertEquals("myket", updatedUser.marketplaceProvider)
    }

    @Test
    fun testRestorePurchases_RestoresActiveEntitlements() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val user = accountRepository.currentUser.value

        // Make purchase in Myket
        billingManager.selectProvider(MarketplaceType.MYKET)
        val plan = MarketplacePlanCatalog.PRO_PLANS.first { it.durationMonths == 3 }
        billingManager.purchasePlan(context, plan, user)

        // Simulate new session where tier might have reset to FREE
        accountRepository.upgradeSubscriptionTier(SubscriptionTier.FREE, 0)
        assertEquals(SubscriptionTier.FREE, accountRepository.currentUser.value.subscriptionTier)

        // Execute Restore Purchases
        val restoreResult = billingManager.restorePurchases(context, accountRepository.currentUser.value)
        assertTrue(restoreResult.isSuccess)
        assertTrue(restoreResult.getOrDefault(0) > 0)

        // User should have PRO restored
        val restoredUser = accountRepository.currentUser.value
        assertEquals(SubscriptionTier.PRO, restoredUser.subscriptionTier)
        assertTrue(restoredUser.isSubscriptionActive)
    }

    @Test
    fun testProductionReadiness_DetailsRequirementsClearly() {
        val myketReqs = MyketBillingAdapter.getProductionReadinessRequirements()
        assertFalse(myketReqs.isReadyForProduction)
        assertTrue(myketReqs.missingProductionRequirements.isNotEmpty())
        assertTrue(myketReqs.missingProductionRequirements.any { it.contains("RSA") })

        val bazaarReqs = BazaarBillingAdapter.getProductionReadinessRequirements()
        assertFalse(bazaarReqs.isReadyForProduction)
        assertTrue(bazaarReqs.missingProductionRequirements.isNotEmpty())
        assertTrue(bazaarReqs.missingProductionRequirements.any { it.contains("Client Secret") })
    }

    // ==========================================
    // 2. REFERRAL SYSTEM TESTS (دستور ۳)
    // ==========================================

    @Test
    fun testReferral_SelfReferralIsBlocked() {
        val currentUser = accountRepository.currentUser.value
        val result = referralManager.redeemReferralCode(currentUser.referralCode)

        assertTrue(result is ReferralRedeemResult.Error)
        val error = result as ReferralRedeemResult.Error
        assertTrue(error.errorMessage.contains("خودتان"))
    }

    @Test
    fun testReferral_SuccessCreditsBonusAndDiscount() {
        val initialCoins = accountRepository.currentUser.value.coinsBalance
        val friendCode = "TAV-987654"

        val result = referralManager.redeemReferralCode(friendCode)
        assertTrue(result is ReferralRedeemResult.Success)

        val updated = accountRepository.currentUser.value
        assertEquals(friendCode, updated.referredByCode)
        assertEquals(initialCoins + 25, updated.coinsBalance)
        assertEquals(15, updated.activeDiscountPercent)
        assertTrue(updated.transactions.isNotEmpty())
    }

    @Test
    fun testReferral_DuplicateRedemptionIsBlocked() {
        referralManager.redeemReferralCode("TAV-111111")
        val secondAttempt = referralManager.redeemReferralCode("TAV-222222")

        assertTrue(secondAttempt is ReferralRedeemResult.Error)
        val error = secondAttempt as ReferralRedeemResult.Error
        assertTrue(error.errorMessage.contains("قبلاً"))
    }

    // ==========================================
    // 3. LOCALIZATION TESTS (دستور ۳)
    // ==========================================

    @Test
    fun testLocalization_AllEightLanguagesSupportedWithCorrectDirections() {
        val requiredLanguages = listOf(
            AppLanguage.FA, // Persian
            AppLanguage.AR, // Arabic
            AppLanguage.EN, // English
            AppLanguage.HI, // Hindi
            AppLanguage.TH, // Thai
            AppLanguage.TL, // Filipino
            AppLanguage.TR, // Turkish
            AppLanguage.ES  // Spanish
        )

        // Verify all 8 exist in enum
        requiredLanguages.forEach { lang ->
            assertNotNull(AppLanguage.fromCode(lang.code))
            val strings = TavanaStringsRegistry.getStrings(lang)
            assertNotNull(strings)
            assertTrue(strings.appName.isNotBlank())
            assertTrue(strings.tabStage.isNotBlank())
            assertTrue(strings.actionRecord.isNotBlank())
        }

        // Verify RTL for Persian and Arabic
        assertTrue(AppLanguage.FA.isRtl)
        assertTrue(AppLanguage.AR.isRtl)

        // Verify LTR for others
        assertFalse(AppLanguage.EN.isRtl)
        assertFalse(AppLanguage.HI.isRtl)
        assertFalse(AppLanguage.TH.isRtl)
        assertFalse(AppLanguage.TL.isRtl)
        assertFalse(AppLanguage.TR.isRtl)
        assertFalse(AppLanguage.ES.isRtl)
    }

    // ==========================================
    // 4. ADMIN MONETIZATION & ROLE CONTROL TESTS
    // ==========================================

    @Test
    fun testAdminRole_RegularUserCannotChangeMonetization() {
        val regularUser = UserAccount.createGuest().copy(adminRole = AdminRole.USER)
        val newConfig = AppMonetizationConfig(aiCoachCoinCost = 99)

        val result = adminConfigRepo.updateMonetizationConfig(regularUser, newConfig)
        assertTrue(result.isFailure)
        assertEquals(20, adminConfigRepo.currentConfig.aiCoachCoinCost) // unchanged
    }

    @Test
    fun testAdminRole_AdminCanUpdateMonetizationConfig() {
        val adminUser = UserAccount.createGuest().copy(adminRole = AdminRole.ADMIN)
        val newConfig = AppMonetizationConfig(
            aiCoachCoinCost = 30,
            aiJudgeCoinCost = 25,
            vocalRemovalCoinCost = 35
        )

        val result = adminConfigRepo.updateMonetizationConfig(adminUser, newConfig)
        assertTrue(result.isSuccess)
        assertEquals(30, adminConfigRepo.currentConfig.aiCoachCoinCost)
        assertEquals(25, adminConfigRepo.currentConfig.aiJudgeCoinCost)
        assertEquals(35, adminConfigRepo.currentConfig.vocalRemovalCoinCost)
    }
}
