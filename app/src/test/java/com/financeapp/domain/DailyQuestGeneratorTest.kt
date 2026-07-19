package com.financeapp.domain

import com.financeapp.data.database.DailyQuestAssignmentDao
import com.financeapp.data.database.QuestTemplateDao
import com.financeapp.data.model.DailyQuestAssignment
import com.financeapp.data.model.QuestCategory
import com.financeapp.data.model.QuestTemplate
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class DailyQuestGeneratorTest {

    private lateinit var templateDao: QuestTemplateDao
    private lateinit var assignmentDao: DailyQuestAssignmentDao
    private lateinit var conditionEvaluator: QuestConditionEvaluator
    private lateinit var generator: DailyQuestGenerator

    private val today = LocalDate.now()

    private val allTemplates = listOf(
        QuestTemplate("catat_transaksi", "Catat Transaksi", "Desc", QuestCategory.PENCATATAN, 10, 10),
        QuestTemplate("catat_3_transaksi", "Catat 3 Transaksi", "Desc", QuestCategory.PENCATATAN, 20, 4),
        QuestTemplate("cek_budget", "Cek Budget", "Desc", QuestCategory.BUDGETING, 10, 8),
        QuestTemplate("cek_dashboard", "Cek Dashboard", "Desc", QuestCategory.EKSPLORASI, 5, 10),
        QuestTemplate("buka_laporan", "Buka Laporan", "Desc", QuestCategory.EKSPLORASI, 10, 5),
        QuestTemplate("cek_kesehatan", "Cek Kesehatan", "Desc", QuestCategory.EKSPLORASI, 5, 6),
        QuestTemplate("tanpa_hiburan", "Tanpa Hiburan", "Desc", QuestCategory.DISIPLIN, 15, 4),
        QuestTemplate("cek_minggu_lalu", "Cek Minggu Lalu", "Desc", QuestCategory.REVIEW, 10, 3)
    )

    private val conditionalTemplate = QuestTemplate(
        "buat_budget_baru", "Buat Budget Baru", "Desc",
        QuestCategory.BUDGETING, 25, 6, "NO_BUDGET_EXISTS"
    )

    @Before
    fun setup() {
        templateDao = mockk(relaxed = true)
        assignmentDao = mockk(relaxed = true)
        conditionEvaluator = mockk(relaxed = true)
        generator = DailyQuestGenerator(templateDao, assignmentDao, conditionEvaluator)
    }

    // ---------- TEST 1: Creates 3 quests when no existing assignments ----------

    @Test
    fun `generateForToday creates 3 quests when no existing assignments`() = runTest {
        coEvery { assignmentDao.getForDate(today) } returns emptyList()
        coEvery { assignmentDao.getQuestIdsSince(any()) } returns emptySet()
        coEvery { templateDao.getAll() } returns allTemplates
        coEvery { conditionEvaluator.evaluate(any()) } returns true
        coEvery { assignmentDao.insert(any()) } just runs

        val result = generator.generateForToday(today)

        assertTrue(result)
        coVerify(exactly = 3) { assignmentDao.insert(any()) }
    }

    // ---------- TEST 2: Returns false when quests already exist ----------

    @Test
    fun `generateForToday returns false when quests already exist for today`() = runTest {
        val existing = listOf(
            DailyQuestAssignment(id = 1, questTemplateId = "catat_transaksi", assignedDate = today)
        )
        coEvery { assignmentDao.getForDate(today) } returns existing

        val result = generator.generateForToday(today)

        assertFalse(result)
        coVerify(exactly = 0) { templateDao.getAll() }
        coVerify(exactly = 0) { assignmentDao.insert(any()) }
    }

    // ---------- TEST 3: No quest repeats within 2 days ----------

    @Test
    fun `generateForToday does not repeat quests used in last 2 days`() = runTest {
        val recentIds = setOf("catat_transaksi", "cek_dashboard", "cek_budget")
        coEvery { assignmentDao.getForDate(today) } returns emptyList()
        coEvery { assignmentDao.getQuestIdsSince(today.minusDays(2)) } returns recentIds
        coEvery { templateDao.getAll() } returns allTemplates
        coEvery { conditionEvaluator.evaluate(any()) } returns true
        coEvery { assignmentDao.insert(any()) } just runs

        val result = generator.generateForToday(today)

        assertTrue(result)

        // Capture all inserted assignments
        val insertedSlots = mutableListOf<DailyQuestAssignment>()
        coVerify(exactly = 3) { assignmentDao.insert(capture(insertedSlots)) }

        // None of the inserted assignments should use recent IDs
        val insertedTemplateIds = insertedSlots.map { it.questTemplateId }.toSet()
        for (recentId in recentIds) {
            assertFalse(
                "Quest '$recentId' was used in last 2 days and should not be repeated",
                recentId in insertedTemplateIds
            )
        }
    }

    // ---------- TEST 4: Weighted random selection respects weights ----------

    @Test
    fun `generateForToday respects weight in selection - higher weight selected more often`() = runTest {
        // Create templates with extreme weight difference
        val heavyTemplate = QuestTemplate("heavy", "Heavy", "Desc", QuestCategory.PENCATATAN, 10, weight = 100)
        val lightTemplates = listOf(
            QuestTemplate("light1", "Light1", "Desc", QuestCategory.BUDGETING, 10, weight = 1),
            QuestTemplate("light2", "Light2", "Desc", QuestCategory.EKSPLORASI, 10, weight = 1),
            QuestTemplate("light3", "Light3", "Desc", QuestCategory.DISIPLIN, 10, weight = 1)
        )
        val pool = listOf(heavyTemplate) + lightTemplates

        coEvery { assignmentDao.getForDate(today) } returns emptyList()
        coEvery { assignmentDao.getQuestIdsSince(any()) } returns emptySet()
        coEvery { templateDao.getAll() } returns pool
        coEvery { conditionEvaluator.evaluate(any()) } returns true
        coEvery { assignmentDao.insert(any()) } just runs

        // Run 50 times and count how often "heavy" appears
        var heavyCount = 0
        repeat(50) {
            coEvery { assignmentDao.getForDate(today) } returns emptyList()
            generator.generateForToday(today)

            val insertedSlots = mutableListOf<DailyQuestAssignment>()
            coVerify(exactly = 3) { assignmentDao.insert(capture(insertedSlots)) }
            if (insertedSlots.any { it.questTemplateId == "heavy" }) {
                heavyCount++
            }
            clearMocks(assignmentDao, answers = false)
            coEvery { assignmentDao.insert(any()) } just runs
        }

        // With weight 100 vs three weight-1 items, "heavy" should appear in most selections
        assertTrue(
            "Heavy template (weight=100) should appear in majority of selections, but appeared $heavyCount/50 times",
            heavyCount > 25
        )
    }

    // ---------- TEST 5: Condition-evaluated quests only appear when condition is true ----------

    @Test
    fun `generateForToday excludes conditional quests when condition is false`() = runTest {
        val templatesWithConditional = allTemplates + conditionalTemplate
        coEvery { assignmentDao.getForDate(today) } returns emptyList()
        coEvery { assignmentDao.getQuestIdsSince(any()) } returns emptySet()
        coEvery { templateDao.getAll() } returns templatesWithConditional
        // All conditions return false except for the conditional template
        coEvery { conditionEvaluator.evaluate("NO_BUDGET_EXISTS") } returns false
        coEvery { conditionEvaluator.evaluate(isNull()) } returns true
        coEvery { assignmentDao.insert(any()) } just runs

        val result = generator.generateForToday(today)

        assertTrue(result)

        val insertedSlots = mutableListOf<DailyQuestAssignment>()
        coVerify(exactly = 3) { assignmentDao.insert(capture(insertedSlots)) }

        val insertedIds = insertedSlots.map { it.questTemplateId }
        assertFalse(
            "Conditional quest should not appear when condition is false",
            "buat_budget_baru" in insertedIds
        )
    }

    @Test
    fun `generateForToday includes conditional quest when condition is true`() = runTest {
        // Use only the conditional template + 2 others to guarantee it gets picked
        val smallPool = listOf(
            conditionalTemplate,
            QuestTemplate("a", "A", "Desc", QuestCategory.PENCATATAN, 10, 10),
            QuestTemplate("b", "B", "Desc", QuestCategory.BUDGETING, 10, 10)
        )
        coEvery { assignmentDao.getForDate(today) } returns emptyList()
        coEvery { assignmentDao.getQuestIdsSince(any()) } returns emptySet()
        coEvery { templateDao.getAll() } returns smallPool
        coEvery { conditionEvaluator.evaluate("NO_BUDGET_EXISTS") } returns true
        coEvery { conditionEvaluator.evaluate(isNull()) } returns true
        coEvery { assignmentDao.insert(any()) } just runs

        val result = generator.generateForToday(today)

        assertTrue(result)

        val insertedSlots = mutableListOf<DailyQuestAssignment>()
        coVerify(exactly = 3) { assignmentDao.insert(capture(insertedSlots)) }

        val insertedIds = insertedSlots.map { it.questTemplateId }
        assertTrue(
            "Conditional quest should appear when condition is true",
            "buat_budget_baru" in insertedIds
        )
    }

    // ---------- TEST 6: Falls back to full pool when eligible < 3 ----------

    @Test
    fun `generateForToday falls back to full pool when eligible less than 3`() = runTest {
        // All templates except 2 are in recent history, making eligible < 3
        val recentIds = allTemplates.drop(2).map { it.id }.toSet()
        coEvery { assignmentDao.getForDate(today) } returns emptyList()
        coEvery { assignmentDao.getQuestIdsSince(today.minusDays(2)) } returns recentIds
        coEvery { templateDao.getAll() } returns allTemplates
        coEvery { conditionEvaluator.evaluate(isNull()) } returns true
        coEvery { assignmentDao.insert(any()) } just runs

        val result = generator.generateForToday(today)

        assertTrue(result)

        // Should still create 3 quests (from the full pool, ignoring recency)
        coVerify(exactly = 3) { assignmentDao.insert(any()) }
    }

    // ---------- EDGE CASES ----------

    @Test
    fun `generateForToday handles empty template pool`() = runTest {
        coEvery { assignmentDao.getForDate(today) } returns emptyList()
        coEvery { assignmentDao.getQuestIdsSince(any()) } returns emptySet()
        coEvery { templateDao.getAll() } returns emptyList()
        coEvery { assignmentDao.insert(any()) } just runs

        val result = generator.generateForToday(today)

        assertTrue(result)
        coVerify(exactly = 0) { assignmentDao.insert(any()) }
    }

    @Test
    fun `generateForToday handles single template in pool`() = runTest {
        val singleTemplate = listOf(
            QuestTemplate("only", "Only", "Desc", QuestCategory.PENCATATAN, 10, 5)
        )
        coEvery { assignmentDao.getForDate(today) } returns emptyList()
        coEvery { assignmentDao.getQuestIdsSince(any()) } returns emptySet()
        coEvery { templateDao.getAll() } returns singleTemplate
        coEvery { conditionEvaluator.evaluate(isNull()) } returns true
        coEvery { assignmentDao.insert(any()) } just runs

        val result = generator.generateForToday(today)

        assertTrue(result)
        // Only 1 template available, so only 1 quest should be created
        coVerify(exactly = 1) { assignmentDao.insert(any()) }
    }
}
