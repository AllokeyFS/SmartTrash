package com.example.smarttrash.viewmodel

import com.example.smarttrash.data.local.entity.WasteItemEntity
import com.example.smarttrash.data.repository.WasteRepository
import com.example.smarttrash.ui.viewmodel.SyncState
import com.example.smarttrash.ui.viewmodel.WasteViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WasteViewModelTest {

    private val repository = mockk<WasteRepository>()
    private lateinit var viewModel: WasteViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    private val testItems = listOf(
        WasteItemEntity("1", "Plastic Bottle", "Plastic",   "Rinse and recycle"),
        WasteItemEntity("2", "Battery",        "Hazardous", "Take to collection point")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { repository.searchWasteItems(any()) } returns flowOf(testItems)
        every { repository.getAllWasteItems() }       returns flowOf(testItems)
        every { repository.getWasteItemsByCategory(any()) } returns flowOf(testItems)
        coEvery { repository.syncWasteItemsFromRemote() }   returns Result.success(Unit)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSearchQueryChange updates searchQuery`() = runTest {
        viewModel = WasteViewModel(repository)
        viewModel.onSearchQueryChange("plastic")
        assertEquals("plastic", viewModel.searchQuery.value)
    }

    @Test
    fun `onCategorySelected updates selectedCategory`() = runTest {
        viewModel = WasteViewModel(repository)
        viewModel.onCategorySelected("Plastic")
        assertEquals("Plastic", viewModel.selectedCategory.value)

        viewModel.onCategorySelected(null)
        assertNull(viewModel.selectedCategory.value)
    }

    @Test
    fun `syncState is Error when remote fails`() = runTest {
        coEvery { repository.syncWasteItemsFromRemote() } returns
                Result.failure(Exception("No internet"))

        viewModel = WasteViewModel(repository)
        viewModel.syncFromRemote()
        advanceUntilIdle()

        assertTrue(viewModel.syncState.value is SyncState.Error)
    }
}
