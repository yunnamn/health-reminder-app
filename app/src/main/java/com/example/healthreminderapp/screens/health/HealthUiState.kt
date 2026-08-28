package com.example.healthreminderapp.screens.health

import com.example.healthreminderapp.data.local.entity.DailyHealthRecordEntity
import com.example.healthreminderapp.data.local.entity.ProfileSex
import com.example.healthreminderapp.data.model.AnalysisCardData
import com.example.healthreminderapp.data.model.AnalysisDetailData
import com.example.healthreminderapp.data.model.AnalysisSuggestion
import com.example.healthreminderapp.util.startOfDay

data class HealthUiState(
    val selectedSection: HealthSection = HealthSection.PROFILE,
    val isLoading: Boolean = true,

    val hasSavedProfile: Boolean = false,
    val isEditingProfile: Boolean = true,

    val nameInput: String = "",
    val sex: ProfileSex? = null,
    val birthDateMillis: Long? = null,
    val calculatedAge: Int? = null,
    val heightCmInput: String = "",

    val dailyHealthRecords: List<DailyHealthRecordEntity> = emptyList(),
    val isAddingHealthRecord: Boolean = false,
    val recordDateTimeMillis: Long = System.currentTimeMillis(),
    val systolicInput: String = "",
    val diastolicInput: String = "",
    val pulseInput: String = "",
    val weightInput: String = "",
    val temperatureInput: String = "",
    val glucoseInput: String = "",

    val analysisCards: List<AnalysisCardData> = emptyList(),
    val archivedAnalysisCards: List<AnalysisCardData> = emptyList(),
    val analysisScreenMode: AnalysisScreenMode = AnalysisScreenMode.LIST,
    val showingArchivedAnalyses: Boolean = false,
    val selectedAnalysisId: Long? = null,
    val selectedAnalysisDetail: AnalysisDetailData? = null,
    val isAnalysisDetailLoading: Boolean = false,

    val editingAnalysisId: Long? = null,
    val analysisNameInput: String = "",
    val analysisUnitInput: String = "ед.",
    val analysisSuggestions: List<AnalysisSuggestion> = emptyList(),

    val analysisResultValueInput: String = "",
    val analysisResultDateMillis: Long = startOfDay(System.currentTimeMillis()),
    val analysisResultNoteInput: String = "",

    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null
)
