package com.st.sotan.amarmaheshkhali.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordDao {

    @Query(
        """
        SELECT * FROM records
        WHERE (:q = '' OR banglaName LIKE '%' || :q || '%' ESCAPE '\'
                       OR englishName LIKE '%' || :q || '%' ESCAPE '\'
                       OR descriptionBn LIKE '%' || :q || '%' ESCAPE '\'
                       OR descriptionEn LIKE '%' || :q || '%' ESCAPE '\'
                       OR village LIKE '%' || :q || '%' ESCAPE '\')
          AND (:category = '' OR category = :category)
          AND (:unionId = '' OR unionId = :unionId)
        ORDER BY CASE WHEN recordId LIKE 'edu-college-%' THEN 0
                      WHEN recordId LIKE 'edu-madrasa-%' THEN 2
                      ELSE 1 END,
                 CASE WHEN recordId LIKE '%-list' THEN 0 ELSE 1 END,
                 CASE WHEN recordId LIKE 'photo-%' THEN recordId ELSE banglaName END
        """
    )
    fun search(q: String, category: String, unionId: String): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE recordId = :id")
    fun byId(id: String): Flow<RecordEntity?>

    @Query("SELECT * FROM records WHERE isFavourite = 1 ORDER BY banglaName")
    fun favourites(): Flow<List<RecordEntity>>

    @Query("SELECT * FROM records WHERE lastViewed > 0 ORDER BY lastViewed DESC LIMIT 20")
    fun recent(): Flow<List<RecordEntity>>

    @Query("SELECT category, COUNT(*) AS total FROM records GROUP BY category")
    fun categoryCounts(): Flow<List<CategoryCount>>

    @Query("SELECT COUNT(*) FROM records")
    suspend fun count(): Int

    @Query("SELECT recordId FROM records WHERE isFavourite = 1")
    suspend fun favouriteIds(): List<String>

    @Query("SELECT recordId FROM records")
    suspend fun allIds(): List<String>

    @Query("SELECT recordId, lastViewed FROM records WHERE lastViewed > 0")
    suspend fun viewedRows(): List<ViewedRow>

    @Query("DELETE FROM records WHERE recordId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<RecordEntity>)

    @Query("UPDATE records SET isFavourite = :value WHERE recordId = :id")
    suspend fun setFavourite(id: String, value: Boolean)

    @Query("UPDATE records SET lastViewed = :time WHERE recordId = :id")
    suspend fun touch(id: String, time: Long)
}

data class CategoryCount(val category: String, val total: Int)

data class ViewedRow(val recordId: String, val lastViewed: Long)
