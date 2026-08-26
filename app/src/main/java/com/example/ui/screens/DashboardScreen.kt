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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.NotificationEntity
import com.example.data.model.AspectScore
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PpiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PpiViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToPpi: (studentId: String?) -> Unit,
    onNavigateToSchool: () -> Unit,
    onOpenPrintDialog: () -> Unit
) {
    val school by viewModel.school.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val students by viewModel.students.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedSemester by viewModel.selectedSemester.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val dailyJournals by viewModel.dailyJournals.collectAsState()
    val progressRecords by viewModel.progressRecords.collectAsState()

    var todoFilter by remember { mutableStateOf("pending") } // pending, done, all

    val pendingNotifications = remember(notifications) {
        notifications.filter { !it.isRead }
    }
    val doneNotifications = remember(notifications) {
        notifications.filter { it.isRead }
    }
    val filteredNotifications = remember(notifications, todoFilter) {
        when (todoFilter) {
            "pending" -> pendingNotifications
            "done" -> doneNotifications
            else -> notifications
        }
    }

    val totalStudents = students.size
    val activePpis = remember(students) { students.count { it.nis.isNotBlank() } }
    val totalJournals = dailyJournals.size
    val averageScore = remember(dailyJournals) {
        if (dailyJournals.isNotEmpty()) {
            dailyJournals.map { it.rubricScore }.average().toInt()
        } else 68
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero School Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    PpiGreenDark,
                                    PpiGreen,
                                    PpiGreenLight
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "SISTEM PPI SEKOLAH INKLUSI",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Period selector
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.clickable {
                                    viewModel.selectedSemester.value = if (selectedSemester == "1") "2" else "1"
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = PpiGreenDark,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "$selectedYear S$selectedSemester",
                                        color = PpiGreenDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = school?.name ?: "PAUDIT Insan Permata Malang",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = school?.tagline ?: "Sekolah Menyenangkan Berkarakter Al-Qur'an",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PpiOrangeLight
                        )
                        Text(
                            text = "Program Pembelajaran Individual terstruktur, berkesinambungan, dan kolaboratif.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Live Google Apps Script Sync Bar
        item {
            val isGasSyncing by viewModel.isGasSyncing.collectAsState()
            val lastGasSyncStatus by viewModel.lastGasSyncStatus.collectAsState()
            val isAutoSync by viewModel.isGasAutoSync.collectAsState()

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToSchool() },
                shape = RoundedCornerShape(14.dp),
                color = PpiGreenSoft.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, PpiGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(PpiGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGasSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isGasSyncing) "Sedang menyinkronkan data..." else "Google Spreadsheet Terhubung",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PpiGreenDark
                            )
                            Text(
                                text = if (isAutoSync) "Otomatis tersimpan ke Apps Script (${lastGasSyncStatus})" else "Sinkronisasi otomatis nonaktif",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.syncAllToGoogleSpreadsheet() },
                        enabled = !isGasSyncing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync All",
                            tint = PpiGreenDark
                        )
                    }
                }
            }
        }

        // Stats Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Anak",
                    value = "$totalStudents",
                    subtitle = "Peserta inklusi",
                    icon = Icons.Default.Groups,
                    color = PpiGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToStudents
                )
                StatCard(
                    title = "Capaian",
                    value = "$averageScore%",
                    subtitle = if (averageScore >= 75) "Kategori: Baik" else "Berkembang",
                    icon = Icons.Default.TrendingUp,
                    color = PpiOrange,
                    modifier = Modifier.weight(1f),
                    onClick = { onNavigateToPpi(null) }
                )
            }
        }

        // To-Do Workflow Queue
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_todo_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(PpiGreenSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Checklist,
                                    contentDescription = null,
                                    tint = PpiGreenDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "To-Do Saya",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tugas workflow peran ${currentUser?.role ?: "Pengguna"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        TextButton(
                            onClick = { viewModel.markAllNotificationsRead() }
                        ) {
                            Text("Tandai Selesai", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "pending" to "Belum Selesai (${pendingNotifications.size})",
                            "done" to "Selesai (${doneNotifications.size})",
                            "all" to "Semua"
                        ).forEach { (key, label) ->
                            val active = todoFilter == key
                            FilterChip(
                                selected = active,
                                onClick = { todoFilter = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PpiGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredNotifications.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Semua tugas workflow untuk peran Anda telah tuntas!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            filteredNotifications.take(4).forEach { item ->
                                TodoItemRow(
                                    item = item,
                                    onOpenTask = {
                                        if (item.studentId.isNotBlank()) {
                                            viewModel.selectStudent(item.studentId)
                                            onNavigateToPpi(item.studentId)
                                        }
                                    },
                                    onMarkRead = { viewModel.markNotificationRead(item.notificationId) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Aksi Cepat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.PersonAdd,
                            label = "Tambah Anak",
                            color = PpiGreen,
                            onClick = onNavigateToStudents
                        )
                        QuickActionButton(
                            icon = Icons.Default.Timeline,
                            label = "Perjalanan PPI",
                            color = Color(0xFF1976D2),
                            onClick = { onNavigateToPpi(null) }
                        )
                        QuickActionButton(
                            icon = Icons.Default.AutoStories,
                            label = "Jurnal Harian",
                            color = PpiOrange,
                            onClick = { onNavigateToPpi(null) }
                        )
                        QuickActionButton(
                            icon = Icons.Default.Print,
                            label = "Cetak PPI",
                            color = Color(0xFF7B1FA2),
                            onClick = onOpenPrintDialog
                        )
                    }
                }
            }
        }

        // Ringkasan Aspek Perkembangan
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Capaian Komponen Perkembangan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Rata-rata perkembangan dari seluruh instrumen PPI periode aktif:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    listOf(
                        AspectScore("Komunikasi & Bahasa", 75, 4, "Time Schedule"),
                        AspectScore("Kemandirian & Bina Diri", 80, 5, "Time Schedule"),
                        AspectScore("Sosial & Emosi", 65, 3, "Program PPI"),
                        AspectScore("Motorik & Sensori", 70, 3, "Planning Matrix")
                    ).forEach { aspect ->
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = aspect.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${aspect.value}%",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Black,
                                    color = if (aspect.value >= 75) PpiGreen else PpiOrange
                                )
                            }
                            LinearProgressIndicator(
                                progress = { aspect.value / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (aspect.value >= 75) PpiGreen else PpiOrange,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoItemRow(
    item: NotificationEntity,
    onOpenTask: () -> Unit,
    onMarkRead: () -> Unit
) {
    val (tagColor, tagBg) = when (item.type) {
        "PPI_SUBMITTED" -> Color(0xFF1565C0) to Color(0xFFE3F2FD)
        "PPI_REVISION" -> Color(0xFFC62828) to Color(0xFFFFEBEE)
        "CASE_VALIDATED_GPK" -> PpiGreenDark to PpiGreenSoft
        else -> PpiOrangeDark to PpiOrangeLight
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (item.isRead) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else PpiGreenSoft.copy(alpha = 0.35f),
        border = null
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (item.isRead) PpiSuccess else PpiOrange)
                    .clickable { onMarkRead() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (item.isRead) Icons.Default.Check else Icons.Default.PriorityHigh,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = tagBg
                ) {
                    Text(
                        text = item.title,
                        color = tagColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = item.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 2.dp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (item.studentName.isNotBlank()) {
                    Text(
                        text = "Anak: ${item.studentName}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.Bold,
                        color = PpiGreenDark
                    )
                }
            }

            Button(
                onClick = onOpenTask,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Buka", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
