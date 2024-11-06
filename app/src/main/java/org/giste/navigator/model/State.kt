package org.giste.navigator.model

import android.net.Uri

data class State(
    val partial: Int = 0,
    val total: Int = 0,
    val roadbookUri: Uri = Uri.EMPTY,
)
