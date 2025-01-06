package io.primer.android.core.data.datasource

import kotlinx.coroutines.flow.Flow

interface BaseDataSource<out R, T : Any> where R : Any {
    fun get(): R
}

interface BaseCacheDataSource<out R, T : Any> where R : Any? {
    fun get(): R

    fun update(input: T) = Unit

    fun clear() = Unit
}

interface BaseFlowCacheDataSource<out R, T : Any> where R : Any {
    fun get(): Flow<R>

    fun update(input: T) = Unit
}
