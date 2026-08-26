package com.example.data.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlanningInputItem(
    val id: String = "",
    val aspek: String = "",
    val jumlah_intervensi: Int = 3,
    val fokus: String = ""
)

@JsonClass(generateAdapter = true)
data class PlanningMatrixItem(
    val no: Int = 1,
    val aspek: String = "",
    val jumlah_intervensi: Int = 3,
    val fokus: String = "",
    val kondisi_saat_ini: String = "",
    val dampak_kondisi: String = "",
    val kebutuhan: List<String> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ProgramItem(
    val id: String = "",
    val nama: String = "",
    val target: String = "",
    val indikator: String = "",
    val strategi: String = "",
    val bidang: String = "",
    val baseline: String = "",
    val kegiatan: String = "",
    val media: String = "",
    val frekuensi: String = "",
    val pic: String = "",
    val evaluasi: String = ""
)

@JsonClass(generateAdapter = true)
data class ProgramCategories(
    val akademik: List<ProgramItem> = emptyList(),
    val program_khusus: List<ProgramItem> = emptyList(),
    val program_spiritual: List<ProgramItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class TransitionInfo(
    val jenjang_sebelumnya: String = "",
    val kondisi_transisi: String = "",
    val kebutuhan_adaptasi: String = "",
    val strategi_transisi: String = "",
    val indikator_kesiapan: String = ""
)

@JsonClass(generateAdapter = true)
data class StageItem(
    val stage_id: String = "",
    val urutan: Int = 1,
    val kegiatan: String = "",
    val bobot_persen: Int = 20,
    val frekuensi: String = "",
    val indikator_keberhasilan: String = ""
)

@JsonClass(generateAdapter = true)
data class TimeScheduleItem(
    val schedule_id: String = "",
    val kategori: String = "",
    val bidang: String = "",
    val tujuan: String = "",
    val periode: String = "Mingguan",
    val durasi: String = "",
    val tahapan: List<StageItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ObjectiveItem(
    val bidang: String = "",
    val tujuan: String = "",
    val indikator: String = "",
    val target_waktu: String = ""
)

@JsonClass(generateAdapter = true)
data class AspectScore(
    val label: String,
    val value: Int,
    val count: Int,
    val source: String
)
