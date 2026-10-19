package com.nelsonurrutia.conectame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nelsonurrutia.conectame.ui.theme.scaledSp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.res.painterResource
import android.content.Intent
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.RadioButton
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.res.pluralStringResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.ui.unit.Dp

class MainActivity : ComponentActivity() {

    private lateinit var repository: MeetingRepository

    /** Meeting to join right away because the app was opened from its home-screen shortcut. */
    private val pendingJoinId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = MeetingRepository(applicationContext)
        // Keep the big home-screen buttons up to date (e.g. after the app's data was cleared).
        MeetingWidget.refreshAll(applicationContext)
        // Only on a fresh start: after a screen rotation the same intent must not join again.
        if (savedInstanceState == null) handleShortcut(intent)
        setContent {
            AppTheme {
                ConnectMeApp(
                    repository = repository,
                    pendingJoinId = pendingJoinId.value,
                    onPendingJoinHandled = { pendingJoinId.value = null },
                    // Opened from the home-screen button and then cancelled: go back to the home screen.
                    onLeaveApp = { finish() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleShortcut(intent)
    }

    private fun handleShortcut(intent: Intent?) {
        if (intent?.action == MeetingShortcuts.ACTION_JOIN) {
            pendingJoinId.value = intent.getStringExtra(MeetingShortcuts.EXTRA_MEETING_ID)
        }
    }
}

/** Space between the screen edges and the content. */
private val ScreenMargin = 20.dp

/** Widest the forms and lists get on tablets (about a large phone), so they don't stretch edge to edge. */
private val ReadableWidth = 640.dp

/** The home screen's meeting buttons get more room: two columns on a tablet. */
private val HomeWidth = 1000.dp

/** A meeting button needs at least this width; on wider screens the buttons go in 2 (or more) columns. */
private val MeetingButtonMinWidth = 340.dp

/** Centers the content and caps its width on tablets; on phones it changes nothing. */
fun Modifier.readableWidth(max: Dp = ReadableWidth): Modifier =
    this.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally).widthIn(max = max)

/**
 * Top bar: a button on the left and, on the right, the screen's name or a second button. Unlike
 * Material's TopAppBar it grows with the font size: when both don't fit on one line (largest font,
 * Spanish), the right-hand part moves to a second line instead of overlapping or being cut off.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ScreenTopBar(
    title: String? = null,
    action: (@Composable () -> Unit)? = null,
    button: @Composable () -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .heightIn(min = 64.dp)
            .padding(horizontal = 4.dp),
    ) {
        Box(Modifier.align(Alignment.CenterVertically)) { button() }
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .semantics { heading() },
            )
        }
        if (action != null) Box(Modifier.align(Alignment.CenterVertically)) { action() }
    }
}

/** Which screen is showing. Stored as a String so it survives screen rotation. */
private const val HOME = "home"
private const val MANAGE = "manage"
private const val SETTINGS = "settings"
private const val HELP = "help"
private const val ABOUT = "about"
private const val EDIT_PREFIX = "edit:" // "edit:" = new meeting, "edit:<id>" = existing meeting

@Composable
fun ConnectMeApp(
    repository: MeetingRepository,
    pendingJoinId: String?,
    onPendingJoinHandled: () -> Unit,
    onLeaveApp: () -> Unit = {},
) {
    val context = LocalContext.current
    var screen by rememberSaveable { mutableStateOf(HOME) }
    // Where the form goes back to: Home when it was opened from the empty home screen.
    var formReturnsTo by rememberSaveable { mutableStateOf(MANAGE) }

    // Opened from a meeting's home-screen shortcut: go to the home screen, which then joins it.
    LaunchedEffect(pendingJoinId) { if (pendingJoinId != null) screen = HOME }

    when {
        screen == HOME -> HomeScreen(
            meetings = repository.meetings,
            onManage = { screen = MANAGE },
            onMenu = { screen = it },
            onAdd = {
                formReturnsTo = HOME
                screen = EDIT_PREFIX
            },
            autoJoinId = pendingJoinId,
            onAutoJoinHandled = onPendingJoinHandled,
            onLeaveApp = onLeaveApp,
        )

        screen == MANAGE -> {
            BackHandler { screen = HOME }
            ManageScreen(
                meetings = repository.meetings,
                onBack = { screen = HOME },
                onAdd = {
                    formReturnsTo = MANAGE
                    screen = EDIT_PREFIX
                },
                onEdit = {
                    formReturnsTo = MANAGE
                    screen = EDIT_PREFIX + it.id
                },
                onDelete = {
                    repository.delete(it.id)
                    MeetingPhotos.delete(context, it.id)
                    MeetingShortcuts.onDeleted(context, it)
                },
            )
        }

        screen == SETTINGS || screen == HELP || screen == ABOUT -> {
            BackHandler { screen = HOME }
            val back = { screen = HOME }
            when (screen) {
                SETTINGS -> SettingsScreen(onBack = back)
                HELP -> MenuPage(stringResource(R.string.help_title), onBack = back) { CaregiverHelpSection() }
                else -> MenuPage(stringResource(R.string.about_title), onBack = back) { AboutSection() }
            }
        }

        screen.startsWith(EDIT_PREFIX) -> {
            val existing = repository.find(screen.removePrefix(EDIT_PREFIX))
            BackHandler { screen = formReturnsTo }
            EditMeetingScreen(
                initial = existing,
                onCancel = { screen = formReturnsTo },
                onSave = {
                    val before = repository.find(it.id)
                    repository.save(it)
                    MeetingShortcuts.sync(context, before, it)
                    screen = formReturnsTo
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Home: one big button per meeting
// ---------------------------------------------------------------------------------------------

@Composable
fun HomeScreen(
    meetings: List<Meeting>,
    onManage: () -> Unit,
    /** Opens a page from the menu: [SETTINGS], [HELP] or [ABOUT]. */
    onMenu: (String) -> Unit = {},
    onAdd: () -> Unit = onManage,
    autoJoinId: String? = null,
    onAutoJoinHandled: () -> Unit = {},
    onLeaveApp: () -> Unit = {},
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    // The meeting being opened right now (shows the "Opening Zoom…" screen), and any error to show.
    var connecting by remember { mutableStateOf<Meeting?>(null) }
    var zoomOpened by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<JoinResult?>(null) }

    // Opened from a home-screen shortcut: join that meeting as if its button was tapped.
    // True when this join came from the big home-screen button (or shortcut), not from inside the app.
    var fromHomeScreenButton by remember { mutableStateOf(false) }
    LaunchedEffect(autoJoinId) {
        if (autoJoinId == null) return@LaunchedEffect
        meetings.firstOrNull { it.id == autoJoinId }?.let {
            connecting = it
            fromHomeScreenButton = true
        }
        onAutoJoinHandled()
    }

    // Coming back from Zoom: hide the "Opening Zoom…" screen. (Not before Zoom was opened: when
    // the app starts from a shortcut, it resumes *after* the join has already been scheduled.)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && zoomOpened) {
                connecting = null
                zoomOpened = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var secondsLeft by remember { mutableIntStateOf(JOIN_COUNTDOWN_SECONDS) }

    // Countdown before Zoom opens: time to read which meeting it is and press Cancel if it's the
    // wrong one (also plenty for TalkBack to finish reading). Cancel sets `connecting` to null,
    // which stops this effect.
    LaunchedEffect(connecting) {
        val meeting = connecting ?: return@LaunchedEffect
        for (s in JOIN_COUNTDOWN_SECONDS downTo 1) {
            secondsLeft = s
            delay(1000)
        }
        secondsLeft = 0
        val result = ZoomLauncher.join(context, meeting)
        if (result == JoinResult.OPENED) {
            zoomOpened = true
            delay(5000) // safety net in case we never get paused/resumed
            connecting = null
            zoomOpened = false
        } else {
            connecting = null
            error = result
        }
    }

    val homeTitle = stringResource(R.string.app_name)
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            modifier = if (connecting != null) {
                Modifier.clearAndSetSemantics { } // the overlay is on top: nothing underneath can be reached
            } else {
                Modifier.semantics { paneTitle = homeTitle }
            },
            topBar = {
                // "Meetings" lives up here, away from where a hand holds the phone.
                ScreenTopBar(
                    action = { HomeMenu(onOpen = onMenu) },
                ) {
                    TextButton(onClick = onManage) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.edit_meetings), fontSize = 18.scaledSp)
                    }
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = ScreenMargin)
                    .readableWidth(HomeWidth),
            ) {
                if (meetings.isNotEmpty()) {
                    Text(
                        stringResource(R.string.home_title),
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 20.dp)
                            .semantics { heading() },
                    )
                }

                if (meetings.isEmpty()) {
                    // No meetings yet: one big button straight to the "new meeting" form.
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        Text(
                            stringResource(R.string.home_empty),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onAdd,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(stringResource(R.string.add_meeting), style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                } else {
                    // One column on phones; two side by side on tablets.
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(MeetingButtonMinWidth),
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        gridItems(meetings, key = { it.id }) { meeting ->
                            val photo = remember(meeting.id, meeting.photo) {
                                MeetingPhotos.load(context, meeting)?.asImageBitmap()
                            }
                            MeetingButton(meeting.title, meeting.color, meeting.icon, photo) {
                                if (connecting == null) { // ignore double taps
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    connecting = meeting
                                    fromHomeScreenButton = false
                                }
                            }
                        }
                    }
                }
            }
        }

        connecting?.let { meeting ->
            // Cancel (or the phone's Back button): back to the meetings, or, when she came from the
            // home-screen button, straight back to the phone's home screen where she started.
            val cancel = {
                connecting = null
                if (fromHomeScreenButton) {
                    fromHomeScreenButton = false
                    onLeaveApp()
                }
            }
            BackHandler(onBack = cancel)
            ConnectingOverlay(meeting, secondsLeft, onCancel = cancel)
        }
    }

    error?.let { result ->
        JoinErrorDialog(
            result = result,
            onDismiss = { error = null },
            onInstallZoom = {
                error = null
                ZoomLauncher.openPlayStore(context)
            },
        )
    }
}

/** Seconds the "Opening Zoom for …" screen waits (with a big Cancel button) before Zoom opens. */
private const val JOIN_COUNTDOWN_SECONDS = 20

/**
 * Full screen in the meeting's color: "Opening Zoom for Familia", a countdown ring, and a big
 * Cancel button for when the wrong meeting was tapped.
 */
@Composable
fun ConnectingOverlay(meeting: Meeting, secondsLeft: Int, onCancel: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(meeting.color.color)
            // Swallow taps so nothing underneath can be pressed while Zoom opens.
            .pointerInput(Unit) { detectTapGestures { } }
            .systemBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.connecting, meeting.title),
            color = Color.White,
            style = MaterialTheme.typography.displaySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
        )
        Spacer(Modifier.height(40.dp))
        if (secondsLeft > 0) {
            // Ring that empties as the seconds go by, with the number in the middle.
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                CircularProgressIndicator(
                    progress = { secondsLeft / JOIN_COUNTDOWN_SECONDS.toFloat() },
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeWidth = 10.dp,
                    modifier = Modifier.fillMaxSize(),
                )
                Text(
                    secondsLeft.toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.clearAndSetSemantics { }, // the sentence below says it for TalkBack
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                pluralStringResource(R.plurals.connecting_countdown, secondsLeft, secondsLeft),
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
        } else {
            CircularProgressIndicator(color = Color.White, strokeWidth = 10.dp, modifier = Modifier.size(140.dp))
        }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF1C1C1C)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = ReadableWidth)
                .heightIn(min = 88.dp),
        ) {
            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(36.dp))
            Spacer(Modifier.width(12.dp))
            Text(stringResource(R.string.cancel), style = MaterialTheme.typography.headlineMedium)
        }
    }
}

/** Stays on screen until dismissed (unlike a Toast), with big text and one clear action. */
@Composable
fun JoinErrorDialog(result: JoinResult, onDismiss: () -> Unit, onInstallZoom: () -> Unit) {
    val zoomMissing = result == JoinResult.ZOOM_MISSING
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(if (zoomMissing) R.string.error_zoom_missing_title else R.string.error_bad_link_title),
                fontSize = 24.scaledSp,
                lineHeight = 30.scaledSp,
            )
        },
        text = {
            Text(
                stringResource(if (zoomMissing) R.string.error_zoom_missing_message else R.string.error_bad_link_message),
                fontSize = 20.scaledSp,
                lineHeight = 28.scaledSp,
            )
        },
        confirmButton = {
            Button(
                onClick = if (zoomMissing) onInstallZoom else onDismiss,
                modifier = Modifier.heightIn(min = 56.dp),
            ) {
                Text(stringResource(if (zoomMissing) R.string.install_zoom else R.string.ok), fontSize = 20.scaledSp)
            }
        },
        dismissButton = if (zoomMissing) {
            {
                TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 56.dp)) {
                    Text(stringResource(R.string.cancel), fontSize = 20.scaledSp)
                }
            }
        } else null,
    )
}

/**
 * The big colored "join" button: the meeting's picture, then its name and "Tap to join".
 * Also used as the live preview on the edit screen.
 */
@Composable
fun MeetingButton(title: String, color: MeetingColor, icon: MeetingIcon, photo: ImageBitmap? = null, onClick: () -> Unit) {
    // TalkBack reads the whole button as one item: "Join Familia meeting, button".
    val description = stringResource(R.string.join_meeting_a11y, title)
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.color, contentColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .semantics { contentDescription = description },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            if (photo != null || icon != MeetingIcon.NONE) {
                MeetingPicture(icon, color, Modifier.size(width = 76.dp, height = 68.dp), photo)
                Spacer(Modifier.width(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.headlineLarge)
                Text(stringResource(R.string.tap_to_join), style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}

/**
 * A meeting's picture (decorative: the button around it already says which meeting it is). White
 * shapes, with the details (eyes, smile, outlines) in the button's color so they look cut out of the
 * white, like the app icon.
 */
@Composable
fun MeetingPicture(icon: MeetingIcon, color: MeetingColor, modifier: Modifier = Modifier, photo: ImageBitmap? = null) {
    if (photo != null) {
        // A real photo (grandchild, doctor…): round, with a white ring so it stands out on the color.
        Box(modifier, contentAlignment = Alignment.Center) {
            Image(
                photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape),
            )
        }
        return
    }
    val base = icon.base ?: return // "No picture"
    Box(modifier) {
        // The camera has no screen frame around it, so it's drawn a bit smaller to match the others.
        val size = if (icon == MeetingIcon.CAMERA) Modifier.align(Alignment.Center).fillMaxSize(0.8f) else Modifier.fillMaxSize()
        Icon(painterResource(base), contentDescription = null, tint = Color.White, modifier = size)
        icon.details?.let {
            Icon(painterResource(it), contentDescription = null, tint = color.color, modifier = Modifier.fillMaxSize())
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Manage: list with Edit / Delete, plus Add
// ---------------------------------------------------------------------------------------------

@Composable
fun ManageScreen(
    meetings: List<Meeting>,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Meeting) -> Unit,
    onDelete: (Meeting) -> Unit,
) {
    var toDelete by remember { mutableStateOf<Meeting?>(null) }
    val screenTitle = stringResource(R.string.edit_meetings)

    Scaffold(
        modifier = Modifier.semantics { paneTitle = screenTitle },
        topBar = {
            ScreenTopBar(title = screenTitle) {
                TextButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.close), fontSize = 18.scaledSp)
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenMargin)
                .readableWidth(),
        ) {
            Button(onClick = onAdd, modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp)) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.add_meeting), style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(24.dp))
            if (meetings.isEmpty()) {
                Text(
                    stringResource(R.string.manage_empty),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                )
            }
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                items(meetings, key = { it.id }) { meeting ->
                    MeetingCard(
                        meeting = meeting,
                        onEdit = { onEdit(meeting) },
                        onDelete = { toDelete = meeting },
                    )
                }
            }
        }
    }

    toDelete?.let { meeting ->
        // Same size as the Zoom error dialog: big text, 56dp buttons, and a clear warning.
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = {
                Text(stringResource(R.string.delete_confirm_title, meeting.title), fontSize = 24.scaledSp, lineHeight = 30.scaledSp)
            },
            text = {
                Text(stringResource(R.string.delete_confirm_message), fontSize = 20.scaledSp, lineHeight = 28.scaledSp)
            },
            confirmButton = {
                Button(
                    onClick = { onDelete(meeting); toDelete = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
                    modifier = Modifier.heightIn(min = 56.dp),
                ) { Text(stringResource(R.string.delete), fontSize = 20.scaledSp) }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }, modifier = Modifier.heightIn(min = 56.dp)) {
                    Text(stringResource(R.string.cancel), fontSize = 20.scaledSp)
                }
            },
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Menu: Settings, Help for caregivers, About
// ---------------------------------------------------------------------------------------------

/**
 * "Menu" at the top right of Home: a labeled button (not just an icon, so it's easy to find) that
 * opens a list of big entries. "Meetings" stays its own button, since it's used much more often.
 */
@Composable
fun HomeMenu(onOpen: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { expanded = true }) {
            Icon(Icons.Default.Menu, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.menu), fontSize = 18.scaledSp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(
                Triple(SETTINGS, Icons.Default.Settings, R.string.settings),
                Triple(HELP, Icons.Default.Person, R.string.help_title),
                Triple(ABOUT, Icons.Default.Info, R.string.about_title),
            ).forEach { (page, icon, label) ->
                DropdownMenuItem(
                    text = { Text(stringResource(label), style = MaterialTheme.typography.titleMedium) },
                    leadingIcon = { Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp)) },
                    onClick = {
                        expanded = false
                        onOpen(page)
                    },
                    modifier = Modifier.heightIn(min = 64.dp),
                )
            }
        }
    }
}

/** A page opened from the menu: its name and "Close" at the top, and scrolling content. */
@Composable
fun MenuPage(title: String, onBack: () -> Unit, content: @Composable () -> Unit) {
    Scaffold(
        modifier = Modifier.semantics { paneTitle = title },
        topBar = {
            ScreenTopBar(title = title) {
                TextButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.close), fontSize = 18.scaledSp)
                }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenMargin)
                .readableWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp),
        ) {
            content()
        }
    }
}

/** Settings: app-wide choices (screen colors, text size). */
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    MenuPage(stringResource(R.string.settings), onBack) {
        SettingPicker(
            title = stringResource(R.string.theme_title),
            options = ThemeMode.entries,
            label = { it.label },
            selected = AppSettings.themeMode(context),
            onSelect = { AppSettings.setThemeMode(context, it) },
        )
        SettingPicker(
            title = stringResource(R.string.text_size_title),
            options = TextSize.entries,
            label = { it.label },
            selected = AppSettings.textSize(context),
            onSelect = { AppSettings.setTextSize(context, it) },
            modifier = Modifier.padding(top = 32.dp),
        )
    }
}

/**
 * One tip in "Help for caregivers": a short title, and the explanation shown when it's opened.
 * [countdown] tips are plurals, filled in with the countdown's length.
 */
private class HelpTip(val title: Int, val body: Int, val countdown: Boolean = false)

/** The tips, in groups, in the order a caregiver meets them: setting up, using, upkeep. */
private val HelpGroups = listOf(
    R.string.help_group_setup to listOf(
        HelpTip(R.string.help_same_link_title, R.string.help_same_link_body),
        HelpTip(R.string.help_passcode_title, R.string.help_passcode_body),
        HelpTip(R.string.help_name_title, R.string.help_name_body),
        HelpTip(R.string.help_zoom_title, R.string.help_zoom_body),
    ),
    R.string.help_group_use to listOf(
        HelpTip(R.plurals.help_countdown_title, R.plurals.help_countdown_body, countdown = true),
    ),
    R.string.help_group_care to listOf(
        HelpTip(R.string.help_remove_title, R.string.help_remove_body),
        HelpTip(R.string.help_new_phone_title, R.string.help_new_phone_body),
    ),
)

/**
 * "Help for caregivers" (from the menu): tips for whoever sets the app up. Each one is a short title
 * that opens its explanation, so the screen isn't a wall of text for the older person.
 */
@Composable
fun CaregiverHelpSection(modifier: Modifier = Modifier) {
    Column(modifier) {
        HelpGroups.forEachIndexed { index, (group, tips) ->
            Text(
                stringResource(group),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = if (index == 0) 0.dp else 16.dp, bottom = 8.dp)
                    .semantics { heading() },
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tips.forEach { HelpTipRow(it) }
            }
        }
    }
}

@Composable
private fun HelpTipRow(tip: HelpTip) {
    var open by rememberSaveable { mutableStateOf(false) }
    val state = stringResource(if (open) R.string.help_expanded else R.string.help_collapsed)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .clickable(role = Role.Button) { open = !open }
                .semantics { stateDescription = state }
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Text(
                if (tip.countdown) pluralStringResource(tip.title, JOIN_COUNTDOWN_SECONDS, JOIN_COUNTDOWN_SECONDS) else stringResource(tip.title),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f),
            )
            Icon(
                if (open) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
            )
        }
        if (open) {
            Text(
                if (tip.countdown) pluralStringResource(tip.body, JOIN_COUNTDOWN_SECONDS, JOIN_COUNTDOWN_SECONDS) else stringResource(tip.body),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            )
        }
    }
}

/**
 * "About" (from the menu): name and version (to tell a helper over the phone), what the
 * app does, where the meetings are kept, and the font's credit (its license asks for one).
 * Plain text only: no links, so nothing can open a browser by accident.
 */
@Composable
fun AboutSection(modifier: Modifier = Modifier) {
    val secondary = MaterialTheme.colorScheme.onSurfaceVariant
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            stringResource(R.string.about_version, stringResource(R.string.app_name), BuildConfig.VERSION_NAME),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(stringResource(R.string.about_description), style = MaterialTheme.typography.bodyLarge)
        Text(stringResource(R.string.about_privacy), style = MaterialTheme.typography.bodyLarge, color = secondary)
        Text(stringResource(R.string.about_font), style = MaterialTheme.typography.bodySmall, color = secondary)
    }
}

/**
 * One setting with a few choices ("Screen colors", "Text size"). Big rows with a radio button,
 * like the shortcut checkbox; TalkBack reads "Light, selected, 2 of 3".
 */
@Composable
fun <T> SettingPicker(
    title: String,
    options: List<T>,
    label: (T) -> Int,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .semantics { heading() },
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.selectableGroup()) {
            options.forEach { option ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .selectable(selected = option == selected, role = Role.RadioButton) { onSelect(option) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    RadioButton(selected = option == selected, onClick = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(16.dp))
                    Text(stringResource(label(option)), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/** One meeting on the "Meetings" screen: color stripe, name, link, and Edit / Delete (red). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MeetingCard(meeting: Meeting, onEdit: () -> Unit, onDelete: () -> Unit) {
    val editDescription = stringResource(R.string.edit_a11y, meeting.title)
    val deleteDescription = stringResource(R.string.delete_a11y, meeting.title)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(Modifier.height(IntrinsicSize.Min)) {
            Box(Modifier.width(12.dp).fillMaxHeight().background(meeting.color.color))
            Column(Modifier.padding(20.dp)) {
                Text(meeting.title, style = MaterialTheme.typography.headlineSmall)
                Text(
                    meeting.link,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp),
                )
                // Side by side normally; with very large text, Delete moves to its own line instead of
                // breaking the word in half ("Elim-inar").
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 16.dp),
                ) {
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .heightIn(min = 52.dp)
                            .semantics { contentDescription = editDescription },
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.edit))
                    }
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .heightIn(min = 52.dp)
                            .semantics { contentDescription = deleteDescription },
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.delete))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Add / edit form
// ---------------------------------------------------------------------------------------------

@Composable
fun EditMeetingScreen(initial: Meeting?, onCancel: () -> Unit, onSave: (Meeting) -> Unit) {
    var title by rememberSaveable { mutableStateOf(initial?.title.orEmpty()) }
    var link by rememberSaveable { mutableStateOf(initial?.link.orEmpty()) }
    var password by rememberSaveable { mutableStateOf(initial?.password.orEmpty()) }
    var displayName by rememberSaveable { mutableStateOf(initial?.displayName.orEmpty()) }
    var colorName by rememberSaveable { mutableStateOf((initial?.color ?: MeetingColor.DEFAULT).name) }
    var iconName by rememberSaveable { mutableStateOf((initial?.icon ?: MeetingIcon.DEFAULT).name) }
    val context = LocalContext.current
    // "Photo" choice: the picked (or already saved) photo file, and whether it's the chosen picture.
    var photoPath by rememberSaveable {
        mutableStateOf(initial?.takeIf { it.hasPhoto }?.let { MeetingPhotos.file(context, it.id).path })
    }
    var usePhoto by rememberSaveable { mutableStateOf(initial?.hasPhoto == true) }
    val photo = remember(photoPath) { MeetingPhotos.load(photoPath)?.asImageBitmap() }
    var homeShortcut by rememberSaveable { mutableStateOf(initial?.homeShortcut ?: false) }
    val shortcutsSupported = MeetingShortcuts.isSupported(LocalContext.current)
    val color = MeetingColor.fromName(colorName)
    val icon = MeetingIcon.fromName(iconName)
    var showErrors by rememberSaveable { mutableStateOf(false) }

    val linkValid = ZoomLinkParser.parse(link) != null
    val titleValid = title.isNotBlank()

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val titleFocus = remember { FocusRequester() }
    val linkFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) { if (initial == null) titleFocus.requestFocus() }
    // Android's photo picker: no permission needed, she only sees the photo she picks.
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                MeetingPhotos.importPicked(context, uri)?.let {
                    photoPath = it
                    usePhoto = true
                }
            }
        }
    }
    val nextField = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })

    val screenTitle = stringResource(if (initial == null) R.string.new_meeting else R.string.edit_meeting)
    val titleError = stringResource(R.string.field_title_error)
    val linkError = stringResource(R.string.field_link_error)
    val previewDescription = stringResource(R.string.preview_a11y, title.ifBlank { screenTitle })

    Scaffold(
        modifier = Modifier.semantics { paneTitle = screenTitle },
        topBar = {
            ScreenTopBar(title = screenTitle) {
                TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel), fontSize = 18.scaledSp) }
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ScreenMargin)
                .readableWidth()
                .verticalScroll(scrollState)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            val titleLabel = stringResource(R.string.field_title)
            LabeledField(titleLabel) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    supportingText = {
                        if (showErrors && !titleValid) ErrorText(titleError) else HelpText(stringResource(R.string.field_title_help))
                    },
                    isError = showErrors && !titleValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                    keyboardActions = nextField,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocus)
                        .semantics {
                            contentDescription = titleLabel
                            // TalkBack reads this exact message instead of a generic "Invalid input".
                            if (showErrors && !titleValid) error(titleError)
                        },
                )
            }
            val linkLabel = stringResource(R.string.field_link)
            LabeledField(linkLabel) {
                OutlinedTextField(
                    value = link,
                    onValueChange = { link = it.replace("\n", "") }, // links never contain line breaks
                    supportingText = {
                        if (showErrors && !linkValid) ErrorText(linkError)
                    },
                    isError = showErrors && !linkValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri, imeAction = ImeAction.Next),
                    keyboardActions = nextField,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(linkFocus)
                        .semantics {
                            contentDescription = linkLabel
                            if (showErrors && !linkValid) error(linkError)
                        },
                )
            }
            val passwordLabel = stringResource(R.string.field_password)
            LabeledField(passwordLabel) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    supportingText = { HelpText(stringResource(R.string.field_password_help)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Next),
                    keyboardActions = nextField,
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = passwordLabel },
                )
            }
            val displayNameLabel = stringResource(R.string.field_display_name)
            LabeledField(displayNameLabel) {
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().semantics { contentDescription = displayNameLabel },
                )
            }

            Text(
                stringResource(R.string.field_color),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            )
            ColorPicker(selected = color, onSelect = { colorName = it.name })

            Text(
                stringResource(R.string.field_icon),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            )
            IconPicker(
                selected = icon.takeUnless { usePhoto },
                color = color,
                onSelect = {
                    iconName = it.name
                    usePhoto = false
                },
                photo = photo,
                photoSelected = usePhoto,
                onPickPhoto = {
                    pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
            )

            Text(
                stringResource(R.string.preview),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp).semantics { heading() },
            )
            // Only a picture of the button: TalkBack reads "Preview: …" and it can't be "pressed".
            Box(Modifier.clearAndSetSemantics { contentDescription = previewDescription }) {
                MeetingButton(title.ifBlank { screenTitle }, color, icon, photo.takeIf { usePhoto }, onClick = {})
            }

            if (shortcutsSupported) {
                ShortcutCheckbox(checked = homeShortcut, onCheckedChange = { homeShortcut = it })
            }

            Button(
                onClick = {
                    if (titleValid && linkValid) {
                        val base = initial ?: Meeting(title = "", link = "", password = "", displayName = "")
                        val chosenPhoto = photoPath
                        val photoStamp = if (usePhoto && chosenPhoto != null) {
                            MeetingPhotos.save(context, chosenPhoto, base.id)
                        } else {
                            if (base.hasPhoto) MeetingPhotos.delete(context, base.id)
                            0L
                        }
                        onSave(
                            base.copy(
                                title = title.trim(),
                                link = link.trim(),
                                password = password.trim(),
                                displayName = displayName.trim(),
                                color = color,
                                icon = icon,
                                photo = photoStamp,
                                homeShortcut = homeShortcut,
                            )
                        )
                    } else {
                        showErrors = true
                        // Take the person straight to the first field that needs fixing. Both are at the top
                        // of the form, so scroll up too (focus alone doesn't scroll if the field already had it).
                        if (!titleValid) titleFocus.requestFocus() else linkFocus.requestFocus()
                        scope.launch { scrollState.animateScrollTo(0) }
                    }
                },
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** "Add a big button on the phone's home screen": the whole row is one big tap target. */
@Composable
fun ShortcutCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .toggleable(value = checked, role = Role.Checkbox, onValueChange = onCheckedChange)
            .padding(horizontal = 12.dp, vertical = 16.dp),
    ) {
        Checkbox(checked = checked, onCheckedChange = null, modifier = Modifier.size(32.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.shortcut_checkbox), style = MaterialTheme.typography.titleSmall)
            Text(
                stringResource(R.string.shortcut_checkbox_help),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/** Form error message that TalkBack announces as soon as it appears. */
@Composable
fun ErrorText(text: String) {
    Text(text, fontSize = 16.scaledSp, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
}

/**
 * A form field with its name above the box rather than inside its border, so a long name at a
 * large font size never runs across the border. The name is hidden from TalkBack: the field reads
 * it itself (its `contentDescription`).
 */
@Composable
fun LabeledField(label: String, field: @Composable () -> Unit) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp).clearAndSetSemantics { },
        )
        field()
    }
}

/** Extra explanation under a form field (replaced by the error message when there is one). */
@Composable
fun HelpText(text: String) {
    Text(text, fontSize = 16.scaledSp)
}

/**
 * 8 big round swatches in two rows of four, each with its name underneath (so color is never the
 * only clue). The chosen one gets a thick border and a check mark; TalkBack reads "Blue, selected".
 */
@Composable
fun ColorPicker(selected: MeetingColor, onSelect: (MeetingColor) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.selectableGroup()) {
        val perRow = pickerColumns()
        MeetingColor.entries.chunked(perRow).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { option ->
                    val isSelected = option == selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(option) }
                            .padding(vertical = 4.dp),
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(option.color)
                                .border(
                                    width = if (isSelected) 5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape,
                                ),
                        ) {
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                            }
                        }
                        Text(
                            stringResource(option.label),
                            fontSize = 16.scaledSp,
                            lineHeight = 20.scaledSp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
                repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** Options per row in the pickers: fewer when the phone's text is large, so names like "Naranja" never break mid-word. */
@Composable
private fun pickerColumns(): Int {
    val fontScale = LocalDensity.current.fontScale
    return when {
        fontScale >= 1.6f -> 2
        fontScale >= 1.15f -> 3
        else -> 4
    }
}

/**
 * The meeting pictures, shown on the chosen button color so they look like the real button. Each has
 * its name underneath; the chosen one gets a thick border and a check mark. TalkBack reads "Family, selected".
 */
/**
 * "Photo" choice in the picture list: opens the phone's photo picker. Shows the chosen photo once
 * there is one (tap again to change it).
 */
@Composable
fun PhotoTile(photo: ImageBitmap?, isSelected: Boolean, color: MeetingColor, onPick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.icon_photo_a11y)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onPick)
            .semantics { contentDescription = description }
            .padding(vertical = 4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(color.color)
                .border(
                    width = if (isSelected) 5.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                    shape = RoundedCornerShape(16.dp),
                ),
        ) {
            if (photo != null) {
                Image(
                    photo,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize(0.72f)
                        .clip(CircleShape)
                        .border(3.dp, Color.White, CircleShape),
                )
            } else {
                Icon(
                    painterResource(R.drawable.ic_photo),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.align(Alignment.Center).fillMaxSize(0.5f),
                )
            }
            if (isSelected) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                }
            }
        }
        Text(
            stringResource(R.string.icon_photo),
            fontSize = 16.scaledSp,
            lineHeight = 20.scaledSp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}

@Composable
fun IconPicker(
    selected: MeetingIcon?,
    color: MeetingColor,
    onSelect: (MeetingIcon) -> Unit,
    photo: ImageBitmap? = null,
    photoSelected: Boolean = false,
    onPickPhoto: () -> Unit = {},
) {
    val perRow = pickerColumns()
    // The drawings, then one more tile (null) for "Photo".
    val options: List<MeetingIcon?> = MeetingIcon.entries + listOf(null)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.selectableGroup()) {
        options.chunked(perRow).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { option ->
                    if (option == null) {
                        PhotoTile(photo, photoSelected, color, onPickPhoto, Modifier.weight(1f))
                        return@forEach
                    }
                    val isSelected = option == selected
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(selected = isSelected, role = Role.RadioButton) { onSelect(option) }
                            .padding(vertical = 4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(color.color)
                                .border(
                                    width = if (isSelected) 5.dp else 0.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = RoundedCornerShape(16.dp),
                                ),
                        ) {
                            if (option == MeetingIcon.NONE) {
                                Icon(
                                    painterResource(R.drawable.ic_no_picture),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.align(Alignment.Center).fillMaxSize(0.5f),
                                )
                            } else {
                                MeetingPicture(
                                    option,
                                    color,
                                    Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth(0.72f)
                                        .aspectRatio(56f / 50f),
                                )
                            }
                            if (isSelected) {
                                // Check in a white circle in the corner, so it doesn't cover the picture.
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(22.dp))
                                }
                            }
                        }
                        Text(
                            stringResource(option.label),
                            fontSize = 16.scaledSp,
                            lineHeight = 20.scaledSp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
                repeat(perRow - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}
