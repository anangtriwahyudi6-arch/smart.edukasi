package com.example.ui.screens

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
import com.example.data.local.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.PpiViewModel
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    viewModel: PpiViewModel
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    var showAddUserDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = PpiGreen,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Tambah Pengguna")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "Manajemen Pengguna & GPK",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Kelola hak akses berjenjang (WAKA, Koordinator Inklusi, GPK) dan simulasi login peran aktif.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Role Switcher Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = PpiGreenSoft)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PpiGreenDark)
                            Text(
                                text = "Simulasi Peran Aktif",
                                fontWeight = FontWeight.Bold,
                                color = PpiGreenDark
                            )
                        }
                        Text(
                            text = "Pengguna aktif saat ini: ${currentUser?.name ?: "-"} (${currentUser?.role ?: "-"})",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            items(allUsers, key = { it.userId }) { user ->
                val isCurrent = currentUser?.userId == user.userId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.switchUser(user) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCurrent) PpiGreenSoft.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 3.dp else 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    when (user.role) {
                                        "SUPER ADMIN" -> Color(0xFFE1BEE7)
                                        "WAKA" -> Color(0xFFBBDEFB)
                                        "KOORDINATOR INKLUSI" -> Color(0xFFC8E6C9)
                                        "GPK" -> Color(0xFFFFE0B2)
                                        else -> Color(0xFFEEEEEE)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.role.take(1),
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF333333)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "${user.role} • @${user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (isCurrent) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PpiGreen
                            ) {
                                Text(
                                    text = "Aktif",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { viewModel.switchUser(user) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Gunakan", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddUserDialog) {
        var name by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var role by remember { mutableStateOf("GPK") }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            title = { Text("Tambah Pengguna / GPK", fontWeight = FontWeight.Bold) },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && username.isNotBlank()) {
                            scope.launch {
                                viewModel.repository.saveUser(
                                    UserEntity(
                                        userId = "USR-" + UUID.randomUUID().toString().take(6).uppercase(),
                                        schoolId = "SCH-PAUDIT-IP",
                                        name = name,
                                        email = email,
                                        username = username,
                                        password = "password123",
                                        role = role
                                    )
                                )
                                viewModel.showToast("Pengguna $name berhasil ditambahkan.")
                                showAddUserDialog = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PpiGreen)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) { Text("Batal") }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap & Gelar") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it },
                        label = { Text("Username") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Pilih Peran / Jabatan:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    listOf("GPK", "KOORDINATOR INKLUSI", "WAKA", "ADMIN SEKOLAH").forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { role = r }
                        ) {
                            RadioButton(selected = role == r, onClick = { role = r })
                            Text(r, fontSize = 12.sp)
                        }
                    }
                }
            }
        )
    }
}
