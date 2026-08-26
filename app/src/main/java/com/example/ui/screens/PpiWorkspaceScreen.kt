package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PpiViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PpiWorkspaceScreen(
    viewModel: PpiViewModel,
    onOpenPrintPreview: () -> Unit
) {
    val students by viewModel.students.collectAsState()
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val currentAssessment by viewModel.currentAssessment.collectAsState()
    val currentPpi by viewModel.currentPpi.collectAsState()
    val dailyJournals by viewModel.dailyJournals.collectAsState()
    val currentAnalysis by viewModel.currentAnalysis.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(1) } // 0: Ringkasan, 1: PPI Periode, 2: Jurnal Harian, 3: Analisis, 4: Refleksi & Dokumen

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Student Selector Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Workspace PPI Peserta Didik",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PpiGreenDark
                        )
                        Text(
                            text = selectedStudent?.name ?: "Pilih Peserta Didik",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (currentPpi != null) {
                        StatusBadge(status = currentPpi!!.approvalStatus)
                    }
                }

                // Student horizontal picker chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(students) { std ->
                        val isSelected = std.studentId == selectedStudent?.studentId
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectStudent(std.studentId) },
                            label = { Text(std.name.take(16) + (if (std.name.length > 16) ".." else ""), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PpiGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Main Tab Row
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    contentColor = PpiGreen,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    listOf(
                        "1. Ringkasan",
                        "2. PPI Periode",
                        "3. Jurnal Harian",
                        "4. Analisis Progress",
                        "5. Refleksi & Cetak"
                    ).forEachIndexed { index, label ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    label,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        // Subtab Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> SummaryTab(
                    student = selectedStudent,
                    ppi = currentPpi,
                    journals = dailyJournals,
                    analysis = currentAnalysis
                )
                1 -> PpiPeriodTab(
                    viewModel = viewModel,
                    student = selectedStudent,
                    assessment = currentAssessment,
                    ppi = currentPpi,
                    currentUser = currentUser,
                    isAiLoading = isAiLoading
                )
                2 -> DailyJournalTab(
                    viewModel = viewModel,
                    student = selectedStudent,
                    ppi = currentPpi,
                    journals = dailyJournals
                )
                3 -> ProgressAnalysisTab(
                    viewModel = viewModel,
                    student = selectedStudent,
                    ppi = currentPpi,
                    analysis = currentAnalysis,
                    currentUser = currentUser,
                    isAiLoading = isAiLoading
                )
                4 -> ReflectionAndDocTab(
                    viewModel = viewModel,
                    student = selectedStudent,
                    ppi = currentPpi,
                    currentUser = currentUser,
                    isAiLoading = isAiLoading,
                    onOpenPrintPreview = onOpenPrintPreview
                )
            }
        }
    }
}

// ==========================================
// SUBTAB 0: RINGKASAN PERKEMBANGAN
// ==========================================
@Composable
fun SummaryTab(
    student: StudentEntity?,
    ppi: PpiEntity?,
    journals: List<DailyJournalEntity>,
    analysis: ProgressAnalysisEntity?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Profil Peserta Didik",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PpiGreenDark
                    )
                    Text(
                        text = ppi?.profile ?: "Data profil PPI belum diisi.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Tujuan Jangka Panjang:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = ppi?.longTermGoal ?: "-",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ringkasan Jurnal & Capaian Semester Ini",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${journals.size}", fontWeight = FontWeight.Black, fontSize = 22.sp, color = PpiGreen)
                            Text("Total Jurnal", fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val reportJournals = journals.count { it.includeFinalReport }
                            Text("$reportJournals", fontWeight = FontWeight.Black, fontSize = 22.sp, color = PpiOrange)
                            Text("Laporan Akhir", fontSize = 11.sp)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val avg = if (journals.isNotEmpty()) journals.map { it.rubricScore }.average().toInt() else 0
                            Text("$avg%", fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF1976D2))
                            Text("Rata-rata Skor", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        if (analysis != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Analisis Perkembangan Terkini",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            StatusBadge(status = analysis.status)
                        }

                        Text(
                            text = analysis.summary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tren Capaian: ${analysis.achievementTrend}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = PpiGreenDark
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SUBTAB 1: PPI PERIODE & WORKFLOW
// ==========================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PpiPeriodTab(
    viewModel: PpiViewModel,
    student: StudentEntity?,
    assessment: AssessmentEntity?,
    ppi: PpiEntity?,
    currentUser: UserEntity?,
    isAiLoading: Boolean
) {
    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pilih peserta didik terlebih dahulu")
        }
        return
    }

    var selectedSection by remember { mutableIntStateOf(0) }
    // 0: Tim & Asesmen, 1: Planning Matrix, 2: Program & Transisi, 3: Time Schedule, 4: Persetujuan & Case Conference

    // Local states for planning input
    var aspek1 by remember { mutableStateOf("Komunikasi & Bahasa") }
    var fokus1 by remember { mutableStateOf("Meningkatkan pengucapan kalimat 2 kata dan kontak mata") }
    var intervensi1 by remember { mutableIntStateOf(3) }

    var aspek2 by remember { mutableStateOf("Kemandirian & Bina Diri") }
    var fokus2 by remember { mutableStateOf("Makan snack mandiri dan respons toilet training") }
    var intervensi2 by remember { mutableIntStateOf(2) }

    // Workflow dialogs
    var showCaseConferenceDialog by remember { mutableStateOf(false) }
    var coordinatorNote by remember { mutableStateOf("") }
    var showRevisionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Navigation Pills
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedSection,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                listOf("1. Asesmen", "2. Matriks", "3. Program", "4. Jadwal", "5. Persetujuan").forEachIndexed { index, label ->
                    Tab(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        text = { Text(label, fontSize = 11.sp, fontWeight = if (selectedSection == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        when (selectedSection) {
            0 -> {
                // Section 1: Tim PPI & Asesmen 18 Kategori
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Identitas Tim PPI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PpiGreenDark
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Kepala Sekolah: ${ppi?.principalName?.ifBlank { "Dra. Hj. Siti Aminah, M.Pd." }}", style = MaterialTheme.typography.bodySmall)
                            Text("• Koordinator Inklusi: ${ppi?.coordinatorName?.ifBlank { "Ustadzah Nurul Hidayati, S.Psi." }}", style = MaterialTheme.typography.bodySmall)
                            Text("• GPK Pendamping: ${student.gpkName.ifBlank { "Ustadzah Aisyah Rahma, S.Pd." }}", style = MaterialTheme.typography.bodySmall)
                            Text("• Wali Murid: ${student.fatherName.ifBlank { "Orang Tua Siswa" }}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Hasil Asesmen Perkembangan Awal",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PpiGreenDark
                            )
                            Text(
                                text = "Kategori Kebutuhan Khusus: ${assessment?.specialNeeds?.joinToString(", ") ?: "Spektrum Autisme"}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Diagnosis: ${assessment?.professionalDiagnosis ?: "Autism Spectrum Disorder"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Kekuatan (Strengths): ${assessment?.strengths ?: "Daya ingat visual tinggi, menyukai keteraturan urutan"}", style = MaterialTheme.typography.bodySmall)
                            Text("Hambatan (Obstacles): ${assessment?.obstacles ?: "Rentang atensi beralih, komunikasi ekspresif belum konsisten"}", style = MaterialTheme.typography.bodySmall)
                            Text("Kemampuan Awal (Baseline): ${assessment?.baseline ?: "Meniru 1-2 kata dengan bantuan kartu"}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            1 -> {
                // Section 2: Planning Matrix & AI Drafting
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Planning Matrix PPI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PpiGreenDark
                                )
                                Button(
                                    onClick = {
                                        val inputs = listOf(
                                            PlanningInputItem(id = "1", aspek = aspek1, jumlah_intervensi = intervensi1, fokus = fokus1),
                                            PlanningInputItem(id = "2", aspek = aspek2, jumlah_intervensi = intervensi2, fokus = fokus2)
                                        )
                                        viewModel.generatePpiWithAi(inputs)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PpiOrange),
                                    shape = RoundedCornerShape(10.dp),
                                    enabled = !isAiLoading
                                ) {
                                    if (isAiLoading) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                                    } else {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("AI Draft Gemini", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Text(
                                text = "Tentukan aspek perkembangan dan jumlah intervensi untuk disusun oleh sistem / AI:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Planning inputs
                            OutlinedTextField(
                                value = aspek1,
                                onValueChange = { aspek1 = it },
                                label = { Text("Aspek Intervensi 1") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = fokus1,
                                onValueChange = { fokus1 = it },
                                label = { Text("Fokus Masalah / Target 1") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = aspek2,
                                onValueChange = { aspek2 = it },
                                label = { Text("Aspek Intervensi 2") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = fokus2,
                                onValueChange = { fokus2 = it },
                                label = { Text("Fokus Masalah / Target 2") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item {
                    val matrixItems = remember(ppi) {
                        if (ppi != null && ppi.planningMatrixJson.isNotBlank()) {
                            viewModel.repository.decodePlanningMatrix(ppi.planningMatrixJson)
                        } else emptyList()
                    }

                    if (matrixItems.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Draf Matriks Perencanaan Terstruktur",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                matrixItems.forEach { item ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = PpiGreenSoft.copy(alpha = 0.5f)
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "${item.no}. Aspek: ${item.aspek}",
                                                fontWeight = FontWeight.Bold,
                                                color = PpiGreenDark
                                            )
                                            Text("• Kondisi Saat Ini: ${item.kondisi_saat_ini}", style = MaterialTheme.typography.bodySmall)
                                            Text("• Dampak: ${item.dampak_kondisi}", style = MaterialTheme.typography.bodySmall)
                                            Text("• Rekomendasi Kebutuhan:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                                            item.kebutuhan.forEach { req ->
                                                Text("   - $req", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Section 3: Program & Transisi
                item {
                    val categories = remember(ppi) {
                        if (ppi != null && ppi.programCategoriesJson.isNotBlank()) {
                            viewModel.repository.decodeCategories(ppi.programCategoriesJson)
                        } else ProgramCategories()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Kategori Program PPI",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PpiGreenDark
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("1. Program Akademik / Pembelajaran", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            categories.akademik.forEach { item ->
                                Text("• ${item.nama}: ${item.target} (Strategi: ${item.strategi})", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("2. Program Khusus / Bina Diri", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            categories.program_khusus.forEach { item ->
                                Text("• ${item.nama}: ${item.target} (Strategi: ${item.strategi})", style = MaterialTheme.typography.bodySmall)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text("3. Program Pembiasaan Spiritual & Karakter", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            categories.program_spiritual.forEach { item ->
                                Text("• ${item.nama}: ${item.target} (Strategi: ${item.strategi})", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }

            3 -> {
                // Section 4: Time Schedule
                item {
                    val schedules = remember(ppi) {
                        if (ppi != null && ppi.timeScheduleJson.isNotBlank()) {
                            viewModel.repository.decodeTimeSchedule(ppi.timeScheduleJson)
                        } else emptyList()
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Time Schedule & Tahapan Intervensi",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PpiGreenDark
                            )
                            Text(
                                text = "Total bobot tahapan pada setiap bidang intervensi terstandarisasi 100%:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            schedules.forEach { sch ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = sch.bidang,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Tujuan: ${sch.tujuan}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        sch.tahapan.forEach { stg ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 2.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "Tahap ${stg.urutan}: ${stg.kegiatan}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = PpiGreenSoft
                                                ) {
                                                    Text(
                                                        text = "${stg.bobot_persen}%",
                                                        fontWeight = FontWeight.Bold,
                                                        color = PpiGreenDark,
                                                        fontSize = 11.sp,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Section 5: Review & Persetujuan Workflow
                item {
                    val currentStatus = ppi?.approvalStatus ?: "DRAFT"

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Workflow Persetujuan PPI",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PpiGreenDark
                                )
                                StatusBadge(status = currentStatus)
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Alur Kerja Resmi Sistem PPI:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "1. GPK menyusun & mengajukan draf PPI.\n" +
                                        "2. Koordinator Inklusi meninjau & menyetujui / memberi catatan revisi.\n" +
                                        "3. Case Conference bersama orang tua & validasi GPK.\n" +
                                        "4. Koordinator mengirim berkas ke WAKA.\n" +
                                        "5. WAKA memfinalisasi berkas PPI.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            if (ppi?.coordinatorNote?.isNotBlank() == true) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    color = PpiOrangeLight
                                ) {
                                    Text(
                                        text = "Catatan Koordinator Inklusi: ${ppi.coordinatorNote}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PpiOrangeDark,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action buttons according to current status & role
                            when (currentStatus) {
                                "DRAFT", "PERLU_REVISI" -> {
                                    Button(
                                        onClick = { viewModel.submitPpiToCoordinator() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Ajukan ke Koordinator Inklusi", fontWeight = FontWeight.Bold)
                                    }
                                }

                                "DIAJUKAN" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { showRevisionDialog = true },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Minta Revisi")
                                        }
                                        Button(
                                            onClick = { viewModel.approvePpiByCoordinator("Disetujui. Silakan laksanakan Case Conference.") },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Setujui Draf", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                "DISETUJUI_KOORDINATOR", "VALIDASI_GPK" -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            color = PpiGreenSoft
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text("Hasil Case Conference Terdaftar:", fontWeight = FontWeight.Bold, color = PpiGreenDark)
                                                Text("• Jadwal: ${ppi?.caseConferenceDate.orEmpty().ifBlank { "Belum diset" }} (${ppi?.caseConferenceTime.orEmpty().ifBlank { "-" }})", style = MaterialTheme.typography.bodySmall)
                                                Text("• Masukan Orang Tua: ${ppi?.parentInput.orEmpty().ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                                                Text("• Kesepakatan: ${ppi?.caseConferenceResult.orEmpty().ifBlank { "-" }}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedButton(
                                                onClick = { showCaseConferenceDialog = true },
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Isi Case Conference", fontSize = 11.sp)
                                            }

                                            if (currentStatus == "DISETUJUI_KOORDINATOR") {
                                                Button(
                                                    onClick = { viewModel.validateCaseConferenceByGpk() },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("Validasi GPK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            } else {
                                                Button(
                                                    onClick = { viewModel.sendPpiToWaka("Hasil Case Conference lengkap dan tervalidasi.") },
                                                    modifier = Modifier.weight(1f),
                                                    colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                                    shape = RoundedCornerShape(10.dp)
                                                ) {
                                                    Text("Kirim ke WAKA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                "DIKIRIM_WAKA" -> {
                                    Button(
                                        onClick = { viewModel.finalizePpiByWaka() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Finalisasi PPI oleh WAKA", fontWeight = FontWeight.Bold)
                                    }
                                }

                                "FINAL" -> {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(10.dp),
                                        color = PpiSuccessContainer
                                    ) {
                                        Text(
                                            text = "✓ Berkas PPI telah final dan disahkan. Siap untuk pencetakan dokumen.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = PpiSuccess,
                                            modifier = Modifier.padding(12.dp),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Case Conference Modal Dialog
    if (showCaseConferenceDialog) {
        CaseConferenceDialog(
            student = student,
            ppi = ppi,
            onDismiss = { showCaseConferenceDialog = false },
            onSave = { date, time, location, participants, note, parentInput, result ->
                viewModel.saveCaseConference(date, time, location, participants, note, parentInput, result)
                showCaseConferenceDialog = false
            },
            onGenerateSummary = { parentInput ->
                viewModel.generateCaseConferenceSummary(parentInput)
            }
        )
    }

    // Revision Dialog
    if (showRevisionDialog) {
        AlertDialog(
            onDismissRequest = { showRevisionDialog = false },
            title = { Text("Kembalikan PPI untuk Revisi") },
            text = {
                OutlinedTextField(
                    value = coordinatorNote,
                    onValueChange = { coordinatorNote = it },
                    label = { Text("Catatan Perbaikan untuk GPK") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.returnPpiForRevision(coordinatorNote)
                        showRevisionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PpiError)
                ) {
                    Text("Kirim Catatan Revisi")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRevisionDialog = false }) { Text("Batal") }
            }
        )
    }
}

// Case Conference Dialog
@Composable
fun CaseConferenceDialog(
    student: StudentEntity,
    ppi: PpiEntity?,
    onDismiss: () -> Unit,
    onSave: (date: String, time: String, location: String, participants: String, note: String, parentInput: String, result: String) -> Unit,
    onGenerateSummary: (parentInput: String) -> Unit
) {
    var date by remember { mutableStateOf(ppi?.caseConferenceDate.orEmpty().ifBlank { "2026-08-25" }) }
    var time by remember { mutableStateOf(ppi?.caseConferenceTime.orEmpty().ifBlank { "09:00" }) }
    var location by remember { mutableStateOf(ppi?.caseConferenceLocation.orEmpty().ifBlank { "Ruang Konsultasi Inklusi PAUDIT" }) }
    var participants by remember { mutableStateOf(ppi?.caseConferenceParticipants.orEmpty().ifBlank { "Orang Tua, GPK, Koordinator Inklusi" }) }
    var parentInput by remember { mutableStateOf(ppi?.parentInput.orEmpty().ifBlank { "Orang tua mendukung kartu komunikasi PECS di rumah dan membatasi screen time." }) }
    var result by remember { mutableStateOf(ppi?.caseConferenceResult.orEmpty().ifBlank { "Target komunikasi 2 kata dan toilet training disepakati bersama." }) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Case Conference Bersama Orang Tua", fontWeight = FontWeight.Bold) },
        confirmButton = {
            Button(
                onClick = { onSave(date, time, location, participants, "", parentInput, result) },
                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen)
            ) {
                Text("Simpan Kesepakatan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Ananda: ${student.name}", fontWeight = FontWeight.Bold, color = PpiGreenDark)
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Tanggal") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = time,
                            onValueChange = { time = it },
                            label = { Text("Jam") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Tempat Pertemuan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = parentInput,
                        onValueChange = { parentInput = it },
                        label = { Text("Masukan / Harapan Orang Tua") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
                item {
                    Button(
                        onClick = { onGenerateSummary(parentInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = PpiOrange),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ringkas dengan AI", fontSize = 12.sp)
                    }
                }
                item {
                    OutlinedTextField(
                        value = result,
                        onValueChange = { result = it },
                        label = { Text("Hasil & Kesepakatan Bersama") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                }
            }
        }
    )
}

// ==========================================
// SUBTAB 2: JURNAL HARIAN & PEKANAN
// ==========================================
@Composable
fun DailyJournalTab(
    viewModel: PpiViewModel,
    student: StudentEntity?,
    ppi: PpiEntity?,
    journals: List<DailyJournalEntity>
) {
    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pilih peserta didik terlebih dahulu")
        }
        return
    }

    var showAddJournalDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddJournalDialog = true },
                containerColor = PpiGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_journal_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Jurnal Harian")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Jurnal Harian & Rekapitulasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${journals.size} catatan harian tersimpan",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (journals.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Belum ada catatan jurnal harian pada periode ini. Tekan tombol + di bawah untuk menambahkan.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(journals, key = { it.journalId }) { jrn ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Event,
                                        contentDescription = null,
                                        tint = PpiGreenDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = jrn.date,
                                        fontWeight = FontWeight.Bold,
                                        color = PpiGreenDark,
                                        fontSize = 13.sp
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (jrn.rubricScore >= 75) PpiSuccessContainer else PpiOrangeLight
                                ) {
                                    Text(
                                        text = "Skor: ${jrn.rubricScore}%",
                                        color = if (jrn.rubricScore >= 75) PpiSuccess else PpiOrangeDark,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = jrn.activity,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Kemampuan Anak: ${jrn.studentAbility}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            if (jrn.evidence.isNotBlank()) {
                                Text(
                                    text = "Bukti / Observasi: ${jrn.evidence}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (jrn.notes.isNotBlank()) {
                                Text(
                                    text = "Catatan: ${jrn.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (jrn.includeFinalReport) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = PpiGreenSoft
                                    ) {
                                        Text(
                                            text = "✓ Masuk Laporan Akhir",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PpiGreenDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                IconButton(
                                    onClick = { viewModel.deleteDailyJournal(jrn.journalId) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = PpiError, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddJournalDialog) {
        AddDailyJournalDialog(
            ppi = ppi,
            onDismiss = { showAddJournalDialog = false },
            onSave = { date, source, schId, stgId, activity, ability, notes, includeReport, score, evidence, followup, photo ->
                viewModel.saveDailyJournal(
                    journalId = null,
                    date = date,
                    source = source,
                    scheduleId = schId,
                    stageId = stgId,
                    activity = activity,
                    studentAbility = ability,
                    notes = notes,
                    includeFinalReport = includeReport,
                    rubricScore = score,
                    evidence = evidence,
                    followup = followup,
                    photoUri = photo
                )
                showAddJournalDialog = false
            }
        )
    }
}

@Composable
fun AddDailyJournalDialog(
    ppi: PpiEntity?,
    onDismiss: () -> Unit,
    onSave: (date: String, source: String, schId: String, stgId: String, activity: String, ability: String, notes: String, includeReport: Boolean, score: Int, evidence: String, followup: String, photo: String) -> Unit
) {
    var date by remember { mutableStateOf("2026-08-25") }
    var source by remember { mutableStateOf("PPI") }
    var activity by remember { mutableStateOf("Latihan menirukan 2 suku kata bergambar") }
    var ability by remember { mutableStateOf("Mampu mengucapkan kata 'mau' dan 'air' secara mandiri 4/5 kali.") }
    var notes by remember { mutableStateOf("Anak antusias dan responsif terhadap kartu visual.") }
    var evidence by remember { mutableStateOf("Menunjuk gelas sambil berucap 'mau air' saat jam istirahat.") }
    var followup by remember { mutableStateOf("Lanjutkan pengenalan nama buah dan mainan.") }
    var rubricScore by remember { mutableIntStateOf(75) }
    var includeFinalReport by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Jurnal Harian PPI", fontWeight = FontWeight.Bold) },
        confirmButton = {
            Button(
                onClick = {
                    if (activity.isNotBlank()) {
                        onSave(date, source, "", "", activity, ability, notes, includeFinalReport, rubricScore, evidence, followup, "")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen)
            ) {
                Text("Simpan Jurnal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 440.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Tanggal (yyyy-mm-dd)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = activity,
                        onValueChange = { activity = it },
                        label = { Text("Kegiatan / Intervensi *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = ability,
                        onValueChange = { ability = it },
                        label = { Text("Kemampuan / Respon Anak *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    RubricSelector(
                        selectedPercent = rubricScore,
                        onPercentSelected = { rubricScore = it }
                    )
                }
                item {
                    OutlinedTextField(
                        value = evidence,
                        onValueChange = { evidence = it },
                        label = { Text("Bukti Observasi / Indikator") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan GPK") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { includeFinalReport = !includeFinalReport }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeFinalReport,
                            onCheckedChange = { includeFinalReport = it }
                        )
                        Text(
                            text = "Tampilkan dalam Laporan Akhir Semester",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    )
}

// ==========================================
// SUBTAB 3: ANALISIS PROGRESS
// ==========================================
@Composable
fun ProgressAnalysisTab(
    viewModel: PpiViewModel,
    student: StudentEntity?,
    ppi: PpiEntity?,
    analysis: ProgressAnalysisEntity?,
    currentUser: UserEntity?,
    isAiLoading: Boolean
) {
    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pilih peserta didik terlebih dahulu")
        }
        return
    }

    var summary by remember(analysis) { mutableStateOf(analysis?.summary ?: "") }
    var trend by remember(analysis) { mutableStateOf(analysis?.achievementTrend ?: "") }
    var teamReflection by remember(analysis) { mutableStateOf(analysis?.teamReflection ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Analisis Perkembangan & Rekomendasi",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PpiGreenDark
                        )
                        Button(
                            onClick = { viewModel.generateProgressAnalysisWithAi() },
                            colors = ButtonDefaults.buttonColors(containerColor = PpiOrange),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isAiLoading
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Analisis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Text(
                        text = "Sistem secara otomatis merangkum tren jurnal harian menjadi deskripsi naratif:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = summary,
                        onValueChange = { summary = it },
                        label = { Text("Ringkasan Capaian") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = trend,
                        onValueChange = { trend = it },
                        label = { Text("Tren Perkembangan") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = teamReflection,
                        onValueChange = { teamReflection = it },
                        label = { Text("Refleksi Tim Pendamping & Evaluasi") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.saveProgressAnalysis(
                                    summary, trend, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), teamReflection, false
                                )
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Simpan Draf")
                        }
                        Button(
                            onClick = {
                                viewModel.saveProgressAnalysis(
                                    summary, trend, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), teamReflection, true
                                )
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Validasi GPK", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUBTAB 4: REFLEKSI, LAPORAN AKHIR & CETAK
// ==========================================
@Composable
fun ReflectionAndDocTab(
    viewModel: PpiViewModel,
    student: StudentEntity?,
    ppi: PpiEntity?,
    currentUser: UserEntity?,
    isAiLoading: Boolean,
    onOpenPrintPreview: () -> Unit
) {
    if (student == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Pilih peserta didik terlebih dahulu")
        }
        return
    }

    var reflection by remember(ppi) { mutableStateOf(ppi?.reflection ?: "") }
    var followUp by remember(ppi) { mutableStateOf(ppi?.followUp ?: "") }
    val reportStatus = ppi?.reportApprovalStatus ?: "DRAFT"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Reflection Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Refleksi & Rencana Tindak Lanjut",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PpiGreenDark
                        )
                        Button(
                            onClick = { viewModel.generateReflectionWithAi() },
                            colors = ButtonDefaults.buttonColors(containerColor = PpiOrange),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isAiLoading
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("AI Refleksi", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = reflection,
                        onValueChange = { reflection = it },
                        label = { Text("Refleksi Pelaksanaan Program") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = followUp,
                        onValueChange = { followUp = it },
                        label = { Text("Rencana Tindak Lanjut (RTL) Semester Depan") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { viewModel.saveReflection(reflection, followUp, true) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Validasi Refleksi & RTL oleh GPK", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Laporan Akhir Semester Workflow
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Status Laporan Akhir Semester",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadge(status = reportStatus)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    when (reportStatus) {
                        "DRAFT" -> {
                            Button(
                                onClick = { viewModel.submitFinalReport() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Ajukan Laporan Akhir ke Koordinator", fontWeight = FontWeight.Bold)
                            }
                        }
                        "DIAJUKAN" -> {
                            Button(
                                onClick = { viewModel.approveFinalReportByCoordinator("Laporan akhir disetujui.") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Setujui Laporan Akhir (Koordinator)", fontWeight = FontWeight.Bold)
                            }
                        }
                        "DISETUJUI_KOORDINATOR" -> {
                            Button(
                                onClick = { viewModel.sendFinalReportToWaka() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Kirim ke WAKA untuk Finalisasi", fontWeight = FontWeight.Bold)
                            }
                        }
                        "DIKIRIM_WAKA" -> {
                            Button(
                                onClick = { viewModel.finalizeFinalReportByWaka() },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Finalisasi Laporan Akhir (WAKA)", fontWeight = FontWeight.Bold)
                            }
                        }
                        "FINAL" -> {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = PpiSuccessContainer
                            ) {
                                Text(
                                    text = "✓ Laporan Akhir Semester telah disahkan dan siap dicetak.",
                                    fontWeight = FontWeight.Bold,
                                    color = PpiSuccess,
                                    modifier = Modifier.padding(10.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Print & Export Preview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Dokumen Siap Cetak A4",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PpiGreenDark
                    )
                    Text(
                        text = "Format dokumen resmi siap print dengan kop sekolah, foto anak, dan 4 tanda tangan:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onOpenPrintPreview,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B1FA2)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pratinjau & Cetak Dokumen A4", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
