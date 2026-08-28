package com.example.healthreminderapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.healthreminderapp.data.local.entity.AnalysisEntity
import com.example.healthreminderapp.data.local.query.AnalysisListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisDao {

    @Query(
        """
        SELECT
            a.id AS id,
            a.name AS name,
            a.default_unit AS default_unit,
            (
                SELECT r.value
                FROM analysis_results r
                WHERE r.analysis_id = a.id
                ORDER BY r.result_date_millis DESC, r.id DESC
                LIMIT 1
            ) AS latest_value,
            (
                SELECT r.unit
                FROM analysis_results r
                WHERE r.analysis_id = a.id
                ORDER BY r.result_date_millis DESC, r.id DESC
                LIMIT 1
            ) AS latest_unit,
            (
                SELECT r.result_date_millis
                FROM analysis_results r
                WHERE r.analysis_id = a.id
                ORDER BY r.result_date_millis DESC, r.id DESC
                LIMIT 1
            ) AS latest_date_millis
        FROM analyses a
        WHERE a.is_archived = 0
        ORDER BY a.name ASC
        """
    )
    fun observeActiveAnalysesWithLatestResult(): Flow<List<AnalysisListItem>>

    @Query(
        """
        SELECT
            a.id AS id,
            a.name AS name,
            a.default_unit AS default_unit,
            (
                SELECT r.value
                FROM analysis_results r
                WHERE r.analysis_id = a.id
                ORDER BY r.result_date_millis DESC, r.id DESC
                LIMIT 1
            ) AS latest_value,
            (
                SELECT r.unit
                FROM analysis_results r
                WHERE r.analysis_id = a.id
                ORDER BY r.result_date_millis DESC, r.id DESC
                LIMIT 1
            ) AS latest_unit,
            (
                SELECT r.result_date_millis
                FROM analysis_results r
                WHERE r.analysis_id = a.id
                ORDER BY r.result_date_millis DESC, r.id DESC
                LIMIT 1
            ) AS latest_date_millis
        FROM analyses a
        WHERE a.is_archived = 1
        ORDER BY a.updated_at_millis DESC, a.name ASC
        """
    )
    fun observeArchivedAnalysesWithLatestResult(): Flow<List<AnalysisListItem>>

    @Query("SELECT * FROM analyses WHERE id = :analysisId LIMIT 1")
    fun observeAnalysisById(analysisId: Long): Flow<AnalysisEntity?>

    @Query("SELECT * FROM analyses WHERE id = :analysisId LIMIT 1")
    suspend fun getAnalysisById(analysisId: Long): AnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAnalysis(analysis: AnalysisEntity): Long

    @Update
    suspend fun updateAnalysis(analysis: AnalysisEntity)

    @Query("SELECT * FROM analyses WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getAnalysisByName(name: String): AnalysisEntity?

    @Query(
        """
        UPDATE analyses
        SET is_archived = :isArchived,
            updated_at_millis = :updatedAtMillis
        WHERE id = :analysisId
        """
    )
    suspend fun setArchivedState(
        analysisId: Long,
        isArchived: Boolean,
        updatedAtMillis: Long
    )
}
