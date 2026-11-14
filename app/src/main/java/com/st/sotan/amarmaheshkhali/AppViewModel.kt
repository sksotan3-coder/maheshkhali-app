package com.st.sotan.amarmaheshkhali

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.st.sotan.amarmaheshkhali.data.AppDatabase
import com.st.sotan.amarmaheshkhali.data.CategoryCount
import com.st.sotan.amarmaheshkhali.data.RecordEntity
import com.st.sotan.amarmaheshkhali.data.Seeder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.st.sotan.amarmaheshkhali.data.FavouritesBackup

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(private val app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.get(app)
    private val dao = db.recordDao()
    private val prefs = app.getSharedPreferences("settings", 0)

    // ---- Settings (saved on the phone only) ----
    val lang = MutableStateFlow(prefs.getString("lang", "bn") ?: "bn")
    val theme = MutableStateFlow(prefs.getString("theme", "system") ?: "system")

    fun setLang(value: String) {
        lang.value = value
        prefs.edit().putString("lang", value).apply()
    }

    fun setTheme(value: String) {
        theme.value = value
        prefs.edit().putString("theme", value).apply()
    }

    // ---- Search / filters ----
    val query = MutableStateFlow("")
    val category = MutableStateFlow("")
    val union = MutableStateFlow("")

    val results: StateFlow<List<RecordEntity>> =
        combine(query, category, union) { q, c, u -> Triple(q.trim(), c, u) }
            .flatMapLatest { (q, c, u) -> dao.search(escapeLike(q), escapeLike(otherDigits(q)), c, u) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favourites: StateFlow<List<RecordEntity>> =
        dao.favourites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recent: StateFlow<List<RecordEntity>> =
        dao.recent().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val counts: StateFlow<List<CategoryCount>> =
        dao.categoryCounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun record(id: String): Flow<RecordEntity?> = dao.byId(id)

    /** Records of one category and/or union (used by the list screen). */
    fun listResults(category: String, union: String): Flow<List<RecordEntity>> =
        dao.search("", "", category, union)

    fun toggleFavourite(r: RecordEntity) {
        viewModelScope.launch(Dispatchers.IO) { dao.setFavourite(r.recordId, !r.isFavourite) }
    }

    fun markViewed(id: String) {
        viewModelScope.launch(Dispatchers.IO) { dao.touch(id, System.currentTimeMillis()) }
    }

    /** True only if the bundled data could not be loaded (shown to the user on the home screen). */
    val seedFailed = MutableStateFlow(false)

    /** Text the user can save/share to keep a backup of the favourite list. Empty if there are none. */
    suspend fun favouritesBackupText(): String = withContext(Dispatchers.IO) {
        FavouritesBackup.build(dao.favouritesNow())
    }

    /** Restore favourites from a backup text. Returns how many were restored (0 if the text is not a backup). */
    suspend fun restoreFavourites(text: String): Int = withContext(Dispatchers.IO) {
        val ids = FavouritesBackup.parseIds(text)
        var n = 0
        ids.chunked(500).forEach { n += dao.markFavourites(it) }
        n
    }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Seeder.seedIfNeeded(app, db)
            } catch (e: Exception) {
                seedFailed.value = true
                // Never crash the app because of a data problem; keep whatever data is already stored.
                Log.e("Seeder", "Could not load bundled data", e)
            }
        }
    }
}

/** Make a typed search text safe for SQL LIKE (so "%" and "_" are plain characters). */
/** Same text with its digits written in the other script (1090 <-> ১০৯০), so both spellings find the same records. */
private fun otherDigits(q: String): String {
    val hasAscii = q.any { it in '0'..'9' }
    val hasBangla = q.any { it in '\u09E6'..'\u09EF' }
    return q.map {
        when {
            hasAscii && it in '0'..'9' -> '\u09E6' + (it - '0')
            hasBangla && it in '\u09E6'..'\u09EF' -> '0' + (it - '\u09E6')
            else -> it
        }
    }.joinToString("")
}

private fun escapeLike(q: String): String =
    q.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")
