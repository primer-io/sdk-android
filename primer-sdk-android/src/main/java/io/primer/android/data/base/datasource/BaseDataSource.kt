package io.primer.android.data.base.datasource

import kotlinx.coroutines.flow.Flow

internal abstract class BaseDataSource<out R, T : Any> where R : Any {
    abstract fun execute(input: T): Flow<R>
}
