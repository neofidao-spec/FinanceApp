package com.financeapp.data.seed

import android.util.Log
import com.financeapp.data.database.QuestTemplateDao
import com.financeapp.data.model.QuestCategory
import com.financeapp.data.model.QuestTemplate
import javax.inject.Inject

class QuestSeeder @Inject constructor(
    private val dao: QuestTemplateDao
) {
    suspend fun seedIfNeeded() {
        try {
            if (dao.count() > 0) return
            dao.insertAll(templates)
        } catch (e: Exception) {
            Log.e("QuestSeeder", "Failed to seed quest templates", e)
        }
    }

    companion object {
        val templates = listOf(
            QuestTemplate("catat_transaksi", "Catat Transaksi", "Catat 1 transaksi hari ini", QuestCategory.PENCATATAN, 10, 10),
            QuestTemplate("catat_3_transaksi", "Catat 3 Transaksi", "Catat 3 transaksi hari ini", QuestCategory.PENCATATAN, 20, 4),
            QuestTemplate("catat_pemasukan", "Catat Pemasukan", "Catat 1 transaksi pemasukan", QuestCategory.PENCATATAN, 15, 5),
            QuestTemplate("cek_budget", "Cek Budget", "Lihat halaman budget hari ini", QuestCategory.BUDGETING, 10, 8),
            QuestTemplate("buat_budget_baru", "Buat Budget Baru", "Buat 1 budget baru", QuestCategory.BUDGETING, 25, 6, "NO_BUDGET_EXISTS"),
            QuestTemplate("update_budget", "Update Budget", "Update batas budget yang ada", QuestCategory.BUDGETING, 15, 3),
            QuestTemplate("cek_budget_limit", "Cek Budget Mendekati Limit", "Periksa kategori yang mendekati limit", QuestCategory.BUDGETING, 15, 4, "HAS_BUDGET_NEAR_LIMIT"),
            QuestTemplate("cek_dashboard", "Cek Dashboard", "Buka dashboard hari ini", QuestCategory.EKSPLORASI, 5, 10),
            QuestTemplate("buka_laporan", "Buka Laporan", "Lihat laporan bulanan", QuestCategory.EKSPLORASI, 10, 5),
            QuestTemplate("lihat_tren", "Lihat Tren", "Cek tren pengeluaran bulanan", QuestCategory.EKSPLORASI, 10, 3, "MIN_3_MONTHS_DATA"),
            QuestTemplate("cek_kesehatan", "Cek Kesehatan", "Lihat skor kesehatan keuangan", QuestCategory.EKSPLORASI, 5, 6),
            QuestTemplate("tanpa_hiburan", "Hari Tanpa Hiburan", "Jangan catat pengeluaran hiburan hari ini", QuestCategory.DISIPLIN, 15, 4),
            QuestTemplate("hemat_hari_ini", "Hemat Hari Ini", "Pengeluaran di bawah rata-rata", QuestCategory.DISIPLIN, 20, 3, "HAS_SPENDING_HISTORY"),
            QuestTemplate("cek_minggu_lalu", "Cek Minggu Lalu", "Review transaksi minggu lalu", QuestCategory.REVIEW, 10, 3),
            QuestTemplate("bandingkan_bulan", "Bandingkan Bulan", "Bandingkan pengeluaran bulan ini vs lalu", QuestCategory.REVIEW, 15, 3, "MIN_2_MONTHS_DATA")
        )
    }
}
