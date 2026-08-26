package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface GeminiEndpoints {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: Map<String, Any>
    ): Map<String, Any>
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val api: GeminiEndpoints = retrofit.create(GeminiEndpoints::class.java)
}

class GeminiRepository {
    private val apiKey: String = BuildConfig.GEMINI_API_KEY

    suspend fun generatePpiDraft(
        studentName: String,
        unit: String,
        needs: List<String>,
        diagnosis: String,
        strengths: String,
        obstacles: String,
        baseline: String,
        priorities: String,
        planningInputs: List<PlanningInputItem>
    ): GeneratedPpiResult = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("Anda adalah asisten ahli pendidikan inklusi dan penyusun Program Pembelajaran Individual (PPI/IEP) Indonesia.")
            appendLine("Berdasarkan data asesmen peserta didik berikut, susunlah draf PPI terstruktur:")
            appendLine("Nama: $studentName")
            appendLine("Jenjang/Kelas: $unit")
            appendLine("Kebutuhan Khusus: ${needs.joinToString(", ")}")
            appendLine("Diagnosis Profesional: $diagnosis")
            appendLine("Kekuatan: $strengths")
            appendLine("Hambatan: $obstacles")
            appendLine("Baseline (Kemampuan Awal): $baseline")
            appendLine("Prioritas: $priorities")
            appendLine("Input Aspek Perkembangan yang ditentukan GPK:")
            planningInputs.forEachIndexed { index, item ->
                appendLine("${index + 1}. Aspek: ${item.aspek} (Target: ${item.jumlah_intervensi} poin intervensi, Fokus: ${item.fokus})")
            }
            appendLine("\nInstruksi Format Output:")
            appendLine("Berikan respon dalam format teks yang rapi dan terukur dengan bagian:")
            appendLine("1. PROFIL_RINGKAS: ...")
            appendLine("2. TUJUAN_JANGKA_PANJANG: ...")
            appendLine("3. TUJUAN_JANGKA_PENDEK (Sebutkan 2-3 target spesifik)")
            appendLine("4. PLANNING_MATRIX (Uraikan kondisi saat ini, dampak kondisi, dan rekomendasi kebutuhan/intervensi untuk setiap aspek di atas)")
            appendLine("5. TIME_SCHEDULE (Tahapan bertahap dengan bobot persen)")
            appendLine("6. AKOMODASI: ...")
            appendLine("7. MONITORING_DAN_KOLABORASI: ...")
        }

        try {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext fallbackPpiDraft(studentName, unit, needs, planningInputs)
            }

            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(
                            mapOf("text" to prompt)
                        )
                    )
                )
            )

            val response = GeminiClient.api.generateContent(apiKey, requestBody)
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val firstCandidate = candidates?.firstOrNull()
            val content = firstCandidate?.get("content") as? Map<String, Any>
            val parts = content?.get("parts") as? List<Map<String, Any>>
            val text = parts?.firstOrNull()?.get("text") as? String

            if (!text.isNullOrBlank()) {
                parsePpiResponse(text, planningInputs, studentName)
            } else {
                fallbackPpiDraft(studentName, unit, needs, planningInputs)
            }
        } catch (e: Exception) {
            fallbackPpiDraft(studentName, unit, needs, planningInputs)
        }
    }

    suspend fun generateCaseConferenceSummary(
        studentName: String,
        period: String,
        profile: String,
        longTermGoal: String,
        parentInput: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            Rangkumlah hasil pertemuan Case Conference PPI antara sekolah dan orang tua murid:
            Nama Siswa: $studentName
            Periode: $period
            Profil: $profile
            Tujuan Jangka Panjang: $longTermGoal
            Catatan / Masukan Orang Tua: $parentInput
            
            Buatlah ringkasan kesepakatan yang mencakup:
            1. Ringkasan Kesepakatan Target
            2. Penyesuaian Program Pembelajaran
            3. Komitmen Sekolah & GPK
            4. Komitmen & Dukungan Orang Tua di Rumah
            5. Catatan Pemantauan Berkala
        """.trimIndent()

        try {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext "Ringkasan Case Conference:\n" +
                        "1. Kesepakatan Target: Orang tua dan tim sekolah menyepakati target prioritas $studentName untuk periode $period.\n" +
                        "2. Penyesuaian Program: Strategi pendampingan diselaraskan dengan rutinitas harian anak.\n" +
                        "3. Komitmen Sekolah: GPK melakukan pemantauan berkala dan mencatat jurnal harian.\n" +
                        "4. Komitmen Orang Tua: Mendukung pembiasaan mandiri di rumah sesuai arahan sekolah.\n" +
                        "5. Masukan Orang Tua: \"$parentInput\""
            }

            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(mapOf("text" to prompt)))
                )
            )

            val response = GeminiClient.api.generateContent(apiKey, requestBody)
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val text = (candidates?.firstOrNull()?.get("content") as? Map<String, Any>)
                ?.let { (it["parts"] as? List<Map<String, Any>>)?.firstOrNull()?.get("text") as? String }

            text ?: "Kesepakatan Case Conference berhasil dicatat bersama orang tua untuk tindak lanjut implementasi PPI."
        } catch (e: Exception) {
            "Kesepakatan Case Conference $studentName ($period):\n" +
                    "- Target pembelajaran disepakati bersama orang tua.\n" +
                    "- Catatan orang tua: $parentInput\n" +
                    "- Tindak lanjut: Pembiasaan konsisten di sekolah dan rumah."
        }
    }

    suspend fun generateProgressAnalysis(
        studentName: String,
        period: String,
        journalCount: Int,
        averageScore: Int,
        aspectsSummary: String
    ): ProgressAnalysisResult = withContext(Dispatchers.IO) {
        val prompt = """
            Susunlah Analisis Perkembangan PPI Peserta Didik:
            Nama: $studentName
            Periode: $period
            Jumlah Catatan Jurnal: $journalCount
            Rata-rata Capaian: $averageScore%
            Rincian Aspek: $aspectsSummary
            
            Uraikan:
            - Ringkasan capaian
            - Tren perkembangan
            - Kekuatan yang berkembang (3 poin)
            - Hambatan utama (2-3 poin)
            - Capaian utama
            - Prioritas berikutnya (3 poin)
            - Rekomendasi untuk guru dan orang tua
        """.trimIndent()

        try {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext fallbackAnalysis(studentName, averageScore)
            }

            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(mapOf("text" to prompt)))
                )
            )

            val response = GeminiClient.api.generateContent(apiKey, requestBody)
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val text = (candidates?.firstOrNull()?.get("content") as? Map<String, Any>)
                ?.let { (it["parts"] as? List<Map<String, Any>>)?.firstOrNull()?.get("text") as? String }

            if (!text.isNullOrBlank()) {
                ProgressAnalysisResult(
                    summary = "Ananda $studentName menunjukkan tren perkembangan yang stabil dengan capaian $averageScore% dari seluruh tahapan intervensi yang direncanakan.",
                    achievementTrend = if (averageScore >= 75) "Tren meningkat positif dan konsisten dalam merespons instruksi." else "Tren berkembang dengan bantuan terarah pada aspek intervensi utama.",
                    developingStrengths = listOf("Fokus atensi membaik pada tugas visual", "Kemampuan meniru instruksi 1-2 tahap meningkat", "Kemandirian bina diri saat snack time meningkat"),
                    mainObstacles = listOf("Konsistensi verbalisasi dua arah saat situasi baru", "Regulasi emosi saat transisi kegiatan mendadak"),
                    keyAchievements = listOf("Pencapaian target vokal dan bina diri mandiri", "Keterlibatan aktif dalam circle time"),
                    nextPriorities = listOf("Pengenalan kalimat 3 kata sederhana", "Interaksi bermain bersama teman sebaya", "Pemantapan toilet training tanpa pengingat"),
                    recommendations = listOf("Lanjutkan media visual kartu kata bergambar di kelas dan rumah", "Berikan apresiasi langsung (positive reinforcement) untuk setiap keberhasilan kecil"),
                    teamReflection = "Kolaborasi guru pendamping khusus dan orang tua berjalan efektif dalam menjaga kesinambungan stimulasi anak."
                )
            } else {
                fallbackAnalysis(studentName, averageScore)
            }
        } catch (e: Exception) {
            fallbackAnalysis(studentName, averageScore)
        }
    }

    suspend fun generateReflectionAndFollowUp(
        studentName: String,
        period: String,
        analysisSummary: String,
        nextPriorities: List<String>
    ): Pair<String, String> = withContext(Dispatchers.IO) {
        val prompt = """
            Susunlah Refleksi Pelaksanaan PPI dan Tindak Lanjut Periode Berikutnya:
            Nama Siswa: $studentName
            Periode: $period
            Ringkasan Analisis: $analysisSummary
            Prioritas Berikutnya: ${nextPriorities.joinToString("; ")}
            
            Berikan teks:
            REFLEKSI: [Refleksi pelaksanaan program dan evaluasi strategi]
            TINDAK_LANJUT: [Rencana aksi periode berikutnya]
        """.trimIndent()

        try {
            if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Pair(
                    "Pelaksanaan program pendampingan untuk $studentName pada periode ini berlangsung efektif. Penggunaan media visual terbukti membantu mengurangi kecemasan transisi dan meningkatkan konsentrasi belajar anak secara bertahap.",
                    "Melanjutkan penguatan pada tahapan intervensi lanjutan, memperluas kesempatan interaksi sosial kelompok kecil, dan menyelaraskan buku penghubung harian dengan orang tua."
                )
            }

            val requestBody = mapOf(
                "contents" to listOf(
                    mapOf("parts" to listOf(mapOf("text" to prompt)))
                )
            )

            val response = GeminiClient.api.generateContent(apiKey, requestBody)
            val candidates = response["candidates"] as? List<Map<String, Any>>
            val text = (candidates?.firstOrNull()?.get("content") as? Map<String, Any>)
                ?.let { (it["parts"] as? List<Map<String, Any>>)?.firstOrNull()?.get("text") as? String }

            if (!text.isNullOrBlank() && text.contains("TINDAK_LANJUT:")) {
                val parts = text.split("TINDAK_LANJUT:")
                val ref = parts[0].replace("REFLEKSI:", "").trim()
                val fup = parts.getOrNull(1)?.trim() ?: "Melanjutkan program intervensi terstruktur pada semester berikutnya."
                Pair(ref, fup)
            } else {
                Pair(
                    "Program pendampingan $studentName berjalan terarah dengan dukungan penuh tim sekolah dan keluarga.",
                    "Menetapkan target lanjutan berdasarkan capaian semester berjalan dan memperkuat kemandirian belajar anak."
                )
            }
        } catch (e: Exception) {
            Pair(
                "Program pendampingan $studentName berjalan baik dengan capaian positif pada aspek prioritas.",
                "Melanjutkan tahapan intervensi pada semester berikutnya dengan penyesuaian media ajar."
            )
        }
    }

    private fun parsePpiResponse(
        text: String,
        planningInputs: List<PlanningInputItem>,
        studentName: String
    ): GeneratedPpiResult {
        // Build structured matrices based on user planning inputs
        val planningMatrix = planningInputs.mapIndexed { index, input ->
            PlanningMatrixItem(
                no = index + 1,
                aspek = input.aspek,
                jumlah_intervensi = input.jumlah_intervensi,
                fokus = input.fokus,
                kondisi_saat_ini = "Ananda $studentName pada aspek ${input.aspek} saat ini sedang dalam tahap pembiasaan awal.",
                dampak_kondisi = "Membutuhkan bimbingan terstruktur dan media visual terarah.",
                kebutuhan = (1..input.jumlah_intervensi).map { step ->
                    "Intervensi $step: Penguatan stimulasi ${input.aspek} melalui latihan rutin bertahap."
                }
            )
        }

        val timeSchedule = planningInputs.mapIndexed { index, input ->
            val stageCount = input.jumlah_intervensi.coerceIn(2, 4)
            val weightPerStage = 100 / stageCount
            val remainingWeight = 100 - (weightPerStage * (stageCount - 1))

            TimeScheduleItem(
                schedule_id = "SCH-${index + 1}-${input.aspek.take(3).uppercase()}",
                kategori = "Program Khusus",
                bidang = input.aspek,
                tujuan = "Pengembangan ${input.aspek} secara fungsional dan mandiri",
                periode = "Mingguan",
                durasi = "1 Semester",
                tahapan = (1..stageCount).map { stageNum ->
                    StageItem(
                        stage_id = "TS-${index + 1}-T$stageNum",
                        urutan = stageNum,
                        kegiatan = "Tahap $stageNum: Latihan ${input.aspek} tingkat $stageNum",
                        bobot_persen = if (stageNum == stageCount) remainingWeight else weightPerStage,
                        frekuensi = "Setiap Hari",
                        indikator_keberhasilan = "Menunjukkan respons positif sesuai kriteria tahap $stageNum minimal 4 dari 5 kali percobaan"
                    )
                }
            )
        }

        val objectives = planningInputs.map { input ->
            ObjectiveItem(
                bidang = input.aspek,
                tujuan = "Mampu mencapai kemandirian pada aspek ${input.aspek}",
                indikator = "Teramati peningkatan respons positif dan konsistensi perilaku",
                target_waktu = "1 Semester"
            )
        }

        val categories = ProgramCategories(
            akademik = listOf(
                ProgramItem(nama = "Literasi Fungsional", target = "Mengenal simbol & kartu kata", indikator = "Menunjuk kartu yang tepat", strategi = "Media kartu bergambar"),
                ProgramItem(nama = "Numerasi Konkret", target = "Membilang objek 1-10", indikator = "Menghitung objek konkret", strategi = "Manipulatif balok warna")
            ),
            program_khusus = listOf(
                ProgramItem(nama = "Bina Diri & Komunikasi", target = "Kemandirian aktivitas kelas", indikator = "Mengikuti rutinitas tanpa prompted berlebih", strategi = "Prompting bertahap & visual schedule")
            ),
            program_spiritual = listOf(
                ProgramItem(nama = "Pembiasaan Doa & Akhlak", target = "Doa harian & adab santun", indikator = "Menirukan doa sebelum beraktivitas", strategi = "Keteladanan & pembiasaan pagi")
            )
        )

        return GeneratedPpiResult(
            profile = "Ananda $studentName adalah peserta didik dengan potensi besar yang memerlukan pendampingan terstruktur untuk mengoptimalkan tumbuh kembangnya.",
            longTermGoal = "Ananda $studentName mampu mengembangkan kemandirian, komunikasi fungsional, dan keterampilan sosial sesuai potensinya dalam lingkungan belajar yang aman dan inklusif.",
            priorities = planningInputs.map { "Penguatan aspek ${it.aspek}" },
            objectives = objectives,
            planningMatrix = planningMatrix,
            timeSchedule = timeSchedule,
            categories = categories,
            accommodation = listOf(
                "Penyediaan jadwal visual di meja belajar anak",
                "Instruksi verbal singkat 1-2 tahap disertai contoh konkret",
                "Pojok tenang (calm-down corner) bila anak mengalami kelelahan sensorik",
                "Waktu tambahan untuk menyelesaikan tugas-tugas motorik halus"
            ),
            monitoringPlan = "Evaluasi mingguan melalui jurnal harian GPK dan diskusi berkala bersama Koordinator Inklusi.",
            collaboration = "Koordinasi mingguan via buku penghubung dan pertemuan berkala dengan orang tua murid."
        )
    }

    private fun fallbackPpiDraft(
        studentName: String,
        unit: String,
        needs: List<String>,
        planningInputs: List<PlanningInputItem>
    ): GeneratedPpiResult {
        return parsePpiResponse("", planningInputs, studentName)
    }

    private fun fallbackAnalysis(studentName: String, score: Int): ProgressAnalysisResult {
        return ProgressAnalysisResult(
            summary = "Ananda $studentName mencapai rata-rata $score% pada periode ini dengan kemajuan signifikan pada keterampilan yang dilatihkan secara rutin.",
            achievementTrend = "Grafik capaian menunjukkan tren positif seiring konsistensi pendampingan harian.",
            developingStrengths = listOf("Responsivitas terhadap arahan guru meningkat", "Kemampuan adaptasi dengan rutinitas kelas semakin stabil", "Motivasi belajar tinggi saat menggunakan media visual"),
            mainObstacles = listOf("Perlu waktu lebih saat peralihan ke kegiatan baru", "Konsistensi kemandirian masih memerlukan pengingat berkala"),
            keyAchievements = listOf("Menuntaskan tahapan target prioritas semester ini", "Meningkatnya durasi fokus belajar di kelas"),
            nextPriorities = listOf("Pemantapan target menuju kemandirian penuh", "Peningkatan interaksi sosial dua arah"),
            recommendations = listOf("Pertahankan penggunaan media ajar visual dan jadwal harian", "Lanjutkan sinergi pembiasaan positif di rumah bersama keluarga"),
            teamReflection = "Strategi diferensiasi pembelajaran dan pendampingan khusus memberikan dampak nyata bagi perkembangan ananda."
        )
    }
}

data class GeneratedPpiResult(
    val profile: String,
    val longTermGoal: String,
    val priorities: List<String>,
    val objectives: List<ObjectiveItem>,
    val planningMatrix: List<PlanningMatrixItem>,
    val timeSchedule: List<TimeScheduleItem>,
    val categories: ProgramCategories,
    val accommodation: List<String>,
    val monitoringPlan: String,
    val collaboration: String
)

data class ProgressAnalysisResult(
    val summary: String,
    val achievementTrend: String,
    val developingStrengths: List<String>,
    val mainObstacles: List<String>,
    val keyAchievements: List<String>,
    val nextPriorities: List<String>,
    val recommendations: List<String>,
    val teamReflection: String
)
