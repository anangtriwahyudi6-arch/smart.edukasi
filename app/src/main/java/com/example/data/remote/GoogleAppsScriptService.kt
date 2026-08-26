package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.local.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GoogleAppsScriptService(context: Context? = null) {

    companion object {
        const val DEFAULT_GAS_URL = "https://script.google.com/macros/s/AKfycbzKbN9GBBuUlw_EVESQIPU1SbdbKf7L-PxZxfIovVFb-QpycXfx6QcWFVVQIb4o1QI0/exec"
        private const val PREFS_NAME = "gas_sync_prefs"
        private const val KEY_GAS_URL = "gas_url"
        private const val KEY_AUTO_SYNC = "gas_auto_sync"
        private const val KEY_LAST_SYNC_TIME = "gas_last_sync_time"
        private const val KEY_LAST_SYNC_STATUS = "gas_last_sync_status"
    }

    private val prefs = context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var gasUrl: String
        get() = prefs?.getString(KEY_GAS_URL, DEFAULT_GAS_URL) ?: DEFAULT_GAS_URL
        set(value) {
            prefs?.edit()?.putString(KEY_GAS_URL, value.ifBlank { DEFAULT_GAS_URL })?.apply()
        }

    var isAutoSyncEnabled: Boolean
        get() = prefs?.getBoolean(KEY_AUTO_SYNC, true) ?: true
        set(value) {
            prefs?.edit()?.putBoolean(KEY_AUTO_SYNC, value)?.apply()
        }

    var lastSyncTime: Long
        get() = prefs?.getLong(KEY_LAST_SYNC_TIME, 0L) ?: 0L
        set(value) {
            prefs?.edit()?.putLong(KEY_LAST_SYNC_TIME, value)?.apply()
        }

    var lastSyncStatus: String
        get() = prefs?.getString(KEY_LAST_SYNC_STATUS, "Siap Terhubung") ?: "Siap Terhubung"
        set(value) {
            prefs?.edit()?.putString(KEY_LAST_SYNC_STATUS, value)?.apply()
        }

    private val okHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .retryOnConnectionFailure(true)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private suspend fun postPayload(payload: Map<String, Any?>): Result<String> = withContext(Dispatchers.IO) {
        val url = gasUrl.trim().ifBlank { DEFAULT_GAS_URL }
        try {
            val jsonAdapter = moshi.adapter(Map::class.java)
            val jsonString = jsonAdapter.toJson(payload)

            val requestBody = jsonString.toRequestBody(jsonMediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful || response.code in 200..399) {
                lastSyncTime = System.currentTimeMillis()
                lastSyncStatus = "Sinkronisasi Berhasil"
                Log.d("GAS_SYNC", "Sync success to $url: $responseBody")
                Result.success(responseBody.ifBlank { "OK" })
            } else {
                lastSyncStatus = "Gagal (${response.code})"
                Log.w("GAS_SYNC", "Sync HTTP error ${response.code}: $responseBody")
                Result.failure(Exception("HTTP Error ${response.code}: $responseBody"))
            }
        } catch (e: Exception) {
            lastSyncStatus = "Error: ${e.localizedMessage ?: "Jaringan bermasalah"}"
            Log.e("GAS_SYNC", "Sync exception to $url", e)
            Result.failure(e)
        }
    }

    suspend fun ping(): Result<String> {
        val payload = mapOf(
            "action" to "ping",
            "type" to "test_connection",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis()
        )
        return postPayload(payload)
    }

    suspend fun syncSchool(school: SchoolEntity): Result<String> {
        if (!isAutoSyncEnabled) return Result.success("Auto-sync off")
        val payload = mapOf(
            "action" to "save_school",
            "table" to "schools",
            "type" to "school",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf(
                "schoolId" to school.schoolId,
                "schoolCode" to school.schoolCode,
                "name" to school.name,
                "level" to school.level,
                "npsn" to school.npsn,
                "tagline" to school.tagline,
                "vision" to school.vision,
                "address" to school.address,
                "phone" to school.phone,
                "mobile" to school.mobile,
                "principalName" to school.principalName,
                "primaryColor" to school.primaryColor,
                "accentColor" to school.accentColor
            )
        )
        return postPayload(payload)
    }

    suspend fun syncStudent(student: StudentEntity, isDelete: Boolean = false): Result<String> {
        if (!isAutoSyncEnabled) return Result.success("Auto-sync off")
        val payload = mapOf(
            "action" to if (isDelete) "delete_student" else "save_student",
            "table" to "students",
            "type" to "student",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf(
                "studentId" to student.studentId,
                "schoolId" to student.schoolId,
                "name" to student.name,
                "nis" to student.nis,
                "nisn" to student.nisn,
                "gender" to student.gender,
                "birthPlace" to student.birthPlace,
                "birthDate" to student.birthDate,
                "unit" to student.unit,
                "className" to student.className,
                "homeroomTeacher" to student.homeroomTeacher,
                "fatherName" to student.fatherName,
                "motherName" to student.motherName,
                "parentPhone" to student.parentPhone,
                "address" to student.address,
                "gpkUserId" to student.gpkUserId,
                "gpkName" to student.gpkName,
                "educationHistory" to student.educationHistory,
                "notes" to student.notes
            )
        )
        return postPayload(payload)
    }

    suspend fun syncAssessment(assessment: AssessmentEntity): Result<String> {
        if (!isAutoSyncEnabled) return Result.success("Auto-sync off")
        val payload = mapOf(
            "action" to "save_assessment",
            "table" to "assessments",
            "type" to "assessment",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf(
                "assessmentId" to assessment.assessmentId,
                "schoolId" to assessment.schoolId,
                "studentId" to assessment.studentId,
                "academicYear" to assessment.academicYear,
                "semester" to assessment.semester,
                "specialNeeds" to assessment.specialNeeds.joinToString(", "),
                "professionalDiagnosis" to assessment.professionalDiagnosis,
                "academic" to assessment.academic,
                "communication" to assessment.communication,
                "social" to assessment.social,
                "emotionalBehavior" to assessment.emotionalBehavior,
                "independence" to assessment.independence,
                "motoric" to assessment.motoric,
                "sensoric" to assessment.sensoric,
                "interestsPotential" to assessment.interestsPotential,
                "strengths" to assessment.strengths,
                "obstacles" to assessment.obstacles,
                "baseline" to assessment.baseline,
                "priorities" to assessment.priorities,
                "updatedAt" to assessment.updatedAt
            )
        )
        return postPayload(payload)
    }

    suspend fun syncPpi(ppi: PpiEntity): Result<String> {
        if (!isAutoSyncEnabled) return Result.success("Auto-sync off")
        val payload = mapOf(
            "action" to "save_ppi",
            "table" to "ppis",
            "type" to "ppi",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf(
                "ppiId" to ppi.ppiId,
                "schoolId" to ppi.schoolId,
                "studentId" to ppi.studentId,
                "academicYear" to ppi.academicYear,
                "semester" to ppi.semester,
                "status" to ppi.status,
                "approvalStatus" to ppi.approvalStatus,
                "aiModel" to ppi.aiModel,
                "principalName" to ppi.principalName,
                "homeroomTeacher" to ppi.homeroomTeacher,
                "coordinatorName" to ppi.coordinatorName,
                "profile" to ppi.profile,
                "strengths" to ppi.strengths,
                "needs" to ppi.needs,
                "baseline" to ppi.baseline,
                "priorities" to ppi.priorities.joinToString("; "),
                "longTermGoal" to ppi.longTermGoal,
                "shortTermGoals" to ppi.shortTermGoalsJson,
                "planningMatrix" to ppi.planningMatrixJson,
                "timeSchedule" to ppi.timeScheduleJson,
                "categories" to ppi.programCategoriesJson,
                "accommodation" to ppi.accommodation.joinToString("; "),
                "monitoringPlan" to ppi.monitoringPlan,
                "collaboration" to ppi.collaboration,
                "caseConferenceDate" to ppi.caseConferenceDate,
                "caseConferenceResult" to ppi.caseConferenceResult,
                "parentInput" to ppi.parentInput,
                "reflection" to ppi.reflection,
                "followUp" to ppi.followUp,
                "reflectionStatus" to ppi.reflectionStatus,
                "reportApprovalStatus" to ppi.reportApprovalStatus,
                "coordinatorNote" to ppi.coordinatorNote,
                "createdBy" to ppi.createdBy,
                "updatedAt" to ppi.updatedAt
            )
        )
        return postPayload(payload)
    }

    suspend fun syncDailyJournal(journal: DailyJournalEntity, isDelete: Boolean = false): Result<String> {
        if (!isAutoSyncEnabled) return Result.success("Auto-sync off")
        val payload = mapOf(
            "action" to if (isDelete) "delete_daily_journal" else "save_daily_journal",
            "table" to "daily_journals",
            "type" to "daily_journal",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf(
                "journalId" to journal.journalId,
                "schoolId" to journal.schoolId,
                "ppiId" to journal.ppiId,
                "studentId" to journal.studentId,
                "academicYear" to journal.academicYear,
                "semester" to journal.semester,
                "date" to journal.date,
                "source" to journal.source,
                "scheduleId" to journal.scheduleId,
                "stageId" to journal.stageId,
                "activity" to journal.activity,
                "studentAbility" to journal.studentAbility,
                "notes" to journal.notes,
                "includeFinalReport" to journal.includeFinalReport,
                "rubricScore" to journal.rubricScore,
                "evidence" to journal.evidence,
                "followup" to journal.followup,
                "createdAt" to journal.createdAt
            )
        )
        return postPayload(payload)
    }

    suspend fun syncWeeklyJournal(journal: WeeklyJournalEntity, isDelete: Boolean = false): Result<String> {
        if (!isAutoSyncEnabled) return Result.success("Auto-sync off")
        val payload = mapOf(
            "action" to if (isDelete) "delete_weekly_journal" else "save_weekly_journal",
            "table" to "weekly_journals",
            "type" to "weekly_journal",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf(
                "weeklyId" to journal.weeklyId,
                "schoolId" to journal.schoolId,
                "ppiId" to journal.ppiId,
                "studentId" to journal.studentId,
                "academicYear" to journal.academicYear,
                "semester" to journal.semester,
                "weekStart" to journal.weekStart,
                "weekEnd" to journal.weekEnd,
                "weekNo" to journal.weekNo,
                "activity" to journal.activity,
                "studentAbility" to journal.studentAbility,
                "monScore" to journal.monScore,
                "tueScore" to journal.tueScore,
                "wedScore" to journal.wedScore,
                "thuScore" to journal.thuScore,
                "friScore" to journal.friScore,
                "weeklyNotes" to journal.weeklyNotes
            )
        )
        return postPayload(payload)
    }

    suspend fun syncProgressAnalysis(analysis: ProgressAnalysisEntity): Result<String> {
        if (!isAutoSyncEnabled) return Result.success("Auto-sync off")
        val payload = mapOf(
            "action" to "save_progress_analysis",
            "table" to "progress_analyses",
            "type" to "progress_analysis",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf(
                "analysisId" to analysis.analysisId,
                "schoolId" to analysis.schoolId,
                "ppiId" to analysis.ppiId,
                "studentId" to analysis.studentId,
                "academicYear" to analysis.academicYear,
                "semester" to analysis.semester,
                "summary" to analysis.summary,
                "achievementTrend" to analysis.achievementTrend,
                "developingStrengths" to analysis.developingStrengths.joinToString("; "),
                "mainObstacles" to analysis.mainObstacles.joinToString("; "),
                "keyAchievements" to analysis.keyAchievements.joinToString("; "),
                "nextPriorities" to analysis.nextPriorities.joinToString("; "),
                "recommendations" to analysis.recommendations.joinToString("; "),
                "teamReflection" to analysis.teamReflection,
                "status" to analysis.status,
                "validatedBy" to analysis.validatedBy,
                "validatedAt" to analysis.validatedAt
            )
        )
        return postPayload(payload)
    }

    suspend fun syncAllData(
        schools: List<SchoolEntity>,
        students: List<StudentEntity>,
        ppis: List<PpiEntity>,
        dailyJournals: List<DailyJournalEntity>,
        weeklyJournals: List<WeeklyJournalEntity>
    ): Result<String> {
        val payload = mapOf(
            "action" to "sync_all",
            "type" to "full_sync",
            "source" to "Android PPI App",
            "timestamp" to System.currentTimeMillis(),
            "data" to mapOf(
                "schoolsCount" to schools.size,
                "studentsCount" to students.size,
                "ppisCount" to ppis.size,
                "dailyJournalsCount" to dailyJournals.size,
                "weeklyJournalsCount" to weeklyJournals.size,
                "schools" to schools.map { s ->
                    mapOf(
                        "schoolId" to s.schoolId,
                        "name" to s.name,
                        "npsn" to s.npsn,
                        "level" to s.level,
                        "principal" to s.principalName
                    )
                },
                "students" to students.map { st ->
                    mapOf(
                        "studentId" to st.studentId,
                        "name" to st.name,
                        "nis" to st.nis,
                        "nisn" to st.nisn,
                        "unit" to st.unit,
                        "className" to st.className,
                        "gpkName" to st.gpkName,
                        "father" to st.fatherName,
                        "mother" to st.motherName,
                        "phone" to st.parentPhone
                    )
                },
                "ppis" to ppis.map { p ->
                    mapOf(
                        "ppiId" to p.ppiId,
                        "studentId" to p.studentId,
                        "academicYear" to p.academicYear,
                        "semester" to p.semester,
                        "status" to p.status,
                        "approvalStatus" to p.approvalStatus,
                        "longTermGoal" to p.longTermGoal
                    )
                },
                "dailyJournals" to dailyJournals.map { j ->
                    mapOf(
                        "journalId" to j.journalId,
                        "studentId" to j.studentId,
                        "date" to j.date,
                        "activity" to j.activity,
                        "ability" to j.studentAbility,
                        "score" to j.rubricScore
                    )
                }
            )
        )
        return postPayload(payload)
    }
}
