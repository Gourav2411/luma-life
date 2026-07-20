package com.luma.collegelife

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.GTranslate
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Route
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.luma.designsystem.LifeAreaPill
import com.luma.designsystem.LumaCard
import com.luma.designsystem.LumaColors
import com.luma.designsystem.LumaTheme
import com.luma.designsystem.PrimaryLumaButton
import com.luma.feature.grow.GrowScreen
import com.luma.feature.luma.LumaScreen
import com.luma.feature.luma.PlanReviewScreen
import com.luma.feature.plans.PlansScreen
import com.luma.feature.today.TodayScreen
import com.luma.model.LifeArea
import com.luma.model.PlanHorizon
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: LumaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var darkMode by rememberSaveable { mutableStateOf(false) }
            LumaTheme(darkTheme = darkMode) {
                LumaRoot(
                    viewModel = viewModel,
                    darkMode = darkMode,
                    onDarkModeChange = { darkMode = it },
                )
            }
        }
    }
}

@Composable
private fun LumaRoot(
    viewModel: LumaViewModel,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    val stage by viewModel.entryStage.collectAsStateWithLifecycle()
    val notice by viewModel.notice.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(notice) {
        notice?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeNotice()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (stage) {
                EntryStage.SPLASH -> SplashScreen()
                EntryStage.WELCOME -> WelcomeScreen(viewModel::advanceEntry)
                EntryStage.AUTH -> AuthScreen(
                    onDemo = viewModel::useProductDemo,
                    onUnavailable = viewModel::notify,
                )
                EntryStage.PERMISSIONS -> PermissionsScreen(viewModel::advanceEntry)
                EntryStage.SETUP -> SetupScreen(viewModel)
                EntryStage.MAIN -> MainShell(viewModel, darkMode, onDarkModeChange)
            }
        }
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LumaColors.Paper),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(104.dp)
                    .background(LumaColors.Lime, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = LumaColors.Ink,
                    modifier = Modifier.size(56.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text("LUMA", style = MaterialTheme.typography.displayLarge, color = LumaColors.Ink)
            Text(
                "LIFE, IN MOTION.",
                style = MaterialTheme.typography.labelLarge,
                color = LumaColors.Ink.copy(alpha = 0.68f),
            )
        }
    }
}

@Composable
private fun WelcomeScreen(onContinue: () -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        item {
            Text("LIFE IS NOT\nA TO-DO LIST.", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(10.dp))
            Text(
                "Luma understands what changed, protects what matters, and proposes a realistic plan for college, skills, health, people, rest and fun.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            LumaCard(containerColor = LumaColors.Lime.copy(alpha = 0.2f)) {
                Text("You say", style = MaterialTheme.typography.labelLarge)
                Text(
                    "“Exam Friday, football at six, date tonight—and I’m exhausted.”",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                WelcomeValue(Icons.Rounded.Psychology, "Understands context", Modifier.weight(1f))
                WelcomeValue(Icons.Rounded.Route, "Plans five horizons", Modifier.weight(1f))
                WelcomeValue(Icons.Rounded.Lock, "You approve changes", Modifier.weight(1f))
            }
        }
        item {
            Text(
                "Propose. Explain. Approve.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        item {
            PrimaryLumaButton(
                text = "Build my first realistic plan",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun WelcomeValue(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    LumaCard(modifier = modifier) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.height(8.dp))
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun AuthScreen(
    onDemo: () -> Unit,
    onUnavailable: (String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("YOUR PLAN.\nYOUR CONTROL.", style = MaterialTheme.typography.displayMedium)
            Text(
                "Identity is handled by Supabase OAuth or an email one-time code. Luma never stores a simulated password.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            OutlinedButton(
                onClick = { onUnavailable("Google OAuth activates when Supabase and Google credentials are configured.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Icon(Icons.Rounded.GTranslate, contentDescription = null)
                Spacer(Modifier.size(10.dp))
                Text("Continue with Google")
            }
        }
        item {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("College or personal email") },
                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                singleLine = true,
            )
        }
        item {
            Button(
                onClick = { onUnavailable("Email OTP activates when the Supabase project URL and publishable key are configured.") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = email.contains("@"),
            ) {
                Text("Send one-time code")
            }
        }
        item {
            HorizontalDivider(Modifier.padding(vertical = 4.dp))
            LumaCard(containerColor = LumaColors.Aqua.copy(alpha = 0.13f)) {
                Text("Review build", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Explore the full offline product journey without creating an account. This is clearly separate from authentication.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = onDemo, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Rounded.Explore, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text("Explore product demo")
                }
            }
        }
        item {
            Text(
                "By continuing, you can review consent, memory and deletion controls before any cloud synchronization.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PermissionsScreen(onContinue: () -> Unit) {
    val context = LocalContext.current
    val requested = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }.toTypedArray()
    var granted by remember {
        mutableStateOf(
            requested.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED },
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result -> granted = result.values.all { it } }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("YOU CHOOSE\nWHAT LUMA USES.", style = MaterialTheme.typography.displayMedium)
            Text(
                "Core planning works without microphone or notifications. Calendar access is connected later and every write still needs approval.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { PermissionCard(Icons.Rounded.Mic, "Microphone", "Voice input only. Raw audio is discarded after processing.", "Optional") }
        item { PermissionCard(Icons.Rounded.Notifications, "Notifications", "Morning plans, transitions and review reminders—with quiet hours.", "Optional") }
        item { PermissionCard(Icons.Rounded.CalendarMonth, "Calendar", "Connected later through secure server-side OAuth.", "Not requested yet") }
        item {
            if (!granted) {
                OutlinedButton(
                    onClick = { launcher.launch(requested) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Allow optional permissions")
                }
            }
            Spacer(Modifier.height(10.dp))
            PrimaryLumaButton(
                text = if (granted) "Continue" else "Continue without them",
                onClick = onContinue,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun PermissionCard(icon: ImageVector, title: String, text: String, status: String) {
    LumaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(30.dp))
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge)
                Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(status, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupScreen(viewModel: LumaViewModel) {
    val setup by viewModel.setup.collectAsStateWithLifecycle()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text("BUILD THE\nWHOLE PICTURE.", style = MaterialTheme.typography.displayMedium)
            Text(
                "About two minutes. You can edit everything later.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            OutlinedTextField(
                value = setup.name,
                onValueChange = { value -> viewModel.updateSetup { it.copy(name = value) } },
                label = { Text("What should Luma call you?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
        }
        item {
            OutlinedTextField(
                value = setup.direction,
                onValueChange = { value -> viewModel.updateSetup { it.copy(direction = value) } },
                label = { Text("What are you trying to grow toward?") },
                placeholder = { Text("e.g. Product engineer, designer, founder…") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
            )
        }
        item {
            Text("What deserves protection?", style = MaterialTheme.typography.titleLarge)
            Text(
                "Select all. Planning is about balance, not sacrifice.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LifeArea.entries.forEach { area ->
                    val selected = area in setup.selectedAreas
                    LumaCard(
                        containerColor = if (selected) LumaColors.Lime.copy(alpha = 0.13f) else MaterialTheme.colorScheme.surface,
                        onClick = {
                            viewModel.updateSetup { state ->
                                state.copy(
                                    selectedAreas = state.selectedAreas.toMutableSet().apply {
                                        if (!add(area)) remove(area)
                                    },
                                )
                            }
                        },
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = selected, onCheckedChange = null)
                            LifeAreaPill(area)
                        }
                    }
                }
            }
        }
        item {
            PrimaryLumaButton(
                text = "Create my starting plan",
                onClick = viewModel::completeSetup,
                modifier = Modifier.fillMaxWidth(),
                enabled = setup.name.isNotBlank() && setup.direction.isNotBlank(),
            )
        }
    }
}

@Composable
private fun MainShell(
    viewModel: LumaViewModel,
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
) {
    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val overlay by viewModel.overlay.collectAsStateWithLifecycle()
    val items by viewModel.items.collectAsStateWithLifecycle()
    val energy by viewModel.energy.collectAsStateWithLifecycle()
    val setup by viewModel.setup.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val input by viewModel.input.collectAsStateWithLifecycle()
    val response by viewModel.lastResponse.collectAsStateWithLifecycle()
    val isThinking by viewModel.isThinking.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val selectedChanges by viewModel.selectedChanges.collectAsStateWithLifecycle()
    val reflections by viewModel.reflections.collectAsStateWithLifecycle()
    var quickCaptureVisible by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        viewModel.setListening(false)
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!text.isNullOrBlank()) {
                viewModel.setInput(text)
                viewModel.askLuma(prompt = text)
            }
        }
    }
    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { allowed ->
        if (allowed) launchSpeech(context, speechLauncher::launch, viewModel)
        else viewModel.notify("Microphone denied. Typed planning remains available.")
    }
    val startVoice = {
        if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            launchSpeech(context, speechLauncher::launch, viewModel)
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    BackHandler(enabled = overlay != OverlayScreen.NONE) { viewModel.closeOverlay() }

    if (overlay != OverlayScreen.NONE) {
        Box(Modifier.fillMaxSize().statusBarsPadding()) {
            when (overlay) {
                OverlayScreen.PLAN_REVIEW -> response?.proposal?.let { proposal ->
                    PlanReviewScreen(
                        proposal = proposal,
                        selectedChangeIds = selectedChanges,
                        onToggleChange = viewModel::toggleProposalChange,
                        onApprove = viewModel::approveProposal,
                        onReject = viewModel::rejectProposal,
                        onEdit = {
                            viewModel.closeOverlay()
                            viewModel.setInput("Adjust this plan: ")
                        },
                    )
                }
                OverlayScreen.FOCUS -> FocusScreen(viewModel::closeOverlay, viewModel::notify)
                OverlayScreen.SETTINGS -> SettingsScreen(
                    darkMode = darkMode,
                    onDarkModeChange = onDarkModeChange,
                    onBack = viewModel::closeOverlay,
                    onCalendar = { viewModel.showOverlay(OverlayScreen.CALENDAR) },
                    onMemory = { viewModel.showOverlay(OverlayScreen.MEMORY) },
                    onReset = viewModel::resetProductDemo,
                    onNotice = viewModel::notify,
                )
                OverlayScreen.CALENDAR -> CalendarScreen(viewModel::closeOverlay, viewModel::notify)
                OverlayScreen.MEMORY -> MemoryScreen(viewModel::closeOverlay, viewModel::notify)
                OverlayScreen.REFLECTION -> ReflectionScreen(viewModel::closeOverlay, viewModel::saveReflection)
                OverlayScreen.NONE -> Unit
            }
        }
    } else {
        Scaffold(
            bottomBar = {
                PremiumBottomBar(
                    selected = destination,
                    onSelect = viewModel::selectDestination,
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
                    .statusBarsPadding(),
            ) {
                when (destination) {
                    MainDestination.TODAY -> TodayScreen(
                        name = setup.name.ifBlank { "Aarav" },
                        formattedDate = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH)),
                        energy = energy,
                        items = items,
                        isOffline = true,
                        onEnergyChange = viewModel::setEnergy,
                        onAskLuma = {
                            viewModel.selectDestination(MainDestination.LUMA)
                            viewModel.setInput("Something changed today: ")
                        },
                        onQuickCapture = { quickCaptureVisible = true },
                        onToggleComplete = viewModel::toggleComplete,
                    )
                    MainDestination.PLANS -> PlansScreen(
                        direction = DemoData.direction,
                        semester = DemoData.semester,
                        week = DemoData.week(items),
                        dayItems = items,
                        session = DemoData.focusSession,
                        onAskLuma = { horizon ->
                            viewModel.selectDestination(MainDestination.LUMA)
                            viewModel.setInput("Help me rethink my ${horizon.name.lowercase()} plan: ")
                        },
                        onStartSession = { viewModel.showOverlay(OverlayScreen.FOCUS) },
                    )
                    MainDestination.GROW -> GrowScreen(
                        roadmap = DemoData.roadmap,
                        reflections = reflections,
                        onPlanNextEvidence = {
                            viewModel.selectDestination(MainDestination.LUMA)
                            viewModel.setInput("Plan my next portfolio evidence: ")
                        },
                        onAddEvidence = { viewModel.notify("Evidence upload is queued for the authenticated backend.") },
                        onReflect = { viewModel.showOverlay(OverlayScreen.REFLECTION) },
                    )
                    MainDestination.LUMA -> LumaScreen(
                        messages = messages,
                        input = input,
                        isListening = isListening,
                        isThinking = isThinking,
                        lastResponse = response,
                        onInputChange = viewModel::setInput,
                        onSend = { viewModel.askLuma() },
                        onToggleVoice = startVoice,
                        onReviewProposal = viewModel::reviewProposal,
                        onOpenSettings = { viewModel.showOverlay(OverlayScreen.SETTINGS) },
                    )
                }
            }
        }
    }

    if (quickCaptureVisible) {
        QuickCaptureDialog(
            onDismiss = { quickCaptureVisible = false },
            onSave = {
                viewModel.quickCapture(it)
                quickCaptureVisible = false
            },
        )
    }
}

@Composable
private fun PremiumBottomBar(
    selected: MainDestination,
    onSelect: (MainDestination) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .offset(x = 3.dp, y = 4.dp)
                .background(LumaColors.Ink, RoundedCornerShape(26.dp)),
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .border(1.25.dp, LumaColors.Ink, RoundedCornerShape(26.dp)),
            color = Color.White,
            shape = RoundedCornerShape(26.dp),
        ) {
            Row(
                modifier = Modifier.padding(7.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                MainDestination.entries.forEach { item ->
                    val isSelected = item == selected
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) LumaColors.Lime else Color.Transparent,
                                RoundedCornerShape(19.dp),
                            )
                            .clickable { onSelect(item) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = item.icon(),
                            contentDescription = item.label(),
                            modifier = Modifier.size(23.dp),
                            tint = LumaColors.Ink,
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            text = item.label().uppercase(),
                            style = MaterialTheme.typography.labelMedium,
                            color = LumaColors.Ink,
                        )
                    }
                }
            }
        }
    }
}

private fun launchSpeech(
    context: android.content.Context,
    launch: (Intent) -> Unit,
    viewModel: LumaViewModel,
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Tell Luma the whole situation")
    }
    if (intent.resolveActivity(context.packageManager) == null) {
        viewModel.notify("Speech recognition is unavailable on this device. Use typed input.")
    } else {
        viewModel.setListening(true)
        launch(intent)
    }
}

@Composable
private fun QuickCaptureDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick capture") },
        text = {
            Column {
                Text("Capture now; decide the time intentionally later.")
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task or thought") })
            }
        },
        confirmButton = { Button(onClick = { onSave(title) }, enabled = title.isNotBlank()) { Text("Save draft") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun FocusScreen(onBack: () -> Unit, onNotice: (String) -> Unit) {
    var secondsRemaining by rememberSaveable { mutableIntStateOf(DemoData.focusSession.durationMinutes * 60) }
    var running by rememberSaveable { mutableStateOf(false) }
    val completedSteps = remember { mutableStateListOf("step-1") }
    LaunchedEffect(running, secondsRemaining) {
        if (running && secondsRemaining > 0) {
            delay(1_000)
            secondsRemaining -= 1
        }
        if (secondsRemaining == 0) running = false
    }
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { OverlayHeader("Focus session", onBack) }
        item {
            LumaCard(containerColor = LumaColors.Ink) {
                Text("ONE OUTCOME", color = LumaColors.Lime, style = MaterialTheme.typography.labelLarge)
                Text(DemoData.focusSession.outcome, color = Color.White, style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(18.dp))
                Text(
                    "%02d:%02d".format(secondsRemaining / 60, secondsRemaining % 60),
                    color = Color.White,
                    style = MaterialTheme.typography.displayMedium,
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { running = !running }) {
                    Icon(if (running) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(if (running) "Pause" else "Start focus")
                }
            }
        }
        item { Text("Microsteps", style = MaterialTheme.typography.titleLarge) }
        DemoData.focusSession.steps.forEach { step ->
            item {
                LumaCard(onClick = {
                    if (!completedSteps.add(step.id)) completedSteps.remove(step.id)
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = step.id in completedSteps, onCheckedChange = null)
                        Text(step.title, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
        item {
            PrimaryLumaButton(
                text = "Finish with evidence",
                onClick = { onNotice("Focus evidence prompt saved for the Grow timeline."); onBack() },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    darkMode: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    onCalendar: () -> Unit,
    onMemory: () -> Unit,
    onReset: () -> Unit,
    onNotice: (String) -> Unit,
) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { OverlayHeader("Settings & control", onBack) }
        item {
            SettingsRow(Icons.Rounded.CalendarMonth, "Calendar connections", "Import safely; write only after approval", onCalendar)
        }
        item {
            SettingsRow(Icons.Rounded.Psychology, "Luma memory", "Review, edit, export or delete facts", onMemory)
        }
        item {
            LumaCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.DarkMode, contentDescription = null)
                    Spacer(Modifier.size(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dark mode", style = MaterialTheme.typography.titleMedium)
                        Text("High-contrast paper and ink palette", style = MaterialTheme.typography.bodyMedium)
                    }
                    Switch(checked = darkMode, onCheckedChange = onDarkModeChange)
                }
            }
        }
        item {
            SettingsRow(Icons.Rounded.Download, "Export my data", "Portable account, plan and memory archive") {
                onNotice("Export requires an authenticated account. No cloud data exists in demo mode.")
            }
        }
        item {
            SettingsRow(Icons.Rounded.Security, "Privacy & consent", "30-day chat retention · raw audio discarded") {
                onNotice("Privacy controls are active: local-only mode, no audio retention, no analytics upload.")
            }
        }
        item {
            SettingsRow(Icons.Rounded.Refresh, "Restart onboarding", "Review the first-run product journey", onReset)
        }
        item {
            SettingsRow(Icons.Rounded.DeleteOutline, "Delete account", "Permanent deletion with confirmation") {
                onNotice("No account exists in product-demo mode. Local cache can be cleared from Android settings.")
            }
        }
        item {
            Text(
                "Luma 1.0 alpha · Server URL ${if (BuildConfig.LUMA_API_BASE_URL.isBlank()) "not configured" else "configured"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    LumaCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.size(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun CalendarScreen(onBack: () -> Unit, onNotice: (String) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { OverlayHeader("Calendar connections", onBack) }
        item {
            LumaCard(containerColor = LumaColors.Cobalt.copy(alpha = 0.12f)) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(36.dp))
                Text("Google Calendar", style = MaterialTheme.typography.headlineMedium)
                Text("Not connected", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(10.dp))
                Text(
                    "Import events to detect conflicts. Every proposed create, move, resize or delete remains a visible diff until you approve.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(12.dp))
                PrimaryLumaButton(
                    text = "Connect securely",
                    onClick = { onNotice("Calendar OAuth activates with the deployed TypeScript service and Google credentials.") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
        item {
            PermissionCard(Icons.Rounded.Lock, "Write protection", "No external event is changed before proposal approval.", "Required")
        }
        item {
            PermissionCard(Icons.Rounded.Refresh, "Idempotent sync", "Cursors, event IDs and versions prevent duplicate writes.", "Designed")
        }
    }
}

@Composable
private fun MemoryScreen(onBack: () -> Unit, onNotice: (String) -> Unit) {
    var remembered by rememberSaveable { mutableStateOf(true) }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { OverlayHeader("Luma memory", onBack) }
        item {
            Text(
                "Luma proposes memories. Durable or sensitive facts require confirmation.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (remembered) {
            item {
                LumaCard(containerColor = LumaColors.Aqua.copy(alpha = 0.12f)) {
                    Text("PLANNING PREFERENCE · CONFIRMED", style = MaterialTheme.typography.labelLarge)
                    Text("Football on weekday evenings is protected.", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { onNotice("Memory editor is ready for authenticated persistence.") }) { Text("Edit") }
                        OutlinedButton(onClick = { remembered = false }) { Text("Delete") }
                    }
                }
            }
        } else {
            item { LumaCard { Text("No confirmed memories in this product demo.") } }
        }
        item { PermissionCard(Icons.Rounded.Timer, "Conversation history", "Defaults to automatic deletion after 30 days.", "30 days") }
        item { PermissionCard(Icons.Rounded.Mic, "Voice", "Raw audio is discarded after transcription.", "Never stored") }
        item {
            OutlinedButton(
                onClick = { onNotice("Memory export requires an authenticated cloud account.") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Rounded.Download, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text("Export memories")
            }
        }
    }
}

@Composable
private fun ReflectionScreen(onBack: () -> Unit, onSave: (String, String) -> Unit) {
    var worked by rememberSaveable { mutableStateOf("") }
    var adjust by rememberSaveable { mutableStateOf("") }
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { OverlayHeader("End-of-day reflection", onBack) }
        item {
            LumaCard(containerColor = LumaColors.Lime.copy(alpha = 0.14f)) {
                Text("No score. No guilt.", style = MaterialTheme.typography.headlineMedium)
                Text("A short signal helps tomorrow fit the person who actually showed up today.")
            }
        }
        item {
            OutlinedTextField(
                value = worked,
                onValueChange = { worked = it },
                label = { Text("What worked well?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }
        item {
            OutlinedTextField(
                value = adjust,
                onValueChange = { adjust = it },
                label = { Text("What should change tomorrow?") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
        }
        item {
            PrimaryLumaButton(
                text = "Save reflection",
                onClick = { onSave(worked, adjust) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun OverlayHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBack, contentDescription = "Back") }
        Text(title, style = MaterialTheme.typography.headlineLarge)
    }
}

private fun MainDestination.label(): String = when (this) {
    MainDestination.TODAY -> "Today"
    MainDestination.PLANS -> "Plans"
    MainDestination.GROW -> "Grow"
    MainDestination.LUMA -> "Luma"
}

private fun MainDestination.icon(): ImageVector = when (this) {
    MainDestination.TODAY -> Icons.Rounded.Home
    MainDestination.PLANS -> Icons.Rounded.CalendarMonth
    MainDestination.GROW -> Icons.Rounded.School
    MainDestination.LUMA -> Icons.Rounded.AutoAwesome
}
