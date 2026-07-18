package com.financeapp.domain

import com.financeapp.data.repository.TransactionRepository
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.YearMonth

@OptIn(ExperimentalCoroutinesApi::class)
class GetHealthScoreUseCaseTest {

    private lateinit var transactionRepository: TransactionRepository
    private lateinit var useCase: GetHealthScoreUseCase

    private val currentMonth = YearMonth.now()
    private val previousMonth = currentMonth.minusMonths(1)

    @Before
    fun setup() {
        transactionRepository = mockk(relaxed = true)
        useCase = GetHealthScoreUseCase(transactionRepository)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ===================== SCORE CALCULATION =====================

    @Test
    fun `score is positive when income greater than expense`() = runTest {
        // income=10000, expense=3000 -> score = (10000-3000)/10000*100 = 70
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(10000.0, 0.0)
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(3000.0, 0.0)

        val result = useCase()

        assertEquals(70, result.score)
        assertEquals("Good", result.category)
    }

    @Test
    fun `score is 100 when income equals expense of 0`() = runTest {
        // income=5000, expense=0 -> score = (5000-0)/5000*100 = 100
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(5000.0, 0.0)
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(0.0, 0.0)

        val result = useCase()

        assertEquals(100, result.score)
        assertEquals("Excellent", result.category)
    }

    @Test
    fun `score is low when expense greater than income`() = runTest {
        // income=2000, expense=8000 -> score = (2000-8000)/2000*100 = -300 -> clamped to 0
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(2000.0, 0.0)
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(8000.0, 0.0)

        val result = useCase()

        assertEquals(0, result.score)
        assertEquals("Critical", result.category)
    }

    @Test
    fun `score is 100 when no transactions at all`() = runTest {
        // income=0, expense=0 -> no transactions = 100
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returns 0.0
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returns 0.0

        val result = useCase()

        assertEquals(100, result.score)
    }

    @Test
    fun `score is 0 when only expenses and no income`() = runTest {
        // income=0, expense=5000 -> only expenses, no income = 0
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(0.0, 0.0)
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(5000.0, 0.0)

        val result = useCase()

        assertEquals(0, result.score)
        assertEquals("Critical", result.category)
    }

    @Test
    fun `score is clamped between 0 and 100`() = runTest {
        // income=10000, expense=500 -> score = 95, clamped
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(10000.0, 0.0)
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(500.0, 0.0)

        val result = useCase()

        assertTrue(result.score in 0..100)
        assertEquals(95, result.score)
    }

    // ===================== TREND CALCULATION =====================

    @Test
    fun `trend is UP when current rate better than previous by more than 5 percent`() = runTest {
        // Current month: income=10000, expense=2000 -> rate = 0.8
        // Previous month: income=10000, expense=6000 -> rate = 0.4
        // 0.8 > 0.4 + 0.05 -> UP
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(
            10000.0, // current income
            10000.0  // previous income
        )
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(
            2000.0,  // current expense
            6000.0   // previous expense
        )

        val result = useCase()

        assertEquals(HealthScore.Trend.UP, result.trend)
    }

    @Test
    fun `trend is DOWN when current rate worse than previous by more than 5 percent`() = runTest {
        // Current month: income=10000, expense=8000 -> rate = 0.2
        // Previous month: income=10000, expense=2000 -> rate = 0.8
        // 0.2 < 0.8 - 0.05 -> DOWN
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(
            10000.0, // current income
            10000.0  // previous income
        )
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(
            8000.0,  // current expense
            2000.0   // previous expense
        )

        val result = useCase()

        assertEquals(HealthScore.Trend.DOWN, result.trend)
    }

    @Test
    fun `trend is STABLE when rates are similar`() = runTest {
        // Current month: income=10000, expense=5000 -> rate = 0.5
        // Previous month: income=10000, expense=5200 -> rate = 0.48
        // Difference is 0.02 < 0.05 -> STABLE
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(
            10000.0, // current income
            10000.0  // previous income
        )
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(
            5000.0,  // current expense
            5200.0   // previous expense
        )

        val result = useCase()

        assertEquals(HealthScore.Trend.STABLE, result.trend)
    }

    @Test
    fun `trend is STABLE when both months have no income`() = runTest {
        // Both months: income=0 -> rates both 0 -> STABLE
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returns 0.0
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returns 0.0

        val result = useCase()

        assertEquals(HealthScore.Trend.STABLE, result.trend)
    }

    // ===================== DESCRIPTION / CATEGORY =====================

    @Test
    fun `category is Excellent when score is 80 or above`() = runTest {
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(10000.0, 0.0)
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(1000.0, 0.0)

        val result = useCase()

        assertEquals(90, result.score)
        assertEquals("Excellent", result.category)
    }

    @Test
    fun `category is Fair when score is between 40 and 59`() = runTest {
        // income=10000, expense=5500 -> score = 45
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(10000.0, 0.0)
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(5500.0, 0.0)

        val result = useCase()

        assertEquals(45, result.score)
        assertEquals("Fair", result.category)
    }

    @Test
    fun `category is Poor when score is between 20 and 39`() = runTest {
        // income=10000, expense=7000 -> score = 30
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returnsMany listOf(10000.0, 0.0)
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returnsMany listOf(7000.0, 0.0)

        val result = useCase()

        assertEquals(30, result.score)
        assertEquals("Poor", result.category)
    }

    @Test
    fun `calls getTotalIncome and getTotalExpense for current and previous month`() = runTest {
        coEvery { transactionRepository.getTotalIncome(any(), any()) } returns 0.0
        coEvery { transactionRepository.getTotalExpense(any(), any()) } returns 0.0

        useCase()

        // Should be called twice for income (current + previous) and twice for expense
        coVerify(exactly = 2) { transactionRepository.getTotalIncome(any(), any()) }
        coVerify(exactly = 2) { transactionRepository.getTotalExpense(any(), any()) }
    }
}
