package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AvaGoldenHighlight
import com.example.ui.theme.AvaSunsetCoral
import com.example.ui.theme.AvaTheme
import com.tavana.studio.account.CoinBundle
import com.tavana.studio.account.CoinTransaction
import com.tavana.studio.account.CoinTransactionType
import com.tavana.studio.account.FeatureAccessDecision
import com.tavana.studio.account.FeatureKey
import com.tavana.studio.account.SubscriptionTier
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SubscriptionBadge(
    tier: SubscriptionTier,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (tier) {
        SubscriptionTier.FREE -> Color(0xFF4A5568) to Color(0xFFE2E8F0)
        SubscriptionTier.PLUS -> Color(0xFF2B6CB0) to Color(0xFFBEE3F8)
        SubscriptionTier.PRO -> Color(0xFFB7791F) to Color(0xFFFEFCBF)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (tier) {
                    SubscriptionTier.FREE -> Icons.Outlined.AccountCircle
                    SubscriptionTier.PLUS -> Icons.Default.Star
                    SubscriptionTier.PRO -> Icons.Default.Diamond
                },
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${tier.tierName} (${tier.persianTitle})",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

@Composable
fun CoinsBalanceCard(
    balance: Int,
    onTopUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AvaCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "coins_balance_card"
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD69E2E).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = "Coins",
                        tint = Color(0xFFECC94B),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "موجودی سکه استودیو (Coins)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$balance سکه",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = Color(0xFFECC94B)
                    )
                }
            }

            AvaSecondaryButton(
                text = "افزایش سکه",
                icon = Icons.Default.Add,
                onClick = onTopUpClick,
                testTag = "btn_topup_coins"
            )
        }
    }
}

@Composable
fun TierComparisonCards(
    currentTier: SubscriptionTier,
    onSelectTier: (SubscriptionTier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "پلن‌های اشتراک TAVANA Voice Studio",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))

        SubscriptionTier.values().forEach { tier ->
            val isCurrent = tier == currentTier
            val borderColor = if (isCurrent) AvaSunsetCoral else AvaTheme.colors.stageBorder

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .border(1.5.dp, borderColor, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) AvaTheme.colors.stageSurfaceElevated else AvaTheme.colors.stageSurface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SubscriptionBadge(tier = tier)
                            if (isCurrent) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "پلن فعلی شما",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AvaSunsetCoral
                                )
                            }
                        }

                        Text(
                            text = if (tier.monthlyPriceToman > 0) "${tier.monthlyPriceToman / 1000} هزار تومان/ماه" else "رایگان همیشگی",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = tier.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (!isCurrent) {
                        AvaPrimaryButton(
                            text = "ارتقا به ${tier.tierName} (${tier.persianTitle})",
                            icon = Icons.Rounded.AutoAwesome,
                            onClick = { onSelectTier(tier) },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_upgrade_${tier.tierName.lowercase()}"
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureGateDialog(
    feature: FeatureKey,
    decision: FeatureAccessDecision.CoinPaymentOption,
    onUseCoins: () -> Unit,
    onUpgrade: (SubscriptionTier) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AvaSunsetCoral,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "دسترسی ویژه به ${feature.titleFa}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column {
                Text(
                    text = decision.reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFECC94B).copy(alpha = 0.15f))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "هزینه این قابلیت:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${decision.coinCost} سکه (موجودی: ${decision.userBalance})",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFD69E2E)
                    )
                }
            }
        },
        confirmButton = {
            if (decision.canAfford) {
                AvaPrimaryButton(
                    text = "پرداخت ${decision.coinCost} سکه و اجرا",
                    icon = Icons.Default.MonetizationOn,
                    onClick = onUseCoins,
                    testTag = "gate_btn_pay_coins"
                )
            } else {
                AvaPrimaryButton(
                    text = "ارتقا به ${decision.requiredTier.tierName}",
                    icon = Icons.Default.Diamond,
                    onClick = { onUpgrade(decision.requiredTier) },
                    testTag = "gate_btn_upgrade"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun CoinShopDialog(
    bundles: List<CoinBundle>,
    onPurchaseBundle: (CoinBundle) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "فروشگاه سکه استودیو",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "بستن")
                    }
                }

                Text(
                    text = "خرید سکه برای استفاده موردی از مربی هوش مصنوعی، داور صوتی و استم‌ها",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                bundles.forEach { bundle ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { onPurchaseBundle(bundle) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (bundle.isPopular) Color(0xFFECC94B).copy(alpha = 0.12f)
                            else AvaTheme.colors.stageSurfaceElevated
                        ),
                        border = if (bundle.isPopular) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFECC94B)) else null,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFECC94B),
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${bundle.totalCoins} سکه",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (bundle.bonusCoins > 0) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "+${bundle.bonusCoins} هدیه",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AvaGoldenHighlight
                                            )
                                        }
                                    }
                                    if (bundle.isPopular) {
                                        Text(
                                            text = "محبوب‌ترین بسته",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFD69E2E)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${bundle.priceToman / 1000} هزار تومان",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "* توجه: درگاه پرداخت جهت تایید امن رسیدها به سرور متصل می‌شود. هیچ پرداخت ساختگی انجام نمی‌شود.",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun PhoneAuthDialog(
    onSendCode: (String) -> Unit,
    onVerifyCode: (String, String) -> Unit,
    onDismiss: () -> Unit,
    isCodeSent: Boolean
) {
    var phoneNumber by remember { mutableStateOf("") }
    var smsCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = AvaSunsetCoral)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ورود با شماره موبایل")
            }
        },
        text = {
            Column {
                if (!isCodeSent) {
                    Text(
                        text = "شماره موبایل خود را وارد کنید تا کد تایید ارسال شود:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("مثال: 09123456789") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_phone_number"),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = "کد پیامک‌شده به $phoneNumber را وارد نمایید:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = smsCode,
                        onValueChange = { smsCode = it },
                        label = { Text("کد ۶ رقمی") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_sms_code"),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            if (!isCodeSent) {
                AvaPrimaryButton(
                    text = "ارسال کد پیامکی",
                    onClick = { if (phoneNumber.isNotBlank()) onSendCode(phoneNumber) },
                    testTag = "btn_send_otp"
                )
            } else {
                AvaPrimaryButton(
                    text = "تایید و ورود",
                    onClick = { if (smsCode.isNotBlank()) onVerifyCode(phoneNumber, smsCode) },
                    testTag = "btn_verify_otp"
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}

@Composable
fun TransactionHistoryItem(
    transaction: CoinTransaction,
    modifier: Modifier = Modifier
) {
    val isDeduction = transaction.type == CoinTransactionType.SPEND
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDeduction) Color(0xFFE53E3E).copy(alpha = 0.15f)
                        else Color(0xFF38A169).copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isDeduction) Icons.Default.MonetizationOn else Icons.Default.Add,
                    contentDescription = null,
                    tint = if (isDeduction) Color(0xFFE53E3E) else Color(0xFF38A169),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(Date(transaction.timestamp)),
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${if (isDeduction) "-" else "+"}${transaction.amount} سکه",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isDeduction) Color(0xFFE53E3E) else Color(0xFF38A169)
            )
            Text(
                text = "مانده: ${transaction.balanceAfter}",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ==========================================
// MARKETPLACE BILLING & SUBSCRIPTION UI (دستور ۲)
// ==========================================

@Composable
fun MarketplaceProviderToggle(
    selected: com.tavana.studio.account.billing.MarketplaceType,
    onSelect: (com.tavana.studio.account.billing.MarketplaceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AvaTheme.colors.stageSurfaceElevated)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        com.tavana.studio.account.billing.MarketplaceType.values().forEach { provider ->
            val isSelected = provider == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSelected) AvaSunsetCoral else Color.Transparent)
                    .clickable { onSelect(provider) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = provider.displayNameFa,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    ),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun ActiveSubscriptionCard(
    userAccount: com.tavana.studio.account.UserAccount,
    onRestorePurchases: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()) }
    val isActive = userAccount.isSubscriptionActive

    AvaCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "card_active_subscription"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Diamond else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (isActive) AvaGoldenHighlight else Color(0xFF718096),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "وضعیت اشتراک استودیو",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                SubscriptionBadge(tier = userAccount.subscriptionTier)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (isActive) {
                Text(
                    text = "اشتراک فعال: ${userAccount.subscriptionTier.tierName} (${userAccount.subscriptionTier.persianTitle})",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF38A169)
                )
                userAccount.tierExpiryTimestamp?.let { exp ->
                    Text(
                        text = "تاریخ انقضا: ${dateFormat.format(Date(exp))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                userAccount.marketplaceProvider?.let { mp ->
                    Text(
                        text = "تهیه شده از: $mp | شناسه سفارش: ${userAccount.lastOrderId ?: "مستقیم"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "در حال حاضر از پلن رایگان (FREE) استفاده می‌کنید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onRestorePurchases,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_restore_purchases")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("بازیابی خریدهای قبلی (Restore Purchase)")
                }
            }
        }
    }
}

@Composable
fun MarketplacePlansGrid(
    plans: List<com.tavana.studio.account.billing.MarketplaceSubscriptionPlan>,
    activeTier: SubscriptionTier,
    selectedMarketplace: com.tavana.studio.account.billing.MarketplaceType,
    onPurchasePlan: (com.tavana.studio.account.billing.MarketplaceSubscriptionPlan) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTierFilter by remember { mutableStateOf(SubscriptionTier.PRO) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "پلن‌های اشتراک دوره‌ای (۱، ۳، ۶، ۹، ۱۲ و ۲۴ ماهه)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "خرید مستقیم از طریق درگاه امن ${selectedMarketplace.displayNameFa} با فعال‌سازی خودکار",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Tier Tab Selector (PLUS vs PRO)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(AvaTheme.colors.stageSurfaceElevated)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(SubscriptionTier.PRO, SubscriptionTier.PLUS).forEach { tier ->
                val isSelected = tier == selectedTierFilter
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) AvaSunsetCoral else Color.Transparent)
                        .clickable { selectedTierFilter = tier }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "طرح‌های ${tier.tierName} (${tier.persianTitle})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 6 Plans: 1, 3, 6, 9, 12, 24 months
        val filteredPlans = plans.filter { it.tier == selectedTierFilter }
        filteredPlans.forEach { plan ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .border(1.dp, AvaTheme.colors.stageBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = AvaTheme.colors.stageSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = plan.titleFa,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            plan.badgeLabel?.let { badge ->
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(AvaSunsetCoral.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = badge,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = AvaSunsetCoral
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${plan.finalPriceToman / 1000} هزار تومان",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = AvaGoldenHighlight
                            )
                            if (plan.discountPercent > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${plan.basePriceToman / 1000}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    AvaPrimaryButton(
                        text = "خرید اشتراک",
                        onClick = { onPurchasePlan(plan) },
                        testTag = "btn_buy_${plan.planId}"
                    )
                }
            }
        }
    }
}

// ==========================================
// REFERRAL & LOCALIZATION UI (دستور ۳)
// ==========================================

@Composable
fun ReferralCard(
    userAccount: com.tavana.studio.account.UserAccount,
    onApplyCode: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredCode by remember { mutableStateOf("") }

    AvaCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "card_referral_system"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = AvaGoldenHighlight,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "برنامه معرف و دعوت از دوستان (Referral)",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "کد معرف خود را با دوستان به اشتراک بگذارید تا هر دو سکه هدیه و تخفیف دریافت کنید.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // User's own referral code
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AvaTheme.colors.stageSurfaceElevated)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "کد معرف اختصاصی شما:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userAccount.referralCode,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = AvaSunsetCoral
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AvaSunsetCoral.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "تعداد معرفی: ${userAccount.referralCount}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AvaSunsetCoral
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Enter a friend's code
            if (userAccount.referredByCode == null) {
                Text(
                    text = "ورود کد معرف دوست شما:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = { enteredCode = it },
                        placeholder = { Text("مثال: TAV-123456") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_referral_code"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    AvaPrimaryButton(
                        text = "اعمال کد",
                        onClick = {
                            if (enteredCode.isNotBlank()) {
                                onApplyCode(enteredCode)
                                enteredCode = ""
                            }
                        },
                        testTag = "btn_apply_referral"
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF38A169))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "کد معرف ${userAccount.referredByCode} با موفقیت ثبت شده است.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF38A169)
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageSwitcherSection(
    currentLanguage: com.tavana.studio.foundation.i18n.AppLanguage,
    onSelectLanguage: (com.tavana.studio.foundation.i18n.AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    val languages = listOf(
        com.tavana.studio.foundation.i18n.AppLanguage.FA,
        com.tavana.studio.foundation.i18n.AppLanguage.AR,
        com.tavana.studio.foundation.i18n.AppLanguage.EN,
        com.tavana.studio.foundation.i18n.AppLanguage.ES,
        com.tavana.studio.foundation.i18n.AppLanguage.TR,
        com.tavana.studio.foundation.i18n.AppLanguage.HI,
        com.tavana.studio.foundation.i18n.AppLanguage.TH,
        com.tavana.studio.foundation.i18n.AppLanguage.TL
    )

    AvaCard(
        modifier = modifier.fillMaxWidth(),
        testTag = "card_language_selector"
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "زبان و چیدمان برنامه (Language & Localization)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "پشتیبانی کامل از زبان‌های فارسی، عربی، انگلیسی، اسپانیایی، ترکی، هندی، تایلندی و فیلیپینی با تغییر جهت RTL/LTR",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                languages.take(4).forEach { lang ->
                    LanguageChip(
                        language = lang,
                        isSelected = lang == currentLanguage,
                        onSelect = { onSelectLanguage(lang) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                languages.drop(4).take(4).forEach { lang ->
                    LanguageChip(
                        language = lang,
                        isSelected = lang == currentLanguage,
                        onSelect = { onSelectLanguage(lang) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageChip(
    language: com.tavana.studio.foundation.i18n.AppLanguage,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AvaSunsetCoral else AvaTheme.colors.stageSurfaceElevated)
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = language.nativeName,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

