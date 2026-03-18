package com.example.smarttrash.repository

import com.example.smarttrash.data.local.dao.RecyclingBinDao
import com.example.smarttrash.data.local.dao.WasteItemDao
import com.example.smarttrash.data.local.entity.WasteItemEntity
import com.example.smarttrash.data.remote.FirestoreDataSource
import com.example.smarttrash.data.repository.WasteRepositoryImpl
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WasteRepositoryTest {

    // Создаём моки вручную — без аннотаций
    private val wasteItemDao    = mockk<WasteItemDao>()
    private val recyclingBinDao = mockk<RecyclingBinDao>()
    private val firestoreSource = mockk<FirestoreDataSource>()

    private lateinit var repository: WasteRepositoryImpl

    private val testItems = listOf(
        WasteItemEntity("1", "Plastic Bottle", "Plastic",   "Rinse and recycle"),
        WasteItemEntity("2", "Battery",        "Hazardous", "Take to collection point"),
        WasteItemEntity("3", "Glass Jar",      "Glass",     "Remove lid and rinse"),
        WasteItemEntity("4", "Newspaper",      "Paper",     "Keep dry")
    )

    @Before
    fun setup() {
        repository = WasteRepositoryImpl(
            wasteItemDao    = wasteItemDao,
            recyclingBinDao = recyclingBinDao,
            firestoreDataSource = firestoreSource
        )
    }

    @Test
    fun `searchWasteItems finds Battery with typo battry`() = runTest {
        every { wasteItemDao.getAllWasteItems() } returns flowOf(testItems)

        val results = mutableListOf<List<WasteItemEntity>>()
        repository.searchWasteItems("battry").collect { results.add(it) }

        assertTrue(
            "Should find Battery with typo battry",
            results.any { list -> list.any { it.name == "Battery" } }
        )
    }

    @Test
    fun `searchWasteItems returns all items for blank query`() = runTest {
        every { wasteItemDao.getAllWasteItems() } returns flowOf(testItems)

        val results = mutableListOf<List<WasteItemEntity>>()
        repository.searchWasteItems("").collect { results.add(it) }

        assertEquals(testItems.size, results.last().size)
    }

    @Test
    fun `syncWasteItemsFromRemote saves items to Room on success`() = runTest {
        coEvery { firestoreSource.fetchAllWasteItems() } returns Result.success(testItems)
        coEvery { wasteItemDao.upsertAll(any()) } just Runs

        val result = repository.syncWasteItemsFromRemote()

        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { wasteItemDao.upsertAll(testItems) }
    }

    @Test
    fun `syncWasteItemsFromRemote returns failure on network error`() = runTest {
        coEvery { firestoreSource.fetchAllWasteItems() } returns
                Result.failure(Exception("Network unavailable"))

        val result = repository.syncWasteItemsFromRemote()

        assertTrue(result.isFailure)
        assertEquals("Network unavailable", result.exceptionOrNull()?.message)
    }
}