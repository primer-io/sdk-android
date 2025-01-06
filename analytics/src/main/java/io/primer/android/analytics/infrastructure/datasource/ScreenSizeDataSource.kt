package io.primer.android.analytics.infrastructure.datasource

import android.content.Context
import io.primer.android.analytics.data.models.ScreenData
import io.primer.android.core.data.datasource.BaseDataSource

internal class ScreenSizeDataSource(private val context: Context) :
    BaseDataSource<ScreenData, Unit> {
    private val displayMetrics by lazy { context.resources.displayMetrics }

    override fun get() = ScreenData(displayMetrics.heightPixels, displayMetrics.widthPixels)
}
