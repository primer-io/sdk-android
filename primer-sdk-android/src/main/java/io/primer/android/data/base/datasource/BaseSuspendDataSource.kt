package io.primer.android.data.base.datasource

internal fun interface BaseSuspendDataSource<out R, T : Any> where R : Any {
    suspend fun execute(input: T): R
}
