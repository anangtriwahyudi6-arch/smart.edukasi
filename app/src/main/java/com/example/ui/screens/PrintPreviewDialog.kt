package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.PpiViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintPreviewDialog(
    viewModel: PpiViewModel,
    onDismiss: () -> Unit
) {
    val school by viewModel.school.collectAsState()
    val student by viewModel.selectedStudent.collectAsState()
    val ppi by viewModel.currentPpi.collectAsState()
    val journals by viewModel.dailyJournals.collectAsState()
    val analysis by viewModel.currentAnalysis.collectAsState()
    val year by viewModel.selectedYear.collectAsState()
    val semester by viewModel.selectedSemester.collectAsState()

    var docType by remember { mutableStateOf("PPI_AWAL") } // PPI_AWAL or LAPORAN_AKHIR

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .testTag("print_preview_dialog"),
        confirmButton = {
            Button(
                onClick = {
                    viewModel.showToast("Mencetak dokumen resmi ke printer / PDF...")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = PpiGreen)
            ) {
                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Cetak / Simpan PDF", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Tutup") }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pratinjau Dokumen Resmi A4", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = docType == "PPI_AWAL",
                        onClick = { docType = "PPI_AWAL" },
                        label = { Text("PPI Awal", fontSize = 10.sp) }
                    )
                    FilterChip(
                        selected = docType == "LAPORAN_AKHIR",
                        onClick = { docType = "LAPORAN_AKHIR" },
                        label = { Text("Laporan Akhir", fontSize = 10.sp) }
                    )
                }
            }
        },
        text = {
            // A4 Paper Simulator
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(8.dp)),
                color = Color.White
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Kop Dokumen Sekolah
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = (school?.name ?: "PAUDIT INSAN PERMATA MALANG").uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = school?.tagline ?: "Sekolah Menyenangkan Berkarakter Al-Qur'an",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF444444)
                            )
                            Text(
                                text = "${school?.address ?: "Jalan Akordion Utara No. 3 Malang"} • Telp: ${school?.phone ?: "-"}",
                                fontSize = 9.sp,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                            Divider(modifier = Modifier.padding(top = 8.dp), thickness = 2.dp, color = Color.Black)
                            Divider(modifier = Modifier.padding(top = 1.dp, bottom = 8.dp), thickness = 0.5.dp, color = Color.Black)
                        }
                    }

                    // Title of Document
                    item {
                        Text(
                            text = if (docType == "PPI_AWAL") "PROGRAM PEMBELAJARAN INDIVIDUAL (PPI)" else "LAPORAN PERKEMBANGAN INDIVIDUAL (LAPORAN AKHIR)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.Black
                        )
                        Text(
                            text = "Tahun Ajaran $year • Semester $semester",
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF555555)
                        )
                    }

                    // Biodata Table
                    item {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(0.5.dp, Color(0xFF888888)),
                            color = Color(0xFFFAFAFA)
                        ) {
                            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                PrintMetaRow("Nama Lengkap", student?.name ?: "-")
                                PrintMetaRow("NIS / NISN", "${student?.nis ?: "-"} / ${student?.nisn ?: "-"}")
                                PrintMetaRow("Jenjang / Kelas", "${student?.unit ?: "-"} • Kelas ${student?.className ?: "-"}")
                                PrintMetaRow("Guru Pendamping (GPK)", student?.gpkName ?: "-")
                                PrintMetaRow("Wali Kelas", student?.homeroomTeacher ?: "-")
                            }
                        }
                    }

                    if (docType == "PPI_AWAL") {
                        item {
                            Text("A. Profil & Tujuan Jangka Panjang", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Text(ppi?.profile ?: "-", fontSize = 10.sp, color = Color.Black)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Tujuan: ${ppi?.longTermGoal ?: "-"}", fontSize = 10.sp, color = Color.Black)
                        }

                        item {
                            Text("B. Kesepakatan Case Conference Orang Tua", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Text("• Masukan Orang Tua: ${ppi?.parentInput.orEmpty().ifBlank { "-" }}", fontSize = 10.sp, color = Color.Black)
                            Text("• Kesepakatan: ${ppi?.caseConferenceResult.orEmpty().ifBlank { "-" }}", fontSize = 10.sp, color = Color.Black)
                        }
                    } else {
                        // Laporan Akhir View
                        item {
                            Text("A. Ringkasan Capaian Intervensi", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Text(analysis?.summary ?: "Perkembangan peserta didik menunjukkan peningkatan konsisten pada aspek komunikasi dan kemandirian.", fontSize = 10.sp, color = Color.Black)
                        }

                        item {
                            Text("B. Catatan Jurnal & Dokumentasi Terpilih", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            val finalJournals = journals.filter { it.includeFinalReport }
                            if (finalJournals.isEmpty()) {
                                Text("- Belum ada jurnal terpilih -", fontSize = 9.sp, color = Color.Gray)
                            } else {
                                finalJournals.take(4).forEach { jrn ->
                                    Text("• [${jrn.date}] ${jrn.activity} -> Capaian: ${jrn.rubricScore}% (${jrn.studentAbility})", fontSize = 9.sp, color = Color.Black)
                                }
                            }
                        }

                        item {
                            Text("C. Refleksi & Rencana Tindak Lanjut (RTL)", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                            Text("Refleksi: ${ppi?.reflection.orEmpty().ifBlank { "Program pendampingan efektif." }}", fontSize = 10.sp, color = Color.Black)
                            Text("Tindak Lanjut: ${ppi?.followUp.orEmpty().ifBlank { "Melanjutkan penguatan di semester depan." }}", fontSize = 10.sp, color = Color.Black)
                        }
                    }

                    // 4 Signature Blocks
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Malang, ${if (docType == "PPI_AWAL") ppi?.ppiPrintDate ?: "2026-08-25" else ppi?.reportPrintDate ?: "2026-12-18"}",
                            fontSize = 10.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SignatureBox("Wali Murid / Orang Tua", student?.fatherName.orEmpty().ifBlank { "Orang Tua Siswa" })
                            SignatureBox("Guru Pendamping Khusus", student?.gpkName.orEmpty().ifBlank { "Ustadzah Aisyah Rahma, S.Pd." })
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SignatureBox("Koordinator Inklusi", ppi?.coordinatorName.orEmpty().ifBlank { "Ustadzah Nurul Hidayati, S.Psi." })
                            SignatureBox("Kepala Sekolah", school?.principalName.orEmpty().ifBlank { "Dra. Hj. Siti Aminah, M.Pd." })
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun PrintMetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, modifier = Modifier.width(130.dp), fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF333333))
        Text(text = ": $value", fontSize = 9.sp, color = Color.Black, modifier = Modifier.weight(1f))
    }
}

@Composable
fun SignatureBox(role: String, name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(120.dp)
    ) {
        Text(text = role, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(34.dp))
        Text(text = "($name)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, textAlign = TextAlign.Center)
    }
}
