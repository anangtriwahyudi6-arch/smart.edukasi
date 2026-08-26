package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.StudentEntity
import com.example.data.local.UserEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.PpiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsScreen(
    viewModel: PpiViewModel,
    onOpenPpiWorkspace: (studentId: String) -> Unit
) {
    val students by viewModel.students.collectAsState()
    val searchQuery by viewModel.studentSearch.collectAsState()
    val filterUnit by viewModel.filterUnit.collectAsState()
    val gpkUsers by viewModel.gpkUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var transferStudent by remember { mutableStateOf<StudentEntity?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingStudent = null
                    showAddDialog = true
                },
                containerColor = PpiGreen,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_student_fab")
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = "Tambah Peserta Didik")
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
            // Header & Title
            item {
                Column {
                    Text(
                        text = "Data Peserta Didik Inklusi",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Kelola biodata anak berkebutuhan khusus, penugasan GPK, dan riwayat serah terima.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Search Bar & Filter Chips
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.studentSearch.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input"),
                    placeholder = { Text("Cari nama anak, NIS, rombel, atau GPK...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.studentSearch.value = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PpiGreen,
                        unfocusedBorderColor = PpiLine
                    ),
                    singleLine = true
                )
            }

            // Unit / Level filter chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("" to "Semua Jenjang", "KB" to "KB", "TK A" to "TK A", "TK B" to "TK B").forEach { (unitVal, unitLabel) ->
                        val isSelected = filterUnit == unitVal
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.filterUnit.value = unitVal },
                            label = { Text(unitLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PpiGreen,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Student Count Summary
            item {
                Text(
                    text = "Ditemukan ${students.size} peserta didik",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = PpiGreenDark
                )
            }

            if (students.isEmpty()) {
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = PpiMuted
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tidak ada data peserta didik yang cocok",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Coba ubah kata kunci pencarian atau tambah data baru.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                items(students, key = { it.studentId }) { student ->
                    StudentCard(
                        student = student,
                        onOpenWorkspace = {
                            viewModel.selectStudent(student.studentId)
                            onOpenPpiWorkspace(student.studentId)
                        },
                        onEdit = {
                            editingStudent = student
                            showAddDialog = true
                        },
                        onTransferGpk = {
                            transferStudent = student
                        },
                        onDelete = {
                            viewModel.deleteStudent(student.studentId)
                        }
                    )
                }
            }
        }
    }

    // Add / Edit Student Dialog
    if (showAddDialog) {
        StudentFormDialog(
            student = editingStudent,
            gpkUsers = gpkUsers,
            onDismiss = { showAddDialog = false },
            onSave = { name, nis, nisn, gender, birthPlace, birthDate, unit, cls, teacher, father, mother, phone, address, gpkId, gpkName, eduHistory, notes, photo ->
                viewModel.saveStudent(
                    studentId = editingStudent?.studentId,
                    name = name,
                    nis = nis,
                    nisn = nisn,
                    gender = gender,
                    birthPlace = birthPlace,
                    birthDate = birthDate,
                    unit = unit,
                    className = cls,
                    homeroomTeacher = teacher,
                    father = father,
                    mother = mother,
                    parentPhone = phone,
                    address = address,
                    gpkUserId = gpkId,
                    gpkName = gpkName,
                    educationHistory = eduHistory,
                    notes = notes,
                    photoUri = photo
                )
                showAddDialog = false
            }
        )
    }

    // Transfer GPK Dialog
    if (transferStudent != null) {
        TransferGpkDialog(
            student = transferStudent!!,
            gpkUsers = gpkUsers,
            onDismiss = { transferStudent = null },
            onConfirmTransfer = { newGpkId, newGpkName, date, reason, newUnit, newCls ->
                viewModel.transferStudentGpk(
                    studentId = transferStudent!!.studentId,
                    newGpkUserId = newGpkId,
                    newGpkName = newGpkName,
                    startDate = date,
                    reason = reason,
                    newUnit = newUnit,
                    newClassName = newCls
                )
                transferStudent = null
            }
        )
    }
}

@Composable
fun StudentCard(
    student: StudentEntity,
    onOpenWorkspace: () -> Unit,
    onEdit: () -> Unit,
    onTransferGpk: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenWorkspace() }
            .testTag("student_card_${student.studentId}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Avatar / Photo Placeholder
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            if (student.gender == "Laki-laki") Color(0xFFE3F2FD) else Color(0xFFFCE4EC)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (student.gender == "Laki-laki") Icons.Default.Face else Icons.Default.Face4,
                        contentDescription = null,
                        tint = if (student.gender == "Laki-laki") Color(0xFF1976D2) else Color(0xFFD81B60),
                        modifier = Modifier.size(34.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${student.unit} • Kelas ${student.className} | NIS: ${student.nis.ifBlank { "-" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = null,
                            tint = PpiGreenDark,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "GPK: ${student.gpkName.ifBlank { "Belum ditugaskan" }}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = PpiGreenDark
                        )
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Siswa", tint = PpiMuted)
                }
            }

            if (student.notes.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = PpiGreenSoft.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = student.notes,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = PpiInk,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onTransferGpk,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Serah Terima GPK", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onOpenWorkspace,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("Buka PPI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormDialog(
    student: StudentEntity?,
    gpkUsers: List<UserEntity>,
    onDismiss: () -> Unit,
    onSave: (
        name: String, nis: String, nisn: String, gender: String, birthPlace: String, birthDate: String,
        unit: String, cls: String, teacher: String, father: String, mother: String, phone: String,
        address: String, gpkId: String, gpkName: String, eduHistory: String, notes: String, photo: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(student?.name ?: "") }
    var nis by remember { mutableStateOf(student?.nis ?: "") }
    var nisn by remember { mutableStateOf(student?.nisn ?: "") }
    var gender by remember { mutableStateOf(student?.gender ?: "Laki-laki") }
    var birthPlace by remember { mutableStateOf(student?.birthPlace ?: "Malang") }
    var birthDate by remember { mutableStateOf(student?.birthDate ?: "2020-01-01") }
    var unit by remember { mutableStateOf(student?.unit ?: "TK A") }
    var className by remember { mutableStateOf(student?.className ?: "An-Nur") }
    var homeroomTeacher by remember { mutableStateOf(student?.homeroomTeacher ?: "Ustadzah Fatimah, S.Pd.") }
    var father by remember { mutableStateOf(student?.fatherName ?: "") }
    var mother by remember { mutableStateOf(student?.motherName ?: "") }
    var phone by remember { mutableStateOf(student?.parentPhone ?: "") }
    var address by remember { mutableStateOf(student?.address ?: "") }
    var selectedGpkId by remember { mutableStateOf(student?.gpkUserId ?: (gpkUsers.firstOrNull()?.userId ?: "")) }
    var selectedGpkName by remember {
        mutableStateOf(student?.gpkName ?: (gpkUsers.firstOrNull()?.name ?: "Ustadzah Aisyah Rahma, S.Pd."))
    }
    var eduHistory by remember { mutableStateOf(student?.educationHistory ?: "") }
    var notes by remember { mutableStateOf(student?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name, nis, nisn, gender, birthPlace, birthDate, unit, className,
                            homeroomTeacher, father, mother, phone, address, selectedGpkId,
                            selectedGpkName, eduHistory, notes, ""
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen)
            ) {
                Text("Simpan Peserta Didik", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        title = {
            Text(
                if (student == null) "Tambah Peserta Didik Inklusi" else "Edit Data Peserta Didik",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap Ananda *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = nis,
                            onValueChange = { nis = it },
                            label = { Text("NIS") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = nisn,
                            onValueChange = { nisn = it },
                            label = { Text("NISN") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Jenjang / Unit") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = className,
                            onValueChange = { className = it },
                            label = { Text("Nama Rombel / Kelas") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = homeroomTeacher,
                        onValueChange = { homeroomTeacher = it },
                        label = { Text("Guru Kelas / Wali Kelas") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    Text(
                        text = "Pilih GPK Pendamping:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PpiGreenDark
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        gpkUsers.forEach { gpk ->
                            val isSelected = selectedGpkId == gpk.userId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedGpkId = gpk.userId
                                        selectedGpkName = gpk.name
                                    },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PpiGreenSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = {
                                            selectedGpkId = gpk.userId
                                            selectedGpkName = gpk.name
                                        }
                                    )
                                    Text(gpk.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                }
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = father,
                        onValueChange = { father = it },
                        label = { Text("Nama Ayah") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = mother,
                        onValueChange = { mother = it },
                        label = { Text("Nama Ibu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("No. HP Orang Tua / WhatsApp") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Khusus / Pendampingan") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }
        }
    )
}

@Composable
fun TransferGpkDialog(
    student: StudentEntity,
    gpkUsers: List<UserEntity>,
    onDismiss: () -> Unit,
    onConfirmTransfer: (newGpkId: String, newGpkName: String, date: String, reason: String, newUnit: String, newCls: String) -> Unit
) {
    var selectedGpk by remember { mutableStateOf(gpkUsers.firstOrNull { it.userId != student.gpkUserId } ?: gpkUsers.firstOrNull()) }
    var effectiveDate by remember { mutableStateOf("2026-09-01") }
    var reason by remember { mutableStateOf("Kenaikan jenjang kelas dan penyesuaian beban pendampingan") }
    var newUnit by remember { mutableStateOf(student.unit) }
    var newClassName by remember { mutableStateOf(student.className) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (selectedGpk != null) {
                        onConfirmTransfer(
                            selectedGpk!!.userId,
                            selectedGpk!!.name,
                            effectiveDate,
                            reason,
                            newUnit,
                            newClassName
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PpiOrange)
            ) {
                Text("Konfirmasi Serah Terima", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        },
        title = {
            Text("Serah Terima / Alih Tugas GPK", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Peserta Didik: ${student.name}",
                    fontWeight = FontWeight.Bold,
                    color = PpiGreenDark
                )
                Text(
                    text = "GPK Saat Ini: ${student.gpkName.ifBlank { "Belum ada" }}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Pilih GPK Baru:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    gpkUsers.forEach { gpk ->
                        val isSelected = selectedGpk?.userId == gpk.userId
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedGpk = gpk },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) PpiOrangeLight else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ) {
                            Row(
                                modifier = Modifier.padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedGpk = gpk }
                                )
                                Text(gpk.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = effectiveDate,
                    onValueChange = { effectiveDate = it },
                    label = { Text("Tanggal Berlaku (yyyy-mm-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Alasan Serah Terima") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        }
    )
}
