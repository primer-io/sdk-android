package io.primer.android.paymentmethods.manager.composable

import io.primer.android.domain.error.models.PrimerError
import io.primer.android.paymentmethods.PrimerInitializationData

interface PrimerHeadlessDataInitializable {

    fun configure(completion: (PrimerInitializationData?, PrimerError?) -> Unit)
}
