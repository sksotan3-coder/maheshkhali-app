package com.st.sotan.amarmaheshkhali.ui

import android.view.Gravity
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Plays a video bundled in assets/videos/. The platform VideoView cannot read assets directly,
 * so the file is copied once into the app cache (no permissions needed) and played from there.
 */
@Composable
fun VideoPlayer(asset: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var path by remember(asset) { mutableStateOf<String?>(null) }
    var failed by remember(asset) { mutableStateOf(false) }
    var position by rememberSaveable(asset) { mutableStateOf(0) }

    LaunchedEffect(asset) {
        withContext(Dispatchers.IO) {
            try {
                val out = File(context.cacheDir, "videos/$asset")
                if (!out.exists() || out.length() == 0L) {
                    out.parentFile?.mkdirs()
                    val tmp = File(out.path + ".tmp")
                    context.assets.open("videos/$asset").use { i -> tmp.outputStream().use { o -> i.copyTo(o) } }
                    tmp.renameTo(out)
                }
                path = out.path
            } catch (e: Exception) {
                android.util.Log.e("VideoPlayer", "Cannot prepare $asset", e)
                failed = true
            }
        }
    }

    Box(
        modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        val p = path
        when {
            failed -> Text(tr("ভিডিও চালানো যাচ্ছে না।", "Cannot play this video."), color = Color.White)
            p == null -> CircularProgressIndicator(color = Color.White)
            else -> {
                var view by remember { mutableStateOf<VideoView?>(null) }
                DisposableEffect(p) {
                    onDispose {
                        view?.let { v ->
                            position = v.currentPosition
                            v.stopPlayback()
                        }
                    }
                }
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        val vv = VideoView(ctx)
                        val mc = MediaController(ctx)
                        mc.setAnchorView(vv)
                        vv.setMediaController(mc)
                        vv.setVideoPath(p)
                        vv.setOnPreparedListener { if (position > 0) vv.seekTo(position) }
                        vv.setOnErrorListener { _, _, _ -> failed = true; true }
                        view = vv
                        FrameLayout(ctx).apply {
                            addView(
                                vv,
                                FrameLayout.LayoutParams(
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    FrameLayout.LayoutParams.MATCH_PARENT,
                                    Gravity.CENTER
                                )
                            )
                        }
                    }
                )
            }
        }
    }
}
