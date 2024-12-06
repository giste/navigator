package org.giste.navigator.model

interface MapRepository {
    fun getMaps(): List<String>
}