package com.example.myloncher

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.tv.TvContract
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import com.example.myloncher.ui.theme.MyLoncherTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

const val DEFAULT_TAGLINE = "Legen... wait for it... dary."

// logo = name of a PNG in res/drawable (without .png). If the file isn't there, the tile shows text.
data class AppItem(val name: String, val packages: List<String>, val logo: String = "")

data class InstalledApp(
    val pkg: String,
    val label: String,
    val icon: ImageBitmap,
    val banner: ImageBitmap?
)

val apps = listOf(
    AppItem("YouTube", listOf("com.google.android.youtube.tv", "com.google.android.youtube"), "logo_youtube"),
    AppItem("Netflix", listOf("com.netflix.mediaclient"), "logo_netflix"),
    AppItem("JioHotstar", listOf("in.startv.hotstar", "in.startv.hotstar.dplus.tv", "com.startv.hotstar"), "logo_jiohotstar"),
    AppItem("Spotify", listOf("com.spotify.music"), "logo_spotify"),
    AppItem("Play Store", listOf("com.android.vending"), "logo_playstore"),
    AppItem("Jellyfin", listOf("org.jellyfin.androidtv", "org.jellyfin.mobile"), "logo_jellyfin")
)

val tabNames = listOf("All applications", "Media", "Tools", "Games")
val tabKeys = listOf("all", "media", "tools", "games")

val mediaPackages = setOf(
    "com.google.android.youtube.tv", "com.google.android.youtube",
    "com.google.android.apps.youtube.music", "com.netflix.mediaclient",
    "com.amazon.amazonvideo.livingroom", "com.amazon.avod.thirdpartyclient",
    "in.startv.hotstar", "in.startv.hotstar.dplus.tv", "com.startv.hotstar",
    "com.disney.disneyplus", "com.sonyliv", "com.graymatrix.did",
    "org.videolan.vlc", "org.xbmc.kodi", "com.plexapp.android",
    "org.jellyfin.androidtv", "org.jellyfin.mobile",
    "com.spotify.music", "com.spotify.tv.android",
    "com.mxtech.videoplayer.ad", "com.mxtech.videoplayer.pro",
    "tv.twitch.android.app", "com.jio.media.ondemand", "com.tv.v18.viola",
    "com.apple.atve.androidtv.appletv", "com.gaana", "com.jio.media.jiobeats",
    "com.bsbportal.music"
)

val toolPackages = setOf(
    "com.android.settings", "com.android.tv.settings", "com.android.chrome",
    "com.android.vending", "com.google.android.apps.nbu.files"
)

val mediaWords = listOf(
    "netflix", "youtube", "hotstar", "prime video", "spotify", "jellyfin", "vlc",
    "kodi", "plex", "music", "video", "player", "zee5", "sonyliv", "sony liv",
    "jio", "disney", "twitch", "voot", "apple tv", "gaana", "wynk", "saavn"
)

val toolWords = listOf(
    "file", "settings", "manager", "explorer", "browser", "chrome", "mirror",
    "cast", "play store", "terminal", "clean"
)

val gameWords = listOf("game", "ps5", "playstation", "xbox", "steam", "minecraft", "roblox")

class MainActivity : ComponentActivity() {
    private var resumeTick by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyLoncherTheme {
                HomeScreen(resumeTick)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        resumeTick++
    }
}

fun prefs(context: Context) =
    context.getSharedPreferences("launcher", Context.MODE_PRIVATE)

fun loadImage(context: Context, key: String, maxSide: Int = 1920): ImageBitmap? {
    return try {
        val saved = prefs(context).getString(key, null) ?: return null
        val uri = Uri.parse(saved)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }

        var sample = 1
        while (bounds.outWidth / sample > maxSide || bounds.outHeight / sample > maxSide) {
            sample *= 2
        }

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }

        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)?.asImageBitmap()
        }
    } catch (e: Exception) {
        null
    }
}

fun savePick(context: Context, key: String, uri: Uri) {
    try {
        context.contentResolver.takePersistableUriPermission(
            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    } catch (e: Exception) {
    }

    prefs(context).edit().putString(key, uri.toString()).apply()
}

fun loadInstalledApps(context: Context): List<InstalledApp> {
    val pm = context.packageManager
    val seen = HashSet<String>()
    val result = mutableListOf<InstalledApp>()

    val intents = listOf(
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER),
        Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    )

    for (intent in intents) {
        for (ri in pm.queryIntentActivities(intent, 0)) {
            val pkg = ri.activityInfo.packageName

            if (pkg == context.packageName) continue
            if (!seen.add(pkg)) continue

            try {
                val icon = ri.loadIcon(pm).toBitmap(128, 128).asImageBitmap()

                val banner = try {
                    (ri.activityInfo.loadBanner(pm) ?: ri.activityInfo.applicationInfo.loadBanner(pm))
                        ?.toBitmap(320, 180)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }

                result.add(InstalledApp(pkg, ri.loadLabel(pm).toString(), icon, banner))
            } catch (e: Exception) {
            }
        }
    }

    return result.sortedBy { it.label.lowercase() }
}

fun defaultCategories(app: InstalledApp): Set<String> {
    val name = app.label.lowercase()
    val result = mutableSetOf<String>()

    if (app.pkg in mediaPackages || mediaWords.any { name.contains(it) }) result.add("media")
    if (app.pkg in toolPackages || toolWords.any { name.contains(it) }) result.add("tools")
    if (gameWords.any { name.contains(it) }) result.add("games")

    return result
}

fun categoriesFor(context: Context, app: InstalledApp): Set<String> {
    val saved = prefs(context).getStringSet("cats:${app.pkg}", null)
    return if (saved != null) HashSet(saved) else defaultCategories(app)
}

fun toggleCategory(context: Context, app: InstalledApp, cat: String) {
    val set = categoriesFor(context, app).toMutableSet()

    if (!set.add(cat)) set.remove(cat)

    prefs(context).edit().putStringSet("cats:${app.pkg}", HashSet(set)).apply()
}

@Composable
fun HomeScreen(resumeTick: Int) {
    val context = LocalContext.current
    val activity = context as ComponentActivity

    var time by remember { mutableStateOf(currentTime()) }
    var wallpaper by remember { mutableStateOf(loadImage(context, "wallpaper")) }
    var appsWallpaper by remember { mutableStateOf(loadImage(context, "appswallpaper")) }
    var saver by remember { mutableStateOf(loadImage(context, "screensaver")) }

    var tagline by remember {
        mutableStateOf(prefs(context).getString("tagline", DEFAULT_TAGLINE) ?: DEFAULT_TAGLINE)
    }

    var page by remember { mutableStateOf("home") }
    var tab by remember { mutableStateOf(0) }
    var installed by remember { mutableStateOf(loadInstalledApps(context)) }
    var menuApp by remember { mutableStateOf<InstalledApp?>(null) }
    var catVersion by remember { mutableStateOf(0) }
    var panelOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var inputOpen by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var pickFm by remember { mutableStateOf(false) }
    var quickOpen by remember { mutableStateOf(false) }
    var saverOn by remember { mutableStateOf(false) }
    var saverSeen by remember { mutableStateOf(false) }
    var editTagline by remember { mutableStateOf(false) }
    var lastActivity by remember { mutableStateOf(System.currentTimeMillis()) }

    val idleMs = 5 * 60 * 1000L

    val firstTileFocus = remember { FocusRequester() }
    val customizeFocus = remember { FocusRequester() }
    val panelFirst = remember { FocusRequester() }
    val tabFocus = remember { FocusRequester() }
    val menuFirst = remember { FocusRequester() }
    val arrowFocus = remember { FocusRequester() }
    val quickFirst = remember { FocusRequester() }
    val sideFirst = remember { FocusRequester() }
    val inputRowFocus = remember { FocusRequester() }
    val inputFirst = remember { FocusRequester() }
    val searchFocus = remember { FocusRequester() }

    val wallpaperPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            savePick(context, "wallpaper", uri)
            wallpaper = loadImage(context, "wallpaper")
        }
    }

    val appsWallpaperPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            savePick(context, "appswallpaper", uri)
            appsWallpaper = loadImage(context, "appswallpaper")
        }
    }

    val saverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            savePick(context, "screensaver", uri)
            saver = loadImage(context, "screensaver")
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            time = currentTime()
            delay(10_000)
        }
    }

    LaunchedEffect(page) {
        quickOpen = false
        menuOpen = false
        inputOpen = false
        delay(100)

        runCatching {
            if (page == "home") firstTileFocus.requestFocus() else tabFocus.requestFocus()
        }
    }

    LaunchedEffect(resumeTick) {
        lastActivity = System.currentTimeMillis()
        saverOn = false
        installed = loadInstalledApps(context)
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5_000)

            val resumed = activity.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

            if (resumed && !saverOn && saver != null &&
                System.currentTimeMillis() - lastActivity > idleMs
            ) {
                panelOpen = false
                quickOpen = false
                menuOpen = false
                inputOpen = false
                saverOn = true
            }
        }
    }

    LaunchedEffect(panelOpen) {
        if (panelOpen) {
            delay(250)
            runCatching { panelFirst.requestFocus() }
        }
    }

    LaunchedEffect(quickOpen) {
        if (quickOpen) {
            delay(250)
            runCatching { quickFirst.requestFocus() }
        }
    }

    LaunchedEffect(menuOpen) {
        if (menuOpen) {
            delay(250)
            runCatching { sideFirst.requestFocus() }
        }
    }

    LaunchedEffect(inputOpen) {
        if (inputOpen) {
            delay(250)
            runCatching { inputFirst.requestFocus() }
        } else if (menuOpen) {
            delay(250)
            runCatching { inputRowFocus.requestFocus() }
        }
    }

    LaunchedEffect(menuApp) {
        if (menuApp != null) {
            delay(250)
            runCatching { menuFirst.requestFocus() }
        }
    }

    LaunchedEffect(saverOn) {
        if (saverOn) {
            saverSeen = true
        } else if (saverSeen) {
            delay(100)
            runCatching {
                if (page == "home") firstTileFocus.requestFocus() else tabFocus.requestFocus()
            }
        }
    }

    BackHandler(enabled = page == "apps") {
        page = "home"
    }

    BackHandler(enabled = quickOpen) {
        quickOpen = false
        runCatching { arrowFocus.requestFocus() }
    }

    BackHandler(enabled = panelOpen) {
        panelOpen = false
        runCatching {
            if (page == "home") customizeFocus.requestFocus() else arrowFocus.requestFocus()
        }
    }

    BackHandler(enabled = menuOpen && !inputOpen) {
        menuOpen = false
        runCatching { customizeFocus.requestFocus() }
    }

    BackHandler(enabled = menuOpen && inputOpen) {
        inputOpen = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .onPreviewKeyEvent { e ->
                lastActivity = System.currentTimeMillis()

                if (saverOn) {
                    if (e.type == KeyEventType.KeyDown) saverOn = false
                    true
                } else {
                    false
                }
            }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent(PointerEventPass.Initial)
                        lastActivity = System.currentTimeMillis()
                    }
                }
            }
    ) {
        if (page == "home") {
            wallpaper?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 60.dp, vertical = 40.dp)
            ) {
                Text(
                    text = time,
                    color = Color.White,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.align(Alignment.TopStart)
                )

                Row(
                    modifier = Modifier.align(Alignment.TopEnd),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TopButton("Apps") {
                        page = "apps"
                    }

                    RefreshIconButton {
                        killBackgroundApps(context)
                    }

                    TopButton("▼", Modifier.focusRequester(customizeFocus)) {
                        inputOpen = false
                        menuOpen = !menuOpen
                    }
                }

                Text(
                    text = tagline,
                    color = Color.White,
                    fontSize = 32.sp,
                    modifier = Modifier.align(Alignment.CenterStart)
                )

                Row(
                    modifier = Modifier.align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    apps.forEachIndexed { index, item ->
                        AppTile(
                            label = item.name,
                            logo = item.logo,
                            modifier = if (index == 0) Modifier.focusRequester(firstTileFocus) else Modifier
                        ) {
                            openApp(context, item)
                        }
                    }
                }
            }
        } else {
            appsWallpaper?.let {
                Image(
                    bitmap = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x99000000))
                )
            }

            val visible = remember(tab, catVersion, installed) {
                if (tab == 0) installed
                else installed.filter { categoriesFor(context, it).contains(tabKeys[tab]) }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 60.dp, vertical = 40.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        tabNames.forEachIndexed { i, name ->
                            TabItem(
                                label = name,
                                selected = tab == i,
                                modifier = if (i == 0) Modifier.focusRequester(tabFocus) else Modifier
                            ) {
                                tab = i
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = time,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Light
                            )
                        }

                        // HOME BUTTON: icon version
                        HomeIconButton {
                            quickOpen = false
                            page = "home"
                        }

                        RefreshIconButton {
                            killBackgroundApps(context)
                        }

                        TopButton("▼", Modifier.focusRequester(arrowFocus)) {
                            quickOpen = !quickOpen
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(160.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(visible, key = { it.pkg }) { app ->
                        AppGridTile(
                            app = app,
                            onClick = { launchInstalled(context, app) },
                            onLongClick = { menuApp = app }
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = quickOpen && page == "apps",
            enter = slideInVertically(tween(250)) { -it } + fadeIn(tween(250)),
            exit = slideOutVertically(tween(250)) { -it } + fadeOut(tween(250)),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 60.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(Color(0xEE111111), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TopButton("Wi-Fi", Modifier.focusRequester(quickFirst)) {
                    quickOpen = false
                    openSettings(context, Settings.ACTION_WIFI_SETTINGS)
                }

                TopButton("Customize") {
                    quickOpen = false
                    panelOpen = true
                }

                TopButton("Settings") {
                    quickOpen = false
                    openSettings(context, Settings.ACTION_SETTINGS)
                }
            }
        }

        // ---------- Home page side menu (right side, Apple TV style) ----------
        AnimatedVisibility(
            visible = menuOpen && !inputOpen && page == "home",
            enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            SidePanel(
                time = time,
                onBack = {
                    menuOpen = false
                    runCatching { customizeFocus.requestFocus() }
                }
            ) {
                SideRow("Search", "search", Modifier.focusRequester(sideFirst)) {
                    menuOpen = false
                    searchQuery = ""
                    searchOpen = true
                }

                SideRow("Customize", "customize") {
                    menuOpen = false
                    panelOpen = true
                }

                SideRow("Input", "input", Modifier.focusRequester(inputRowFocus)) {
                    inputOpen = true
                }

                SideRow("File Manager", "folder") {
                    menuOpen = false
                    openFileManager(context, installed)
                }

                SideRow("Wi-Fi", "wifi") {
                    menuOpen = false
                    openSettings(context, Settings.ACTION_WIFI_SETTINGS)
                }

                SideRow("Settings", "settings") {
                    menuOpen = false
                    openSettings(context, Settings.ACTION_SETTINGS)
                }
            }
        }

        // ---------- Input panel (second side panel) - unchanged ----------
        AnimatedVisibility(
            visible = menuOpen && inputOpen && page == "home",
            enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            SidePanel(time) {
                SideRow("Input", "back") {
                    inputOpen = false
                }

                SideRow("HDMI 1", "hdmi", Modifier.focusRequester(inputFirst)) {
                    menuOpen = false
                    inputOpen = false
                    openTvInput(context, "hdmi", 1)
                }

                SideRow("HDMI 2", "hdmi") {
                    menuOpen = false
                    inputOpen = false
                    openTvInput(context, "hdmi", 2)
                }

                SideRow("HDMI 3", "hdmi") {
                    menuOpen = false
                    inputOpen = false
                    openTvInput(context, "hdmi", 3)
                }

                SideRow("AV 1", "av") {
                    menuOpen = false
                    inputOpen = false
                    openTvInput(context, "av", 1)
                }

                SideRow("AV 2", "av") {
                    menuOpen = false
                    inputOpen = false
                    openTvInput(context, "av", 2)
                }
            }
        }

        // ---------- Customize panel ----------
        AnimatedVisibility(
            visible = panelOpen,
            enter = slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)),
            exit = slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Column(
                modifier = Modifier
                    .width(300.dp)
                    .fillMaxHeight()
                    .background(Color(0xEE111111))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BackIconButton {
                        panelOpen = false
                        runCatching {
                            if (page == "home") customizeFocus.requestFocus() else arrowFocus.requestFocus()
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        "Customize",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light
                    )
                }

                PanelItem("Home screen wallpaper", Modifier.focusRequester(panelFirst)) {
                    wallpaperPicker.launch(arrayOf("image/*"))
                }

                PanelItem("Apps screen wallpaper") {
                    appsWallpaperPicker.launch(arrayOf("image/*"))
                }

                PanelItem("Screen saver picture") {
                    saverPicker.launch(arrayOf("image/*"))
                }

                PanelItem("Edit tagline") {
                    editTagline = true
                }

                PanelItem("Choose file manager") {
                    panelOpen = false
                    pickFm = true
                }

                PanelItem("Start screen saver now") {
                    if (saver == null) {
                        Toast.makeText(
                            context,
                            "Pick a screen saver picture first",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        panelOpen = false
                        saverOn = true
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = saverOn,
            enter = fadeIn(tween(1500)),
            exit = fadeOut(tween(300))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable {
                        saverOn = false
                        lastActivity = System.currentTimeMillis()
                    },
                contentAlignment = Alignment.Center
            ) {
                saver?.let {
                    Image(
                        bitmap = it,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxHeight(0.8f)
                    )
                }
            }
        }
    }

    // ---------- App long-press menu ----------
    menuApp?.let { app ->
        val cats = remember(catVersion, app) { categoriesFor(context, app) }

        Dialog(onDismissRequest = { menuApp = null }) {
            Column(
                modifier = Modifier
                    .width(340.dp)
                    .background(Color(0xFF151515), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    app.label,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light
                )

                PanelItem(
                    (if ("media" in cats) "[x] " else "[  ] ") + "Media",
                    Modifier.focusRequester(menuFirst)
                ) {
                    toggleCategory(context, app, "media")
                    catVersion++
                }

                PanelItem((if ("tools" in cats) "[x] " else "[  ] ") + "Tools") {
                    toggleCategory(context, app, "tools")
                    catVersion++
                }

                PanelItem((if ("games" in cats) "[x] " else "[  ] ") + "Games") {
                    toggleCategory(context, app, "games")
                    catVersion++
                }

                PanelItem("App info") {
                    menuApp = null
                    openAppInfo(context, app.pkg)
                }

                PanelItem("Uninstall") {
                    menuApp = null
                    uninstallApp(context, app.pkg)
                }

                PanelItem("Cancel") {
                    menuApp = null
                }
            }
        }
    }

    // ---------- Search apps dialog ----------
    if (searchOpen) {
        val results = remember(searchQuery, installed) {
            if (searchQuery.isBlank()) {
                emptyList()
            } else {
                installed.filter { it.label.contains(searchQuery, ignoreCase = true) }.take(5)
            }
        }

        Dialog(onDismissRequest = { searchOpen = false }) {
            LaunchedEffect(Unit) {
                delay(300)
                runCatching { searchFocus.requestFocus() }
            }

            Column(
                modifier = Modifier
                    .width(420.dp)
                    .background(Color(0xFF151515), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Search apps",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light
                )

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(searchFocus)
                )

                results.forEach { app ->
                    PanelItem(app.label) {
                        searchOpen = false
                        launchInstalled(context, app)
                    }
                }

                PanelItem("Cancel") {
                    searchOpen = false
                }
            }
        }
    }

    // ---------- Choose file manager dialog ----------
    if (pickFm) {
        val sorted = remember(installed) {
            installed.sortedBy { if (it.label.contains("file", ignoreCase = true)) 0 else 1 }
        }

        Dialog(onDismissRequest = { pickFm = false }) {
            Column(
                modifier = Modifier
                    .width(380.dp)
                    .height(440.dp)
                    .background(Color(0xFF151515), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Choose file manager",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Light
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sorted, key = { it.pkg }) { app ->
                        PanelItem(app.label) {
                            prefs(context).edit().putString("filemanager", app.pkg).apply()
                            pickFm = false
                            Toast.makeText(
                                context,
                                "File manager set to ${app.label}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                PanelItem("Cancel") {
                    pickFm = false
                }
            }
        }
    }

    // ---------- Edit tagline dialog ----------
    if (editTagline) {
        var draft by remember { mutableStateOf(tagline) }

        AlertDialog(
            onDismissRequest = { editTagline = false },
            title = { Text("Edit tagline") },
            text = {
                TextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    tagline = draft
                    prefs(context).edit().putString("tagline", draft).apply()
                    editTagline = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editTagline = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AppTile(label: String, logo: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val context = LocalContext.current
    val resId = remember(logo) {
        if (logo.isEmpty()) 0 else context.resources.getIdentifier(logo, "drawable", context.packageName)
    }

    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.12f else 1f, label = "tileScale")

    Box(
        modifier = modifier
            .width(110.dp)
            .height(80.dp)
            .scale(scale)
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) Color.White else Color(0xFF444444),
                shape = RoundedCornerShape(10.dp)
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (resId != 0) {
            Image(
                painter = painterResource(resId),
                contentDescription = label,
                modifier = Modifier.size(46.dp)
            )
        } else {
            Text(label, color = Color.White, fontSize = 18.sp)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppGridTile(app: InstalledApp, onClick: () -> Unit, onLongClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.1f else 1f, label = "gridScale")
    val shape = RoundedCornerShape(10.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(if (focused) 1f else 0f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .scale(scale)
                .clip(shape)
                .background(Color(0xFF2B2B2B))
                .border(if (focused) 3.dp else 0.dp, Color.White, shape)
                .onFocusChanged { focused = it.isFocused }
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
            contentAlignment = Alignment.Center
        ) {
            if (app.banner != null) {
                Image(
                    bitmap = app.banner,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = app.icon,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp)
                    )

                    Text(
                        app.label,
                        color = Color.White,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Text(
            text = if (focused && app.banner != null) app.label else "",
            color = Color.White,
            fontSize = 13.sp,
            maxLines = 1,
            modifier = Modifier.height(22.dp).padding(top = 6.dp)
        )
    }
}

@Composable
fun TabItem(
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onFocus: () -> Unit
) {
    var focused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .onFocusChanged {
                focused = it.isFocused
                if (it.isFocused) onFocus()
            }
            .clickable { onFocus() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            label,
            color = if (selected) Color.White else Color(0xFF888888),
            fontSize = if (selected || focused) 22.sp else 18.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun TopButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.15f else 1f, label = "topScale")

    Box(
        modifier = modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            color = if (focused) Color.White else Color(0xFFAAAAAA),
            fontSize = 16.sp
        )
    }
}

@Composable
fun PanelItem(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = if (focused) 2.dp else 1.dp,
                color = if (focused) Color.White else Color(0xFF444444),
                shape = RoundedCornerShape(10.dp)
            )
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(label, color = Color.White, fontSize = 18.sp)
    }
}

// ---------- Side panel pieces (Apple TV style) ----------

// onBack = null keeps the panel exactly as before (time only, no arrow).
@Composable
fun SidePanel(
    time: String,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .width(360.dp)
            .fillMaxHeight()
            .background(
                Color(0xF01B2128),
                RoundedCornerShape(topStart = 32.dp, bottomStart = 32.dp)
            )
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                BackIconButton(onBack)
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }

            Text(
                text = time,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        content()
    }
}

@Composable
fun SideRow(label: String, icon: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.04f else 1f, label = "sideRowScale")
    val shape = RoundedCornerShape(percent = 50)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(shape)
            .background(if (focused) Color(0xF2FFFFFF) else Color.Transparent)
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (focused) Color(0x22000000) else Color(0x33FFFFFF)),
            contentAlignment = Alignment.Center
        ) {
            PanelIcon(icon, if (focused) Color(0xFF111111) else Color.White)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            color = if (focused) Color(0xFF111111) else Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun PanelIcon(kind: String, tint: Color) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                val st = 2.dp.toPx()

                when (kind) {
                    "search" -> {
                        drawCircle(
                            tint,
                            radius = w * 0.30f,
                            center = Offset(w * 0.42f, h * 0.42f),
                            style = Stroke(width = st)
                        )
                        drawLine(tint, Offset(w * 0.64f, h * 0.64f), Offset(w * 0.95f, h * 0.95f), st)
                    }

                    "customize" -> {
                        drawLine(tint, Offset(0f, h * 0.2f), Offset(w, h * 0.2f), st)
                        drawLine(tint, Offset(0f, h * 0.5f), Offset(w, h * 0.5f), st)
                        drawLine(tint, Offset(0f, h * 0.8f), Offset(w, h * 0.8f), st)
                        drawCircle(tint, radius = w * 0.13f, center = Offset(w * 0.3f, h * 0.2f))
                        drawCircle(tint, radius = w * 0.13f, center = Offset(w * 0.7f, h * 0.5f))
                        drawCircle(tint, radius = w * 0.13f, center = Offset(w * 0.4f, h * 0.8f))
                    }

                    "input" -> {
                        drawLine(tint, Offset(w * 0.35f, h * 0.1f), Offset(w * 0.95f, h * 0.1f), st)
                        drawLine(tint, Offset(w * 0.95f, h * 0.1f), Offset(w * 0.95f, h * 0.9f), st)
                        drawLine(tint, Offset(w * 0.95f, h * 0.9f), Offset(w * 0.35f, h * 0.9f), st)
                        drawLine(tint, Offset(0f, h * 0.5f), Offset(w * 0.65f, h * 0.5f), st)
                        drawLine(tint, Offset(w * 0.65f, h * 0.5f), Offset(w * 0.45f, h * 0.3f), st)
                        drawLine(tint, Offset(w * 0.65f, h * 0.5f), Offset(w * 0.45f, h * 0.7f), st)
                    }

                    "folder" -> {
                        drawRect(
                            tint,
                            topLeft = Offset(0f, h * 0.25f),
                            size = Size(w, h * 0.65f),
                            style = Stroke(width = st)
                        )
                        drawRect(
                            tint,
                            topLeft = Offset(0f, h * 0.08f),
                            size = Size(w * 0.4f, h * 0.17f)
                        )
                    }

                    "wifi" -> {
                        val cx = w / 2
                        val cy = h * 0.88f
                        for (r in listOf(0.85f, 0.55f, 0.27f)) {
                            val rad = w * r
                            drawArc(
                                color = tint,
                                startAngle = 225f,
                                sweepAngle = 90f,
                                useCenter = false,
                                topLeft = Offset(cx - rad, cy - rad),
                                size = Size(rad * 2, rad * 2),
                                style = Stroke(width = st)
                            )
                        }
                        drawCircle(tint, radius = st, center = Offset(cx, cy))
                    }

                    "settings" -> {
                        val cx = w / 2
                        val cy = h / 2
                        drawCircle(
                            tint,
                            radius = w * 0.22f,
                            center = Offset(cx, cy),
                            style = Stroke(width = st)
                        )
                        for (i in 0 until 8) {
                            val a = i * PI / 4
                            val dx = cos(a).toFloat()
                            val dy = sin(a).toFloat()
                            drawLine(
                                tint,
                                Offset(cx + dx * w * 0.34f, cy + dy * h * 0.34f),
                                Offset(cx + dx * w * 0.50f, cy + dy * h * 0.50f),
                                st
                            )
                        }
                    }

                    "hdmi" -> {
                        drawRect(
                            tint,
                            topLeft = Offset(w * 0.05f, h * 0.2f),
                            size = Size(w * 0.9f, h * 0.45f),
                            style = Stroke(width = st)
                        )
                        drawLine(tint, Offset(w * 0.3f, h * 0.65f), Offset(w * 0.3f, h * 0.95f), st)
                        drawLine(tint, Offset(w * 0.7f, h * 0.65f), Offset(w * 0.7f, h * 0.95f), st)
                    }

                    "av" -> {
                        drawLine(Color(0xFFE53935), Offset(w * 0.15f, h * 0.05f), Offset(w * 0.15f, h * 0.95f), st * 1.5f)
                        drawLine(Color(0xFF1E88E5), Offset(w * 0.5f, h * 0.05f), Offset(w * 0.5f, h * 0.95f), st * 1.5f)
                        drawLine(Color(0xFF43A047), Offset(w * 0.85f, h * 0.05f), Offset(w * 0.85f, h * 0.95f), st * 1.5f)
                    }

                    "back" -> {
                        drawLine(tint, Offset(w * 0.65f, h * 0.1f), Offset(w * 0.25f, h * 0.5f), st)
                        drawLine(tint, Offset(w * 0.25f, h * 0.5f), Offset(w * 0.65f, h * 0.9f), st)
                    }

                    else -> {
                    }
                }
            }
    )
}

// ---------- Helpers ----------

fun currentTime(): String =
    SimpleDateFormat("hh:mm", Locale.getDefault()).format(Date())

fun openSettings(context: Context, action: String) {
    try {
        context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, "Can't open that here", Toast.LENGTH_SHORT).show()
    }
}

fun openAppInfo(context: Context, pkg: String) {
    try {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        Toast.makeText(context, "App info isn't available on this TV", Toast.LENGTH_SHORT).show()
    }
}

fun uninstallApp(context: Context, pkg: String) {
    try {
        context.startActivity(
            Intent(Intent.ACTION_DELETE, Uri.parse("package:$pkg"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        Toast.makeText(context, "Can't uninstall that here", Toast.LENGTH_SHORT).show()
    }
}

fun launchInstalled(context: Context, app: InstalledApp) {
    val pm = context.packageManager
    val launch = pm.getLeanbackLaunchIntentForPackage(app.pkg)
        ?: pm.getLaunchIntentForPackage(app.pkg)

    try {
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
        } else {
            Toast.makeText(context, "Can't open ${app.label}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Can't open ${app.label}", Toast.LENGTH_SHORT).show()
    }
}

fun openApp(context: Context, item: AppItem) {
    val pm = context.packageManager
    var launch: Intent? = null

    for (p in item.packages) {
        launch = pm.getLeanbackLaunchIntentForPackage(p) ?: pm.getLaunchIntentForPackage(p)
        if (launch != null) break
    }

    try {
        if (launch != null) {
            launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)
        } else {
            val store = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=${item.packages.first()}")
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            context.startActivity(store)
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Can't open ${item.name}", Toast.LENGTH_SHORT).show()
    }
}

// Uses the file manager you chose in Customize, or the first installed app with "file" in its name.
fun openFileManager(context: Context, installed: List<InstalledApp>) {
    val saved = prefs(context).getString("filemanager", null)

    val chosen = installed.firstOrNull { it.pkg == saved }
        ?: installed.firstOrNull { it.label.contains("file", ignoreCase = true) }

    if (chosen != null) {
        launchInstalled(context, chosen)
    } else {
        Toast.makeText(
            context,
            "No file manager found. Choose one in Customize.",
            Toast.LENGTH_LONG
        ).show()
    }
}

// Asks the TV to switch to an HDMI or AV input. Only works if this TV reports its inputs to Android.
fun openTvInput(context: Context, kind: String, number: Int) {
    val tm = context.getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager

    if (tm == null) {
        Toast.makeText(context, "This device has no TV inputs", Toast.LENGTH_SHORT).show()
        return
    }

    try {
        val all = tm.tvInputList

        val found: TvInputInfo? = if (kind == "hdmi") {
            val hdmi = all.filter { it.type == TvInputInfo.TYPE_HDMI }
            hdmi.firstOrNull {
                val label = (it.loadLabel(context)?.toString() ?: "").lowercase().replace(" ", "")
                label.contains("hdmi$number")
            } ?: hdmi.getOrNull(number - 1)
        } else {
            val av = all.filter {
                it.type == TvInputInfo.TYPE_COMPOSITE ||
                        it.type == TvInputInfo.TYPE_COMPONENT ||
                        it.type == TvInputInfo.TYPE_SCART
            }

            av.firstOrNull {
                val label = (it.loadLabel(context)?.toString() ?: "").lowercase().replace(" ", "")
                label.contains("av$number")
            } ?: av.getOrNull(number - 1)
        }

        if (found == null) {
            Toast.makeText(context, "This TV didn't report that input", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = TvContract.buildChannelUriForPassthroughInput(found.id)
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
        Toast.makeText(context, "Couldn't switch to that input", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun HomeIconButton(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.15f else 1f, label = "homeScale")
    val tint = if (focused) Color.White else Color(0xFFAAAAAA)

    Box(
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .size(22.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                val stroke = 2.dp.toPx()

                // roof
                drawLine(tint, Offset(0f, h * 0.45f), Offset(w / 2, 0f), stroke)
                drawLine(tint, Offset(w, h * 0.45f), Offset(w / 2, 0f), stroke)

                // walls and floor
                drawLine(tint, Offset(w * 0.15f, h * 0.4f), Offset(w * 0.15f, h), stroke)
                drawLine(tint, Offset(w * 0.85f, h * 0.4f), Offset(w * 0.85f, h), stroke)
                drawLine(tint, Offset(w * 0.15f, h), Offset(w * 0.85f, h), stroke)

                // door
                drawRect(
                    tint,
                    topLeft = Offset(w * 0.4f, h * 0.6f),
                    size = Size(w * 0.2f, h * 0.4f)
                )
            }
    )
}

fun killBackgroundApps(context: Context) {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    val pm = context.packageManager

    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
    val intent2 = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    val pkgs = HashSet<String>()
    for (i in listOf(intent, intent2)) {
        for (ri in pm.queryIntentActivities(i, 0)) {
            pkgs.add(ri.activityInfo.packageName)
        }
    }

    pkgs.remove(context.packageName)

    for (p in pkgs) {
        try {
            am.killBackgroundProcesses(p)
        } catch (e: Exception) {
        }
    }

    Toast.makeText(context, "Cleared background apps", Toast.LENGTH_SHORT).show()
}

@Composable
fun RefreshIconButton(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.15f else 1f, label = "refreshScale")
    val tint = if (focused) Color.White else Color(0xFFAAAAAA)

    Box(
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .size(22.dp)
            .drawBehind {
                val stroke = 2.dp.toPx()
                val inset = stroke
                drawArc(
                    color = tint,
                    startAngle = -60f,
                    sweepAngle = 290f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    style = Stroke(width = stroke)
                )

                // arrow head at the open end of the circle
                val w = size.width
                val h = size.height
                drawLine(tint, Offset(w * 0.78f, h * 0.02f), Offset(w * 0.78f, h * 0.30f), stroke)
                drawLine(tint, Offset(w * 0.78f, h * 0.30f), Offset(w * 0.50f, h * 0.30f), stroke)
            }
    )
}

// Back arrow (same chevron shape as the Input row), used in the home menu and Customize panels.
@Composable
fun BackIconButton(onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (focused) 1.15f else 1f, label = "backScale")
    val tint = if (focused) Color.White else Color(0xFFAAAAAA)

    Box(
        modifier = Modifier
            .scale(scale)
            .onFocusChanged { focused = it.isFocused }
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .size(22.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                val stroke = 2.dp.toPx()

                drawLine(tint, Offset(w * 0.65f, h * 0.1f), Offset(w * 0.25f, h * 0.5f), stroke)
                drawLine(tint, Offset(w * 0.25f, h * 0.5f), Offset(w * 0.65f, h * 0.9f), stroke)
            }
    )
}