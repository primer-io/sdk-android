package io.primer.composable.internal.data.repositories

import io.primer.android.components.manager.raw.PrimerHeadlessUniversalCheckoutRawDataManagerInterface
import io.primer.android.core.di.DISdkComponent
import io.primer.composable.internal.domain.repositories.RawDataManagerRepository

class RawDataManagerRepositoryImpl(
    private val cardManager: PrimerHeadlessUniversalCheckoutRawDataManagerInterface
) : RawDataManagerRepository, DISdkComponent {

}
