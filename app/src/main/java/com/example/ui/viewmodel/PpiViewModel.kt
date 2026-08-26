package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.GoogleAppsScriptService
import com.example.data.repository.PpiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class PpiViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val gasService = GoogleAppsScriptService(application)
    val repository = PpiRepository(database.ppiDao(), gasService = gasService)

    // Google Apps Script Sync States
    val gasUrl = MutableStateFlow(gasService.gasUrl)
    val isGasAutoSync = MutableStateFlow(gasService.isAutoSyncEnabled)
    val lastGasSyncTime = MutableStateFlow(gasService.lastSyncTime)
    val lastGasSyncStatus = MutableStateFlow(gasService.lastSyncStatus)
    val isGasSyncing = MutableStateFlow(false)

    // School & License
    val school: StateFlow<SchoolEntity?> = repository.getSchool()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val license: StateFlow<LicenseEntity?> = repository.getLicense("SCH-PAUDIT-IP")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Session / Active User
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Academic Period selection
    val selectedYear = MutableStateFlow("2026/2027")
    val selectedSemester = MutableStateFlow("1")

    // Filter & Search states
    val studentSearch = MutableStateFlow("")
    val filterUnit = MutableStateFlow("")
    val filterClass = MutableStateFlow("")
    val filterGpk = MutableStateFlow("")
    val filterPpiStage = MutableStateFlow("")
    val filterReportStage = MutableStateFlow("")

    // Raw students
    private val _allStudents = repository.getStudents("SCH-PAUDIT-IP")
    val students: StateFlow<List<StudentEntity>> = combine(
        _allStudents,
        _currentUser,
        studentSearch,
        filterUnit,
        filterClass,
        filterGpk,
        filterPpiStage
    ) { params ->
        val rawList = params[0] as List<StudentEntity>
        val user = params[1] as? UserEntity
        val query = (params[2] as String).trim().lowercase()
        val unit = params[3] as String
        val cls = params[4] as String
        val gpk = params[5] as String
        val ppiStage = params[6] as String

        var filtered = rawList

        // GPK role isolation
        if (user != null && user.role.equals("GPK", ignoreCase = true)) {
            filtered = filtered.filter { it.gpkUserId == user.userId || it.gpkName.equals(user.name, true) }
        }

        if (query.isNotBlank()) {
            filtered = filtered.filter {
                it.name.lowercase().contains(query) ||
                        it.nis.contains(query) ||
                        it.nisn.contains(query) ||
                        it.className.lowercase().contains(query) ||
                        it.gpkName.lowercase().contains(query)
            }
        }
        if (unit.isNotBlank()) {
            filtered = filtered.filter { it.unit == unit }
        }
        if (cls.isNotBlank()) {
            filtered = filtered.filter { it.className == cls }
        }
        if (gpk.isNotBlank()) {
            filtered = filtered.filter { it.gpkName == gpk }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gpkUsers: StateFlow<List<UserEntity>> = repository.getActiveGpkUsers("SCH-PAUDIT-IP")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getUsers("SCH-PAUDIT-IP")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Student in IEP Workspace
    val selectedStudentId = MutableStateFlow<String?>(null)

    val selectedStudent: StateFlow<StudentEntity?> = selectedStudentId.flatMapLatest { id ->
        if (id == null) flowOf(null) else repository.observeStudentById(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Assessment
    val currentAssessment: StateFlow<AssessmentEntity?> = combine(
        selectedStudentId, selectedYear, selectedSemester
    ) { id, year, sem ->
        Triple(id, year, sem)
    }.flatMapLatest { (id, year, sem) ->
        if (id == null) flowOf(null) else repository.getAssessment(id, year, sem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current PPI
    val currentPpi: StateFlow<PpiEntity?> = combine(
        selectedStudentId, selectedYear, selectedSemester
    ) { id, year, sem ->
        Triple(id, year, sem)
    }.flatMapLatest { (id, year, sem) ->
        if (id == null) flowOf(null) else repository.getPpi(id, year, sem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Historical PPIs for student
    val studentPpiHistory: StateFlow<List<PpiEntity>> = selectedStudentId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getAllPpisForStudent(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Daily Journals
    val dailyJournals: StateFlow<List<DailyJournalEntity>> = combine(
        selectedStudentId, selectedYear, selectedSemester
    ) { id, year, sem ->
        Triple(id, year, sem)
    }.flatMapLatest { (id, year, sem) ->
        if (id == null) flowOf(emptyList()) else repository.getDailyJournals(id, year, sem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Weekly Journals
    val weeklyJournals: StateFlow<List<WeeklyJournalEntity>> = combine(
        selectedStudentId, selectedYear, selectedSemester
    ) { id, year, sem ->
        Triple(id, year, sem)
    }.flatMapLatest { (id, year, sem) ->
        if (id == null) flowOf(emptyList()) else repository.getWeeklyJournals(id, year, sem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Progress Records
    val progressRecords: StateFlow<List<ProgressEntity>> = combine(
        selectedStudentId, selectedYear, selectedSemester
    ) { id, year, sem ->
        Triple(id, year, sem)
    }.flatMapLatest { (id, year, sem) ->
        if (id == null) flowOf(emptyList()) else repository.getProgressRecords(id, year, sem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Progress Analysis
    val currentAnalysis: StateFlow<ProgressAnalysisEntity?> = combine(
        selectedStudentId, selectedYear, selectedSemester
    ) { id, year, sem ->
        Triple(id, year, sem)
    }.flatMapLatest { (id, year, sem) ->
        if (id == null) flowOf(null) else repository.getProgressAnalysis(id, year, sem)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current Student GPK Assignment History
    val currentGpkAssignments: StateFlow<List<GpkAssignmentEntity>> = selectedStudentId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList()) else repository.getGpkAssignments(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications / To-Do Tasks
    val notifications: StateFlow<List<NotificationEntity>> = _currentUser.flatMapLatest { user ->
        val role = user?.role ?: "GPK"
        val userId = user?.userId ?: ""
        repository.getNotifications("SCH-PAUDIT-IP", role, userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        val role = user?.role ?: "GPK"
        val userId = user?.userId ?: ""
        repository.getUnreadCount("SCH-PAUDIT-IP", role, userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI Feedback & Loading
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    init {
        // Auto initialize with default user (e.g. GPK 1 or Admin)
        viewModelScope.launch {
            val user = repository.getUserByUsername("gpk1")
                ?: repository.getUserByUsername("admin")
            _currentUser.value = user
            val firstStudent = _allStudents.firstOrNull()?.firstOrNull()
            if (firstStudent != null) {
                selectedStudentId.value = firstStudent.studentId
            }
        }
    }

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun triggerGasSync(actionName: String, block: suspend (GoogleAppsScriptService) -> Result<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!gasService.isAutoSyncEnabled) return@launch
            isGasSyncing.value = true
            try {
                val result = block(gasService)
                lastGasSyncTime.value = gasService.lastSyncTime
                lastGasSyncStatus.value = gasService.lastSyncStatus
                if (result.isSuccess) {
                    Log.d("GAS_SYNC", "Auto-sync $actionName success")
                } else {
                    Log.w("GAS_SYNC", "Auto-sync $actionName notice: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                lastGasSyncStatus.value = "Error: ${e.message}"
                Log.e("GAS_SYNC", "Auto-sync $actionName error", e)
            } finally {
                isGasSyncing.value = false
            }
        }
    }

    fun testGasConnection() {
        viewModelScope.launch(Dispatchers.IO) {
            isGasSyncing.value = true
            try {
                val res = gasService.ping()
                lastGasSyncTime.value = gasService.lastSyncTime
                lastGasSyncStatus.value = gasService.lastSyncStatus
                if (res.isSuccess) {
                    showToast("✅ Berhasil terhubung ke Google Apps Script Spreadsheet!")
                } else {
                    showToast("⚠️ Respon: ${res.exceptionOrNull()?.message ?: "Gagal terhubung"}")
                }
            } catch (e: Exception) {
                showToast("❌ Gagal terhubung ke Google Apps Script: ${e.message}")
            } finally {
                isGasSyncing.value = false
            }
        }
    }

    fun updateGasSettings(url: String, autoSync: Boolean) {
        gasService.gasUrl = url
        gasService.isAutoSyncEnabled = autoSync
        gasUrl.value = gasService.gasUrl
        isGasAutoSync.value = gasService.isAutoSyncEnabled
        showToast("Pengaturan koneksi Google Apps Script disimpan.")
    }

    fun syncAllToGoogleSpreadsheet() {
        viewModelScope.launch(Dispatchers.IO) {
            isGasSyncing.value = true
            try {
                val allSch = repository.getAllSchools().firstOrNull() ?: emptyList()
                val allStud = repository.getStudents("SCH-PAUDIT-IP").firstOrNull() ?: emptyList()
                val allPpi = repository.getPpisForPeriod("SCH-PAUDIT-IP", selectedYear.value, selectedSemester.value).firstOrNull() ?: emptyList()
                val studentId = selectedStudentId.value
                val allDaily = if (studentId != null) repository.getDailyJournals(studentId, selectedYear.value, selectedSemester.value).firstOrNull() ?: emptyList() else emptyList()
                val allWeekly = if (studentId != null) repository.getWeeklyJournals(studentId, selectedYear.value, selectedSemester.value).firstOrNull() ?: emptyList() else emptyList()

                val result = gasService.syncAllData(
                    schools = allSch,
                    students = allStud,
                    ppis = allPpi,
                    dailyJournals = allDaily,
                    weeklyJournals = allWeekly
                )
                lastGasSyncTime.value = gasService.lastSyncTime
                lastGasSyncStatus.value = gasService.lastSyncStatus
                if (result.isSuccess) {
                    showToast("✅ Semua data berhasil disinkronkan ke Google Spreadsheet!")
                } else {
                    showToast("⚠️ Sinkronisasi selesai dengan catatan: ${result.exceptionOrNull()?.message}")
                }
            } catch (e: Exception) {
                showToast("❌ Gagal sinkronisasi data: ${e.message}")
            } finally {
                isGasSyncing.value = false
            }
        }
    }

    fun switchUser(user: UserEntity) {
        _currentUser.value = user
        showToast("Beralih ke peran: ${user.name} (${user.role})")
    }

    fun selectStudent(studentId: String) {
        selectedStudentId.value = studentId
    }

    // --- School & Theme ---
    fun updateSchoolIdentity(
        name: String,
        npsn: String,
        level: String,
        tagline: String,
        vision: String,
        address: String,
        phone: String,
        mobile: String,
        principal: String,
        primaryColor: String,
        accentColor: String
    ) {
        viewModelScope.launch {
            val current = school.value ?: return@launch
            val updated = current.copy(
                name = name,
                npsn = npsn,
                level = level,
                tagline = tagline,
                vision = vision,
                address = address,
                phone = phone,
                mobile = mobile,
                principalName = principal,
                primaryColor = primaryColor,
                accentColor = accentColor
            )
            repository.updateSchool(updated)
            triggerGasSync("School") { it.syncSchool(updated) }
            showToast("Identitas sekolah & warna tema berhasil diperbarui.")
        }
    }

    // --- Student CRUD ---
    fun saveStudent(
        studentId: String?,
        name: String,
        nis: String,
        nisn: String,
        gender: String,
        birthPlace: String,
        birthDate: String,
        unit: String,
        className: String,
        homeroomTeacher: String,
        father: String,
        mother: String,
        parentPhone: String,
        address: String,
        gpkUserId: String,
        gpkName: String,
        educationHistory: String,
        notes: String,
        photoUri: String
    ) {
        viewModelScope.launch {
            val id = studentId ?: ("STD-" + UUID.randomUUID().toString().take(6).uppercase())
            val entity = StudentEntity(
                studentId = id,
                schoolId = "SCH-PAUDIT-IP",
                name = name,
                nis = nis,
                nisn = nisn,
                gender = gender,
                birthPlace = birthPlace,
                birthDate = birthDate,
                unit = unit,
                className = className,
                homeroomTeacher = homeroomTeacher,
                fatherName = father,
                motherName = mother,
                parentPhone = parentPhone,
                address = address,
                gpkUserId = gpkUserId,
                gpkName = gpkName,
                educationHistory = educationHistory,
                notes = notes,
                photoUri = photoUri
            )
            repository.saveStudent(entity)
            triggerGasSync("Student") { it.syncStudent(entity) }
            if (selectedStudentId.value == null) {
                selectedStudentId.value = id
            }
            showToast("Data peserta didik $name berhasil disimpan.")
        }
    }

    fun deleteStudent(studentId: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            triggerGasSync("DeleteStudent") {
                it.syncStudent(
                    StudentEntity(
                        studentId = studentId,
                        schoolId = "SCH-PAUDIT-IP",
                        name = "Dihapus"
                    ),
                    isDelete = true
                )
            }
            if (selectedStudentId.value == studentId) {
                selectedStudentId.value = students.value.firstOrNull { it.studentId != studentId }?.studentId
            }
            showToast("Data peserta didik berhasil dihapus.")
        }
    }

    fun transferStudentGpk(
        studentId: String,
        newGpkUserId: String,
        newGpkName: String,
        startDate: String,
        reason: String,
        newUnit: String = "",
        newClassName: String = ""
    ) {
        viewModelScope.launch {
            repository.transferGpk(
                studentId = studentId,
                newGpkUserId = newGpkUserId,
                newGpkName = newGpkName,
                startDate = startDate,
                reason = reason,
                newUnit = newUnit,
                newClassName = newClassName
            )
            showToast("Serah terima GPK berhasil. GPK baru otomatis memiliki akses pendampingan.")
        }
    }

    // --- Assessment ---
    fun saveAssessment(
        specialNeeds: List<String>,
        professionalDiagnosis: String,
        academic: String,
        communication: String,
        social: String,
        emotionalBehavior: String,
        independence: String,
        motoric: String,
        sensoric: String,
        interestsPotential: String,
        strengths: String,
        obstacles: String,
        baseline: String,
        priorities: String
    ) {
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val current = currentAssessment.value
            val entity = AssessmentEntity(
                assessmentId = current?.assessmentId ?: ("ASM-" + UUID.randomUUID().toString().take(6).uppercase()),
                schoolId = student.schoolId,
                studentId = student.studentId,
                academicYear = selectedYear.value,
                semester = selectedSemester.value,
                specialNeeds = specialNeeds,
                professionalDiagnosis = professionalDiagnosis,
                academic = academic,
                communication = communication,
                social = social,
                emotionalBehavior = emotionalBehavior,
                independence = independence,
                motoric = motoric,
                sensoric = sensoric,
                interestsPotential = interestsPotential,
                strengths = strengths,
                obstacles = obstacles,
                baseline = baseline,
                priorities = priorities,
                updatedAt = System.currentTimeMillis()
            )
            repository.saveAssessment(entity)
            triggerGasSync("Assessment") { it.syncAssessment(entity) }
            showToast("Asesmen awal peserta didik berhasil disimpan.")
        }
    }

    // --- AI PPI Drafting ---
    fun generatePpiWithAi(planningInputs: List<PlanningInputItem>) {
        val student = selectedStudent.value ?: return
        val assessment = currentAssessment.value
        if (assessment == null) {
            showToast("Lengkapi asesmen awal peserta didik terlebih dahulu.")
            return
        }

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val result = repository.generatePpiDraftWithAi(student, assessment, planningInputs)
                val current = currentPpi.value
                val ppiId = current?.ppiId ?: ("PPI-" + UUID.randomUUID().toString().take(6).uppercase())
                val schoolEntity = school.value

                val newPpi = PpiEntity(
                    ppiId = ppiId,
                    schoolId = student.schoolId,
                    studentId = student.studentId,
                    academicYear = selectedYear.value,
                    semester = selectedSemester.value,
                    status = "DRAFT",
                    approvalStatus = "DRAFT",
                    aiModel = "gemini-3.5-flash",
                    principalName = schoolEntity?.principalName ?: "Dra. Hj. Siti Aminah, M.Pd.",
                    homeroomTeacher = student.gpkName.ifBlank { "Ustadzah Aisyah Rahma, S.Pd." },
                    coordinatorName = "Ustadzah Nurul Hidayati, S.Psi.",
                    profile = result.profile,
                    strengths = assessment.strengths,
                    needs = assessment.obstacles,
                    baseline = assessment.baseline,
                    priorities = result.priorities,
                    longTermGoal = result.longTermGoal,
                    shortTermGoalsJson = repository.encodeObjectives(result.objectives),
                    planningInputJson = repository.encodePlanningInputs(planningInputs),
                    planningMatrixJson = repository.encodePlanningMatrix(result.planningMatrix),
                    timeScheduleJson = repository.encodeTimeSchedule(result.timeSchedule),
                    programCategoriesJson = repository.encodeCategories(result.categories),
                    accommodation = result.accommodation,
                    monitoringPlan = result.monitoringPlan,
                    collaboration = result.collaboration,
                    generalNotes = "Draf disusun otomatis oleh Gemini AI berdasarkan data asesmen.",
                    createdBy = currentUser.value?.userId ?: "USR-GPK1",
                    updatedAt = System.currentTimeMillis()
                )

                repository.savePpi(newPpi)
                triggerGasSync("PpiDraft") { it.syncPpi(newPpi) }
                showToast("Draf PPI berhasil disusun oleh AI. Periksa dan simpan draft.")
            } catch (e: Exception) {
                showToast("Gagal menyusun PPI dengan AI: ${e.message}")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    // --- Save & Workflow Management for PPI ---
    fun savePpiDraft(
        profile: String,
        strengths: String,
        needs: String,
        baseline: String,
        priorities: List<String>,
        longTermGoal: String,
        objectives: List<ObjectiveItem>,
        planningInputs: List<PlanningInputItem>,
        planningMatrix: List<PlanningMatrixItem>,
        timeSchedule: List<TimeScheduleItem>,
        categories: ProgramCategories,
        transition: TransitionInfo,
        accommodation: List<String>,
        monitoring: String,
        collaboration: String,
        notes: String
    ) {
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val current = currentPpi.value
            val ppiId = current?.ppiId ?: ("PPI-" + UUID.randomUUID().toString().take(6).uppercase())
            val schoolEntity = school.value

            val entity = PpiEntity(
                ppiId = ppiId,
                schoolId = student.schoolId,
                studentId = student.studentId,
                academicYear = selectedYear.value,
                semester = selectedSemester.value,
                status = current?.status ?: "DRAFT",
                approvalStatus = current?.approvalStatus ?: "DRAFT",
                aiModel = current?.aiModel ?: "gemini-3.5-flash",
                principalName = schoolEntity?.principalName ?: "Dra. Hj. Siti Aminah, M.Pd.",
                homeroomTeacher = student.gpkName.ifBlank { "Ustadzah Aisyah Rahma, S.Pd." },
                coordinatorName = "Ustadzah Nurul Hidayati, S.Psi.",
                profile = profile,
                strengths = strengths,
                needs = needs,
                baseline = baseline,
                priorities = priorities,
                longTermGoal = longTermGoal,
                shortTermGoalsJson = repository.encodeObjectives(objectives),
                planningInputJson = repository.encodePlanningInputs(planningInputs),
                planningMatrixJson = repository.encodePlanningMatrix(planningMatrix),
                timeScheduleJson = repository.encodeTimeSchedule(timeSchedule),
                programCategoriesJson = repository.encodeCategories(categories),
                transitionJson = repository.encodeTransition(transition),
                accommodation = accommodation,
                monitoringPlan = monitoring,
                collaboration = collaboration,
                generalNotes = notes,
                caseConferenceDate = current?.caseConferenceDate ?: "",
                caseConferenceTime = current?.caseConferenceTime ?: "",
                caseConferenceLocation = current?.caseConferenceLocation ?: "",
                caseConferenceParticipants = current?.caseConferenceParticipants ?: "",
                caseConferenceScheduleNote = current?.caseConferenceScheduleNote ?: "",
                parentInput = current?.parentInput ?: "",
                caseConferenceResult = current?.caseConferenceResult ?: "",
                reflection = current?.reflection ?: "",
                followUp = current?.followUp ?: "",
                reflectionStatus = current?.reflectionStatus ?: "DRAFT",
                reportApprovalStatus = current?.reportApprovalStatus ?: "DRAFT",
                coordinatorNote = current?.coordinatorNote ?: "",
                createdBy = current?.createdBy ?: (currentUser.value?.userId ?: "USR-GPK1"),
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(entity)
            triggerGasSync("PpiDraft") { it.syncPpi(entity) }
            showToast("Draft PPI berhasil disimpan.")
        }
    }

    fun submitPpiToCoordinator() {
        val ppi = currentPpi.value ?: return
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                approvalStatus = "DIAJUKAN",
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("SubmitPpi") { it.syncPpi(updated) }

            // Create notification for Koordinator
            repository.saveNotification(
                NotificationEntity(
                    notificationId = "NTF-" + UUID.randomUUID().toString().take(8),
                    schoolId = ppi.schoolId,
                    recipientRole = "KOORDINATOR INKLUSI",
                    title = "PPI Menunggu Persetujuan Koordinator",
                    message = "GPK ${student.gpkName} telah mengajukan draf PPI untuk ${student.name}.",
                    type = "PPI_SUBMITTED",
                    ppiId = ppi.ppiId,
                    studentId = ppi.studentId,
                    studentName = student.name,
                    academicYear = ppi.academicYear,
                    semester = ppi.semester
                )
            )
            showToast("PPI berhasil diajukan ke Koordinator Inklusi.")
        }
    }

    fun approvePpiByCoordinator(note: String) {
        val ppi = currentPpi.value ?: return
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                approvalStatus = "DISETUJUI_KOORDINATOR",
                coordinatorNote = note,
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("ApprovePpi") { it.syncPpi(updated) }

            repository.saveNotification(
                NotificationEntity(
                    notificationId = "NTF-" + UUID.randomUUID().toString().take(8),
                    schoolId = ppi.schoolId,
                    recipientRole = "GPK",
                    recipientUserId = student.gpkUserId,
                    title = "PPI Disetujui Koordinator Inklusi",
                    message = "PPI untuk ${student.name} telah disetujui. Silakan jadwalkan Case Conference bersama orang tua.",
                    type = "PPI_APPROVED_COORDINATOR",
                    ppiId = ppi.ppiId,
                    studentId = ppi.studentId,
                    studentName = student.name,
                    academicYear = ppi.academicYear,
                    semester = ppi.semester
                )
            )
            showToast("PPI disetujui. Tahap Case Conference bersama orang tua sekarang aktif.")
        }
    }

    fun returnPpiForRevision(note: String) {
        val ppi = currentPpi.value ?: return
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                approvalStatus = "PERLU_REVISI",
                coordinatorNote = note,
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("RevisionPpi") { it.syncPpi(updated) }

            repository.saveNotification(
                NotificationEntity(
                    notificationId = "NTF-" + UUID.randomUUID().toString().take(8),
                    schoolId = ppi.schoolId,
                    recipientRole = "GPK",
                    recipientUserId = student.gpkUserId,
                    title = "PPI Perlu Direvisi",
                    message = "Catatan Koordinator: $note",
                    type = "PPI_REVISION",
                    ppiId = ppi.ppiId,
                    studentId = ppi.studentId,
                    studentName = student.name,
                    academicYear = ppi.academicYear,
                    semester = ppi.semester
                )
            )
            showToast("PPI dikembalikan ke GPK untuk revisi.")
        }
    }

    // --- Case Conference ---
    fun saveCaseConference(
        date: String,
        time: String,
        location: String,
        participants: String,
        scheduleNote: String,
        parentInput: String,
        result: String
    ) {
        val ppi = currentPpi.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                caseConferenceDate = date,
                caseConferenceTime = time,
                caseConferenceLocation = location,
                caseConferenceParticipants = participants,
                caseConferenceScheduleNote = scheduleNote,
                parentInput = parentInput,
                caseConferenceResult = result,
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("CaseConference") { it.syncPpi(updated) }
            showToast("Data Case Conference berhasil disimpan.")
        }
    }

    fun generateCaseConferenceSummary(parentInput: String) {
        val ppi = currentPpi.value ?: return
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val summary = repository.generateCaseConferenceSummaryWithAi(
                    studentName = student.name,
                    period = "${ppi.academicYear} Semester ${ppi.semester}",
                    profile = ppi.profile,
                    longTermGoal = ppi.longTermGoal,
                    parentInput = parentInput
                )
                val updated = ppi.copy(
                    parentInput = parentInput,
                    caseConferenceResult = summary,
                    updatedAt = System.currentTimeMillis()
                )
                repository.savePpi(updated)
                triggerGasSync("CaseConferenceSummary") { it.syncPpi(updated) }
                showToast("Ringkasan Case Conference berhasil disusun oleh AI.")
            } catch (e: Exception) {
                showToast("Gagal menyusun ringkasan: ${e.message}")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun validateCaseConferenceByGpk() {
        val ppi = currentPpi.value ?: return
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                approvalStatus = "VALIDASI_GPK",
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("ValidateCaseConference") { it.syncPpi(updated) }

            repository.saveNotification(
                NotificationEntity(
                    notificationId = "NTF-" + UUID.randomUUID().toString().take(8),
                    schoolId = ppi.schoolId,
                    recipientRole = "KOORDINATOR INKLUSI",
                    title = "Case Conference Divalidasi GPK",
                    message = "GPK telah menyelesaikan dan memvalidasi Case Conference ${student.name}. Periksa lalu ajukan ke WAKA.",
                    type = "CASE_VALIDATED_GPK",
                    ppiId = ppi.ppiId,
                    studentId = ppi.studentId,
                    studentName = student.name,
                    academicYear = ppi.academicYear,
                    semester = ppi.semester
                )
            )
            showToast("Case Conference divalidasi dan terkirim ke Koordinator Inklusi. Jurnal Harian kini aktif!")
        }
    }

    fun sendPpiToWaka(note: String) {
        val ppi = currentPpi.value ?: return
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                approvalStatus = "DIKIRIM_WAKA",
                coordinatorNote = note,
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("SendPpiWaka") { it.syncPpi(updated) }

            repository.saveNotification(
                NotificationEntity(
                    notificationId = "NTF-" + UUID.randomUUID().toString().take(8),
                    schoolId = ppi.schoolId,
                    recipientRole = "WAKA",
                    title = "PPI Siap Difinalisasi WAKA",
                    message = "Koordinator telah memverifikasi berkas PPI ${student.name}. Silakan periksa dan finalisasi.",
                    type = "PPI_SENT_WAKA",
                    ppiId = ppi.ppiId,
                    studentId = ppi.studentId,
                    studentName = student.name,
                    academicYear = ppi.academicYear,
                    semester = ppi.semester
                )
            )
            showToast("Berkas PPI diajukan ke WAKA untuk finalisasi.")
        }
    }

    fun finalizePpiByWaka() {
        val ppi = currentPpi.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                status = "FINAL",
                approvalStatus = "FINAL",
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("FinalizePpiWaka") { it.syncPpi(updated) }
            showToast("PPI berhasil difinalisasi WAKA dan siap disahkan Kepala Sekolah.")
        }
    }

    // --- Daily Journal ---
    fun saveDailyJournal(
        journalId: String?,
        date: String,
        source: String,
        scheduleId: String,
        stageId: String,
        activity: String,
        studentAbility: String,
        notes: String,
        includeFinalReport: Boolean,
        rubricScore: Int,
        evidence: String,
        followup: String,
        photoUri: String
    ) {
        val student = selectedStudent.value ?: return
        val ppi = currentPpi.value ?: return
        viewModelScope.launch {
            val id = journalId ?: ("JRN-" + UUID.randomUUID().toString().take(8).uppercase())
            val entry = DailyJournalEntity(
                journalId = id,
                schoolId = student.schoolId,
                ppiId = ppi.ppiId,
                studentId = student.studentId,
                academicYear = selectedYear.value,
                semester = selectedSemester.value,
                date = date,
                source = source,
                scheduleId = scheduleId,
                stageId = stageId,
                activity = activity,
                studentAbility = studentAbility,
                notes = notes,
                includeFinalReport = includeFinalReport,
                rubricScore = rubricScore,
                evidence = evidence,
                followup = followup,
                photoUri = photoUri
            )
            repository.saveDailyJournal(entry)
            triggerGasSync("DailyJournal") { it.syncDailyJournal(entry) }
            showToast("Jurnal harian berhasil dicatat.")
        }
    }

    fun deleteDailyJournal(journalId: String) {
        viewModelScope.launch {
            repository.deleteDailyJournal(journalId)
            triggerGasSync("DeleteDailyJournal") {
                it.syncDailyJournal(
                    DailyJournalEntity(
                        journalId = journalId,
                        schoolId = "SCH-PAUDIT-IP",
                        ppiId = "",
                        studentId = "",
                        academicYear = "",
                        semester = "",
                        date = "",
                        activity = ""
                    ),
                    isDelete = true
                )
            }
            showToast("Jurnal harian dihapus.")
        }
    }

    // --- Weekly Journal ---
    fun saveWeeklyJournal(
        weeklyId: String?,
        weekStart: String,
        weekEnd: String,
        weekNo: Int,
        activity: String,
        studentAbility: String,
        mon: Int, monNote: String,
        tue: Int, tueNote: String,
        wed: Int, wedNote: String,
        thu: Int, thuNote: String,
        fri: Int, friNote: String,
        weeklyNotes: String
    ) {
        val student = selectedStudent.value ?: return
        val ppi = currentPpi.value ?: return
        viewModelScope.launch {
            val id = weeklyId ?: ("WKL-" + UUID.randomUUID().toString().take(8).uppercase())
            val entry = WeeklyJournalEntity(
                weeklyId = id,
                schoolId = student.schoolId,
                ppiId = ppi.ppiId,
                studentId = student.studentId,
                academicYear = selectedYear.value,
                semester = selectedSemester.value,
                weekStart = weekStart,
                weekEnd = weekEnd,
                weekNo = weekNo,
                activity = activity,
                studentAbility = studentAbility,
                monScore = mon, monNote = monNote,
                tueScore = tue, tueNote = tueNote,
                wedScore = wed, wedNote = wedNote,
                thuScore = thu, thuNote = thuNote,
                friScore = fri, friNote = friNote,
                weeklyNotes = weeklyNotes
            )
            repository.saveWeeklyJournal(entry)
            triggerGasSync("WeeklyJournal") { it.syncWeeklyJournal(entry) }
            showToast("Penilaian pekanan berhasil disimpan.")
        }
    }

    // --- Progress Analysis ---
    fun generateProgressAnalysisWithAi() {
        val student = selectedStudent.value ?: return
        val journals = dailyJournals.value
        val avg = if (journals.isNotEmpty()) {
            val finalJournals = journals.filter { it.includeFinalReport }
            if (finalJournals.isNotEmpty()) finalJournals.map { it.rubricScore }.average().toInt() else 65
        } else 50

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val result = repository.generateProgressAnalysisWithAi(
                    studentName = student.name,
                    period = "${selectedYear.value} Semester ${selectedSemester.value}",
                    journalCount = journals.size,
                    averageScore = avg,
                    aspectsSummary = "Komunikasi, Kemandirian, Sosial Emosi"
                )

                val ppi = currentPpi.value
                val analysisId = currentAnalysis.value?.analysisId ?: ("ANL-" + UUID.randomUUID().toString().take(8).uppercase())

                val entity = ProgressAnalysisEntity(
                    analysisId = analysisId,
                    schoolId = student.schoolId,
                    ppiId = ppi?.ppiId ?: "",
                    studentId = student.studentId,
                    academicYear = selectedYear.value,
                    semester = selectedSemester.value,
                    summary = result.summary,
                    achievementTrend = result.achievementTrend,
                    developingStrengths = result.developingStrengths,
                    mainObstacles = result.mainObstacles,
                    keyAchievements = result.keyAchievements,
                    nextPriorities = result.nextPriorities,
                    recommendations = result.recommendations,
                    teamReflection = result.teamReflection,
                    status = "DRAFT"
                )

                repository.saveProgressAnalysis(entity)
                triggerGasSync("ProgressAnalysis") { it.syncProgressAnalysis(entity) }
                showToast("Analisis perkembangan berhasil disusun. Telaah dan validasi.")
            } catch (e: Exception) {
                showToast("Gagal menyusun analisis: ${e.message}")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun saveProgressAnalysis(
        summary: String,
        trend: String,
        strengths: List<String>,
        obstacles: List<String>,
        achievements: List<String>,
        priorities: List<String>,
        recommendations: List<String>,
        teamReflection: String,
        validate: Boolean
    ) {
        val student = selectedStudent.value ?: return
        val ppi = currentPpi.value ?: return
        viewModelScope.launch {
            val current = currentAnalysis.value
            val id = current?.analysisId ?: ("ANL-" + UUID.randomUUID().toString().take(8).uppercase())

            val entity = ProgressAnalysisEntity(
                analysisId = id,
                schoolId = student.schoolId,
                ppiId = ppi.ppiId,
                studentId = student.studentId,
                academicYear = selectedYear.value,
                semester = selectedSemester.value,
                summary = summary,
                achievementTrend = trend,
                developingStrengths = strengths,
                mainObstacles = obstacles,
                keyAchievements = achievements,
                nextPriorities = priorities,
                recommendations = recommendations,
                teamReflection = teamReflection,
                status = if (validate) "VALIDATED" else "DRAFT",
                validatedBy = if (validate) (currentUser.value?.userId ?: "") else "",
                validatedAt = if (validate) "Tervalidasi GPK" else ""
            )

            repository.saveProgressAnalysis(entity)
            triggerGasSync("ProgressAnalysis") { it.syncProgressAnalysis(entity) }
            showToast(if (validate) "Analisis Progress berhasil divalidasi oleh GPK." else "Analisis tersimpan sebagai draf.")
        }
    }

    // --- Reflection & Follow-up ---
    fun generateReflectionWithAi() {
        val student = selectedStudent.value ?: return
        val ppi = currentPpi.value ?: return
        val analysis = currentAnalysis.value

        viewModelScope.launch {
            _isAiLoading.value = true
            try {
                val (ref, fup) = repository.generateReflectionWithAi(
                    studentName = student.name,
                    period = "${selectedYear.value} Semester ${selectedSemester.value}",
                    analysisSummary = analysis?.summary ?: "Perkembangan positif dan konsisten.",
                    nextPriorities = analysis?.nextPriorities ?: listOf("Peningkatan kemandirian", "Sosialisasi")
                )

                val updated = ppi.copy(
                    reflection = ref,
                    followUp = fup,
                    reflectionStatus = "DRAFT",
                    updatedAt = System.currentTimeMillis()
                )
                repository.savePpi(updated)
                triggerGasSync("Reflection") { it.syncPpi(updated) }
                showToast("Refleksi dan tindak lanjut berhasil disusun.")
            } catch (e: Exception) {
                showToast("Gagal menyusun refleksi: ${e.message}")
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun saveReflection(reflection: String, followUp: String, validate: Boolean) {
        val ppi = currentPpi.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                reflection = reflection,
                followUp = followUp,
                reflectionStatus = if (validate) "VALIDATED" else "DRAFT",
                reflectionValidatedBy = if (validate) (currentUser.value?.name ?: "GPK") else "",
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("Reflection") { it.syncPpi(updated) }
            showToast(if (validate) "Refleksi & RTL divalidasi GPK. Siap diajukan ke Koordinator." else "Refleksi disimpan.")
        }
    }

    fun submitFinalReport() {
        val ppi = currentPpi.value ?: return
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                reportApprovalStatus = "DIAJUKAN",
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("FinalReportSubmitted") { it.syncPpi(updated) }

            repository.saveNotification(
                NotificationEntity(
                    notificationId = "NTF-" + UUID.randomUUID().toString().take(8),
                    schoolId = ppi.schoolId,
                    recipientRole = "KOORDINATOR INKLUSI",
                    title = "Laporan Akhir Semester Diajukan",
                    message = "GPK mengajukan Laporan Akhir Semester ${student.name} untuk diperiksa Koordinator Inklusi.",
                    type = "FINAL_REPORT_SUBMITTED",
                    ppiId = ppi.ppiId,
                    studentId = ppi.studentId,
                    studentName = student.name,
                    academicYear = ppi.academicYear,
                    semester = ppi.semester
                )
            )
            showToast("Laporan Akhir Semester diajukan ke Koordinator Inklusi.")
        }
    }

    fun approveFinalReportByCoordinator(note: String) {
        val ppi = currentPpi.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                reportApprovalStatus = "DISETUJUI_KOORDINATOR",
                coordinatorNote = note,
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("FinalReportApproved") { it.syncPpi(updated) }
            showToast("Refleksi & RTL disetujui Koordinator Inklusi. Siap dikirim ke WAKA.")
        }
    }

    fun sendFinalReportToWaka() {
        val ppi = currentPpi.value ?: return
        val student = selectedStudent.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                reportApprovalStatus = "DIKIRIM_WAKA",
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("FinalReportSentWaka") { it.syncPpi(updated) }

            repository.saveNotification(
                NotificationEntity(
                    notificationId = "NTF-" + UUID.randomUUID().toString().take(8),
                    schoolId = ppi.schoolId,
                    recipientRole = "WAKA",
                    title = "Laporan Akhir Semester Masuk ke WAKA",
                    message = "Koordinator Inklusi telah memverifikasi laporan akhir ${student.name}. Silakan finalisasi.",
                    type = "FINAL_REPORT_SENT_WAKA",
                    ppiId = ppi.ppiId,
                    studentId = ppi.studentId,
                    studentName = student.name,
                    academicYear = ppi.academicYear,
                    semester = ppi.semester
                )
            )
            showToast("Laporan akhir dikirim ke WAKA.")
        }
    }

    fun finalizeFinalReportByWaka() {
        val ppi = currentPpi.value ?: return
        viewModelScope.launch {
            val updated = ppi.copy(
                reportApprovalStatus = "FINAL",
                updatedAt = System.currentTimeMillis()
            )
            repository.savePpi(updated)
            triggerGasSync("FinalReportFinalized") { it.syncPpi(updated) }
            showToast("Laporan Akhir Semester difinalisasi WAKA dan siap dicetak serta disahkan Kepala Sekolah.")
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead("SCH-PAUDIT-IP")
            showToast("Semua To-Do ditandai selesai.")
        }
    }
}
