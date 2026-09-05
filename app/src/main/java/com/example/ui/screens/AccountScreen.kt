package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ActiveSubscriptionCard
import com.example.ui.components.AvaCard
import com.example.ui.components.AvaPrimaryButton
import com.example.ui.components.AvaSecondaryButton
import com.example.ui.components.CoinsBalanceCard
import com.example.ui.components.LanguageSwitcherSection
import com.example.ui.components.MarketplacePlansGrid
import com.example.ui.components.MarketplaceProviderToggle
import com.example.ui.components.ReferralCard
import com.example.ui.components.SubscriptionBadge
import com.example.ui.components.TierComparisonCards
import com.example.ui.components.TransactionHistoryItem
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.studio.account.AuthProviderType
import com.tavana.studio.account.SubscriptionTier
import com.tavana.studio.account.UserAccount
import com.tavana.studio.account.billing.MarketplaceSubscriptionPlan
import com.tavana.studio.account.billing.MarketplaceType
import com.tavana.studio.foundation.i18n.AppLanguage

@Composable
fun AccountScreen(
    userAccount: UserAccount,
    selectedMarketplace: MarketplaceType = MarketplaceType.BAZAAR,
    availablePlans: List<MarketplaceSubscriptionPlan> = emptyList(),
    currentLanguage: AppLanguage = AppLanguage.FA,
    onSelectMarketplace: (MarketplaceType) -> Unit = {},
    onPurchasePlan: (MarketplaceSubscriptionPlan) -> Unit = {},
    onRestorePurchases: () -> Unit = {},
    onApplyReferralCode: (String) -> Unit = {},
    onSelectLanguage: (AppLanguage) -> Unit = {},
    onSignInGoogle: () -> Unit,
    onSignInPhone: () -> Unit,
    onLinkGoogle: () -> Unit,
    onLinkPhone: () -> Unit,
    onTopUpCoins: () -> Unit,
    onUpgradeTier: (SubscriptionTier) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item(key = "account_header") {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "پروفایل و اشتراک استودیو",
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "مدیریت خرید اشتراک از مارکت‌پلیس، سکه‌ها، رفرال و زبان",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                SubscriptionBadge(tier = userAccount.subscriptionTier)
            }
        }

        // Active Subscription Status & Restore Purchase
        item(key = "active_subscription_card") {
            ActiveSubscriptionCard(
                userAccount = userAccount,
                onRestorePurchases = onRestorePurchases
            )
        }

        // Marketplace Provider Switcher (Bazaar vs Myket)
        item(key = "marketplace_provider_toggle") {
            Column {
                Text(
                    text = "انتخاب مارکت‌پلیس خرید اشتراک:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                MarketplaceProviderToggle(
                    selected = selectedMarketplace,
                    onSelect = onSelectMarketplace
                )
            }
        }

        // Marketplace Plans Showcase (1, 3, 6, 9, 12, 24 months)
        item(key = "marketplace_plans_grid") {
            MarketplacePlansGrid(
                plans = availablePlans,
                activeTier = userAccount.subscriptionTier,
                selectedMarketplace = selectedMarketplace,
                onPurchasePlan = onPurchasePlan
            )
        }

        // Coins Balance Card (Separate from subscriptions)
        item(key = "coins_card") {
            CoinsBalanceCard(
                balance = userAccount.coinsBalance,
                onTopUpClick = onTopUpCoins
            )
        }

        // Referral System Card (دستور ۳)
        item(key = "referral_card") {
            ReferralCard(
                userAccount = userAccount,
                onApplyCode = onApplyReferralCode
            )
        }

        // Language & Localization Selector (دستور ۳ - ۸ زبان)
        item(key = "language_switcher") {
            LanguageSwitcherSection(
                currentLanguage = currentLanguage,
                onSelectLanguage = onSelectLanguage
            )
        }

        // Profile Card
        item(key = "user_profile_card") {
            AvaCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "card_user_profile"
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(AvaSunsetCoral.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccountCircle,
                                contentDescription = "Profile",
                                tint = AvaSunsetCoral,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = userAccount.displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (userAccount.email != null) {
                                Text(
                                    text = userAccount.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (userAccount.phoneNumber != null) {
                                Text(
                                    text = userAccount.phoneNumber,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = AvaTheme.colors.stageBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Account Linking Section (Google + Phone to Single Account)
                    Text(
                        text = "اتصال هویت و ورود یکپارچه (Account Linking)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "گوگل و شماره موبایل به این حساب واحد متصل می‌شوند و اشتراک و سکه‌ها حفظ می‌گردند.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Google provider status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AvaTheme.colors.stageSurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (userAccount.isGoogleLinked) Icons.Default.CheckCircle else Icons.Default.Link,
                                contentDescription = null,
                                tint = if (userAccount.isGoogleLinked) Color(0xFF38A169) else Color(0xFF718096),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (userAccount.isGoogleLinked) "گوگل: ${userAccount.email ?: "متصل شد"}" else "حساب گوگل (Google Account)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (!userAccount.isGoogleLinked) {
                            OutlinedButton(
                                onClick = onLinkGoogle,
                                modifier = Modifier.testTag("btn_link_google")
                            ) {
                                Text("اتصال به گوگل", fontSize = 12.sp)
                            }
                        } else {
                            Text(
                                text = "متصل",
                                color = Color(0xFF38A169),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Phone provider status
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(AvaTheme.colors.stageSurfaceElevated)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (userAccount.isPhoneLinked) Icons.Default.CheckCircle else Icons.Default.Phone,
                                contentDescription = null,
                                tint = if (userAccount.isPhoneLinked) Color(0xFF38A169) else Color(0xFF718096),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (userAccount.isPhoneLinked) "موبایل: ${userAccount.phoneNumber}" else "شماره موبایل (SMS)",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (!userAccount.isPhoneLinked) {
                            OutlinedButton(
                                onClick = onLinkPhone,
                                modifier = Modifier.testTag("btn_link_phone")
                            ) {
                                Text("اتصال شماره", fontSize = 12.sp)
                            }
                        } else {
                            Text(
                                text = "متصل",
                                color = Color(0xFF38A169),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }

        // Coins Balance Card
        item(key = "coins_card") {
            CoinsBalanceCard(
                balance = userAccount.coinsBalance,
                onTopUpClick = onTopUpCoins
            )
        }

        // Subscription Tier Upgrade Section
        item(key = "subscription_tiers_section") {
            TierComparisonCards(
                currentTier = userAccount.subscriptionTier,
                onSelectTier = onUpgradeTier
            )
        }

        // Transaction History Section
        item(key = "transaction_history_section") {
            AvaCard(
                modifier = Modifier.fillMaxWidth(),
                testTag = "card_tx_history"
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تاریخچه تراکنش‌های سکه استودیو",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (userAccount.transactions.isEmpty()) {
                        Text(
                            text = "هنوز تراکنشی ثبت نشده است.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        userAccount.transactions.take(8).forEach { tx ->
                            TransactionHistoryItem(transaction = tx)
                            HorizontalDivider(color = AvaTheme.colors.stageBorder.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }

        // Security & Backend Architecture Transparency
        item(key = "security_backend_notice") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF3182CE),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "امنیت و معماری Backend",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "هیچ کلید خصوصی در کلاینت قرار ندارد. تایید رسیدهای خرید و احراز هویت پیامکی از طریق Gateway امن سرور انجام می‌شود.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Sign Out Button
        item(key = "sign_out_button") {
            AvaSecondaryButton(
                text = "خروج از حساب / حالت مهمان",
                icon = Icons.Default.Logout,
                onClick = onSignOut,
                modifier = Modifier.fillMaxWidth(),
                testTag = "btn_sign_out"
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
