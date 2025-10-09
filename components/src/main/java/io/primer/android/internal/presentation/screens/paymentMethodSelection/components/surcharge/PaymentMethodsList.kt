package io.primer.android.internal.presentation.screens.paymentMethodSelection.components.surcharge

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.utils.getValue
import io.primer.android.scope.PrimerPaymentMethodSelectionScope

fun LazyListScope.paymentMethodsList(
    paymentMethods: List<PrimerComposablePaymentMethod>,
    scope: PrimerPaymentMethodSelectionScope,
) {
    val groupedPaymentMethods = paymentMethods.groupBy { it.surcharge?.getValue() ?: 0 }

    if (groupedPaymentMethods.size == 1 && groupedPaymentMethods.containsKey(0)) {
        items(groupedPaymentMethods[0] ?: emptyList()) { paymentMethod ->
            scope.components.paymentMethodItem(scope, paymentMethod)
        }
    } else {
        val sortedGroups = groupedPaymentMethods.toList()
        items(sortedGroups) { (value, methods) ->
            if (methods.isNotEmpty()) {
                with(scope) {
                    SurchargeGroupCard(
                        value = value,
                        paymentMethods = methods,
                    )
                }
            }
        }
    }
}
