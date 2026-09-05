package com.example

import com.tavana.studio.account.AccountRepository
import com.tavana.studio.account.AuthProviderType
import com.tavana.studio.account.AuthResult
import com.tavana.studio.account.CoinTransactionType
import com.tavana.studio.account.FeatureAccessDecision
import com.tavana.studio.account.FeatureAccessManager
import com.tavana.studio.account.FeatureKey
import com.tavana.studio.account.FeaturePricingPolicy
import com.tavana.studio.account.SubscriptionTier
import com.tavana.studio.account.UserAccount
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TavanaAccountSystemTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var featureAccessManager: FeatureAccessManager

    @Before
    fun setup() {
        accountRepository = AccountRepository()
        featureAccessManager = FeatureAccessManager()
    }

    @Test
    fun testUnifiedAccount_GoogleAndPhoneLinking() = runBlocking {
        // Sign in with Google
        val googleResult = accountRepository.signInWithGoogle(
            idToken = "google_token_123",
            email = "artist@example.com",
            displayName = "Persian Artist"
        )
        assertTrue(googleResult is AuthResult.Success)

        val currentUser = accountRepository.currentUser.value
        assertEquals("artist@example.com", currentUser.email)
        assertTrue(currentUser.isGoogleLinked)
        assertFalse(currentUser.isPhoneLinked)
        val originalUid = currentUser.uid

        // Link Phone number to the same account
        val linkResult = accountRepository.linkPhoneToCurrentAccount(
            phoneNumber = "+989123456789",
            verificationId = "verify_otp_123",
            smsCode = "123456"
        )
        assertTrue(linkResult is AuthResult.Success)

        val linkedUser = accountRepository.currentUser.value
        assertEquals(originalUid, linkedUser.uid) // Same unified user ID
        assertEquals("artist@example.com", linkedUser.email)
        assertEquals("+989123456789", linkedUser.phoneNumber)
        assertTrue(linkedUser.isGoogleLinked)
        assertTrue(linkedUser.isPhoneLinked)
        assertTrue(linkedUser.linkedProviders.contains(AuthProviderType.GOOGLE))
        assertTrue(linkedUser.linkedProviders.contains(AuthProviderType.PHONE))
    }

    @Test
    fun testCoinsWallet_BalanceAndTransactionHistory() {
        val user = accountRepository.currentUser.value
        val initialBalance = user.coinsBalance

        // Add coins via bundle purchase
        accountRepository.addCoins(100, "bundle_standard", "خرید بسته سکه استاندارد")
        val updatedUser = accountRepository.currentUser.value
        assertEquals(initialBalance + 100, updatedUser.coinsBalance)
        assertEquals(1, updatedUser.transactions.size)
        assertEquals(CoinTransactionType.PURCHASE, updatedUser.transactions.first().type)
        assertEquals(100, updatedUser.transactions.first().amount)

        // Spend coins on AI Coach
        val spendResult = accountRepository.spendCoins(20, FeatureKey.AI_COACH, "استفاده از مربی صوتی")
        assertTrue(spendResult.isSuccess)
        val finalUser = accountRepository.currentUser.value
        assertEquals(initialBalance + 80, finalUser.coinsBalance)
        assertEquals(2, finalUser.transactions.size)
        assertEquals(CoinTransactionType.SPEND, finalUser.transactions.first().type)
    }

    @Test
    fun testCoinsWallet_InsufficientBalanceFailsSafely() {
        val user = accountRepository.currentUser.value
        // Guest user starts with 0 coins
        val spendResult = accountRepository.spendCoins(50, FeatureKey.AI_COACH, "تلاش ناموفق")
        assertTrue(spendResult.isFailure)
        assertEquals(user.coinsBalance, accountRepository.currentUser.value.coinsBalance)
    }

    @Test
    fun testFeatureGating_FreeTierLimitations() {
        val freeUser = UserAccount.createGuest() // FREE tier, 0 coins

        // 1. AI Coach: Not granted in Free without coins (prompts for coins or PRO)
        val aiDecision = featureAccessManager.evaluateAccess(freeUser, FeatureKey.AI_COACH)
        assertTrue(aiDecision is FeatureAccessDecision.CoinPaymentOption)
        val paymentOption = aiDecision as FeatureAccessDecision.CoinPaymentOption
        assertEquals(20, paymentOption.coinCost)
        assertFalse(paymentOption.canAfford)
        assertEquals(SubscriptionTier.PRO, paymentOption.requiredTier)

        // 2. AI Judge: Not granted in Free without coins (costs 15 coins or PRO)
        val judgeDecision = featureAccessManager.evaluateAccess(freeUser, FeatureKey.AI_JUDGE)
        assertTrue(judgeDecision is FeatureAccessDecision.CoinPaymentOption)

        // 3. Vocal Removal: Free user gets up to 2 tracks <= 4 minutes (240s)
        val vocalUnder4Min = featureAccessManager.evaluateAccess(freeUser, FeatureKey.VOCAL_REMOVAL, durationSeconds = 180)
        assertTrue(vocalUnder4Min is FeatureAccessDecision.GrantedFreeAllowance)

        // 4. Vocal Removal: Over 4 minutes (>240s) requires coin or upgrade
        val vocalOver4Min = featureAccessManager.evaluateAccess(freeUser, FeatureKey.VOCAL_REMOVAL, durationSeconds = 300)
        assertTrue(vocalOver4Min is FeatureAccessDecision.CoinPaymentOption)
    }

    @Test
    fun testFeatureGating_PlusAndProTiers() {
        // PLUS Tier: Unlimited Vocal removal and multi-track export
        val plusUser = UserAccount.createGuest().copy(subscriptionTier = SubscriptionTier.PLUS)
        val stemDecisionPlus = featureAccessManager.evaluateAccess(plusUser, FeatureKey.VOCAL_REMOVAL, durationSeconds = 600)
        assertTrue(stemDecisionPlus is FeatureAccessDecision.GrantedUnlimited)

        val exportPlus = featureAccessManager.evaluateAccess(plusUser, FeatureKey.MULTI_TRACK_EXPORT)
        assertTrue(exportPlus is FeatureAccessDecision.GrantedUnlimited)

        // PRO Tier: Unlimited AI Coach and AI Judge
        val proUser = UserAccount.createGuest().copy(subscriptionTier = SubscriptionTier.PRO)
        val aiDecisionPro = featureAccessManager.evaluateAccess(proUser, FeatureKey.AI_COACH)
        assertTrue(aiDecisionPro is FeatureAccessDecision.GrantedUnlimited)

        val judgeDecisionPro = featureAccessManager.evaluateAccess(proUser, FeatureKey.AI_JUDGE)
        assertTrue(judgeDecisionPro is FeatureAccessDecision.GrantedUnlimited)
    }

    @Test
    fun testPricingPolicy_Consistency() {
        assertTrue(FeaturePricingPolicy.availableCoinBundles.isNotEmpty())
        val popular = FeaturePricingPolicy.availableCoinBundles.find { it.isPopular }
        assertNotNull(popular)
        assertTrue(popular!!.priceToman > 0)
    }
}
