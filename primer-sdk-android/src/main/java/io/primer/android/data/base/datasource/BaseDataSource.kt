package io.primer.android.data.base.datasource

import kotlinx.coroutines.flow.Flow

internal interface BaseDataSource<out R, T : Any> where R : Any {
    fun execute(input: T): Flow<R>
}
