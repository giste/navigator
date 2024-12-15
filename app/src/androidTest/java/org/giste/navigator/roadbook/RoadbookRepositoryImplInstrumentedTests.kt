package org.giste.navigator.roadbook

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.giste.navigator.model.RoadbookScroll
import org.giste.navigator.roadbook.RoadbookRepositoryImpl.Companion.ROADBOOK_PAGE_INDEX
import org.giste.navigator.roadbook.RoadbookRepositoryImpl.Companion.ROADBOOK_PAGE_OFFSET
import org.giste.navigator.roadbook.RoadbookRepositoryImpl.Companion.ROADBOOK_URI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private const val TEST_DATASTORE: String = "test_datastore"

class RoadbookRepositoryImplInstrumentedTests {
    companion object {
        private val testContext: Context = ApplicationProvider.getApplicationContext()
        private val testDataStore: DataStore<Preferences> =
            PreferenceDataStoreFactory.create(
                produceFile = { testContext.preferencesDataStoreFile(TEST_DATASTORE) }
            )
    }

    private val roadbookDatasource: RoadbookDatasource = FakeRoadbookDatasource(50)

    private lateinit var repository: RoadbookRepository

    @BeforeEach
    fun beforeEach() = runTest {
        // Reset DataStore
        testDataStore.edit {
            it[ROADBOOK_PAGE_INDEX] = 0
            it[ROADBOOK_PAGE_OFFSET] = 0
            it[ROADBOOK_URI] = ""
        }
        repository = RoadbookRepositoryImpl(
            dataStore = testDataStore,
            roadbookDatasource = roadbookDatasource,
        )
    }

    @Test
    fun `when new scroll is provided scroll is saved`() = runTest {
        val scroll = RoadbookScroll(2, 50)

        repository.saveScroll(scroll)

        val readScroll = testDataStore.data.map {
            RoadbookScroll(
                pageIndex = it[ROADBOOK_PAGE_INDEX] ?: 0,
                pageOffset = it[ROADBOOK_PAGE_OFFSET] ?: 0
            )
        }.first()

        assertEquals(scroll, readScroll)
    }

    @Test
    fun `when new uri is loaded scroll is reset`() = runTest {
        // Set scroll
        testDataStore.edit {
            it[ROADBOOK_PAGE_INDEX] = 2
            it[ROADBOOK_PAGE_OFFSET] = 50
        }

        repository.load("new uri")

        val readScroll = testDataStore.data.map {
            RoadbookScroll(
                pageIndex = it[ROADBOOK_PAGE_INDEX] ?: 0,
                pageOffset = it[ROADBOOK_PAGE_OFFSET] ?: 0
            )
        }.first()
        assertEquals(RoadbookScroll(), readScroll)
    }

    @Test
    fun `given empty uri getPages returns NotLoaded`() = runTest {
        val result = repository.getPages().first()

        assertEquals(RoadbookResult.NotLoaded, result)
    }

    @Test
    fun `given an uri getPages returns Loaded result`() = runTest {
        testDataStore.edit {
            it[ROADBOOK_PAGE_INDEX] = 2
            it[ROADBOOK_PAGE_OFFSET] = 50
            it[ROADBOOK_URI] = "Non empty URI"
        }

        val result = repository.getPages().first()

        assertTrue(result is RoadbookResult.Loaded)
        assertEquals(RoadbookScroll(2, 50), (result as RoadbookResult.Loaded).initialScroll)
    }

    class FakeRoadbookDatasource(private val pageCount: Int) : RoadbookDatasource {
        override suspend fun loadRoadbook(uri: String) {

        }

        override fun getPageCount(): Int {
            return pageCount
        }

        override suspend fun loadPages(startPosition: Int, loadSize: Int): List<RoadbookPage> {
            val pages = mutableListOf<RoadbookPage>()

            for (index in startPosition until (startPosition + loadSize).coerceAtMost(pageCount)) {
                pages.add(RoadbookPage(index, getBitmap()))
            }

            return pages
        }

        private fun getBitmap(): Bitmap {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }
    }
}