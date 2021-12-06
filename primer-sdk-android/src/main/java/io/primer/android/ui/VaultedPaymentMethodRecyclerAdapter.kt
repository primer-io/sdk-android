package io.primer.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import io.primer.android.PrimerTheme
import io.primer.android.R

enum class PaymentItemStatus {
    UNSELECTED, SELECTED, EDITING
}

enum class AlternativePaymentMethodType {
    PayPal, Klarna, DirectDebit, Generic
}

sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal fun configureCheckIcon(
        itemView: View,
        status: PaymentItemStatus,
        theme: PrimerTheme,
    ) {
        val checkIcon: ImageView = itemView.findViewById(R.id.check_icon)
        checkIcon.apply {
            when (status) {
                PaymentItemStatus.EDITING -> {
                    isInvisible = false
                    checkIcon.setImageResource(R.drawable.ic_delete)
                    DrawableCompat.setTint(
                        DrawableCompat.wrap(checkIcon.drawable),
                        theme.errorText.defaultColor.getColor(context, theme.isDarkMode)
                    )
                }
                PaymentItemStatus.UNSELECTED -> {
                    isInvisible = true
                }
                PaymentItemStatus.SELECTED -> {
                    isInvisible = false
                    checkIcon.setImageResource(R.drawable.ic_check)
                    DrawableCompat.setTint(
                        DrawableCompat.wrap(checkIcon.drawable),
                        theme.primaryColor.getColor(context, theme.isDarkMode)
                    )
                }
            }
        }
    }

    class AlternativePaymentMethod(view: View, val theme: PrimerTheme) : ViewHolder(view) {

        private fun setCardIcon(type: AlternativePaymentMethodType) {
            val iconView = itemView.findViewById<ImageView>(R.id.payment_method_icon)
            when (type) {
                AlternativePaymentMethodType.PayPal ->
                    iconView.setImageResource(R.drawable.ic_paypal_card)
                AlternativePaymentMethodType.Klarna ->
                    iconView.setImageResource(R.drawable.ic_klarna_card)
                AlternativePaymentMethodType.DirectDebit ->
                    iconView.setImageResource(R.drawable.ic_directdebit_card)
                AlternativePaymentMethodType.Generic ->
                    iconView.setImageResource(R.drawable.ic_generic_card)
            }
        }

        fun bind(item: AlternativePaymentMethodData, status: PaymentItemStatus) {

            val titleLabel = itemView.findViewById<TextView>(R.id.title_label)
            val lastFourLabel = itemView.findViewById<TextView>(R.id.last_four_label)
            val expiryLabel: TextView = itemView.findViewById(R.id.expiry_label)
            titleLabel.text = item.title
            lastFourLabel.text = ""
            expiryLabel.text = ""

            val textColor = theme.paymentMethodButton.text.defaultColor.getColor(
                itemView.context,
                theme.isDarkMode
            )
            titleLabel.setTextColor(textColor)

            setCardIcon(item.type)
            configureCheckIcon(itemView, status, theme)
        }
    }

    class Card(view: View, val theme: PrimerTheme) : ViewHolder(view) {

        private fun setCardIcon(network: String?) {
            val iconView = itemView.findViewById<ImageView>(R.id.payment_method_icon)
            when (network) {
                "Visa" -> iconView.setImageResource(R.drawable.ic_visa_card)
                "Mastercard" -> iconView.setImageResource(R.drawable.ic_mastercard_card)
                else -> iconView.setImageResource(R.drawable.ic_generic_card)
            }
        }

        fun bind(item: CardData, status: PaymentItemStatus) {
            val titleLabel = itemView.findViewById<TextView>(R.id.title_label)
            val lastFourLabel = itemView.findViewById<TextView>(R.id.last_four_label)
            val expiryLabel: TextView = itemView.findViewById(R.id.expiry_label)
            titleLabel.text = item.title
            lastFourLabel.text = itemView.context.getString(R.string.last_four, item.lastFour)

            val expirationYear = "${item.expiryYear}"
            val expirationMonth = "${item.expiryMonth}".padStart(2, '0')
            expiryLabel.text = itemView.context
                .getString(R.string.expiry_date, expirationMonth, expirationYear)

            val textColor = theme.paymentMethodButton.text.defaultColor.getColor(
                itemView.context,
                theme.isDarkMode
            )
            titleLabel.setTextColor(textColor)
            lastFourLabel.setTextColor(textColor)
            expiryLabel.setTextColor(textColor)

            setCardIcon(item.network)
            configureCheckIcon(itemView, status, theme)
        }
    }
}

interface PaymentMethodItemData

data class AlternativePaymentMethodData(
    val title: String,
    val tokenId: String,
    val type: AlternativePaymentMethodType,
) : PaymentMethodItemData

data class CardData(
    val title: String,
    val lastFour: Int,
    val expiryMonth: Int,
    val expiryYear: Int,
    val network: String?,
    val tokenId: String,
) : PaymentMethodItemData

private const val VIEW_TYPE_ALTERNATIVE_PAYMENT_METHOD = 1
private const val VIEW_TYPE_CARD = 2

enum class VaultViewAction {
    SELECT, DELETE
}

class VaultedPaymentMethodRecyclerAdapter(
    private val onClickWith: (id: String, action: VaultViewAction) -> Unit,
    private val theme: PrimerTheme,
) : RecyclerView.Adapter<ViewHolder>() {

    var selectedPaymentMethodId: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var itemData: List<PaymentMethodItemData> = listOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var isEditing: Boolean = false

    private fun getStatusForItemWith(id: String) =
        when {
            isEditing -> PaymentItemStatus.EDITING
            selectedPaymentMethodId == id -> PaymentItemStatus.SELECTED
            else -> PaymentItemStatus.UNSELECTED
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ALTERNATIVE_PAYMENT_METHOD -> {
                val view = inflater.inflate(R.layout.payment_method_item_vault, parent, false)
                ViewHolder.AlternativePaymentMethod(view, theme)
            }
            VIEW_TYPE_CARD -> {
                val view = inflater.inflate(R.layout.payment_method_item_vault, parent, false)
                ViewHolder.Card(view, theme)
            }
            else -> throw IllegalStateException("View type \"$viewType\" not valid")
        }
    }

    private fun invokeListener(id: String) {
        if (isEditing) {
            onClickWith(id, VaultViewAction.DELETE)
        } else {
            onClickWith(id, VaultViewAction.SELECT)
            selectedPaymentMethodId = id
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder.AlternativePaymentMethod -> {
                val item = itemData[position] as AlternativePaymentMethodData
                holder.itemView.setOnClickListener {
                    invokeListener(item.tokenId)
                }
                holder.bind(item, getStatusForItemWith(item.tokenId))
            }
            is ViewHolder.Card -> {
                val item = itemData[position] as CardData
                holder.itemView.setOnClickListener { invokeListener(item.tokenId) }
                holder.bind(item, getStatusForItemWith(item.tokenId))
            }
        }
    }

    override fun getItemCount(): Int =
        itemData.size

    override fun getItemViewType(position: Int): Int =
        when (itemData[position]) {
            is CardData -> VIEW_TYPE_CARD
            is AlternativePaymentMethodData -> VIEW_TYPE_ALTERNATIVE_PAYMENT_METHOD
            else -> throw IllegalStateException("Unexpected view type \"${itemData[position]}\"")
        }
}
