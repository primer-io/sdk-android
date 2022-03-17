package io.primer.android.data.base.datasource

import kotlinx.coroutines.flow.Flow

internal interface BaseDataSource<out R, T : Any> where R : Any {

    fun get(): R
}

internal interface BaseCacheDataSource<out R, T : Any> where R : Any {

    fun get(): R

    fun update(input: T) = Unit
}

internal interface BaseFlowCacheDataSource<out R, T : Any> where R : Any {

    fun get(): Flow<R>

    fun update(input: T) = Unit
}
