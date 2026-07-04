package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.util.NotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class LawyerViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val repository = LawyerRepository(db)
    val securityManager = SecurityManager(application)
    private val notificationHelper = NotificationHelper(application)

    val allCases: StateFlow<List<CaseEntity>> = repository.allCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeCases: StateFlow<List<CaseEntity>> = repository.activeCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val disposedCases: StateFlow<List<CaseEntity>> = repository.disposedCases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSteps: StateFlow<List<StepEntity>> = repository.allSteps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCourts: StateFlow<List<CourtEntity>> = repository.allCourts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCaseTypes: StateFlow<List<CaseTypeEntity>> = repository.allCaseTypes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLocked = MutableStateFlow(securityManager.isAppLocked())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    private val _hasPassword = MutableStateFlow(securityManager.isPasswordSet())
    val hasPassword: StateFlow<Boolean> = _hasPassword.asStateFlow()

    private val _lastBackupDate = MutableStateFlow(securityManager.getLastBackupDate())
    val lastBackupDate: StateFlow<String> = _lastBackupDate.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCaseId = MutableStateFlow<Int?>(null)
    val selectedCaseId: StateFlow<Int?> = _selectedCaseId.asStateFlow()

    val selectedCaseDetails: StateFlow<CaseWithDetails?> = _selectedCaseId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else {
                combine(
                    repository.caseDao.getCaseById(id),
                    repository.stepDao.getStepsForCase(id),
                    repository.documentDao.getDocumentsForCase(id)
                ) { case, steps, docs ->
                    if (case != null) CaseWithDetails(case, steps, docs) else null
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            checkUpcomingReminders()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCase(caseId: Int?) {
        _selectedCaseId.value = caseId
    }

    // Security actions
    fun verifyPassword(input: String): Boolean {
        val valid = securityManager.verifyPassword(input)
        if (valid) {
            securityManager.setAppLocked(false)
            _isLocked.value = false
        }
        return valid
    }

    fun setPassword(newPin: String) {
        securityManager.setPassword(newPin)
        _hasPassword.value = true
        _isLocked.value = false
        securityManager.setAppLocked(false)
    }

    fun removePassword() {
        securityManager.removePassword()
        _hasPassword.value = false
        _isLocked.value = false
    }

    fun lockApp() {
        if (securityManager.isPasswordSet()) {
            securityManager.setAppLocked(true)
            _isLocked.value = true
        }
    }

    // Case actions
    fun addCase(caseEntity: CaseEntity, initialStep: StepEntity? = null, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.caseDao.insertCase(caseEntity)
            if (initialStep != null) {
                repository.stepDao.insertStep(initialStep.copy(caseId = id.toInt()))
            }
            onComplete(id.toInt())
        }
    }

    fun updateCase(caseEntity: CaseEntity) {
        viewModelScope.launch {
            repository.caseDao.updateCase(caseEntity)
        }
    }

    fun deleteCase(caseEntity: CaseEntity) {
        viewModelScope.launch {
            repository.stepDao.deleteStepsForCase(caseEntity.id)
            repository.documentDao.deleteDocumentsForCase(caseEntity.id)
            repository.caseDao.deleteCase(caseEntity)
        }
    }

    fun setCaseDisposed(caseId: Int, disposed: Boolean) {
        viewModelScope.launch {
            repository.caseDao.setCaseDisposedStatus(caseId, disposed)
        }
    }

    // Step actions
    fun addStep(stepEntity: StepEntity) {
        viewModelScope.launch {
            repository.stepDao.insertStep(stepEntity)
            checkUpcomingReminders()
        }
    }

    fun deleteStep(stepEntity: StepEntity) {
        viewModelScope.launch {
            repository.stepDao.deleteStep(stepEntity)
        }
    }

    // Document actions
    fun addDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.documentDao.insertDocument(doc)
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.documentDao.deleteDocument(doc)
        }
    }

    // Court & CaseType actions
    fun addCourt(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.courtDao.insertCourt(CourtEntity(name = name.trim()))
            }
        }
    }

    fun deleteCourt(court: CourtEntity) {
        viewModelScope.launch {
            repository.courtDao.deleteCourt(court)
        }
    }

    fun addCaseType(name: String) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.caseTypeDao.insertCaseType(CaseTypeEntity(name = name.trim()))
            }
        }
    }

    fun deleteCaseType(type: CaseTypeEntity) {
        viewModelScope.launch {
            repository.caseTypeDao.deleteCaseType(type)
        }
    }

    // Backup & Restore
    fun exportBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val json = repository.exportBackupJson()
            securityManager.recordBackupDate()
            _lastBackupDate.value = securityManager.getLastBackupDate()
            onResult(json)
        }
    }

    fun restoreBackup(jsonString: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.restoreBackupJson(jsonString)
            if (success) {
                securityManager.recordBackupDate()
                _lastBackupDate.value = securityManager.getLastBackupDate()
            }
            onResult(success)
        }
    }

    // Reminders
    fun triggerManualReminderCheck(onAlert: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val count = checkUpcomingReminders(forceNotify = true)
            onAlert(count)
        }
    }

    private suspend fun checkUpcomingReminders(forceNotify: Boolean = false): Int {
        if (!securityManager.isReminderEnabled() && !forceNotify) return 0
        val steps = repository.stepDao.getAllSteps().first()
        val cases = repository.caseDao.getAllCases().first()
        val caseMap = cases.associateBy { it.id }

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        val today = sdf.format(Date())

        var upcomingCount = 0
        val alertSteps = mutableListOf<Pair<StepEntity, CaseEntity>>()

        for (step in steps) {
            if (!step.isReminder || step.adjournDate.isBlank()) continue
            val case = caseMap[step.caseId] ?: continue
            if (case.isDisposed) continue

            // Compare date strings lexicographically for yyyy-MM-dd
            if (step.adjournDate >= today) {
                upcomingCount++
                alertSteps.add(step to case)
            }
        }

        if (alertSteps.isNotEmpty() && (forceNotify || securityManager.isReminderEnabled())) {
            val nextStep = alertSteps.minByOrNull { it.first.adjournDate }
            if (nextStep != null) {
                val (step, case) = nextStep
                notificationHelper.showHearingReminder(
                    title = "تنبيه جلسة قادمة: ${case.title} (${case.caseNumber}/${case.caseYear})",
                    message = "موعد الجلسة: ${step.adjournDate} - المحكمة: ${case.courtName}\nالغرض: ${step.purpose}",
                    notificationId = step.id + 2000
                )
            }
        }
        return upcomingCount
    }
}
