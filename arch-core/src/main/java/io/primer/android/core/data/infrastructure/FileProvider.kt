package io.primer.android.core.data.infrastructure

import java.io.File

fun interface FileProvider {

    fun getFile(path: String): File
}
