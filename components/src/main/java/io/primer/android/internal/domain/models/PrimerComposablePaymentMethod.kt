package io.primer.android.internal.domain.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.core.models.PrimerPaymentMethodManagerCategory
import io.primer.android.configuration.domain.model.Surcharge

data class PrimerComposablePaymentMethod(
    val paymentMethodType: String,
    val paymentMethodName: String?,
    val supportedPrimerSessionIntents: List<PrimerSessionIntent>,
    val paymentMethodManagerCategories: List<PrimerPaymentMethodManagerCategory>,
    val surcharge: Surcharge? = null,
)
