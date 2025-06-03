package io.primer.composable.internal.domain.interactor

import io.primer.android.components.domain.error.PrimerInputValidationError
import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerListener
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository

class SetRawDataManagerListenerInteractor : DISdkComponent {

    private val rawDataManagerRepository: RawDataManagerRepository by lazy { resolve() }

    fun setListener(onValidationChanged: (isValid: Boolean, errors: List<PrimerInputValidationError>) -> Unit) {
        val listener = object : PrimerHeadlessUniversalCheckoutRawDataManagerListener {
            override fun onValidationChanged(
                isValid: Boolean,
                errors: List<PrimerInputValidationError>
            ) {
                onValidationChanged(isValid, errors)
            }
        }
        
        rawDataManagerRepository.setListener(listener)
    }
}
