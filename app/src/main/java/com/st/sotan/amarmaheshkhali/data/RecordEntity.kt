package com.st.sotan.amarmaheshkhali.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One information record. Fields follow the "Data Collection Form" (Part 6).
 * verificationStatus: VERIFIED | PENDING | HISTORICAL | INFORMATION UNAVAILABLE
 */
@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey val recordId: String,
    val banglaName: String,
    val englishName: String,
    val category: String,
    val descriptionBn: String = "",
    val descriptionEn: String = "",
    val unionId: String = "",
    val ward: String = "",
    val village: String = "",
    val address: String = "",
    val phone: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val image: String = "",
    val officialSource: String = "",
    val verificationStatus: String = "PENDING",
    val lastUpdated: String = "",
    val isFavourite: Boolean = false,
    val lastViewed: Long = 0L
)
