package io.primer.composable.internal.domain.interactor

import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.android.paymentmethods.PrimerRawData
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository

class SetRawDataInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    fun setRawData(rawData: PrimerRawData) {
        rawDataManagerRepository.setRawData(rawData)
    }
}
