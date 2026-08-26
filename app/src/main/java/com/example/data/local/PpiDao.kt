package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PpiDao {
    // Schools
    @Query("SELECT * FROM schools LIMIT 1")
    fun getActiveSchool(): Flow<SchoolEntity?>

    @Query("SELECT * FROM schools WHERE schoolId = :schoolId")
    suspend fun getSchoolById(schoolId: String): SchoolEntity?

    @Query("SELECT * FROM schools WHERE schoolCode = :code LIMIT 1")
    suspend fun getSchoolByCode(code: String): SchoolEntity?

    @Query("SELECT * FROM schools")
    fun getAllSchools(): Flow<List<SchoolEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchool(school: SchoolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchools(schools: List<SchoolEntity>)

    // Users
    @Query("SELECT * FROM users WHERE schoolId = :schoolId")
    fun getUsersBySchool(schoolId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE schoolId = :schoolId AND role = 'GPK' AND status = 'AKTIF'")
    fun getActiveGpkUsers(schoolId: String): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    // Students
    @Query("SELECT * FROM students WHERE schoolId = :schoolId ORDER BY name ASC")
    fun getStudentsBySchool(schoolId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE schoolId = :schoolId AND gpkUserId = :gpkUserId ORDER BY name ASC")
    fun getStudentsByGpk(schoolId: String, gpkUserId: String): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): StudentEntity?

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    fun observeStudentById(studentId: String): Flow<StudentEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentEntity>)

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudent(studentId: String)

    // Assessments
    @Query("SELECT * FROM assessments WHERE studentId = :studentId AND academicYear = :year AND semester = :semester LIMIT 1")
    fun getAssessment(studentId: String, year: String, semester: String): Flow<AssessmentEntity?>

    @Query("SELECT * FROM assessments WHERE studentId = :studentId AND academicYear = :year AND semester = :semester LIMIT 1")
    suspend fun getAssessmentSync(studentId: String, year: String, semester: String): AssessmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssessment(assessment: AssessmentEntity)

    // PPIs
    @Query("SELECT * FROM ppis WHERE studentId = :studentId AND academicYear = :year AND semester = :semester LIMIT 1")
    fun getPpi(studentId: String, year: String, semester: String): Flow<PpiEntity?>

    @Query("SELECT * FROM ppis WHERE studentId = :studentId AND academicYear = :year AND semester = :semester LIMIT 1")
    suspend fun getPpiSync(studentId: String, year: String, semester: String): PpiEntity?

    @Query("SELECT * FROM ppis WHERE ppiId = :ppiId LIMIT 1")
    suspend fun getPpiById(ppiId: String): PpiEntity?

    @Query("SELECT * FROM ppis WHERE studentId = :studentId ORDER BY academicYear DESC, semester DESC")
    fun getAllPpisForStudent(studentId: String): Flow<List<PpiEntity>>

    @Query("SELECT * FROM ppis WHERE schoolId = :schoolId AND academicYear = :year AND semester = :semester")
    fun getPpisForPeriod(schoolId: String, year: String, semester: String): Flow<List<PpiEntity>>

    @Query("SELECT * FROM ppis WHERE schoolId = :schoolId")
    fun getAllPpisForSchool(schoolId: String): Flow<List<PpiEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPpi(ppi: PpiEntity)

    @Query("DELETE FROM ppis WHERE ppiId = :ppiId")
    suspend fun deletePpi(ppiId: String)

    // Daily Journals
    @Query("SELECT * FROM daily_journals WHERE studentId = :studentId AND academicYear = :year AND semester = :semester ORDER BY date DESC, createdAt DESC")
    fun getDailyJournals(studentId: String, year: String, semester: String): Flow<List<DailyJournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyJournal(journal: DailyJournalEntity)

    @Query("DELETE FROM daily_journals WHERE journalId = :journalId")
    suspend fun deleteDailyJournal(journalId: String)

    // Weekly Journals
    @Query("SELECT * FROM weekly_journals WHERE studentId = :studentId AND academicYear = :year AND semester = :semester ORDER BY weekStart DESC, createdAt DESC")
    fun getWeeklyJournals(studentId: String, year: String, semester: String): Flow<List<WeeklyJournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeeklyJournal(journal: WeeklyJournalEntity)

    @Query("DELETE FROM weekly_journals WHERE weeklyId = :weeklyId")
    suspend fun deleteWeeklyJournal(weeklyId: String)

    // Progress
    @Query("SELECT * FROM progress_records WHERE studentId = :studentId AND academicYear = :year AND semester = :semester ORDER BY date DESC, createdAt DESC")
    fun getProgressRecords(studentId: String, year: String, semester: String): Flow<List<ProgressEntity>>

    @Query("SELECT * FROM progress_records WHERE schoolId = :schoolId AND academicYear = :year AND semester = :semester")
    fun getAllProgressForPeriod(schoolId: String, year: String, semester: String): Flow<List<ProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgress(progress: ProgressEntity)

    @Query("DELETE FROM progress_records WHERE progressId = :progressId")
    suspend fun deleteProgress(progressId: String)

    // Progress Analysis
    @Query("SELECT * FROM progress_analyses WHERE studentId = :studentId AND academicYear = :year AND semester = :semester LIMIT 1")
    fun getProgressAnalysis(studentId: String, year: String, semester: String): Flow<ProgressAnalysisEntity?>

    @Query("SELECT * FROM progress_analyses WHERE studentId = :studentId AND academicYear = :year AND semester = :semester LIMIT 1")
    suspend fun getProgressAnalysisSync(studentId: String, year: String, semester: String): ProgressAnalysisEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgressAnalysis(analysis: ProgressAnalysisEntity)

    // Notifications / To-Do
    @Query("SELECT * FROM notifications WHERE schoolId = :schoolId AND (recipientRole = :role OR recipientRole = 'ADMIN SEKOLAH' OR recipientUserId = :userId) ORDER BY createdAt DESC")
    fun getNotifications(schoolId: String, role: String, userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE schoolId = :schoolId AND (recipientRole = :role OR recipientRole = 'ADMIN SEKOLAH' OR recipientUserId = :userId) AND isRead = 0")
    fun getUnreadNotificationCount(schoolId: String, role: String, userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE schoolId = :schoolId")
    suspend fun markAllNotificationsAsRead(schoolId: String)

    // GPK Assignment History
    @Query("SELECT * FROM gpk_assignments WHERE studentId = :studentId ORDER BY startDate DESC")
    fun getGpkAssignments(studentId: String): Flow<List<GpkAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGpkAssignment(assignment: GpkAssignmentEntity)

    // Licenses
    @Query("SELECT * FROM licenses WHERE schoolId = :schoolId LIMIT 1")
    fun getLicense(schoolId: String): Flow<LicenseEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLicense(license: LicenseEntity)
}
