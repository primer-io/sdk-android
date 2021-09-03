package com.example.myapplication

import android.widget.ImageView
import android.widget.TextView
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import io.primer.android.model.dto.PaymentMethodToken

class PaymentMethodItem(
    private val token: PaymentMethodToken,
    private val onSelect: (token: PaymentMethodToken) -> Unit,
) : Item<GroupieViewHolder>() {

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val tokenLabel = viewHolder.itemView.findViewById<TextView>(R.id.token_title)
        val icon = viewHolder.itemView.findViewById<ImageView>(R.id.icon_view)

        viewHolder.itemView.setOnClickListener { onSelect(token) }

        when (token.paymentInstrumentType) {
            "KLARNA_CUSTOMER_TOKEN" -> {
                val account = token.paymentInstrumentData?.sessionData?.billingAddress?.email
                tokenLabel.text = "Email: $account" ?: "Pay with Klarna"
                icon.setImageResource(R.drawable.ic_klarna_card)
            }
            "PAYPAL_BILLING_AGREEMENT" -> {
                tokenLabel.text = token.paymentInstrumentData?.externalPayerInfo?.email ?: "PayPal"
                icon.setImageResource(R.drawable.ic_paypal_card)
            }
            "APAYA" -> {
                tokenLabel.text =
                    "${token.paymentInstrumentData?.hashedIdentifier}"
                icon.setImageResource(R.drawable.ic_mobile)
            }
            "PAYMENT_CARD" -> {
                tokenLabel.text =
                    ("Card ending with " + token.paymentInstrumentData?.last4Digits.toString())
                icon.setImageResource(R.drawable.ic_klarna_card)
                when (token.paymentInstrumentData?.network) {
                    "Visa" -> icon.setImageResource(io.primer.android.R.drawable.ic_visa_card)
                    "Mastercard" -> icon.setImageResource(io.primer.android.R.drawable.ic_mastercard_card)
                    else -> icon.setImageResource(io.primer.android.R.drawable.ic_generic_card)
                }
            }
            else -> {
                tokenLabel.text = "Saved card"
                icon.setImageResource(R.drawable.ic_generic_card)
            }
        }
    }

    override fun getLayout(): Int =
        R.layout.payment_method_item_row
}