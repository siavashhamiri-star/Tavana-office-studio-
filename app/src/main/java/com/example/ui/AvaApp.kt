package com.example.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AvaBottomBar
import com.example.ui.components.AvaNavDestination
import com.example.ui.components.CoinShopDialog
import com.example.ui.components.FeatureGateDialog
import com.example.ui.components.PhoneAuthDialog
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KaraokeStageScreen
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.RecordingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.AvaTheme
import com.example.ui.viewmodel.AvaViewModel
import com.tavana.studio.account.FeaturePricingPolicy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.tavana.studio.foundation.accessibility.LocalAccessibilityProfile
import com.tavana.studio.foundation.i18n.LocalAppLanguage
import com.tavana.studio.foundation.i18n.LocalTavanaStrings
import com.tavana.studio.foundation.i18n.TavanaStringsRegistry

@Composable
fun AvaApp(
    viewModel: AvaViewModel = viewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.attachContext(context)
    }

    val uiState by viewModel.uiState.collectAsState()
    val songs by viewModel.songs.collectAsState()
    val recordings by viewModel.recordings.collectAsState()
    val exercises by viewModel.practiceExercises.collectAsState()

    val currentLanguage = uiState.appLanguage
    val localizedStrings = TavanaStringsRegistry.getStrings(currentLanguage)
    val layoutDirection = if (currentLanguage.isRtl || uiState.isPersianRtlEnabled) LayoutDirection.Rtl else LayoutDirection.Ltr

    AvaTheme {
        CompositionLocalProvider(
            LocalLayoutDirection provides layoutDirection,
            LocalAppLanguage provides currentLanguage,
            LocalTavanaStrings provides localizedStrings,
            LocalAccessibilityProfile provides uiState.accessibilityProfile
        ) {
            if (uiState.isKaraokeScreenActive) {
                // Immersive Karaoke Singing Stage
                KaraokeStageScreen(
                    uiState = uiState,
                    onBackClick = { viewModel.closeKaraokeStage() },
                    onPlayPauseToggle = { viewModel.togglePlayPause() },
                    onSeek = { viewModel.seekTo(it) },
                    onPitchShiftChange = { viewModel.setPitchShift(it) },
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) },
                    onVocalGuideToggle = { viewModel.toggleVocalGuide() },
                    onStartRecording = { viewModel.startRecording() },
                    onStopRecording = { viewModel.stopRecordingAndEvaluate() },
                    onPauseResumeRecording = { viewModel.pauseResumeRecording() },
                    onSaveTake = { viewModel.saveCompletedTake() },
                    onSingAgain = {
                        viewModel.dismissScoreDialog()
                        viewModel.seekTo(0L)
                        viewModel.startRecording()
                    },
                    onDismissScoreDialog = { viewModel.dismissScoreDialog() },
                    onToggleRtl = { viewModel.togglePersianRtl() },
                    onToggleVoiceMonitoring = { viewModel.toggleVoiceMonitoring() },
                    onPlayRecordedTake = { viewModel.playLatestTake() }
                )
            } else {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        AvaBottomBar(
                            currentDestination = uiState.currentTab,
                            onNavigateTo = { viewModel.selectTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = uiState.currentTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tabTransition"
                        ) { targetTab ->
                            when (targetTab) {
                                AvaNavDestination.STAGE -> {
                                    HomeScreen(
                                        songs = songs,
                                        recordings = recordings,
                                        exercises = exercises,
                                        onStartSingingHero = {
                                            viewModel.launchSongOnStage(songs.first())
                                        },
                                        onSongSelected = { song ->
                                            viewModel.launchSongOnStage(song)
                                        },
                                        onStartPractice = { exercise ->
                                            viewModel.startPracticeDrill(exercise)
                                        },
                                        onViewAllRecordings = {
                                            viewModel.selectTab(AvaNavDestination.RECORDINGS)
                                        },
                                        onToggleRtl = { viewModel.togglePersianRtl() },
                                        isRtlActive = uiState.isPersianRtlEnabled,
                                        onPlayRecordingTake = { take ->
                                            viewModel.playRecordingTake(take)
                                        },
                                        playingRecordingId = uiState.playingRecordingId
                                    )
                                }
                                AvaNavDestination.PRACTICE -> {
                                    PracticeScreen(
                                        exercises = exercises,
                                        activeExercise = uiState.activePracticeExercise,
                                        onSelectExercise = { viewModel.startPracticeDrill(it) },
                                        onDismissActiveDrill = { viewModel.dismissPracticeDrill() }
                                    )
                                }
                                AvaNavDestination.RECORDINGS -> {
                                    RecordingsScreen(
                                        recordings = recordings,
                                        onSingSong = { song -> viewModel.launchSongOnStage(song) },
                                        onNavigateToStage = { viewModel.selectTab(AvaNavDestination.STAGE) },
                                        onPlayRecordingTake = { take ->
                                            viewModel.playRecordingTake(take)
                                        },
                                        playingTakeId = uiState.playingRecordingId
                                    )
                                }
                                AvaNavDestination.STUDIO -> {
                                    StudioScreen(
                                        uiState = uiState,
                                        onTogglePersianRtl = { viewModel.togglePersianRtl() },
                                        onSwitchWorkspace = { viewModel.switchWorkspace(it) },
                                        onSetTrackVolume = { trackId, vol -> viewModel.setTrackVolume(trackId, vol) },
                                        onSetTrackPan = { trackId, pan -> viewModel.setTrackPan(trackId, pan) },
                                        onToggleTrackMute = { viewModel.toggleTrackMute(it) },
                                        onToggleTrackSolo = { viewModel.toggleTrackSolo(it) },
                                        onSetMasterVolume = { viewModel.setMasterVolume(it) },
                                        onExportProject = { viewModel.exportCurrentProject(it) }
                                    )
                                }
                                AvaNavDestination.ACCOUNT -> {
                                    val context = androidx.compose.ui.platform.LocalContext.current
                                    AccountScreen(
                                        userAccount = uiState.userAccount,
                                        selectedMarketplace = uiState.selectedMarketplace,
                                        availablePlans = uiState.availablePlans,
                                        currentLanguage = uiState.appLanguage,
                                        onSelectMarketplace = { viewModel.selectMarketplaceProvider(it) },
                                        onPurchasePlan = { plan -> viewModel.purchaseSubscriptionPlan(context, plan) },
                                        onRestorePurchases = { viewModel.restorePurchases(context) },
                                        onApplyReferralCode = { code -> viewModel.applyReferralCode(code) },
                                        onSelectLanguage = { lang -> viewModel.changeAppLanguage(lang) },
                                        onSignInGoogle = { viewModel.signInWithGoogle() },
                                        onSignInPhone = { viewModel.openPhoneAuthDialog(isLinking = false) },
                                        onLinkGoogle = { viewModel.linkGoogleAccount() },
                                        onLinkPhone = { viewModel.openPhoneAuthDialog(isLinking = true) },
                                        onTopUpCoins = { viewModel.openCoinShop() },
                                        onUpgradeTier = { viewModel.upgradeSubscriptionTier(it) },
                                        onSignOut = { viewModel.signOutAccount() }
                                    )
                                }
                            }
                        }
                    }
                }

                // Feature Gate Dialog
                uiState.activeFeatureGate?.let { gate ->
                    FeatureGateDialog(
                        feature = gate.feature,
                        decision = gate.decision,
                        onUseCoins = { viewModel.confirmFeatureGatePayment() },
                        onUpgrade = { tier ->
                            viewModel.dismissFeatureGate()
                            viewModel.upgradeSubscriptionTier(tier)
                        },
                        onDismiss = { viewModel.dismissFeatureGate() }
                    )
                }

                // Coin Shop Dialog
                if (uiState.isCoinShopOpen) {
                    CoinShopDialog(
                        bundles = FeaturePricingPolicy.availableCoinBundles,
                        onPurchaseBundle = { bundle -> viewModel.topUpCoins(bundle) },
                        onDismiss = { viewModel.closeCoinShop() }
                    )
                }

                // Phone Auth Dialog
                if (uiState.isPhoneAuthOpen) {
                    PhoneAuthDialog(
                        onSendCode = { phone -> viewModel.sendPhoneAuthCode(phone) },
                        onVerifyCode = { phone, code -> viewModel.verifyPhoneAuthCode(phone, code) },
                        onDismiss = { viewModel.closePhoneAuthDialog() },
                        isCodeSent = uiState.isPhoneCodeSent
                    )
                }

                // Account Notification Dialog
                uiState.accountNotification?.let { msg ->
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissAccountNotification() },
                        title = { Text("TAVANA Studio") },
                        text = { Text(msg) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.dismissAccountNotification() }) {
                                Text("باشه")
                            }
                        }
                    )
                }

                // Marketplace Billing Feedback Dialog
                uiState.billingMessage?.let { billingMsg ->
                    AlertDialog(
                        onDismissRequest = { viewModel.dismissBillingMessage() },
                        title = { Text("خرید و فعال‌سازی اشتراک") },
                        text = { Text(billingMsg) },
                        confirmButton = {
                            TextButton(onClick = { viewModel.dismissBillingMessage() }) {
                                Text("متوجه شدم")
                            }
                        }
                    )
                }
            }
        }
    }
}
