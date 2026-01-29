package io.primer.android.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.LinearLayout
import io.primer.android.R
import io.primer.android.components.ui.views.PrimerPaymentMethodViewFactory
import io.primer.android.configuration.domain.model.Surcharge
import io.primer.android.payment.config.BaseDisplayMetadata
import io.primer.android.paymentMethods.core.ui.descriptors.PaymentMethodDropInDescriptor
import io.primer.android.surcharge.utils.SurchargeFormatter
import io.primer.android.ui.components.PaymentMethodButtonGroupBox

internal class PaymentMethodButtonGroupFactory(
    private var surcharges: Map<String, Surcharge>,
    private val formatter: SurchargeFormatter,
) {
    @Suppress("LongMethod", "CyclomaticComplexMethod", "NestedBlockDepth")
    fun build(
        context: Context,
        viewFactory: PrimerPaymentMethodViewFactory,
        displayMetadata: List<BaseDisplayMetadata>,
        descriptors: List<PaymentMethodDropInDescriptor>,
        onClick: (paymentMethod: PaymentMethodDropInDescriptor) -> Unit,
    ): List<PaymentMethodButtonGroupBox> {
        val surchargeMapping = mutableMapOf<Int, PaymentMethodButtonGroupBox>()
        descriptors.filter { displayMetadata.map { it.paymentMethodType }.contains(it.paymentMethodType) }
            .forEach { d ->
                val key = getPaymentMethodGroupKey(d)
                val box =
                    surchargeMapping[key] ?: PaymentMethodButtonGroupBox(context).apply {
                        if (surcharges.isNotEmpty()) {
                            if (key == 0) {
                                hideSurchargeFrame(FRAME_PADDING)
                            } else {
                                val text = getPaymentMethodSurchargeLabel(context = context, descriptor = d)
                                showSurchargeLabel(text)
                            }
                        }
                    }
                val button: View = viewFactory.getViewForPaymentMethod(
                    context = context,
                    displayMetadata.first {
                        d.paymentMethodType == it.paymentMethodType
                    },
                    box,
                )

                var matchingSurcharges = descriptors.count { item -> getPaymentMethodGroupKey(item) == key }

                if (key == 0) {
                    matchingSurcharges += descriptors.count { getPaymentMethodGroupKey(it) == 0 }
                }

                if (box.childCount < matchingSurcharges || surcharges.isEmpty()) {
                    button.layoutParams =
                        button.layoutParams.apply {
                            val layoutParams = this as LinearLayout.LayoutParams
                            layoutParams.bottomMargin =
                                context.resources.getDimension(R.dimen.medium_vertical_margin2).toInt()
                        }
                }

                button.setOnClickListener {
                    onClick(d)
                }
                box.addView(button)
                surchargeMapping[key] = box
            }

        if (surchargeMapping.keys.all { it == 0 }) {
            surchargeMapping.values.forEach {
                it.apply {
                    setPadding(0, 0, 0, 0)
                    background =
                        GradientDrawable().apply {
                            this.color = ColorStateList.valueOf(Color.WHITE)
                        }
                }
            }
        }

        return surchargeMapping.toSortedMap().map { it.value }
    }

    private fun getPaymentMethodGroupKey(descriptor: PaymentMethodDropInDescriptor): Int {
        val paymentMethodType = descriptor.paymentMethodType

        return when (val surcharge = surcharges[paymentMethodType]) {
            is Surcharge.CardNetworksSurcharge -> {
                if (surcharge.surcharges.any { it.value != 0 }) KEY_SURCHARGING_BOX else 0
            }
            is Surcharge.PaymentMethodSurcharge -> {
                surcharge.amount
            }
            null -> 0
        }
    }

    private fun getPaymentMethodSurchargeLabel(
        context: Context,
        descriptor: PaymentMethodDropInDescriptor,
    ): String {
        val paymentMethodType = descriptor.paymentMethodType

        return when (val surcharge = surcharges[paymentMethodType]) {
            is Surcharge.CardNetworksSurcharge -> formatter.getSurchargeLabelTextForPaymentMethodType(
                null,
                context = context,
            )

            is Surcharge.PaymentMethodSurcharge -> formatter.formatSurchargeAsString(
                surcharge.amount,
                context = context,
            )

            null -> ""
        }
    }

    companion object {
        private const val FRAME_PADDING = 24

        private const val KEY_SURCHARGING_BOX = 100000
    }
}
