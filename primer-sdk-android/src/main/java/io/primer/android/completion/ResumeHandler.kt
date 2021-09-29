package io.primer.android.completion

import java.lang.Error

interface ResumeHandler {

    fun handleError(error: Error)

    fun handleSuccess()

    fun handleNewClientToken(clientToken: String)
}
