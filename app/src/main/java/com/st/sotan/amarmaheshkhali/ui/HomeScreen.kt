package com.st.sotan.amarmaheshkhali.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.st.sotan.amarmaheshkhali.AppViewModel
import com.st.sotan.amarmaheshkhali.data.CATEGORIES
import com.st.sotan.amarmaheshkhali.data.CategoryInfo
import com.st.sotan.amarmaheshkhali.data.UNIONS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: AppViewModel,
    onSearch: () -> Unit,
    onCategory: (String) -> Unit,
    onUnion: (String) -> Unit,
    onAbout: () -> Unit
) {
    val lang by vm.lang.collectAsState()
    val counts by vm.counts.collectAsState()
    val countMap = remember(counts) { counts.associate { it.category to it.total } }

    Column {
        AppTopBar("AMAR MOHESHKHALI A TO Z")
        LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp)) {

            // Cover / intro
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text(
                                tr("আমার মহেশখালী A to Z", "Amar Maheshkhali A to Z"),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                tr(
                                    "মহেশখালী বাংলাদেশের কক্সবাজার জেলার একটি উপজেলা। এই অ্যাপটি সম্পূর্ণ অফলাইন তথ্যভান্ডার।",
                                    "Maheshkhali is an upazila of Cox's Bazar District, Bangladesh. This app is a fully offline information encyclopedia."
                                ),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Search bar (opens search screen)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable(onClick = onSearch),
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Text(
                            tr("খুঁজুন…", "Search…"),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Unions
            item { SectionTitle(tr("ইউনিয়ন", "Unions")) }
            item {
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UNIONS) { u ->
                        AssistChip(
                            onClick = { onUnion(u.id) },
                            label = { Text(if (lang == "bn") u.bn else u.en) }
                        )
                    }
                }
            }

            // Quick access: emergency, government, tourism
            item { SectionTitle(tr("দ্রুত প্রবেশ", "Quick Access")) }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickCard(tr("জরুরি যোগাযোগ", "Emergency"), Modifier.weight(1f)) { onCategory("emergency") }
                    QuickCard(tr("সরকারি সেবা", "Government"), Modifier.weight(1f)) { onCategory("government_offices") }
                    QuickCard(tr("পর্যটন", "Tourism"), Modifier.weight(1f)) { onCategory("tourism") }
                }
            }

            // All categories
            item { SectionTitle(tr("সব বিভাগ", "All Categories")) }
            items(CATEGORIES.chunked(2)) { pair ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { c ->
                        CategoryCard(c, lang, countMap[c.id] ?: 0, Modifier.weight(1f)) { onCategory(c.id) }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }

            // About
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable(onClick = onAbout),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.padding(horizontal = 6.dp))
                        Text(tr("অ্যাপ সম্পর্কে • Developed By: ST SOTAN", "About the app • Developed By: ST SOTAN"))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun QuickCard(title: String, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
            Text(
                title,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun CategoryCard(c: CategoryInfo, lang: String, count: Int, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp)) {
            Text(
                if (lang == "bn") c.bn else c.en,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                minLines = 2,
                maxLines = 2
            )
            Spacer(Modifier.height(6.dp))
            Text(
                tr("${localNumber(count, "bn")} টি রেকর্ড", "$count records"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
