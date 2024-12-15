package org.giste.navigator.roadbook

interface RoadbookDatasource {
    suspend fun loadRoadbook(uri: String)
    fun getPageCount(): Int
    suspend fun loadPages(startPosition: Int, loadSize: Int): List<RoadbookPage>
}