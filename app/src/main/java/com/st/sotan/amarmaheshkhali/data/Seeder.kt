package com.st.sotan.amarmaheshkhali.data

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat
import androidx.room.withTransaction
import org.json.JSONArray
import org.json.JSONObject

/**
 * Loads the bundled records (assets/seed/records.json) into Room.
 *
 * It runs on first launch and again whenever the app itself is updated, so new bundled data
 * always appears. Favourites and "recently viewed" are kept. The whole refresh is one
 * transaction, so a crash half-way cannot leave the database partly updated.
 */
object Seeder {

    /** Shown in Settings only. */
    const val DATA_VERSION = "0.5.0"
    const val DATA_DATE = "2026-11-14"

    private const val PREF_KEY = "seed_app_version"

    suspend fun seedIfNeeded(context: Context, db: AppDatabase) {
        val dao = db.recordDao()
        val prefs = context.getSharedPreferences("settings", 0)
        val appVersion = PackageInfoCompat.getLongVersionCode(
            context.packageManager.getPackageInfo(context.packageName, 0)
        )
        if (dao.count() > 0 && prefs.getLong(PREF_KEY, -1L) == appVersion) return

        val text = context.assets.open("seed/records.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val arr = JSONArray(text)
        val list = ArrayList<RecordEntity>(arr.length())
        for (i in 0 until arr.length()) list.add(parse(arr.getJSONObject(i)))

        db.withTransaction {
            val favs = dao.favouriteIds().toHashSet()
            val viewed = dao.viewedRows().associate { it.recordId to it.lastViewed }
            val keep = list.mapTo(HashSet()) { it.recordId }
            dao.allIds().filter { it !in keep }.chunked(500).forEach { dao.deleteByIds(it) }
            dao.upsertAll(
                list.map {
                    it.copy(isFavourite = it.recordId in favs, lastViewed = viewed[it.recordId] ?: 0L)
                }
            )
        }
        prefs.edit().putLong(PREF_KEY, appVersion).apply()
    }

    private fun parse(o: JSONObject) = RecordEntity(
        recordId = o.getString("id"),
        banglaName = o.getString("bn"),
        englishName = o.getString("en"),
        category = o.getString("cat"),
        descriptionBn = o.optString("descBn", ""),
        descriptionEn = o.optString("descEn", ""),
        unionId = o.optString("union", ""),
        ward = o.optString("ward", ""),
        village = o.optString("village", ""),
        address = o.optString("address", ""),
        phone = o.optString("phone", ""),
        latitude = if (o.has("lat") && !o.isNull("lat")) o.getDouble("lat") else null,
        longitude = if (o.has("lng") && !o.isNull("lng")) o.getDouble("lng") else null,
        image = o.optString("image", ""),
        officialSource = o.optString("source", ""),
        verificationStatus = o.optString("status", "PENDING"),
        lastUpdated = o.optString("updated", "")
    )
}
