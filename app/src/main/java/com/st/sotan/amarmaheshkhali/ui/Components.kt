package com.st.sotan.amarmaheshkhali.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.st.sotan.amarmaheshkhali.data.RecordEntity
import com.st.sotan.amarmaheshkhali.data.categoryName
import com.st.sotan.amarmaheshkhali.data.unionName
import android.graphics.BitmapFactory
import java.util.Locale

fun localNumber(n: Int, lang: String): String =
    if (lang == "bn") String.format(Locale.forLanguageTag("bn-BD"), "%d", n) else n.toString()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr("পেছনে", "Back"))
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun StatusBadge(status: String) {
    val (bg, label) = when (status) {
        "VERIFIED" -> Color(0xFF1B7F4B) to tr("যাচাইকৃত", "VERIFIED")
        "HISTORICAL" -> Color(0xFF546E7A) to tr("ঐতিহাসিক", "HISTORICAL")
        "INFORMATION UNAVAILABLE" -> Color(0xFF757575) to tr("তথ্য পাওয়া যায়নি", "INFORMATION UNAVAILABLE")
        else -> Color(0xFFB26A00) to tr("যাচাই বাকি", "PENDING")
    }
    Surface(color = bg, shape = RoundedCornerShape(50)) {
        Text(
            label,
            color = Color.White,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun RecordCard(r: RecordEntity, lang: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column {
            PhotoThumb(r)
            Column(Modifier.padding(16.dp)) {
            Text(
                if (lang == "bn") r.banglaName else r.englishName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (lang == "bn") r.englishName else r.banglaName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    categoryName(r.category, lang),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (r.unionId.isNotEmpty()) {
                    Text("•", color = MaterialTheme.colorScheme.primary)
                    Text(unionName(r.unionId, lang), style = MaterialTheme.typography.labelMedium)
                }
            }
            }
        }
    }
}

/** Photo shown on top of a gallery card (only for records whose image lives in assets/photos/). */
@Composable
private fun PhotoThumb(r: RecordEntity) {
    val path = r.phone.lineSequence()
        .map { it.split("|") }
        .firstOrNull { it.size == 3 && it[0] == "image" && it[2].startsWith("photos/") }
        ?.get(2) ?: return
    val context = LocalContext.current
    val bmp = remember(path) {
        try {
            val opts = BitmapFactory.Options().apply { inSampleSize = 1 }
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, opts) }?.asImageBitmap()
        } catch (e: Exception) {
            android.util.Log.e("PhotoThumb", "Cannot load $path", e)
            null
        }
    } ?: return
    Image(
        bitmap = bmp,
        contentDescription = r.banglaName,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    )
}

@Composable
fun EmptyState(message: String) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    if (value.isBlank()) return
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(
            label,
            modifier = Modifier.width(120.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}
