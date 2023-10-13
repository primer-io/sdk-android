package io.primer.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import io.primer.android.R
import io.primer.android.databinding.PaymentMethodItemVaultBinding
import io.primer.android.ui.settings.PrimerTheme

internal enum class PaymentItemStatus {
    UNSELECTED, SELECTED, EDITING
}

internal enum class AlternativePaymentMethodType {
    PayPal, Klarna, DirectDebit, Apaya, Generic
}

internal sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    internal fun configureCheckIcon(
        binding: PaymentMethodItemVaultBinding,
        status: PaymentItemStatus,
        theme: PrimerTheme
    ) {
        val checkIcon: ImageView = binding.checkIcon
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

    class AlternativePaymentMethod(
        private val binding: PaymentMethodItemVaultBinding,
        private val theme: PrimerTheme
    ) : ViewHolder(binding.root) {

        private fun setCardIcon(type: AlternativePaymentMethodType) {
            val iconView = binding.paymentMethodIcon
            when (type) {
                AlternativePaymentMethodType.PayPal ->
                    iconView.setImageResource(R.drawable.ic_paypal_card)
                AlternativePaymentMethodType.Klarna ->
                    iconView.setImageResource(R.drawable.ic_klarna_card)
                AlternativePaymentMethodType.DirectDebit ->
                    iconView.setImageResource(R.drawable.ic_directdebit_card)
                AlternativePaymentMethodType.Apaya ->
                    iconView.setImageResource(R.drawable.ic_logo_apaya)
                AlternativePaymentMethodType.Generic ->
                    iconView.setImageResource(R.drawable.ic_generic_card)
            }
        }

        fun bind(item: AlternativePaymentMethodData, status: PaymentItemStatus) {
            val titleLabel = binding.titleLabel
            val lastFourLabel = binding.lastFourLabel
            val expiryLabel: TextView = binding.expiryLabel
            titleLabel.text = item.title
            lastFourLabel.text = ""
            expiryLabel.text = ""

            val textColor = theme.paymentMethodButton.text.defaultColor.getColor(
                itemView.context,
                theme.isDarkMode
            )
            titleLabel.setTextColor(textColor)

            setCardIcon(item.type)
            configureCheckIcon(binding, status, theme)
        }
    }

    class Card(private val binding: PaymentMethodItemVaultBinding, val theme: PrimerTheme) :
        ViewHolder(binding.root) {

        private fun setCardIcon(network: String?) {
            val iconView = binding.paymentMethodIcon
            when (network) {
                "Visa" -> iconView.setImageResource(R.drawable.ic_visa_card)
                "Mastercard" -> iconView.setImageResource(R.drawable.ic_mastercard_card)
                else -> iconView.setImageResource(R.drawable.ic_generic_card)
            }
        }

        fun bind(item: CardData, status: PaymentItemStatus) {
            val titleLabel = binding.titleLabel
            val lastFourLabel = binding.lastFourLabel
            val expiryLabel: TextView = binding.expiryLabel
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
            configureCheckIcon(binding, status, theme)
        }
    }
}

interface PaymentMethodItemData

internal data class AlternativePaymentMethodData(
    val title: String,
    val tokenId: String,
    val type: AlternativePaymentMethodType
) : PaymentMethodItemData

internal data class CardData(
    val title: String,
    val lastFour: Int,
    val expiryMonth: Int,
    val expiryYear: Int,
    val network: String?,
    val tokenId: String
) : PaymentMethodItemData

private const val VIEW_TYPE_ALTERNATIVE_PAYMENT_METHOD = 1
private const val VIEW_TYPE_CARD = 2

internal enum class VaultViewAction {
    SELECT, DELETE
}

internal class VaultedPaymentMethodRecyclerAdapter(
    private val onClickWith: (id: String, action: VaultViewAction) -> Unit,
    private val theme: PrimerTheme
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
                ViewHolder.AlternativePaymentMethod(
                    PaymentMethodItemVaultBinding.inflate(
                        inflater,
                        parent,
                        false
                    ),
                    theme
                )
            }
            VIEW_TYPE_CARD -> {
                ViewHolder.Card(
                    PaymentMethodItemVaultBinding.inflate(
                        inflater,
                        parent,
                        false
                    ),
                    theme
                )
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

    override fun getItemCount(): Int = itemData.size

    override fun getItemViewType(position: Int): Int =
        when (itemData[position]) {
            is CardData -> VIEW_TYPE_CARD
            is AlternativePaymentMethodData -> VIEW_TYPE_ALTERNATIVE_PAYMENT_METHOD
            else -> throw IllegalStateException("Unexpected view type \"${itemData[position]}\"")
        }
}
