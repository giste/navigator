package org.giste.navigator.roadbook

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.giste.navigator.model.RoadbookScroll
import org.giste.navigator.roadbook.RoadbookRepositoryImpl.Companion.ROADBOOK_PAGE_INDEX
import org.giste.navigator.roadbook.RoadbookRepositoryImpl.Companion.ROADBOOK_PAGE_OFFSET
import org.giste.navigator.roadbook.RoadbookRepositoryImpl.Companion.ROADBOOK_URI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException
import java.io.IOException

private const val TEST_DATASTORE: String = "test_datastore"

class RoadbookRepositoryImplInstrumentedTests {
    private val testContext: Context = ApplicationProvider.getApplicationContext()
    private val testDataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            produceFile = { testContext.preferencesDataStoreFile(TEST_DATASTORE) }
        )
    private val pagingSourceFactory: PagingSourceFactory = FakePagingSourceFactory(50)

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
            pagingSourceFactory = pagingSourceFactory,
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

    class FakePagingSourceFactory(private val pageCount: Int = 50) : PagingSourceFactory {
        override fun createPagingSource(uri: String): PagingSource<Int, RoadbookPage> {
            return FakePagingSource(pageCount)
        }

    }

    class FakePagingSource(private val pageCount: Int) : PagingSource<Int, RoadbookPage>() {
        override fun getRefreshKey(state: PagingState<Int, RoadbookPage>): Int? {
            return 0
        }

        override suspend fun load(params: LoadParams<Int>): LoadResult<Int, RoadbookPage> {
            val position = params.key ?: 0

            try {
                val bitmaps = this.loadPages(position, params.loadSize)
                val prevKey = if (position == 0) {
                    null
                } else {
                    safeStart(position - params.loadSize)
                }
                val nextKey = if ((position + params.loadSize) >= pageCount) {
                    null
                } else {
                    safeEnd(position + params.loadSize)
                }

                return LoadResult.Page(bitmaps, prevKey, nextKey)

            } catch (e: IOException) {
                return LoadResult.Error(e)
            } catch (e: SecurityException) {
                return LoadResult.Error(e)
            } catch (e: FileNotFoundException) {
                return LoadResult.Error(e)
            }
        }

        private fun loadPages(startPosition: Int, loadSize: Int): List<RoadbookPage> {
            val pages = mutableListOf<RoadbookPage>()

            for (index in startPosition until safeEnd(startPosition + loadSize)) {
                pages.add(RoadbookPage(index, getBitmap()))
            }

            return pages
        }

        private fun getBitmap(): Bitmap {
            return Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        }

        private fun safeStart(startPosition: Int): Int =
            startPosition.coerceAtLeast(0)

        private fun safeEnd(endPosition: Int): Int = endPosition.coerceAtMost(pageCount)
    }



}