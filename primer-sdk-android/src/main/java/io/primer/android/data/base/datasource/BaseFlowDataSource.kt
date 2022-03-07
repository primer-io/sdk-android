package io.primer.android.data.base.datasource

import kotlinx.coroutines.flow.Flow

internal interface BaseFlowDataSource<out R, T : Any> where R : Any {
    fun execute(input: T): Flow<R>
}
