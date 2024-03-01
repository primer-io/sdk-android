package io.primer.android.ui.fragments.klarna.model

import android.view.View

internal sealed interface KlarnaPaymentCategory {
    val id: String
    val name: String
    val iconUrl: String

    data class UnselectedKlarnaPaymentCategory(
        override val id: String,
        override val name: String,
        override val iconUrl: String
    ) : KlarnaPaymentCategory

    data class SelectedKlarnaPaymentCategory(
        override val id: String,
        override val name: String,
        override val iconUrl: String,
        val view: View
    ) : KlarnaPaymentCategory
}
