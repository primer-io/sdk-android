package io.primer.android.payments.core.tokenization.presentation.composable

interface TokenizationCollectable<in I : TokenizationInputable> {

    suspend fun submitTokenizationInputData(input: I)
}
