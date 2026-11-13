package com.st.sotan.amarmaheshkhali.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color as AColor
import android.os.Build
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

private val PlayerGreen = Color(0xFF3DDC84)

/** Shared state of one video, so the inline view and the full-screen view continue from the same spot. */
@Stable
class VideoState {
    var view: VideoView? = null
    var position by mutableIntStateOf(0)
    var duration by mutableIntStateOf(0)
    var playing by mutableStateOf(false)
    var ended by mutableStateOf(false)
    var failed by mutableStateOf(false)
    var dragging by mutableStateOf(false)

    fun play() {
        val v = view
        if (ended) { v?.seekTo(0); position = 0; ended = false }
        v?.start()
        playing = true
    }

    fun pause() {
        view?.pause()
        playing = false
    }

    fun seekTo(ms: Int) {
        val t = ms.coerceIn(0, if (duration > 0) duration else Int.MAX_VALUE)
        view?.seekTo(t)
        position = t
        if (ended && t < duration) ended = false
    }
}

private fun formatTime(ms: Int): String {
    val total = (ms.coerceAtLeast(0)) / 1000
    return String.format(Locale.US, "%02d:%02d", total / 60, total % 60)
}

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/**
 * Plays a video bundled in assets/videos/ with custom controls (play/pause, ±10 s, seek bar,
 * time, full-screen). The platform VideoView cannot read assets directly, so the file is copied
 * once into the app cache (no permissions, no internet) and played from there.
 */
@Composable
fun VideoPlayer(asset: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val state = remember(asset) { VideoState() }
    var path by remember(asset) { mutableStateOf<String?>(null) }
    var fullscreen by remember(asset) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val orientation = LocalConfiguration.current.orientation

    LaunchedEffect(asset) {
        withContext(Dispatchers.IO) {
            try {
                val out = File(context.cacheDir, "videos/$asset")
                val expected = try { context.assets.openFd("videos/$asset").use { it.length } } catch (_: Exception) { -1L }
                if (!out.exists() || out.length() == 0L || (expected > 0 && out.length() != expected)) {
                    out.parentFile?.mkdirs()
                    val tmp = File(out.path + ".tmp")
                    context.assets.open("videos/$asset").use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }
                    tmp.renameTo(out)
                }
                path = out.path
            } catch (e: Exception) {
                android.util.Log.e("VideoPlayer", "Cannot prepare $asset", e)
                state.failed = true
            }
        }
    }

    // Rotate the phone sideways -> full screen; upright -> back to the page.
    LaunchedEffect(orientation) {
        fullscreen = orientation == Configuration.ORIENTATION_LANDSCAPE
    }

    // Pause when the app goes to the background.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, e -> if (e == Lifecycle.Event.ON_PAUSE) state.pause() }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(obs)
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // Update the clock / seek bar while playing.
    LaunchedEffect(state) {
        while (true) {
            val v = state.view
            if (v != null && !state.dragging) {
                try {
                    if (v.isPlaying) state.position = v.currentPosition
                    if (state.duration <= 0 && v.duration > 0) state.duration = v.duration
                } catch (_: Exception) {
                }
            }
            delay(200)
        }
    }

    fun enterFullscreen() {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        fullscreen = true
    }

    fun exitFullscreen() {
        fullscreen = false
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            scope.launch {
                delay(1200)
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            }
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val ratioBox = modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
        .background(Color.Black)

    val p = path
    when {
        state.failed -> Box(ratioBox, contentAlignment = Alignment.Center) {
            Text(tr("ভিডিও চালানো যাচ্ছে না।", "Cannot play this video."), color = Color.White)
        }
        p == null -> Box(ratioBox, contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.White)
        }
        fullscreen -> {
            Box(ratioBox)
            Dialog(
                onDismissRequest = { exitFullscreen() },
                properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
            ) {
                val view = LocalView.current
                DisposableEffect(view) {
                    val window = (view.parent as? DialogWindowProvider)?.window
                    if (window != null) {
                        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                        window.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(AColor.BLACK))
                        if (Build.VERSION.SDK_INT >= 28) {
                            window.attributes.layoutInDisplayCutoutMode =
                                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }
                        WindowCompat.setDecorFitsSystemWindows(window, false)
                        WindowInsetsControllerCompat(window, view).apply {
                            hide(WindowInsetsCompat.Type.systemBars())
                            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
                    }
                    onDispose { }
                }
                PlayerSurfaceWithControls(
                    state, p, true, { exitFullscreen() },
                    Modifier.fillMaxSize().background(Color.Black)
                )
            }
        }
        else -> PlayerSurfaceWithControls(state, p, false, { enterFullscreen() }, ratioBox)
    }
}

@Composable
private fun PlayerSurfaceWithControls(
    state: VideoState,
    path: String,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier
) {
    var controlsVisible by remember { mutableStateOf(true) }
    var touchCount by remember { mutableIntStateOf(0) }
    val show = controlsVisible || !state.playing

    LaunchedEffect(controlsVisible, state.playing, touchCount, state.dragging) {
        if (controlsVisible && state.playing && !state.dragging) {
            delay(3000)
            controlsVisible = false
        }
    }

    Box(modifier) {
        VideoSurface(state, path, Modifier.fillMaxSize())

        // Tap anywhere on the video to show / hide the controls.
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { controlsVisible = !controlsVisible }
        )

        if (show) {
            Box(Modifier.fillMaxSize().background(Color(0x55000000))) {
                // centre buttons
                Row(
                    Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(if (isFullscreen) 40.dp else 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SkipButton(
                        forward = false,
                        description = tr("১০ সেকেন্ড পেছনে", "Back 10 seconds")
                    ) { state.seekTo(state.position - 10_000); touchCount++ }

                    Box(
                        Modifier
                            .size(if (isFullscreen) 68.dp else 56.dp)
                            .clip(CircleShape)
                            .background(Color(0x99000000))
                            .semantics {
                                contentDescription = if (state.playing) "পজ" else "প্লে"
                            }
                            .clickable {
                                if (state.playing) state.pause() else state.play()
                                touchCount++
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (state.playing) PauseGlyph() else PlayGlyph()
                    }

                    SkipButton(
                        forward = true,
                        description = tr("১০ সেকেন্ড সামনে", "Forward 10 seconds")
                    ) { state.seekTo(state.position + 10_000); touchCount++ }
                }

                if (state.ended) {
                    Text(
                        tr("ভিডিও শেষ", "Video ended"),
                        color = Color.White,
                        fontSize = 14.sp,
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 12.dp)
                    )
                }

                // bottom bar: time + seek bar + full-screen
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 4.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatTime(state.position),
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                    val dur = state.duration
                    Slider(
                        value = if (dur > 0) (state.position.toFloat() / dur).coerceIn(0f, 1f) else 0f,
                        onValueChange = {
                            state.dragging = true
                            if (dur > 0) state.position = (it * dur).toInt()
                        },
                        onValueChangeFinished = {
                            state.seekTo(state.position)
                            state.dragging = false
                            touchCount++
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = PlayerGreen,
                            activeTrackColor = PlayerGreen,
                            inactiveTrackColor = Color(0x66FFFFFF)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp)
                            .semantics { contentDescription = "সিক বার" }
                    )
                    Text(
                        formatTime(dur),
                        color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium
                    )
                    Box(
                        Modifier
                            .size(44.dp)
                            .semantics {
                                contentDescription = if (isFullscreen) "ফুলস্ক্রিন থেকে বের হন" else "ফুলস্ক্রিন"
                            }
                            .clickable { onToggleFullscreen() },
                        contentAlignment = Alignment.Center
                    ) { FullscreenGlyph(exit = isFullscreen) }
                }
            }
        }
    }
}

@Composable
private fun VideoSurface(state: VideoState, path: String, modifier: Modifier) {
    val mine = remember(path) { arrayOfNulls<VideoView>(1) }
    DisposableEffect(path) {
        onDispose {
            val v = mine[0]
            if (v != null) {
                try {
                    if (!state.ended && v.currentPosition > 0) state.position = v.currentPosition
                    v.stopPlayback()
                } catch (_: Exception) {
                }
                if (state.view === v) state.view = null
            }
        }
    }
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val vv = VideoView(ctx)
            vv.setVideoPath(path)
            vv.setOnPreparedListener {
                android.util.Log.i("VideoPlayer", "prepared duration=${vv.duration}")
                state.duration = vv.duration
                state.ended = false
                if (state.position > 0) vv.seekTo(state.position)
                if (state.playing) vv.start()
            }
            vv.setOnCompletionListener {
                state.ended = true
                state.playing = false
                state.position = state.duration
            }
            vv.setOnErrorListener { _, what, extra ->
                android.util.Log.e("VideoPlayer", "error what=$what extra=$extra")
                state.failed = true; true
            }
            mine[0] = vv
            state.view = vv
            FrameLayout(ctx).apply {
                setBackgroundColor(AColor.BLACK)
                addView(
                    vv,
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER
                    )
                )
            }
        },
        update = { it.keepScreenOn = state.playing }
    )
}

// ---- small drawn icons (no extra icon library needed) ----

@Composable
private fun PlayGlyph() {
    Canvas(Modifier.size(26.dp)) {
        val path = Path().apply {
            moveTo(size.width * 0.22f, size.height * 0.10f)
            lineTo(size.width * 0.92f, size.height * 0.50f)
            lineTo(size.width * 0.22f, size.height * 0.90f)
            close()
        }
        drawPath(path, Color.White)
    }
}

@Composable
private fun PauseGlyph() {
    Canvas(Modifier.size(26.dp)) {
        val w = size.width * 0.26f
        drawRect(Color.White, Offset(size.width * 0.14f, size.height * 0.10f), Size(w, size.height * 0.80f))
        drawRect(Color.White, Offset(size.width * 0.60f, size.height * 0.10f), Size(w, size.height * 0.80f))
    }
}

@Composable
private fun FullscreenGlyph(exit: Boolean) {
    Canvas(Modifier.size(22.dp)) {
        val u = size.width / 24f
        val sw = 2.4f * u
        val a = 6f * u
        fun corner(cx: Float, cy: Float, dx: Float, dy: Float) {
            drawLine(Color.White, Offset(cx * u, cy * u), Offset(cx * u + dx * a, cy * u), sw, StrokeCap.Round)
            drawLine(Color.White, Offset(cx * u, cy * u), Offset(cx * u, cy * u + dy * a), sw, StrokeCap.Round)
        }
        if (!exit) {
            corner(4f, 4f, 1f, 1f); corner(20f, 4f, -1f, 1f)
            corner(4f, 20f, 1f, -1f); corner(20f, 20f, -1f, -1f)
        } else {
            corner(9f, 9f, -1f, -1f); corner(15f, 9f, 1f, -1f)
            corner(9f, 15f, -1f, 1f); corner(15f, 15f, 1f, 1f)
        }
    }
}

@Composable
private fun SkipButton(forward: Boolean, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(CircleShape)
            .semantics { contentDescription = description }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(40.dp)) {
            val sw = 2.6f * (size.width / 40f)
            val r = size.width * 0.40f
            val c = Offset(size.width / 2f, size.height / 2f)
            scale(if (forward) 1f else -1f, 1f, pivot = c) {
                // arc: clockwise from -50 deg, 290 deg long (gap at the top, arrow at its end)
                drawArc(
                    Color.White, startAngle = -50f, sweepAngle = 290f, useCenter = false,
                    topLeft = Offset(c.x - r, c.y - r), size = Size(2 * r, 2 * r),
                    style = Stroke(width = sw, cap = StrokeCap.Round)
                )
                val endRad = Math.toRadians(240.0)
                val ex = c.x + r * Math.cos(endRad).toFloat()
                val ey = c.y + r * Math.sin(endRad).toFloat()
                // clockwise tangent at the end point
                val tx = (-Math.sin(endRad)).toFloat()
                val ty = Math.cos(endRad).toFloat()
                val nx = -ty
                val ny = tx
                val h = size.width * 0.14f
                val head = Path().apply {
                    moveTo(ex + tx * h, ey + ty * h)
                    lineTo(ex - tx * h * 0.2f + nx * h, ey - ty * h * 0.2f + ny * h)
                    lineTo(ex - tx * h * 0.2f - nx * h, ey - ty * h * 0.2f - ny * h)
                    close()
                }
                drawPath(head, Color.White)
            }
        }
        Text("10", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}
