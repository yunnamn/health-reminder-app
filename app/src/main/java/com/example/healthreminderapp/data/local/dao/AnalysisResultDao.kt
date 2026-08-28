package com.example.healthreminderapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.healthreminderapp.data.local.entity.AnalysisResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisResultDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: AnalysisResultEntity): Long

    @Query(
        """
        SELECT * FROM analysis_results
        WHERE analysis_id = :analysisId
        ORDER BY result_date_millis DESC, id DESC
        """
    )
    fun observeResultsForAnalysis(analysisId: Long): Flow<List<AnalysisResultEntity>>
}