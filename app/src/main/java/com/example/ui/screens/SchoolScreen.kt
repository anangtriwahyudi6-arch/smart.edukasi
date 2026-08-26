package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.*
import com.example.ui.viewmodel.PpiViewModel

@Composable
fun SchoolScreen(
    viewModel: PpiViewModel
) {
    val school by viewModel.school.collectAsState()

    var name by remember(school) { mutableStateOf(school?.name ?: "PAUDIT Insan Permata Malang") }
    var npsn by remember(school) { mutableStateOf(school?.npsn ?: "20559950") }
    var level by remember(school) { mutableStateOf(school?.level ?: "PAUD") }
    var tagline by remember(school) { mutableStateOf(school?.tagline ?: "Sekolah Menyenangkan Berkarakter Al-Qur'an") }
    var vision by remember(school) { mutableStateOf(school?.vision ?: "Menjadi lembaga PAUD yang kokoh dalam membentuk generasi muslim.") }
    var address by remember(school) { mutableStateOf(school?.address ?: "Jalan Akordion Utara No.3, Malang") }
    var phone by remember(school) { mutableStateOf(school?.phone ?: "(0341) 490-887") }
    var mobile by remember(school) { mutableStateOf(school?.mobile ?: "0819-9443-4343") }
    var principal by remember(school) { mutableStateOf(school?.principalName ?: "Dra. Hj. Siti Aminah, M.Pd.") }
    var selectedPrimaryColor by remember(school) { mutableStateOf(school?.primaryColor ?: "#008D3F") }
    var selectedAccentColor by remember(school) { mutableStateOf(school?.accentColor ?: "#F57C00") }

    val presetPalettes = listOf(
        Triple("Hijau - Oranye (Resmi)", "#008D3F", "#F57C00"),
        Triple("Biru Edukasi - Emas", "#1565C0", "#FFB300"),
        Triple("Teal Inklusi - Coral", "#00897B", "#FF7043"),
        Triple("Marun Prestasi - Amber", "#880E4F", "#FFA000")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = "Identitas & Branding Lembaga",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Data ini otomatis disematkan pada kop dokumen, lembar PPI, dan laporan resmi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Color Palette Selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Tema & Palet Warna Sekolah",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PpiGreenDark
                    )
                    Text(
                        text = "Pilih preset warna yang mencerminkan identitas sekolah inklusi Anda:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        presetPalettes.forEach { (title, prim, acc) ->
                            val isSelected = selectedPrimaryColor == prim
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedPrimaryColor = prim
                                        selectedAccentColor = acc
                                    },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) PpiGreenSoft else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, PpiGreen) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(prim)))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(Color(android.graphics.Color.parseColor(acc)))
                                        )
                                    }
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PpiGreen, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Form Identitas Sekolah
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Profil Lembaga Pendidikan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PpiGreenDark
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lembaga / Sekolah *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = npsn,
                            onValueChange = { npsn = it },
                            label = { Text("NPSN") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = level,
                            onValueChange = { level = it },
                            label = { Text("Jenjang (PAUD/SD/SMP)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = principal,
                        onValueChange = { principal = it },
                        label = { Text("Nama Kepala Sekolah & Gelar *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Slogan / Tagline") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Alamat Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Telepon") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = mobile,
                            onValueChange = { mobile = it },
                            label = { Text("WhatsApp") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Button(
                        onClick = {
                            viewModel.updateSchoolIdentity(
                                name = name,
                                npsn = npsn,
                                level = level,
                                tagline = tagline,
                                vision = vision,
                                address = address,
                                phone = phone,
                                mobile = mobile,
                                principal = principal,
                                primaryColor = selectedPrimaryColor,
                                accentColor = selectedAccentColor
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_school_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Identitas & Tema", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Google Apps Script / Google Spreadsheet Sync Integration Card
        item {
            val gasUrlState by viewModel.gasUrl.collectAsState()
            val isAutoSyncState by viewModel.isGasAutoSync.collectAsState()
            val lastSyncTimeState by viewModel.lastGasSyncTime.collectAsState()
            val lastSyncStatusState by viewModel.lastGasSyncStatus.collectAsState()
            val isGasSyncing by viewModel.isGasSyncing.collectAsState()

            var tempGasUrl by remember(gasUrlState) { mutableStateOf(gasUrlState) }
            var tempAutoSync by remember(isAutoSyncState) { mutableStateOf(isAutoSyncState) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PpiGreenSoft,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CloudSync,
                                    contentDescription = null,
                                    tint = PpiGreenDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Koneksi Google Spreadsheet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PpiGreenDark
                            )
                            Text(
                                text = "Otomatis tersimpan & sinkron dengan Google Apps Script",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Status Sinkronisasi:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isGasSyncing) "⏳ Sedang Mengirim..." else lastSyncStatusState,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lastSyncStatusState.contains("Gagal") || lastSyncStatusState.contains("Error")) Color(0xFFD32F2F) else PpiGreenDark
                                )
                            }
                            if (lastSyncTimeState > 0L) {
                                val timeStr = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTimeState))
                                Text(
                                    text = "Terakhir tersimpan: $timeStr",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = tempGasUrl,
                        onValueChange = { tempGasUrl = it },
                        label = { Text("URL Google Apps Script Exec Webhook") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        trailingIcon = {
                            IconButton(onClick = {
                                tempGasUrl = com.example.data.remote.GoogleAppsScriptService.DEFAULT_GAS_URL
                            }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reset URL", tint = PpiGreen)
                            }
                        }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Otomatis Simpan Real-time",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Kirim ke Spreadsheet setiap ada pengisian di aplikasi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = tempAutoSync,
                            onCheckedChange = {
                                tempAutoSync = it
                                viewModel.updateGasSettings(tempGasUrl, it)
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = PpiGreen, checkedTrackColor = PpiGreenSoft)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.updateGasSettings(tempGasUrl, tempAutoSync)
                                viewModel.testGasConnection()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isGasSyncing
                        ) {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uji Koneksi", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.updateGasSettings(tempGasUrl, tempAutoSync)
                                viewModel.syncAllToGoogleSpreadsheet()
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PpiGreen),
                            enabled = !isGasSyncing
                        ) {
                            if (isGasSyncing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sinkronkan Semua Data", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
