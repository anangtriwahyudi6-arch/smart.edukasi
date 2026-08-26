package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.GeminiRepository
import com.example.data.remote.GeneratedPpiResult
import com.example.data.remote.GoogleAppsScriptService
import com.example.data.remote.ProgressAnalysisResult
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class PpiRepository(
    private val dao: PpiDao,
    private val geminiRepo: GeminiRepository = GeminiRepository(),
    val gasService: GoogleAppsScriptService = GoogleAppsScriptService()
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    // Moshi adapters for JSON columns
    private val objectivesAdapter = moshi.adapter<List<ObjectiveItem>>(
        Types.newParameterizedType(List::class.java, ObjectiveItem::class.java)
    )
    private val programsAdapter = moshi.adapter<List<ProgramItem>>(
        Types.newParameterizedType(List::class.java, ProgramItem::class.java)
    )
    private val planningInputsAdapter = moshi.adapter<List<PlanningInputItem>>(
        Types.newParameterizedType(List::class.java, PlanningInputItem::class.java)
    )
    private val planningMatrixAdapter = moshi.adapter<List<PlanningMatrixItem>>(
        Types.newParameterizedType(List::class.java, PlanningMatrixItem::class.java)
    )
    private val timeScheduleAdapter = moshi.adapter<List<TimeScheduleItem>>(
        Types.newParameterizedType(List::class.java, TimeScheduleItem::class.java)
    )
    private val categoriesAdapter = moshi.adapter(ProgramCategories::class.java)
    private val transitionAdapter = moshi.adapter(TransitionInfo::class.java)

    // School
    fun getSchool(): Flow<SchoolEntity?> = dao.getActiveSchool()
    suspend fun updateSchool(school: SchoolEntity) = dao.insertSchool(school)
    suspend fun getSchoolByCode(code: String) = dao.getSchoolByCode(code)
    fun getAllSchools(): Flow<List<SchoolEntity>> = dao.getAllSchools()

    // Users
    fun getUsers(schoolId: String): Flow<List<UserEntity>> = dao.getUsersBySchool(schoolId)
    fun getActiveGpkUsers(schoolId: String): Flow<List<UserEntity>> = dao.getActiveGpkUsers(schoolId)
    suspend fun getUserByUsername(username: String) = dao.getUserByUsername(username)
    suspend fun saveUser(user: UserEntity) = dao.insertUser(user)
    suspend fun deleteUser(user: UserEntity) = dao.deleteUser(user)

    // Students
    fun getStudents(schoolId: String): Flow<List<StudentEntity>> = dao.getStudentsBySchool(schoolId)
    fun getStudentsByGpk(schoolId: String, gpkId: String): Flow<List<StudentEntity>> =
        dao.getStudentsByGpk(schoolId, gpkId)
    suspend fun getStudentById(id: String) = dao.getStudentById(id)
    fun observeStudentById(id: String): Flow<StudentEntity?> = dao.observeStudentById(id)
    suspend fun saveStudent(student: StudentEntity) = dao.insertStudent(student)
    suspend fun deleteStudent(studentId: String) = dao.deleteStudent(studentId)

    // Assessments
    fun getAssessment(studentId: String, year: String, semester: String): Flow<AssessmentEntity?> =
        dao.getAssessment(studentId, year, semester)
    suspend fun saveAssessment(assessment: AssessmentEntity) = dao.insertAssessment(assessment)

    // PPI
    fun getPpi(studentId: String, year: String, semester: String): Flow<PpiEntity?> =
        dao.getPpi(studentId, year, semester)
    suspend fun getPpiSync(studentId: String, year: String, semester: String) =
        dao.getPpiSync(studentId, year, semester)
    suspend fun getPpiById(ppiId: String) = dao.getPpiById(ppiId)
    fun getAllPpisForStudent(studentId: String): Flow<List<PpiEntity>> =
        dao.getAllPpisForStudent(studentId)
    fun getPpisForPeriod(schoolId: String, year: String, semester: String): Flow<List<PpiEntity>> =
        dao.getPpisForPeriod(schoolId, year, semester)
    suspend fun savePpi(ppi: PpiEntity) = dao.insertPpi(ppi)
    suspend fun deletePpi(ppiId: String) = dao.deletePpi(ppiId)

    // Journals
    fun getDailyJournals(studentId: String, year: String, semester: String): Flow<List<DailyJournalEntity>> =
        dao.getDailyJournals(studentId, year, semester)
    suspend fun saveDailyJournal(journal: DailyJournalEntity) {
        dao.insertDailyJournal(journal)
        if (journal.includeFinalReport) {
            // Also mirror as progress entry
            val progress = ProgressEntity(
                progressId = "PRG-" + UUID.randomUUID().toString().take(8),
                schoolId = journal.schoolId,
                ppiId = journal.ppiId,
                studentId = journal.studentId,
                academicYear = journal.academicYear,
                semester = journal.semester,
                date = journal.date,
                domain = journal.activity,
                target = journal.studentAbility,
                scheduleId = journal.scheduleId,
                stageId = journal.stageId,
                stageActivity = journal.activity,
                indicator = journal.studentAbility,
                achievementPercent = journal.rubricScore,
                achievementStatus = when {
                    journal.rubricScore >= 80 -> "Tercapai"
                    journal.rubricScore >= 60 -> "Hampir Tercapai"
                    journal.rubricScore >= 30 -> "Berkembang"
                    else -> "Perlu Dukungan"
                },
                evidence = journal.evidence,
                photoUri = journal.photoUri,
                notes = journal.notes,
                followup = journal.followup
            )
            dao.insertProgress(progress)
        }
    }
    suspend fun deleteDailyJournal(journalId: String) = dao.deleteDailyJournal(journalId)

    fun getWeeklyJournals(studentId: String, year: String, semester: String): Flow<List<WeeklyJournalEntity>> =
        dao.getWeeklyJournals(studentId, year, semester)
    suspend fun saveWeeklyJournal(journal: WeeklyJournalEntity) = dao.insertWeeklyJournal(journal)
    suspend fun deleteWeeklyJournal(weeklyId: String) = dao.deleteWeeklyJournal(weeklyId)

    // Progress
    fun getProgressRecords(studentId: String, year: String, semester: String): Flow<List<ProgressEntity>> =
        dao.getProgressRecords(studentId, year, semester)
    fun getAllProgressForPeriod(schoolId: String, year: String, semester: String): Flow<List<ProgressEntity>> =
        dao.getAllProgressForPeriod(schoolId, year, semester)
    suspend fun saveProgress(progress: ProgressEntity) = dao.insertProgress(progress)
    suspend fun deleteProgress(progressId: String) = dao.deleteProgress(progressId)

    // Progress Analysis
    fun getProgressAnalysis(studentId: String, year: String, semester: String): Flow<ProgressAnalysisEntity?> =
        dao.getProgressAnalysis(studentId, year, semester)
    suspend fun saveProgressAnalysis(analysis: ProgressAnalysisEntity) = dao.insertProgressAnalysis(analysis)

    // Notifications / To-Do
    fun getNotifications(schoolId: String, role: String, userId: String): Flow<List<NotificationEntity>> =
        dao.getNotifications(schoolId, role, userId)
    fun getUnreadCount(schoolId: String, role: String, userId: String): Flow<Int> =
        dao.getUnreadNotificationCount(schoolId, role, userId)
    suspend fun saveNotification(notification: NotificationEntity) = dao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: String) = dao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead(schoolId: String) = dao.markAllNotificationsAsRead(schoolId)

    // GPK Handover / Assignment
    fun getGpkAssignments(studentId: String): Flow<List<GpkAssignmentEntity>> = dao.getGpkAssignments(studentId)
    suspend fun transferGpk(
        studentId: String,
        newGpkUserId: String,
        newGpkName: String,
        startDate: String,
        reason: String,
        newUnit: String = "",
        newClassName: String = ""
    ) {
        val student = dao.getStudentById(studentId) ?: return
        val updated = student.copy(
            gpkUserId = newGpkUserId,
            gpkName = newGpkName,
            unit = if (newUnit.isNotBlank()) newUnit else student.unit,
            className = if (newClassName.isNotBlank()) newClassName else student.className
        )
        dao.insertStudent(updated)

        dao.insertGpkAssignment(
            GpkAssignmentEntity(
                assignmentId = "GPKA-" + UUID.randomUUID().toString().take(8),
                schoolId = student.schoolId,
                studentId = studentId,
                gpkUserId = newGpkUserId,
                gpkName = newGpkName,
                startDate = startDate,
                reason = reason,
                unit = updated.unit,
                className = updated.className
            )
        )
    }

    // License
    fun getLicense(schoolId: String): Flow<LicenseEntity?> = dao.getLicense(schoolId)
    suspend fun saveLicense(license: LicenseEntity) = dao.insertLicense(license)

    // AI Generation Helpers
    suspend fun generatePpiDraftWithAi(
        student: StudentEntity,
        assessment: AssessmentEntity,
        planningInputs: List<PlanningInputItem>
    ): GeneratedPpiResult {
        return geminiRepo.generatePpiDraft(
            studentName = student.name,
            unit = student.unit + " " + student.className,
            needs = assessment.specialNeeds,
            diagnosis = assessment.professionalDiagnosis,
            strengths = assessment.strengths,
            obstacles = assessment.obstacles,
            baseline = assessment.baseline,
            priorities = assessment.priorities,
            planningInputs = planningInputs
        )
    }

    suspend fun generateCaseConferenceSummaryWithAi(
        studentName: String,
        period: String,
        profile: String,
        longTermGoal: String,
        parentInput: String
    ): String {
        return geminiRepo.generateCaseConferenceSummary(
            studentName, period, profile, longTermGoal, parentInput
        )
    }

    suspend fun generateProgressAnalysisWithAi(
        studentName: String,
        period: String,
        journalCount: Int,
        averageScore: Int,
        aspectsSummary: String
    ): ProgressAnalysisResult {
        return geminiRepo.generateProgressAnalysis(
            studentName, period, journalCount, averageScore, aspectsSummary
        )
    }

    suspend fun generateReflectionWithAi(
        studentName: String,
        period: String,
        analysisSummary: String,
        nextPriorities: List<String>
    ): Pair<String, String> {
        return geminiRepo.generateReflectionAndFollowUp(
            studentName, period, analysisSummary, nextPriorities
        )
    }

    // JSON adapters converters helpers
    fun encodeObjectives(list: List<ObjectiveItem>): String = objectivesAdapter.toJson(list)
    fun decodeObjectives(json: String): List<ObjectiveItem> = try {
        objectivesAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    fun encodePrograms(list: List<ProgramItem>): String = programsAdapter.toJson(list)
    fun decodePrograms(json: String): List<ProgramItem> = try {
        programsAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    fun encodePlanningInputs(list: List<PlanningInputItem>): String = planningInputsAdapter.toJson(list)
    fun decodePlanningInputs(json: String): List<PlanningInputItem> = try {
        planningInputsAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    fun encodePlanningMatrix(list: List<PlanningMatrixItem>): String = planningMatrixAdapter.toJson(list)
    fun decodePlanningMatrix(json: String): List<PlanningMatrixItem> = try {
        planningMatrixAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    fun encodeTimeSchedule(list: List<TimeScheduleItem>): String = timeScheduleAdapter.toJson(list)
    fun decodeTimeSchedule(json: String): List<TimeScheduleItem> = try {
        timeScheduleAdapter.fromJson(json) ?: emptyList()
    } catch (e: Exception) { emptyList() }

    fun encodeCategories(cat: ProgramCategories): String = categoriesAdapter.toJson(cat)
    fun decodeCategories(json: String): ProgramCategories = try {
        categoriesAdapter.fromJson(json) ?: ProgramCategories()
    } catch (e: Exception) { ProgramCategories() }

    fun encodeTransition(t: TransitionInfo): String = transitionAdapter.toJson(t)
    fun decodeTransition(json: String): TransitionInfo = try {
        transitionAdapter.fromJson(json) ?: TransitionInfo()
    } catch (e: Exception) { TransitionInfo() }
}
