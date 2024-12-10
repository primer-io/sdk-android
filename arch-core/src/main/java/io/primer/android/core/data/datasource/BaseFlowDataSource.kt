package io.primer.android.core.data.datasource

import kotlinx.coroutines.flow.Flow

fun interface BaseFlowDataSource<out R, T : Any> where R : Any {
    fun execute(input: T): Flow<R>
}
