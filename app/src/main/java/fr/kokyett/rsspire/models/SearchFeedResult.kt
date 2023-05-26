package fr.kokyett.rsspire.models

import android.graphics.Bitmap

class SearchFeedResult(
    val url: String,
    val title: String?,
    val iconUrl: String?,
    var bitmap: Bitmap? = null
)