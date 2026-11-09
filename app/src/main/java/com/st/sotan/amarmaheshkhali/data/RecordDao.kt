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
        WHERE (:q = '' OR banglaName LIKE '%' || :q || '%'
                       OR englishName LIKE '%' || :q || '%'
                       OR descriptionBn LIKE '%' || :q || '%'
                       OR descriptionEn LIKE '%' || :q || '%'
                       OR village LIKE '%' || :q || '%')
          AND (:category = '' OR category = :category)
          AND (:unionId = '' OR unionId = :unionId)
        ORDER BY banglaName
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

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<RecordEntity>)

    @Query("SELECT recordId FROM records WHERE isFavourite = 1")
    suspend fun favouriteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<RecordEntity>)

    @Query("UPDATE records SET isFavourite = :value WHERE recordId = :id")
    suspend fun setFavourite(id: String, value: Boolean)

    @Query("UPDATE records SET lastViewed = :time WHERE recordId = :id")
    suspend fun touch(id: String, time: Long)
}

data class CategoryCount(val category: String, val total: Int)
