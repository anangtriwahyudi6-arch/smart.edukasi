package com.example.data.local

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        SchoolEntity::class,
        UserEntity::class,
        StudentEntity::class,
        AssessmentEntity::class,
        PpiEntity::class,
        DailyJournalEntity::class,
        WeeklyJournalEntity::class,
        ProgressEntity::class,
        ProgressAnalysisEntity::class,
        NotificationEntity::class,
        GpkAssignmentEntity::class,
        LicenseEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ppiDao(): PpiDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sistem_ppi_database"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.ppiDao())
                }
            }
        }
    }
}

suspend fun populateInitialData(dao: PpiDao) {
    val schoolId = "SCH-PAUDIT-IP"
    val defaultYear = "2026/2027"
    val defaultSem = "1"

    // 1. Default School
    dao.insertSchool(
        SchoolEntity(
            schoolId = schoolId,
            schoolCode = "PAUDIT-IP",
            name = "PAUDIT Insan Permata Malang",
            level = "PAUD",
            npsn = "20559950",
            primaryColor = "#008D3F",
            accentColor = "#F57C00",
            tagline = "Sekolah Menyenangkan Berkarakter Al-Qur'an",
            vision = "Menjadi lembaga PAUD yang kokoh dalam membentuk generasi muslim yang kreatif dan berkarakter Al-Qur'an.",
            address = "Jalan Akordion Utara No.3 Tunggulwulung, Lowokwaru, Malang",
            phone = "(0341) 490-887",
            mobile = "0819-9443-4343",
            njsit = "6.35.73.01.002",
            accreditation = "A",
            principalName = "Dra. Hj. Siti Aminah, M.Pd."
        )
    )

    // 2. Default Users
    dao.insertUsers(
        listOf(
            UserEntity(
                userId = "USR-SUPER",
                schoolId = "MASTER",
                name = "Super Administrator",
                email = "superadmin@ppi.sch.id",
                username = "superadmin",
                password = "password123",
                role = "SUPER ADMIN"
            ),
            UserEntity(
                userId = "USR-ADMIN",
                schoolId = schoolId,
                name = "Administrator Sekolah",
                email = "admin@insanpermata.sch.id",
                username = "admin",
                password = "password123",
                role = "ADMIN SEKOLAH"
            ),
            UserEntity(
                userId = "USR-WAKA",
                schoolId = schoolId,
                name = "Ustadz H. Ridwan, M.Pd.I",
                email = "waka@insanpermata.sch.id",
                username = "waka",
                password = "password123",
                role = "WAKA"
            ),
            UserEntity(
                userId = "USR-COORD",
                schoolId = schoolId,
                name = "Ustadzah Nurul Hidayati, S.Psi.",
                email = "koordinator@insanpermata.sch.id",
                username = "koordinator",
                password = "password123",
                role = "KOORDINATOR INKLUSI"
            ),
            UserEntity(
                userId = "USR-GPK1",
                schoolId = schoolId,
                name = "Ustadzah Aisyah Rahma, S.Pd.",
                email = "aisyah.gpk@insanpermata.sch.id",
                username = "gpk1",
                password = "password123",
                role = "GPK"
            ),
            UserEntity(
                userId = "USR-GPK2",
                schoolId = schoolId,
                name = "Ustadz Budi Santoso, S.Pd.",
                email = "budi.gpk@insanpermata.sch.id",
                username = "gpk2",
                password = "password123",
                role = "GPK"
            )
        )
    )

    // 3. Students
    val std1 = StudentEntity(
        studentId = "STD-001",
        schoolId = schoolId,
        name = "Muhammad Rayyan Al-Fatih",
        nis = "2026001",
        nisn = "3194829101",
        gender = "Laki-laki",
        birthPlace = "Malang",
        birthDate = "2020-04-15",
        unit = "TK A",
        className = "An-Nur",
        homeroomTeacher = "Ustadzah Fatimah, S.Pd.",
        fatherName = "Rendra Alamsyah",
        motherName = "Dina Safitri",
        parentPhone = "081234567890",
        address = "Perumahan Griya Permata No. 12, Lowokwaru, Malang",
        gpkUserId = "USR-GPK1",
        gpkName = "Ustadzah Aisyah Rahma, S.Pd.",
        educationHistory = "KB Insan Permata (1 tahun)",
        notes = "Fokus pada pengembangan komunikasi ekspresif dan kemandirian toilet training."
    )

    val std2 = StudentEntity(
        studentId = "STD-002",
        schoolId = schoolId,
        name = "Aqeela Dania Putri",
        nis = "2026002",
        nisn = "3195827392",
        gender = "Perempuan",
        birthPlace = "Surabaya",
        birthDate = "2019-11-20",
        unit = "TK B",
        className = "Bintang Al-Khawarizmi",
        homeroomTeacher = "Ustadzah Nurul, S.Pd.",
        fatherName = "Danang Wijaya",
        motherName = "Lestari",
        parentPhone = "081987654321",
        address = "Jl. Candi Mendut Baru No. 45, Malang",
        gpkUserId = "USR-GPK1",
        gpkName = "Ustadzah Aisyah Rahma, S.Pd.",
        educationHistory = "TK A Pelita Hati",
        notes = "Hambatan sensorik suara bising, potensi tinggi pada daya ingat visual dan hafalan doa."
    )

    val std3 = StudentEntity(
        studentId = "STD-003",
        schoolId = schoolId,
        name = "Zaidan Ibrahim",
        nis = "2026003",
        nisn = "3201839201",
        gender = "Laki-laki",
        birthPlace = "Malang",
        birthDate = "2021-02-10",
        unit = "KB",
        className = "Matahari",
        homeroomTeacher = "Ustadzah Rina, S.Pd.",
        fatherName = "Ibrahim Surya",
        motherName = "Annisa",
        parentPhone = "081345678912",
        address = "Jl. Soekarno Hatta No. 88, Malang",
        gpkUserId = "USR-GPK2",
        gpkName = "Ustadz Budi Santoso, S.Pd.",
        educationHistory = "-",
        notes = "Hambatan atensi dan konsentrasi (ADHD), menyukai aktivitas motorik kasar."
    )

    dao.insertStudents(listOf(std1, std2, std3))

    // 4. Initial Assessment for STD-001
    dao.insertAssessment(
        AssessmentEntity(
            assessmentId = "ASM-001",
            schoolId = schoolId,
            studentId = "STD-001",
            academicYear = defaultYear,
            semester = defaultSem,
            specialNeeds = listOf("Spektrum Autisme", "Hambatan Komunikasi / Bahasa"),
            professionalDiagnosis = "Diagnosa Psikolog Perkembangan RSUB: Mild Autism Spectrum Disorder dengan keterlambatan bicara (Speech Delay).",
            academic = "Mengenal warna dasar (merah, kuning, biru, hijau) dan angka 1-5 dengan bantuan kartu visual.",
            communication = "Menggunakan 1-2 kata tunggal (misal: 'mau', 'air', 'makan'). Kontak mata bertahan 3-5 detik saat dipanggil.",
            social = "Mulai mau duduk berdampingan dengan teman (parallel play), masih memerlukan arahan untuk berbagi mainan.",
            emotionalBehavior = "Kadang tantrum bila ada perubahan jadwal mendadak. Tenang dengan media sensori slime atau pasir kinetik.",
            independence = "Dapat memakai sepatu berperekat sendiri. Sedang tahap pembiasaan toilet training berkala setiap 2 jam.",
            motoric = "Motorik kasar baik (berlari, melompat). Motorik halus memegang sendok dan gunting aman masih perlu penguatan.",
            sensoric = "Sensitif terhadap tekstur lengket tertentu, namun sangat responsif terhadap musik dan ritme.",
            interestsPotential = "Tertarik pada balok angka, puzzle binatang, dan menyimak murottal juz 30.",
            strengths = "Daya ingat visual kuat, menyukai keteraturan urutan benda.",
            obstacles = "Rentang atensi mudah beralih, kemampuan ekspresif verbal belum konsisten.",
            baseline = "Mampu meniru 2 kata sederhana dan merespons instruksi 1 tahap dengan gesture.",
            priorities = "1. Peningkatan kosakata fungsional\n2. Kepatuhan instruksi 2 tahap\n3. Pembiasaan makan mandiri"
        )
    )

    // 5. Initial PPI for STD-001
    val ppiJsonObjectives = """
        [
            {"bidang":"Komunikasi & Bahasa","tujuan":"Anak mampu mengucapkan kalimat 2-3 kata untuk meminta kebutuhan pokok","indikator":"Mengucapkan 'mau minum air' atau 'buka pintu' secara spontan tanpa prompted berlebih","target_waktu":"12 Pekan"},
            {"bidang":"Kemandirian","tujuan":"Anak mampu makan dan minum sendiri dengan rapi menggunakan sendok","indikator":"Menghabiskan snack tanpa tumpah berlebih dalam waktu 15 menit","target_waktu":"8 Pekan"},
            {"bidang":"Sosial & Emosi","tujuan":"Anak mampu duduk tenang dalam lingkaran (circle time) selama 10 menit","indikator":"Duduk mengikuti nyanyian pagi minimal 10 menit tanpa meninggalkan karpet","target_waktu":"16 Pekan"}
        ]
    """.trimIndent()

    val ppiJsonPlanningMatrix = """
        [
            {
                "no":1,
                "aspek":"Komunikasi & Bahasa",
                "jumlah_intervensi":3,
                "fokus":"Peningkatan kemampuan verbal fungsional",
                "kondisi_saat_ini":"Anak baru mengucapkan 1 kata tunggal dan menunjuk bila menginginkan sesuatu.",
                "dampak_kondisi":"Sering frustrasi atau menarik tangan guru saat permintaannya belum dipahami.",
                "kebutuhan":[
                    "Latihan meniru vokal dan suku kata bermakna dengan media kartu gambar",
                    "Penerapan komunikasi augmentatif (PECS / Picture Exchange) saat snack time",
                    "Pembiasaan mengucapkan kata sapaan dan terima kasih secara terstruktur"
                ]
            },
            {
                "no":2,
                "aspek":"Kemandirian & Bina Diri",
                "jumlah_intervensi":2,
                "fokus":"Toilet training dan makan mandiri",
                "kondisi_saat_ini":"Makan masih sering disuap dan perlu diingatkan ke toilet setiap 2 jam.",
                "dampak_kondisi":"Ketergantungan tinggi pada guru pendamping selama jam sekolah.",
                "kebutuhan":[
                    "Jadwal rutin ke toilet (toilet schedule) visual bergambar",
                    "Latihan memegang sendok dan memotong makanan lunak"
                ]
            }
        ]
    """.trimIndent()

    val ppiJsonTimeSchedule = """
        [
            {
                "schedule_id":"SCH-1-KOM",
                "kategori":"Program Khusus",
                "bidang":"Komunikasi & Bahasa",
                "tujuan":"Mampu mengucapkan kalimat 2-3 kata",
                "periode":"Mingguan",
                "durasi":"1 Semester",
                "tahapan":[
                    {"stage_id":"TS-1-T1","urutan":1,"kegiatan":"Meniru bunyi vokal & konsonan dengan kartu gambar","bobot_persen":25,"frekuensi":"Setiap Hari","indikator_keberhasilan":"Meniru 5 suara hewan/benda dengan artikulasi jelas"},
                    {"stage_id":"TS-1-T2","urutan":2,"kegiatan":"Menggabungkan 2 kata (Subjek + Kata Kerja)","bobot_persen":25,"frekuensi":"Setiap Hari","indikator_keberhasilan":"Mengucapkan 'Rayyan makan' / 'Mau pipis' saat dibutuhkan"},
                    {"stage_id":"TS-1-T3","urutan":3,"kegiatan":"Menggunakan kartu PECS untuk menyusun 3 kata","bobot_persen":25,"frekuensi":"3x Sepekan","indikator_keberhasilan":"Menempelkan kartu 'Saya mau roti' ke papan komunikasi"},
                    {"stage_id":"TS-1-T4","urutan":4,"kegiatan":"Verbalisasi spontan dalam interaksi kelas","bobot_persen":25,"frekuensi":"Setiap Hari","indikator_keberhasilan":"Menjawab pertanyaan sederhana guru dengan 2 kata"}
                ]
            },
            {
                "schedule_id":"SCH-2-MAN",
                "kategori":"Program Khusus",
                "bidang":"Kemandirian",
                "tujuan":"Makan mandiri dan toilet training",
                "periode":"Mingguan",
                "durasi":"1 Semester",
                "tahapan":[
                    {"stage_id":"TS-2-T1","urutan":1,"kegiatan":"Makan snack dengan sendok tanpa bantuan fisik","bobot_persen":50,"frekuensi":"Setiap Hari","indikator_keberhasilan":"Menghabiskan makanan dengan bantuan verbal minimal"},
                    {"stage_id":"TS-2-T2","urutan":2,"kegiatan":"Merespons alarm toilet visual dan membuka celana mandiri","bobot_persen":50,"frekuensi":"Setiap 2 Jam","indikator_keberhasilan":"Berjalan ke toilet mandiri saat simbol toilet ditunjukkan"}
                ]
            }
        ]
    """.trimIndent()

    val ppiJsonCategories = """
        {
            "akademik":[
                {"nama":"Literasi Visual & Fonik","target":"Mengenal huruf A-I dengan kartu sentuh","indikator":"Menunjuk huruf yang benar 4 dari 5 percobaan","strategi":"Permainan tebak kartu bermagnet"},
                {"nama":"Numerasi Konkret","target":"Membilang benda 1-5","indikator":"Mengelompokkan balok sesuai angka yang ditunjukkan","strategi":"Menghitung buah mainan dan memasukkan ke keranjang"}
            ],
            "program_khusus":[
                {"nama":"Bina Diri & Komunikasi Fungsional","target":"Mengungkapkan rasa lapar/haus","indikator":"Menggunakan kata atau gesture terarah","strategi":"Modeling dan prompting bertahap"}
            ],
            "program_spiritual":[
                {"nama":"Pembiasaan Doa Harian & Murottal","target":"Menirukan doa sebelum makan & surat Al-Fatihah","indikator":"Mengangkat tangan saat berdoa dan menirukan lafadz akhir ayat","strategi":"Mendengarkan murottal bersama di awal pagi"}
            ]
        }
    """.trimIndent()

    dao.insertPpi(
        PpiEntity(
            ppiId = "PPI-001",
            schoolId = schoolId,
            studentId = "STD-001",
            academicYear = defaultYear,
            semester = defaultSem,
            status = "DRAFT",
            approvalStatus = "VALIDASI_GPK",
            aiModel = "gemini-3.5-flash",
            principalName = "Dra. Hj. Siti Aminah, M.Pd.",
            homeroomTeacher = "Ustadzah Aisyah Rahma, S.Pd.",
            coordinatorName = "Ustadzah Nurul Hidayati, S.Psi.",
            profile = "Rayyan adalah ananda berusia 6 tahun dengan Spektrum Autisme ringan. Memiliki semangat belajar tinggi, kemampuan memori visual yang sangat kuat, dan antusias dengan musik edukatif.",
            strengths = "Cepat menghafal visual warna, bentuk geometri, dan susunan huruf.",
            needs = "Pendampingan konsisten pada komunikasi dua arah, ekspresi verbal, dan pengelolaan emosi saat transisi kegiatan.",
            baseline = "Mengucapkan 1 kata tunggal dan kontak mata 3-5 detik.",
            priorities = listOf("Komunikasi verbal fungsional 2-3 kata", "Kemandirian bina diri saat snack time", "Kepatuhan aturan transisi kelas"),
            longTermGoal = "Ananda mampu berkomunikasi fungsional 2-3 kata, berpartisipasi aktif dalam kegiatan kelompok kecil, dan mandiri dalam aktivitas bina diri dasar di sekolah.",
            shortTermGoalsJson = ppiJsonObjectives,
            planningMatrixJson = ppiJsonPlanningMatrix,
            timeScheduleJson = ppiJsonTimeSchedule,
            programCategoriesJson = ppiJsonCategories,
            caseConferenceDate = "2026-08-20",
            caseConferenceTime = "09:00",
            caseConferenceLocation = "Ruang Konsultasi Inklusi PAUDIT",
            caseConferenceParticipants = "Ayah, Ibu, GPK (Ustzh Aisyah), Koordinator Inklusi (Ustzh Nurul)",
            caseConferenceScheduleNote = "Membahas target komunikasi PECS di rumah dan penyelarasan toilet training.",
            parentInput = "Orang tua sepakat menerapkan kartu komunikasi PECS yang seragam di rumah dan membatasi screen time 30 menit sehari.",
            caseConferenceResult = "Kesepakatan bersama: Target komunikasi 2 kata ditargetkan tercapai pada bulan ke-3. Orang tua mengirimkan laporan toilet training mingguan via buku penghubung.",
            reflection = "Rayyan menunjukkan perkembangan pesat pada pengenalan kartu visual kata. Tantrum berkurang signifikan setelah adanya papan jadwal bergambar (visual schedule).",
            followUp = "Melanjutkan ke tahap penggabungan 3 kata dan mengenalkan permainan kelompok dengan 2 orang teman.",
            reflectionStatus = "DRAFT",
            reportApprovalStatus = "DRAFT",
            createdBy = "USR-GPK1"
        )
    )

    // 6. Sample Daily Journals
    dao.insertDailyJournal(
        DailyJournalEntity(
            journalId = "JRN-001",
            schoolId = schoolId,
            ppiId = "PPI-001",
            studentId = "STD-001",
            academicYear = defaultYear,
            semester = defaultSem,
            date = "2026-08-22",
            source = "PPI",
            scheduleId = "SCH-1-KOM",
            stageId = "TS-1-T1",
            activity = "Latihan menirukan suara vokal dan kartu gambar hewan",
            studentAbility = "Mampu menirukan suara sapi ('moo') dan kucing ('meow') dengan jelas 4 dari 5 kali.",
            notes = "Ananda sangat ceria dan fokus saat menggunakan media flashcard bertekstur.",
            includeFinalReport = true,
            rubricScore = 75,
            evidence = "Menunjuk kartu kucing sambil mengucapkan 'meow' tanpa diarahkan tangan guru.",
            followup = "Lanjutkan ke suara hewan lain dan pengucapan nama benda pokok."
        )
    )

    dao.insertDailyJournal(
        DailyJournalEntity(
            journalId = "JRN-002",
            schoolId = schoolId,
            ppiId = "PPI-001",
            studentId = "STD-001",
            academicYear = defaultYear,
            semester = defaultSem,
            date = "2026-08-24",
            source = "PPI",
            scheduleId = "SCH-2-MAN",
            stageId = "TS-2-T1",
            activity = "Makan snack buah pisang menggunakan sendok mandiri",
            studentAbility = "Mampu memegang sendok sendiri dan menyuap 6 suapan tanpa bantuan tangan guru.",
            notes = "Masih ada sedikit tumpahan saat mengangkat sendok, namun koordinasi mata-tangan membaik.",
            includeFinalReport = true,
            rubricScore = 50,
            evidence = "Menghabiskan potongan buah dalam wadah snack sendiri.",
            followup = "Pertahankan latihan rutin saat jam istirahat pagi."
        )
    )

    // 7. Sample Notifications / To-Do Tasks
    dao.insertNotifications(
        listOf(
            NotificationEntity(
                notificationId = "NTF-001",
                schoolId = schoolId,
                recipientRole = "KOORDINATOR INKLUSI",
                recipientUserId = "USR-COORD",
                title = "Case Conference Menunggu Pemeriksaan",
                message = "GPK Ustadzah Aisyah telah memvalidasi hasil Case Conference untuk Muhammad Rayyan Al-Fatih. Periksa dan kirim ke WAKA.",
                type = "CASE_VALIDATED_GPK",
                ppiId = "PPI-001",
                studentId = "STD-001",
                studentName = "Muhammad Rayyan Al-Fatih"
            ),
            NotificationEntity(
                notificationId = "NTF-002",
                schoolId = schoolId,
                recipientRole = "GPK",
                recipientUserId = "USR-GPK1",
                title = "Jadwalkan Asesmen Semester Berjalan",
                message = "Silakan lengkapi asesmen awal dan draf PPI untuk Aqeela Dania Putri.",
                type = "PPI_SUBMITTED",
                ppiId = "",
                studentId = "STD-002",
                studentName = "Aqeela Dania Putri"
            )
        )
    )

    // 8. License
    dao.insertLicense(
        LicenseEntity(
            licenseId = "LIC-001",
            schoolId = schoolId,
            plan = "PROFESSIONAL",
            status = "AKTIF",
            startDate = "2026-07-01",
            endDate = "2027-06-30",
            aiMonthlyQuota = 500,
            studentLimit = 200,
            userLimit = 30,
            paymentStatus = "LUNAS"
        )
    )
}
