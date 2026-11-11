package com.st.sotan.amarmaheshkhali.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.onEach
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.st.sotan.amarmaheshkhali.AppViewModel
import com.st.sotan.amarmaheshkhali.data.CATEGORIES
import com.st.sotan.amarmaheshkhali.data.Seeder
import com.st.sotan.amarmaheshkhali.data.UNIONS
import com.st.sotan.amarmaheshkhali.data.categoryName
import com.st.sotan.amarmaheshkhali.data.unionName

// ---------------------------------------------------------------- List
@Composable
fun ListScreen(
    vm: AppViewModel,
    cat: String,
    unionId: String,
    onOpen: (String) -> Unit,
    onBack: () -> Unit
) {
    val lang by vm.lang.collectAsState()
    val items by remember(cat, unionId) { vm.listResults(cat, unionId) }.collectAsState(initial = null)

    val title = when {
        cat.isNotEmpty() -> categoryName(cat, lang)
        unionId.isNotEmpty() -> unionName(unionId, lang)
        else -> tr("সব তথ্য", "All Records")
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(title, onBack)
        val list = items
        if (list == null) {
            // still loading
        } else if (list.isEmpty()) {
            EmptyState(
                tr(
                    "এই বিভাগে এখনও কোনো তথ্য যুক্ত করা হয়নি।",
                    "No information has been added to this section yet."
                )
            )
        } else {
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)) {
                items(list, key = { it.recordId }) { r ->
                    RecordCard(r, lang) { onOpen(r.recordId) }
                }
            }
        }
    }
}

// -------------------------------------------------------------- Detail
@Composable
fun DetailScreen(vm: AppViewModel, id: String, onBack: () -> Unit) {
    val lang by vm.lang.collectAsState()
    var loaded by remember { mutableStateOf(false) }
    val record by remember(id) { vm.record(id).onEach { loaded = true } }.collectAsState(initial = null)
    val context = LocalContext.current

    LaunchedEffect(id) { vm.markViewed(id) }

    Column(Modifier.fillMaxSize()) {
        val r = record
        AppTopBar(
            if (r == null) "" else if (lang == "bn") r.banglaName else r.englishName,
            onBack
        )
        if (r == null) {
            if (loaded) EmptyState(tr("তথ্যটি পাওয়া যায়নি।", "This record was not found."))
        } else {
            Column(
                Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { vm.toggleFavourite(r) }) {
                        Icon(
                            if (r.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = tr("প্রিয়", "Favourite"),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                val desc = if (lang == "bn") r.descriptionBn else r.descriptionEn
                if (desc.isNotBlank()) {
                    FormattedDescription(desc)
                }
                Spacer(Modifier.height(16.dp))

                InfoRow(tr("ইউনিয়ন", "Union"), unionName(r.unionId, lang))
                InfoRow(tr("ওয়ার্ড", "Ward"), r.ward)
                InfoRow(tr("গ্রাম", "Village"), r.village)
                InfoRow(tr("ঠিকানা", "Address"), r.address)
                if (r.latitude != null && r.longitude != null) {
                    InfoRow(tr("অবস্থান", "Location"), "${r.latitude}, ${r.longitude}")
                }

                val allContacts = parseContacts(r.phone)
                allContacts.filter { it.type == "video" }.forEach { c ->
                    Spacer(Modifier.height(8.dp))
                    VideoPlayer(c.value)
                }
                val contacts = allContacts.filter { it.type != "video" }
                if (contacts.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    SectionLabel(tr("যোগাযোগ", "Contact"))
                    contacts.forEach { c ->
                        ContactButton(c, lang) { openContact(context, c) }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------- Search
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(vm: AppViewModel, onOpen: (String) -> Unit) {
    val lang by vm.lang.collectAsState()
    val q by vm.query.collectAsState()
    val cat by vm.category.collectAsState()
    val un by vm.union.collectAsState()
    val items by vm.results.collectAsState()
    val recent by vm.recent.collectAsState()

    LaunchedEffect(Unit) {
        vm.category.value = ""
        vm.union.value = ""
    }

    Column(Modifier.fillMaxSize()) {
        AppTopBar(tr("অফলাইন সার্চ", "Offline Search"))
        OutlinedTextField(
            value = q,
            onValueChange = { vm.query.value = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (q.isNotEmpty()) {
                    IconButton(onClick = { vm.query.value = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = tr("মুছুন", "Clear"))
                    }
                }
            },
            placeholder = { Text(tr("নাম বা বিবরণ লিখুন", "Type a name or description")) },
            shape = RoundedCornerShape(50)
        )

        Text(
            tr("বিভাগ", "Category"),
            modifier = Modifier.padding(horizontal = 16.dp),
            style = MaterialTheme.typography.labelLarge
        )
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CATEGORIES) { c ->
                FilterChip(
                    selected = cat == c.id,
                    onClick = { vm.category.value = if (cat == c.id) "" else c.id },
                    label = { Text(if (lang == "bn") c.bn else c.en) }
                )
            }
        }
        Text(
            tr("ইউনিয়ন", "Union"),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelLarge
        )
        LazyRow(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(UNIONS) { u ->
                FilterChip(
                    selected = un == u.id,
                    onClick = { vm.union.value = if (un == u.id) "" else u.id },
                    label = { Text(if (lang == "bn") u.bn else u.en) }
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        if (items.isEmpty()) {
            EmptyState(tr("কিছু পাওয়া যায়নি।", "No results found."))
        } else {
            LazyColumn {
                if (q.isEmpty() && cat.isEmpty() && un.isEmpty() && recent.isNotEmpty()) {
                    item {
                        Text(
                            tr("সম্প্রতি দেখা", "Recently viewed"),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(recent, key = { "recent-" + it.recordId }) { r ->
                        RecordCard(r, lang) { onOpen(r.recordId) }
                    }
                    item {
                        Text(
                            tr("সব তথ্য", "All records"),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                items(items, key = { it.recordId }) { r ->
                    RecordCard(r, lang) { onOpen(r.recordId) }
                }
            }
        }
    }
}

// ---------------------------------------------------------- Favourites
@Composable
fun FavouritesScreen(vm: AppViewModel, onOpen: (String) -> Unit) {
    val lang by vm.lang.collectAsState()
    val favs by vm.favourites.collectAsState()
    Column(Modifier.fillMaxSize()) {
        AppTopBar(tr("প্রিয় তথ্য", "Favourite Information"))
        if (favs.isEmpty()) {
            EmptyState(
                tr(
                    "এখনও কোনো প্রিয় তথ্য নেই। কোনো তথ্যের পাতায় হার্ট আইকনে চাপ দিন।",
                    "No favourites yet. Tap the heart icon on a record."
                )
            )
        } else {
            LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)) {
                items(favs, key = { it.recordId }) { r -> RecordCard(r, lang) { onOpen(r.recordId) } }
            }
        }
    }
}

// ------------------------------------------------------------ Settings
@Composable
fun SettingsScreen(vm: AppViewModel, onAbout: () -> Unit) {
    val lang by vm.lang.collectAsState()
    val theme by vm.theme.collectAsState()

    Column(Modifier.fillMaxSize()) {
        AppTopBar(tr("সেটিংস", "Settings"))
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingTitle(tr("ভাষা", "Language"))
            RadioRow("বাংলা", lang == "bn") { vm.setLang("bn") }
            RadioRow("English", lang == "en") { vm.setLang("en") }

            Spacer(Modifier.height(16.dp))
            SettingTitle(tr("থিম", "Theme"))
            RadioRow(tr("সিস্টেম অনুযায়ী", "Follow system"), theme == "system") { vm.setTheme("system") }
            RadioRow(tr("লাইট", "Light"), theme == "light") { vm.setTheme("light") }
            RadioRow(tr("ডার্ক", "Dark"), theme == "dark") { vm.setTheme("dark") }

            Spacer(Modifier.height(16.dp))
            SettingTitle(tr("অ্যাপ তথ্য", "App Information"))
            InfoRow(tr("অ্যাপ", "App"), "AMAR MOHESHKHALI A TO Z")
            InfoRow(tr("ডেভেলপার", "Developer"), "ST SOTAN")
            InfoRow(tr("ডেটা সংস্করণ", "Data Version"), Seeder.DATA_VERSION)
            InfoRow(tr("সর্বশেষ আপডেট", "Last Updated"), Seeder.DATA_DATE)
            InfoRow(
                tr("অফলাইন মোড", "Offline Mode"),
                tr(
                    "অ্যাপের সব তথ্য ফোনেই সংরক্ষিত। ইন্টারনেট লাগে না।",
                    "All data is stored on your phone. No internet is needed."
                )
            )
            InfoRow(
                tr("গোপনীয়তা", "Privacy"),
                tr(
                    "কোনো লগইন নেই, কোনো ব্যক্তিগত তথ্য সংগ্রহ বা পাঠানো হয় না। অ্যাপে ইন্টারনেটের অনুমতিই নেই।",
                    "No login. No personal data is collected or sent. The app has no internet permission."
                )
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onAbout) { Text(tr("অ্যাপ সম্পর্কে", "About the app")) }
        }
    }
}

@Composable
private fun SettingTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun RadioRow(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect, role = Role.RadioButton)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.padding(horizontal = 6.dp))
        Text(label)
    }
}

// --------------------------------------------------------------- About
@Composable
fun AboutScreen(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        AppTopBar(tr("অ্যাপ সম্পর্কে", "About"), onBack)
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "AMAR MOHESHKHALI A TO Z",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Developed By: ST SOTAN",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                tr(
                    "ST SOTAN একজন প্রযুক্তিপ্রেমী ও উদ্ভাবনী চিন্তাধারার মানুষ, যিনি প্রযুক্তির মাধ্যমে মানুষের দৈনন্দিন জীবনকে আরও সহজ, সুন্দর ও তথ্যসমৃদ্ধ করার লক্ষ্যে কাজ করছেন।\n\n" +
                        "AMAR MOHESHKHALI A TO Z তাঁর একটি উদ্যোগ। এই অ্যাপের মূল উদ্দেশ্য হলো মহেশখালীর মানুষের কাছে প্রয়োজনীয় স্থানীয় তথ্য, গুরুত্বপূর্ণ সেবা ও বিভিন্ন তথ্যভান্ডার সহজেই পৌঁছে দেওয়া।\n\n" +
                        "প্রযুক্তির সঠিক ব্যবহার, নির্ভরযোগ্য তথ্য এবং মানুষের উপকারকে গুরুত্ব দিয়েই এই উদ্যোগের যাত্রা।",
                    "ST SOTAN is a technology enthusiast with an innovative mindset, working to make people's daily lives easier, better and more informed through technology.\n\n" +
                        "AMAR MOHESHKHALI A TO Z is one of his initiatives. The main purpose of this app is to bring essential local information, important services and various information resources to the people of Maheshkhali.\n\n" +
                        "This initiative began with a focus on the right use of technology, reliable information and benefit to people."
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                tr("আমাদের লক্ষ্য (Our Vision)", "Our Vision"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                tr(
                    "প্রযুক্তির মাধ্যমে মহেশখালীকে আরও তথ্যসমৃদ্ধ, আধুনিক ও ডিজিটালভাবে সংযুক্ত করা।",
                    "To make Maheshkhali more informed, modern and digitally connected through technology."
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(16.dp))
            Text(
                tr(
                    "এটি কক্সবাজার জেলার মহেশখালী উপজেলার একটি অফলাইন ডিজিটাল তথ্যভান্ডার।\n\n" +
                        "তথ্যের নীতি: কোনো তথ্য বানিয়ে লেখা হয় না। \n\n" +
                        "রাজনৈতিক তথ্য নিরপেক্ষভাবে উপস্থাপন করা হবে; কোনো দল, প্রার্থী বা ব্যক্তির প্রচার করা হয় না।\n\n" +
                        "তথ্য হালনাগাদ করতে ভবিষ্যতে নতুন ডেটা প্যাকেজ বা অ্যাপ সংস্করণ ব্যবহার করা হবে।",
                    "This is an offline digital information encyclopedia for Maheshkhali Upazila, Cox's Bazar.\n\n" +
                        "Data policy: nothing is invented.\n\n" +
                        "Political information is presented neutrally; no party, candidate or person is promoted.\n\n" +
                        "Updates will arrive as new data packages or app versions."
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "© 2026 ST SOTAN. All Rights Reserved.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

// ------------------------------------------------------------ Contacts
/** Contact line format stored in RecordEntity.phone: "call|label|number", "mail|label|email", "web|label|url". */
data class Contact(val type: String, val label: String, val value: String)

fun parseContacts(raw: String): List<Contact> =
    raw.lines().mapNotNull { line ->
        val p = line.split("|", limit = 3)
        if (p.size == 3 && p[2].isNotBlank()) Contact(p[0].trim(), p[1].trim(), p[2].trim()) else null
    }

private fun toBanglaDigits(s: String): String =
    s.map { if (it in '0'..'9') ('\u09E6' + (it - '0')) else it }.joinToString("")

private fun openContact(context: android.content.Context, c: Contact) {
    val uri = when (c.type) {
        "call" -> Uri.parse("tel:" + c.value.filter { it.isDigit() || it == '+' })
        "mail" -> Uri.parse("mailto:" + c.value)
        else -> Uri.parse(c.value)
    }
    val action = if (c.type == "call") Intent.ACTION_DIAL else Intent.ACTION_VIEW
    try {
        context.startActivity(Intent(action, uri))
    } catch (e: Exception) {
        // No app can handle it; do nothing.
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(Modifier.height(6.dp))
}

@Composable
private fun ContactButton(c: Contact, lang: String, onClick: () -> Unit) {
    val icon = when (c.type) {
        "call" -> Icons.Default.Call
        "mail" -> Icons.Default.Email
        else -> if (c.value.contains("maps")) Icons.Default.Place else Icons.Default.Info
    }
    val shown = if (c.type == "call" || c.type == "mail") {
        c.label + ": " + (if (lang == "bn" && c.type == "call") toBanglaDigits(c.value) else c.value)
    } else c.label
    val content: @Composable () -> Unit = {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.padding(horizontal = 4.dp))
        Text(shown)
    }
    if (c.type == "call") {
        Button(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) { content() }
    } else {
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) { content() }
    }
}

/** Plain text with two light markers: "## heading" lines and "* bullet" lines. Everything else is a normal paragraph. */
@Composable
private fun FormattedDescription(text: String) {
    if (!text.contains("## ") && !text.contains("\n* ") && !text.startsWith("* ")) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        return
    }
    text.split("\n").forEach { line ->
        when {
            line.isBlank() -> Spacer(Modifier.height(4.dp))
            line.startsWith("## ") -> {
                Spacer(Modifier.height(10.dp))
                Text(
                    line.removePrefix("## "),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
            }
            line.startsWith("* ") -> {
                val body = line.removePrefix("* ")
                val i = body.indexOf(": ")
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text("•  ", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)
                    Text(
                        if (i in 1..40) buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(body.substring(0, i + 1)) }
                            append(body.substring(i + 1))
                        } else buildAnnotatedString { append(body) },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> Text(line, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
