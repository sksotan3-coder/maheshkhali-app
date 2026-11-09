package com.st.sotan.amarmaheshkhali.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Loads the bundled records (assets/seed/records.json) into Room on first launch. */
object Seeder {

    const val DATA_VERSION = "0.1.0-seed"
    const val DATA_DATE = "2026-11-09"

    suspend fun seedIfEmpty(context: Context, dao: RecordDao) {
        if (dao.count() > 0) return
        val text = context.assets.open("seed/records.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val arr = JSONArray(text)
        val list = ArrayList<RecordEntity>(arr.length())
        for (i in 0 until arr.length()) {
            list.add(parse(arr.getJSONObject(i)))
        }
        dao.insertAll(list)
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
        lastUpdated = o.optString("updated", DATA_DATE)
    )
}
