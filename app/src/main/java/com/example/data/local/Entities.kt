package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schools")
data class SchoolEntity(
    @PrimaryKey val schoolId: String,
    val schoolCode: String,
    val name: String,
    val level: String = "PAUD", // PAUD, SD, SMP, SMA, PKBM
    val npsn: String = "",
    val logoUrl: String = "",
    val primaryColor: String = "#008D3F",
    val accentColor: String = "#F57C00",
    val tagline: String = "Sekolah Menyenangkan Berkarakter Al-Qur'an",
    val vision: String = "Menjadi lembaga pendidikan inklusi berkualitas yang ramah anak dan berkarakter.",
    val address: String = "Jalan Akordion Utara No. 3, Malang",
    val phone: String = "(0341) 490-887",
    val mobile: String = "0819-9443-4343",
    val njsit: String = "6.35.73.01.002",
    val npsnKb: String = "69840171",
    val npsnTk: String = "20559950",
    val npsnTpa: String = "69909881",
    val accreditation: String = "A",
    val principalName: String = "Dra. Hj. Siti Aminah, M.Pd."
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val schoolId: String,
    val name: String,
    val email: String = "",
    val username: String,
    val password: String = "password123",
    val role: String, // SUPER ADMIN, ADMIN SEKOLAH, WAKA, KOORDINATOR INKLUSI, GPK
    val status: String = "AKTIF"
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey val studentId: String,
    val schoolId: String,
    val name: String,
    val nis: String = "",
    val nisn: String = "",
    val gender: String = "Laki-laki",
    val birthPlace: String = "Malang",
    val birthDate: String = "2019-05-12",
    val unit: String = "TK A", // e.g. KB, TK A, TK B, Kelas 1, etc.
    val className: String = "Al-Fatih",
    val homeroomTeacher: String = "Ustadzah Fatimah, S.Pd.",
    val fatherName: String = "Ahmad Pratama",
    val motherName: String = "Khadijah",
    val guardianName: String = "",
    val parentPhone: String = "081234567890",
    val address: String = "Jl. Terusan Danau Sentani, Malang",
    val gpkUserId: String = "",
    val gpkName: String = "",
    val educationHistory: String = "PAUD Permata Bunda",
    val notes: String = "Memerlukan pendampingan visual dan rutinitas terstruktur.",
    val photoUri: String = ""
)

@Entity(tableName = "assessments")
data class AssessmentEntity(
    @PrimaryKey val assessmentId: String,
    val schoolId: String,
    val studentId: String,
    val academicYear: String = "2026/2027",
    val semester: String = "1",
    val specialNeeds: List<String> = emptyList(), // e.g. Autisme, ADHD, dll
    val professionalDiagnosis: String = "",
    val academic: String = "",
    val communication: String = "",
    val social: String = "",
    val emotionalBehavior: String = "",
    val independence: String = "",
    val motoric: String = "",
    val sensoric: String = "",
    val interestsPotential: String = "",
    val strengths: String = "",
    val obstacles: String = "",
    val baseline: String = "",
    val priorities: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ppis")
data class PpiEntity(
    @PrimaryKey val ppiId: String,
    val schoolId: String,
    val studentId: String,
    val academicYear: String = "2026/2027",
    val semester: String = "1",
    val status: String = "DRAFT", // DRAFT, FINAL
    val approvalStatus: String = "DRAFT", // DRAFT, DIAJUKAN, DISETUJUI_KOORDINATOR, VALIDASI_GPK, DIKIRIM_WAKA, FINAL
    val aiModel: String = "gemini-3.5-flash",
    val principalName: String = "",
    val homeroomTeacher: String = "",
    val coordinatorName: String = "",
    val profile: String = "",
    val strengths: String = "",
    val needs: String = "",
    val baseline: String = "",
    val priorities: List<String> = emptyList(),
    val longTermGoal: String = "",
    val shortTermGoalsJson: String = "", // List<ObjectiveItem>
    val learningProgramJson: String = "", // List<ProgramItem>
    val planningInputJson: String = "", // List<PlanningInputItem>
    val planningMatrixJson: String = "", // List<PlanningMatrixItem>
    val programCategoriesJson: String = "", // ProgramCategories
    val transitionJson: String = "", // TransitionInfo
    val timeScheduleJson: String = "", // List<TimeScheduleItem>
    val printCity: String = "Malang",
    val ppiPrintDate: String = "2026-08-10",
    val reportPrintDate: String = "2026-12-18",
    val accommodation: List<String> = emptyList(),
    val monitoringPlan: String = "",
    val collaboration: String = "",
    val generalNotes: String = "",
    // Case Conference
    val caseConferenceDate: String = "",
    val caseConferenceTime: String = "",
    val caseConferenceLocation: String = "",
    val caseConferenceParticipants: String = "",
    val caseConferenceScheduleNote: String = "",
    val parentInput: String = "",
    val caseConferenceResult: String = "",
    // Reflection & Final Report
    val reflection: String = "",
    val followUp: String = "",
    val reflectionStatus: String = "DRAFT", // DRAFT, VALIDATED
    val reflectionValidatedBy: String = "",
    val reportApprovalStatus: String = "DRAFT", // DRAFT, DIAJUKAN, DISETUJUI_KOORDINATOR, PERLU_REVISI, DIKIRIM_WAKA, FINAL
    val coordinatorNote: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "daily_journals")
data class DailyJournalEntity(
    @PrimaryKey val journalId: String,
    val schoolId: String,
    val ppiId: String,
    val studentId: String,
    val academicYear: String,
    val semester: String,
    val date: String, // yyyy-MM-dd
    val source: String = "PPI", // PPI, MANUAL
    val scheduleId: String = "",
    val stageId: String = "",
    val activity: String = "",
    val studentAbility: String = "",
    val notes: String = "",
    val includeFinalReport: Boolean = true,
    val rubricScore: Int = 50, // 25, 50, 75, 100
    val evidence: String = "",
    val followup: String = "",
    val photoUri: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "weekly_journals")
data class WeeklyJournalEntity(
    @PrimaryKey val weeklyId: String,
    val schoolId: String,
    val ppiId: String,
    val studentId: String,
    val academicYear: String,
    val semester: String,
    val weekStart: String, // yyyy-MM-dd (Monday)
    val weekEnd: String, // yyyy-MM-dd (Friday)
    val weekNo: Int = 1,
    val activity: String,
    val studentAbility: String,
    val monScore: Int = 0,
    val monNote: String = "",
    val tueScore: Int = 0,
    val tueNote: String = "",
    val wedScore: Int = 0,
    val wedNote: String = "",
    val thuScore: Int = 0,
    val thuNote: String = "",
    val friScore: Int = 0,
    val friNote: String = "",
    val weeklyNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "progress_records")
data class ProgressEntity(
    @PrimaryKey val progressId: String,
    val schoolId: String,
    val ppiId: String,
    val studentId: String,
    val academicYear: String,
    val semester: String,
    val date: String,
    val domain: String,
    val target: String,
    val scheduleId: String = "",
    val stageId: String = "",
    val stageActivity: String = "",
    val indicator: String = "",
    val targetWeight: Int = 20,
    val achievementPercent: Int = 50,
    val achievementStatus: String = "Berkembang", // Berkembang, Hampir Tercapai, Tercapai
    val evidence: String = "",
    val photoUri: String = "",
    val notes: String = "",
    val followup: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "progress_analyses")
data class ProgressAnalysisEntity(
    @PrimaryKey val analysisId: String,
    val schoolId: String,
    val ppiId: String,
    val studentId: String,
    val academicYear: String,
    val semester: String,
    val summary: String = "",
    val achievementTrend: String = "",
    val developingStrengths: List<String> = emptyList(),
    val mainObstacles: List<String> = emptyList(),
    val keyAchievements: List<String> = emptyList(),
    val nextPriorities: List<String> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val teamReflection: String = "",
    val status: String = "DRAFT", // DRAFT, VALIDATED
    val validatedBy: String = "",
    val validatedAt: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val schoolId: String,
    val recipientRole: String, // KOORDINATOR INKLUSI, WAKA, GPK, ADMIN SEKOLAH
    val recipientUserId: String = "",
    val title: String,
    val message: String,
    val type: String, // PPI_SUBMITTED, CASE_VALIDATED_GPK, PPI_SENT_WAKA, FINAL_REPORT_SUBMITTED, etc.
    val ppiId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val academicYear: String = "2026/2027",
    val semester: String = "1",
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gpk_assignments")
data class GpkAssignmentEntity(
    @PrimaryKey val assignmentId: String,
    val schoolId: String,
    val studentId: String,
    val gpkUserId: String,
    val gpkName: String,
    val startDate: String,
    val endDate: String = "",
    val reason: String = "Penugasan awal",
    val unit: String = "",
    val className: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "licenses")
data class LicenseEntity(
    @PrimaryKey val licenseId: String,
    val schoolId: String,
    val plan: String = "PROFESSIONAL", // BASIC, PROFESSIONAL, ENTERPRISE
    val status: String = "AKTIF",
    val startDate: String = "2026-07-01",
    val endDate: String = "2027-06-30",
    val aiMonthlyQuota: Int = 500,
    val studentLimit: Int = 200,
    val userLimit: Int = 30,
    val paymentStatus: String = "LUNAS"
)
