package com.example.healthreminderapp.data.repository

import com.example.healthreminderapp.data.local.AppDatabase
import com.example.healthreminderapp.data.local.entity.AnalysisEntity
import com.example.healthreminderapp.data.local.entity.AnalysisResultEntity
import com.example.healthreminderapp.data.local.entity.DailyHealthRecordEntity
import com.example.healthreminderapp.data.local.entity.UserProfileEntity
import com.example.healthreminderapp.data.model.AnalysisCardData
import com.example.healthreminderapp.data.model.AnalysisDetailData
import com.example.healthreminderapp.data.model.AnalysisResultHistoryItem
import com.example.healthreminderapp.data.model.NewAnalysisData
import com.example.healthreminderapp.data.model.NewAnalysisResultData
import com.example.healthreminderapp.data.model.NewDailyHealthRecordData
import com.example.healthreminderapp.data.model.UpdateAnalysisData
import com.example.healthreminderapp.data.model.UserProfileData
import com.example.healthreminderapp.util.startOfDay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class HealthRepository(
    private val database: AppDatabase
) {
    private val userProfileDao = database.userProfileDao()
    private val dailyHealthRecordDao = database.dailyHealthRecordDao()
    private val analysisDao = database.analysisDao()
    private val analysisResultDao = database.analysisResultDao()

    fun observeUserProfile(): Flow<UserProfileEntity?> {
        return userProfileDao.observeProfile()
    }

    suspend fun saveUserProfile(profileData: UserProfileData) {
        userProfileDao.insertOrUpdateProfile(
            UserProfileEntity(
                id = 1,
                name = profileData.name?.trim()?.takeIf { it.isNotBlank() },
                sex = profileData.sex,
                birthDateMillis = profileData.birthDateMillis?.let(::startOfDay),
                heightCm = profileData.heightCm
            )
        )
    }

    fun observeDailyHealthRecords(): Flow<List<DailyHealthRecordEntity>> {
        return dailyHealthRecordDao.observeAllRecords()
    }

    suspend fun addDailyHealthRecord(data: NewDailyHealthRecordData) {
        dailyHealthRecordDao.insertRecord(
            DailyHealthRecordEntity(
                recordedAtMillis = data.recordedAtMillis,
                systolicPressure = data.systolicPressure,
                diastolicPressure = data.diastolicPressure,
                pulse = data.pulse,
                weightKg = data.weightKg,
                temperatureC = data.temperatureC,
                glucoseMmolL = data.glucoseMmolL
            )
        )
    }

    fun observeAnalysisCards(): Flow<List<AnalysisCardData>> {
        return analysisDao.observeActiveAnalysesWithLatestResult().map(::mapAnalysisCards)
    }

    fun observeArchivedAnalysisCards(): Flow<List<AnalysisCardData>> {
        return analysisDao.observeArchivedAnalysesWithLatestResult().map(::mapAnalysisCards)
    }

    fun observeAnalysisDetail(analysisId: Long): Flow<AnalysisDetailData?> {
        return combine(
            analysisDao.observeAnalysisById(analysisId),
            analysisResultDao.observeResultsForAnalysis(analysisId)
        ) { analysis, results ->
            analysis?.let {
                AnalysisDetailData(
                    id = it.id,
                    name = it.name,
                    defaultUnit = it.defaultUnit,
                    isArchived = it.isArchived,
                    results = results.map { result ->
                        AnalysisResultHistoryItem(
                            id = result.id,
                            valueText = "${formatDecimal(result.value)} ${result.unit}",
                            resultDateMillis = result.resultDateMillis,
                            note = result.note
                        )
                    }
                )
            }
        }
    }

    suspend fun addAnalysis(data: NewAnalysisData): Long {
        val normalizedName = data.name.trim()
        val normalizedUnit = data.defaultUnit.trim().ifBlank { "ед." }
        val existing = analysisDao.getAnalysisByName(normalizedName)

        if (existing != null) {
            throw IllegalArgumentException("Анализ с таким названием уже существует")
        }

        return analysisDao.insertAnalysis(
            AnalysisEntity(
                name = normalizedName,
                defaultUnit = normalizedUnit,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun updateAnalysis(data: UpdateAnalysisData) {
        val normalizedName = data.name.trim()
        val normalizedUnit = data.defaultUnit.trim().ifBlank { "ед." }

        val current = analysisDao.getAnalysisById(data.id)
            ?: throw IllegalArgumentException("Анализ не найден")

        val existingWithSameName = analysisDao.getAnalysisByName(normalizedName)
        if (existingWithSameName != null && existingWithSameName.id != data.id) {
            throw IllegalArgumentException("Анализ с таким названием уже существует")
        }

        analysisDao.updateAnalysis(
            current.copy(
                name = normalizedName,
                defaultUnit = normalizedUnit,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun addAnalysisResult(data: NewAnalysisResultData) {
        val analysis = analysisDao.getAnalysisById(data.analysisId)
            ?: throw IllegalArgumentException("Анализ не найден")

        analysisResultDao.insertResult(
            AnalysisResultEntity(
                analysisId = analysis.id,
                value = data.value,
                unit = analysis.defaultUnit,
                resultDateMillis = startOfDay(data.resultDateMillis),
                note = data.note?.trim()?.takeIf { it.isNotBlank() }
            )
        )
    }

    suspend fun archiveAnalysis(analysisId: Long) {
        analysisDao.setArchivedState(
            analysisId = analysisId,
            isArchived = true,
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    suspend fun restoreAnalysis(analysisId: Long) {
        analysisDao.setArchivedState(
            analysisId = analysisId,
            isArchived = false,
            updatedAtMillis = System.currentTimeMillis()
        )
    }

    private fun mapAnalysisCards(items: List<com.example.healthreminderapp.data.local.query.AnalysisListItem>): List<AnalysisCardData> {
        return items.map { item ->
            AnalysisCardData(
                id = item.id,
                name = item.name,
                latestValueText = item.latestValue?.let { value ->
                    val unit = item.latestUnit ?: item.defaultUnit
                    "${formatDecimal(value)} $unit"
                } ?: "Пока нет результатов",
                latestDateMillis = item.latestDateMillis
            )
        }
    }

    private fun formatDecimal(value: Double): String {
        return if (value % 1.0 == 0.0) {
            value.toInt().toString()
        } else {
            value.toString().replace('.', ',')
        }
    }
}
