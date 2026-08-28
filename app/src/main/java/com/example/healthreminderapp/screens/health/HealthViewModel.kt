package com.example.healthreminderapp.screens.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthreminderapp.data.local.entity.ProfileSex
import com.example.healthreminderapp.data.model.AnalysisSuggestion
import com.example.healthreminderapp.data.model.NewAnalysisData
import com.example.healthreminderapp.data.model.NewAnalysisResultData
import com.example.healthreminderapp.data.model.NewDailyHealthRecordData
import com.example.healthreminderapp.data.model.UpdateAnalysisData
import com.example.healthreminderapp.data.model.UserProfileData
import com.example.healthreminderapp.data.repository.HealthRepository
import com.example.healthreminderapp.util.startOfDay
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HealthViewModel(
    private val repository: HealthRepository
) : ViewModel() {

    private val commonAnalysisSuggestions = listOf(
        AnalysisSuggestion(name = "Ферритин", defaultUnit = "нг/мл"),
        AnalysisSuggestion(name = "Гемоглобин", defaultUnit = "г/л"),
        AnalysisSuggestion(name = "Витамин D", defaultUnit = "нг/мл"),
        AnalysisSuggestion(name = "Холестерин", defaultUnit = "ммоль/л")
    )

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    private var selectedAnalysisJob: Job? = null

    init {
        viewModelScope.launch {
            repository.observeUserProfile().collect { profile ->
                _uiState.update { current ->
                    val hasProfile = profile != null

                    current.copy(
                        isLoading = false,
                        hasSavedProfile = hasProfile,
                        isEditingProfile = when {
                            !hasProfile -> true
                            !current.hasSavedProfile -> false
                            else -> current.isEditingProfile
                        },
                        nameInput = profile?.name.orEmpty(),
                        sex = profile?.sex,
                        birthDateMillis = profile?.birthDateMillis,
                        calculatedAge = calculateAge(profile?.birthDateMillis),
                        heightCmInput = profile?.heightCm?.toString().orEmpty()
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.observeDailyHealthRecords().collect { records ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        dailyHealthRecords = records
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.observeAnalysisCards().collect { cards ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        analysisCards = cards
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.observeArchivedAnalysisCards().collect { cards ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        archivedAnalysisCards = cards
                    )
                }
            }
        }
    }
    fun handleEntry(
        section: HealthSection,
        action: HealthEntryAction
    ) {
        when (action) {
            HealthEntryAction.NONE -> {
                _uiState.update {
                    it.copy(
                        selectedSection = section,
                        errorMessage = null,
                        infoMessage = null
                    )
                }
            }

            HealthEntryAction.ADD_INDICATOR -> {
                _uiState.update {
                    it.copy(
                        selectedSection = HealthSection.INDICATORS,
                        errorMessage = null,
                        infoMessage = null
                    )
                }
                startAddingHealthRecord()
            }

            HealthEntryAction.ADD_ANALYSIS -> {
                _uiState.update {
                    it.copy(
                        selectedSection = HealthSection.ANALYSES,
                        errorMessage = null,
                        infoMessage = null
                    )
                }
                startAddingAnalysis()
            }
        }
    }
    fun selectSection(section: HealthSection) {
        _uiState.update { it.copy(selectedSection = section) }
    }

    fun startEditingProfile() {
        _uiState.update {
            it.copy(
                isEditingProfile = true,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun updateName(value: String) {
        _uiState.update { it.copy(nameInput = value) }
    }

    fun updateSex(value: ProfileSex) {
        _uiState.update { state ->
            state.copy(sex = if (state.sex == value) null else value)
        }
    }

    fun updateBirthDate(dateMillis: Long) {
        val normalizedDate = startOfDay(dateMillis)
        _uiState.update {
            it.copy(
                birthDateMillis = normalizedDate,
                calculatedAge = calculateAge(normalizedDate)
            )
        }
    }

    fun updateHeightCm(value: String) {
        _uiState.update {
            it.copy(heightCmInput = value.filter(Char::isDigit).take(3))
        }
    }

    fun startAddingHealthRecord() {
        _uiState.update {
            it.copy(
                isAddingHealthRecord = true,
                recordDateTimeMillis = System.currentTimeMillis(),
                systolicInput = "",
                diastolicInput = "",
                pulseInput = "",
                weightInput = "",
                temperatureInput = "",
                glucoseInput = "",
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun cancelAddingHealthRecord() {
        _uiState.update {
            it.copy(
                isAddingHealthRecord = false,
                systolicInput = "",
                diastolicInput = "",
                pulseInput = "",
                weightInput = "",
                temperatureInput = "",
                glucoseInput = "",
                errorMessage = null
            )
        }
    }

    fun updateHealthRecordDate(dateMillis: Long) {
        val current = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.recordDateTimeMillis
        }
        val selectedDate = Calendar.getInstance().apply {
            timeInMillis = dateMillis
            set(Calendar.HOUR_OF_DAY, current.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, current.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        _uiState.update { it.copy(recordDateTimeMillis = selectedDate.timeInMillis) }
    }

    fun updateHealthRecordTime(hour: Int, minute: Int) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = _uiState.value.recordDateTimeMillis
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        _uiState.update { it.copy(recordDateTimeMillis = calendar.timeInMillis) }
    }

    fun updateSystolicInput(value: String) {
        _uiState.update { it.copy(systolicInput = value.filter(Char::isDigit).take(3)) }
    }

    fun updateDiastolicInput(value: String) {
        _uiState.update { it.copy(diastolicInput = value.filter(Char::isDigit).take(3)) }
    }

    fun updatePulseInput(value: String) {
        _uiState.update { it.copy(pulseInput = value.filter(Char::isDigit).take(3)) }
    }

    fun updateWeightInput(value: String) {
        _uiState.update { it.copy(weightInput = sanitizeDecimalInput(value)) }
    }

    fun updateTemperatureInput(value: String) {
        _uiState.update { it.copy(temperatureInput = sanitizeDecimalInput(value)) }
    }

    fun updateGlucoseInput(value: String) {
        _uiState.update { it.copy(glucoseInput = sanitizeDecimalInput(value)) }
    }

    fun openArchivedAnalyses() {
        _uiState.update {
            it.copy(
                analysisScreenMode = AnalysisScreenMode.ARCHIVED_LIST,
                showingArchivedAnalyses = true,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun startAddingAnalysis() {
        _uiState.update {
            it.copy(
                analysisScreenMode = AnalysisScreenMode.ADD_ANALYSIS,
                showingArchivedAnalyses = false,
                editingAnalysisId = null,
                analysisNameInput = "",
                analysisUnitInput = "ед.",
                analysisSuggestions = commonAnalysisSuggestions,
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun startEditingSelectedAnalysis() {
        val detail = _uiState.value.selectedAnalysisDetail ?: return

        _uiState.update {
            it.copy(
                analysisScreenMode = AnalysisScreenMode.EDIT_ANALYSIS,
                editingAnalysisId = detail.id,
                analysisNameInput = detail.name,
                analysisUnitInput = detail.defaultUnit,
                analysisSuggestions = filterSuggestions(detail.name),
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun cancelAnalysisEditing() {
        _uiState.update { state ->
            state.copy(
                analysisScreenMode = if (state.selectedAnalysisId == null) {
                    if (state.showingArchivedAnalyses) AnalysisScreenMode.ARCHIVED_LIST else AnalysisScreenMode.LIST
                } else {
                    AnalysisScreenMode.DETAIL
                },
                editingAnalysisId = null,
                analysisNameInput = "",
                analysisUnitInput = "ед.",
                analysisSuggestions = emptyList(),
                errorMessage = null
            )
        }
    }

    fun updateAnalysisName(value: String) {
        _uiState.update {
            it.copy(
                analysisNameInput = value,
                analysisSuggestions = filterSuggestions(value)
            )
        }
    }

    fun updateAnalysisUnit(value: String) {
        _uiState.update { it.copy(analysisUnitInput = value) }
    }

    fun applyAnalysisSuggestion(suggestion: AnalysisSuggestion) {
        _uiState.update {
            it.copy(
                analysisNameInput = suggestion.name,
                analysisUnitInput = suggestion.defaultUnit,
                analysisSuggestions = filterSuggestions(suggestion.name)
            )
        }
    }

    fun openAnalysis(analysisId: Long) {
        selectedAnalysisJob?.cancel()

        _uiState.update {
            it.copy(
                selectedAnalysisId = analysisId,
                selectedAnalysisDetail = null,
                isAnalysisDetailLoading = true,
                analysisScreenMode = AnalysisScreenMode.DETAIL,
                errorMessage = null,
                infoMessage = null
            )
        }

        selectedAnalysisJob = viewModelScope.launch {
            repository.observeAnalysisDetail(analysisId).collect { detail ->
                _uiState.update {
                    it.copy(
                        selectedAnalysisDetail = detail,
                        isAnalysisDetailLoading = false
                    )
                }
            }
        }
    }

    fun backToAnalysesList() {
        selectedAnalysisJob?.cancel()
        selectedAnalysisJob = null

        _uiState.update {
            it.copy(
                selectedAnalysisId = null,
                selectedAnalysisDetail = null,
                isAnalysisDetailLoading = false,
                analysisScreenMode = if (it.showingArchivedAnalyses) {
                    AnalysisScreenMode.ARCHIVED_LIST
                } else {
                    AnalysisScreenMode.LIST
                },
                editingAnalysisId = null,
                analysisNameInput = "",
                analysisUnitInput = "ед.",
                analysisSuggestions = emptyList(),
                analysisResultValueInput = "",
                analysisResultDateMillis = startOfDay(System.currentTimeMillis()),
                analysisResultNoteInput = "",
                errorMessage = null
            )
        }
    }

    fun backToActiveAnalysesList() {
        selectedAnalysisJob?.cancel()
        selectedAnalysisJob = null

        _uiState.update {
            it.copy(
                showingArchivedAnalyses = false,
                selectedAnalysisId = null,
                selectedAnalysisDetail = null,
                isAnalysisDetailLoading = false,
                analysisScreenMode = AnalysisScreenMode.LIST,
                editingAnalysisId = null,
                analysisNameInput = "",
                analysisUnitInput = "ед.",
                analysisSuggestions = emptyList(),
                analysisResultValueInput = "",
                analysisResultDateMillis = startOfDay(System.currentTimeMillis()),
                analysisResultNoteInput = "",
                errorMessage = null
            )
        }
    }

    fun startAddingResult() {
        if (_uiState.value.selectedAnalysisDetail == null) return

        _uiState.update {
            it.copy(
                analysisScreenMode = AnalysisScreenMode.ADD_RESULT,
                analysisResultValueInput = "",
                analysisResultDateMillis = startOfDay(System.currentTimeMillis()),
                analysisResultNoteInput = "",
                errorMessage = null,
                infoMessage = null
            )
        }
    }

    fun cancelAddingResult() {
        _uiState.update {
            it.copy(
                analysisScreenMode = AnalysisScreenMode.DETAIL,
                analysisResultValueInput = "",
                analysisResultDateMillis = startOfDay(System.currentTimeMillis()),
                analysisResultNoteInput = "",
                errorMessage = null
            )
        }
    }

    fun updateAnalysisResultValue(value: String) {
        _uiState.update { it.copy(analysisResultValueInput = sanitizeDecimalInput(value)) }
    }

    fun updateAnalysisResultDate(dateMillis: Long) {
        _uiState.update { it.copy(analysisResultDateMillis = startOfDay(dateMillis)) }
    }

    fun updateAnalysisResultNote(value: String) {
        _uiState.update { it.copy(analysisResultNoteInput = value) }
    }

    fun archiveSelectedAnalysis() {
        val detail = _uiState.value.selectedAnalysisDetail ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                repository.archiveAnalysis(detail.id)
                selectedAnalysisJob?.cancel()
                selectedAnalysisJob = null

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showingArchivedAnalyses = true,
                        selectedAnalysisId = null,
                        selectedAnalysisDetail = null,
                        isAnalysisDetailLoading = false,
                        analysisScreenMode = AnalysisScreenMode.ARCHIVED_LIST,
                        infoMessage = "Анализ перемещён в архив"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Не удалось переместить анализ в архив"
                    )
                }
            }
        }
    }

    fun restoreSelectedAnalysis() {
        val detail = _uiState.value.selectedAnalysisDetail ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                repository.restoreAnalysis(detail.id)
                selectedAnalysisJob?.cancel()
                selectedAnalysisJob = null

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showingArchivedAnalyses = false,
                        selectedAnalysisId = null,
                        selectedAnalysisDetail = null,
                        isAnalysisDetailLoading = false,
                        analysisScreenMode = AnalysisScreenMode.LIST,
                        infoMessage = "Анализ восстановлен из архива"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Не удалось восстановить анализ"
                    )
                }
            }
        }
    }

    fun saveAnalysis() {
        val state = _uiState.value
        val name = state.analysisNameInput.trim()
        val unit = state.analysisUnitInput.trim().ifBlank { "ед." }

        when {
            name.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Введите название анализа") }
                return
            }

            unit.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Введите единицу измерения") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                if (state.analysisScreenMode == AnalysisScreenMode.EDIT_ANALYSIS) {
                    val editingId = state.editingAnalysisId
                        ?: throw IllegalArgumentException("Анализ не найден")

                    repository.updateAnalysis(
                        UpdateAnalysisData(
                            id = editingId,
                            name = name,
                            defaultUnit = unit
                        )
                    )

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            analysisScreenMode = AnalysisScreenMode.DETAIL,
                            analysisNameInput = "",
                            analysisUnitInput = "ед.",
                            analysisSuggestions = emptyList(),
                            editingAnalysisId = null,
                            infoMessage = "Анализ обновлён"
                        )
                    }
                } else {
                    val insertedId = repository.addAnalysis(
                        NewAnalysisData(
                            name = name,
                            defaultUnit = unit
                        )
                    )

                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            showingArchivedAnalyses = false,
                            analysisScreenMode = AnalysisScreenMode.LIST,
                            analysisNameInput = "",
                            analysisUnitInput = "ед.",
                            analysisSuggestions = emptyList(),
                            editingAnalysisId = null,
                            infoMessage = "Анализ добавлен"
                        )
                    }

                    openAnalysis(insertedId)
                }
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Не удалось сохранить анализ"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Не удалось сохранить анализ"
                    )
                }
            }
        }
    }

    fun saveAnalysisResult() {
        val state = _uiState.value
        val analysisId = state.selectedAnalysisId
        val parsedValue = state.analysisResultValueInput.replace(",", ".").toDoubleOrNull()

        when {
            analysisId == null -> {
                _uiState.update { it.copy(errorMessage = "Анализ не выбран") }
                return
            }

            parsedValue == null -> {
                _uiState.update { it.copy(errorMessage = "Введите корректное значение") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                repository.addAnalysisResult(
                    NewAnalysisResultData(
                        analysisId = analysisId,
                        value = parsedValue,
                        resultDateMillis = state.analysisResultDateMillis,
                        note = state.analysisResultNoteInput
                    )
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        analysisScreenMode = AnalysisScreenMode.DETAIL,
                        analysisResultValueInput = "",
                        analysisResultDateMillis = startOfDay(System.currentTimeMillis()),
                        analysisResultNoteInput = "",
                        infoMessage = "Результат сохранён"
                    )
                }
            } catch (e: IllegalArgumentException) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Не удалось сохранить результат"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Не удалось сохранить результат"
                    )
                }
            }
        }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun saveProfile() {
        val state = _uiState.value
        val parsedHeight = state.heightCmInput.toIntOrNull()
        val today = startOfDay(System.currentTimeMillis())

        when {
            state.heightCmInput.isNotBlank() && (parsedHeight == null || parsedHeight !in 30..300) -> {
                _uiState.update { it.copy(errorMessage = "Введите корректный рост в сантиметрах") }
                return
            }

            state.birthDateMillis != null && state.birthDateMillis > today -> {
                _uiState.update { it.copy(errorMessage = "Дата рождения не может быть в будущем") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                repository.saveUserProfile(
                    UserProfileData(
                        name = state.nameInput,
                        sex = state.sex,
                        birthDateMillis = state.birthDateMillis,
                        heightCm = parsedHeight
                    )
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        hasSavedProfile = true,
                        isEditingProfile = false,
                        infoMessage = "Личные данные сохранены"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Не удалось сохранить личные данные"
                    )
                }
            }
        }
    }

    fun saveHealthRecord() {
        val state = _uiState.value

        val systolic = state.systolicInput.toIntOrNull()
        val diastolic = state.diastolicInput.toIntOrNull()
        val pulse = state.pulseInput.toIntOrNull()
        val weight = state.weightInput.replace(",", ".").toDoubleOrNull()
        val temperature = state.temperatureInput.replace(",", ".").toDoubleOrNull()
        val glucose = state.glucoseInput.replace(",", ".").toDoubleOrNull()

        val hasAnyIndicator =
            systolic != null ||
                    diastolic != null ||
                    pulse != null ||
                    weight != null ||
                    temperature != null ||
                    glucose != null

        when {
            !hasAnyIndicator -> {
                _uiState.update { it.copy(errorMessage = "Заполните хотя бы один показатель") }
                return
            }

            (systolic == null) != (diastolic == null) -> {
                _uiState.update { it.copy(errorMessage = "Для давления укажите и верхнее, и нижнее значение") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            try {
                repository.addDailyHealthRecord(
                    NewDailyHealthRecordData(
                        recordedAtMillis = state.recordDateTimeMillis,
                        systolicPressure = systolic,
                        diastolicPressure = diastolic,
                        pulse = pulse,
                        weightKg = weight,
                        temperatureC = temperature,
                        glucoseMmolL = glucose
                    )
                )

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isAddingHealthRecord = false,
                        systolicInput = "",
                        diastolicInput = "",
                        pulseInput = "",
                        weightInput = "",
                        temperatureInput = "",
                        glucoseInput = "",
                        infoMessage = "Запись сохранена"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Не удалось сохранить запись"
                    )
                }
            }
        }
    }

    private fun calculateAge(birthDateMillis: Long?): Int? {
        if (birthDateMillis == null) return null

        val birthCalendar = Calendar.getInstance().apply {
            timeInMillis = birthDateMillis
        }
        val todayCalendar = Calendar.getInstance()

        var age = todayCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

        val hasHadBirthdayThisYear =
            todayCalendar.get(Calendar.MONTH) > birthCalendar.get(Calendar.MONTH) ||
                    (
                            todayCalendar.get(Calendar.MONTH) == birthCalendar.get(Calendar.MONTH) &&
                                    todayCalendar.get(Calendar.DAY_OF_MONTH) >= birthCalendar.get(Calendar.DAY_OF_MONTH)
                            )

        if (!hasHadBirthdayThisYear) {
            age -= 1
        }

        return age.takeIf { it >= 0 }
    }

    private fun sanitizeDecimalInput(value: String): String {
        return value.filter { it.isDigit() || it == ',' || it == '.' }.take(12)
    }

    private fun filterSuggestions(query: String): List<AnalysisSuggestion> {
        val normalizedQuery = query.trim()

        return if (normalizedQuery.isBlank()) {
            commonAnalysisSuggestions
        } else {
            commonAnalysisSuggestions.filter { suggestion ->
                suggestion.name.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }
}
