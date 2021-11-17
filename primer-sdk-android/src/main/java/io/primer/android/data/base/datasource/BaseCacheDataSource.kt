package io.primer.android.data.base.datasource

import kotlinx.coroutines.flow.Flow

internal abstract class BaseCacheDataSource<out R, T : Any> where R : Any {

    abstract fun get(): R

    abstract fun update(input: T)
}

internal abstract class BaseFlowCacheDataSource<out R, T : Any> where R : Any {

    abstract fun get(): Flow<R>

    abstract fun update(input: T)
}
