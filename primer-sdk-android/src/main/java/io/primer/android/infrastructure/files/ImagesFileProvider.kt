package io.primer.android.infrastructure.files

import android.content.Context
import java.io.File

internal class ImagesFileProvider(private val context: Context) {

    fun getFile(path: String): File {
        val directory = File(context.filesDir, FILES_DIRECTORY)
        if (!directory.exists()) directory.mkdirs()
        val file = File(
            context.filesDir,
            FILES_DIRECTORY + File.pathSeparator + path
        )
        if (!file.exists()) file.createNewFile()
        return file
    }

    companion object {
        const val FILES_DIRECTORY = "primer-sdk/images/"
    }
}
