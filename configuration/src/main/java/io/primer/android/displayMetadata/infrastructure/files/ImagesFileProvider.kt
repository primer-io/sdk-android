package io.primer.android.displayMetadata.infrastructure.files

import android.content.Context
import io.primer.android.core.data.infrastructure.FileProvider
import java.io.File

internal class ImagesFileProvider(private val context: Context) : FileProvider {
    override fun getFile(path: String): File {
        val directory = File(context.filesDir, FILES_DIRECTORY)
        if (!directory.exists()) directory.mkdirs()
        val file =
            File(
                context.filesDir,
                FILES_DIRECTORY + File.pathSeparator + path,
            )
        if (!file.exists()) file.createNewFile()
        return file
    }

    companion object {
        const val FILES_DIRECTORY = "primer-sdk/images/"
    }
}
