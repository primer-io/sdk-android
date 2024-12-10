package io.primer.android.core.data.datasource

fun interface BaseSuspendDataSource<out R, T : Any> where R : Any {
    suspend fun execute(input: T): R
}
