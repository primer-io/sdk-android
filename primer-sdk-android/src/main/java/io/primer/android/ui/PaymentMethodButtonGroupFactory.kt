package io.primer.android.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.LinearLayout
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.payment.PaymentMethodDescriptor
import io.primer.android.ui.components.PaymentMethodButtonGroupBox
import io.primer.android.utils.SurchargeFormatter

internal class PaymentMethodButtonGroupFactory(
    private var surcharges: Map<String, Int>,
    private val formatter: SurchargeFormatter,
) {

    fun build(
        context: Context,
        descriptors: List<PaymentMethodDescriptor>,
        onClick: (paymentMethod: PaymentMethodDescriptor) -> Unit,
    ): List<PaymentMethodButtonGroupBox> {

        val surchargeMapping = mutableMapOf<Int, PaymentMethodButtonGroupBox>()
        descriptors.forEach { d ->

            // todo: include card in main group if no surcharge

            if (d.config.type == PaymentMethodType.PAYMENT_CARD) {
                val box = PaymentMethodButtonGroupBox(context)
                val text = formatter
                    .getSurchargeLabelTextForPaymentMethodType(null, context)
                box.showSurchargeLabel(text)
                val button: View = d.createButton(box)
                button.setOnClickListener { onClick(d) }
                box.addView(button)
                surchargeMapping[100000] = box
            } else {
                val key = getSurcharge(d)
                val box = surchargeMapping[key] ?: PaymentMethodButtonGroupBox(context).apply {
                    if (surcharges.count() != 0) {
                        if (key == 0) {
                            hideSurchargeFrame(24)
                        } else {
                            val text = formatter.formatSurchargeAsString(key, context = context)
                            showSurchargeLabel(text)
                        }
                    }
                }
                val button: View = d.createButton(box)

                val nonCardOptions = surcharges.filter { item -> item.key != "PAYMENT_CARD" }

                var matchingSurcharges = nonCardOptions.count { item -> (item.value) == key }

                if (key == 0) {
                    matchingSurcharges += descriptors.count { getSurcharge(it) == 0 }
                }

                if (box.childCount < matchingSurcharges || surcharges.count() == 0) {
                    button.layoutParams = button.layoutParams.apply {
                        val layoutParams = this as LinearLayout.LayoutParams
                        layoutParams.bottomMargin = 20
                    }
                }

                button.setOnClickListener { onClick(d) }
                box.addView(button)
                surchargeMapping[key] = box
            }
        }

        if (surchargeMapping.keys.all { it == 0 }) {
            surchargeMapping.values.forEach {
                it.apply {
                    setPadding(0, 0, 0, 0)
                    background = GradientDrawable().apply {
                        this.color = ColorStateList.valueOf(Color.WHITE)
                    }
                }
            }
        }

        return surchargeMapping.toSortedMap().map { it.value }
    }

    private fun getSurcharge(descriptor: PaymentMethodDescriptor): Int {
        val paymentMethodType = descriptor.config.type.toString()
        return surcharges[paymentMethodType] ?: return 0
    }
}
