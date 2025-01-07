package io.primer.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.primer.android.R
import io.primer.android.components.ui.assets.PrimerHeadlessUniversalCheckoutAssetsManager
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.databinding.PaymentMethodItemVaultBinding
import io.primer.android.ui.settings.PrimerTheme

internal enum class PaymentItemStatus {
    UNSELECTED,
    SELECTED,
    EDITING,
}

internal enum class AlternativePaymentMethodType {
    PayPal,
    Klarna,
    Generic,
}

internal sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    internal fun configureCheckIcon(
        binding: PaymentMethodItemVaultBinding,
        status: PaymentItemStatus,
        theme: PrimerTheme,
    ) {
        val checkIcon: ImageView = binding.checkIcon
        checkIcon.apply {
            when (status) {
                PaymentItemStatus.EDITING -> {
                    isInvisible = false
                    checkIcon.setImageResource(R.drawable.ic_delete)
                    DrawableCompat.setTint(
                        DrawableCompat.wrap(checkIcon.drawable),
                        theme.errorText.defaultColor.getColor(context, theme.isDarkMode),
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
                        theme.primaryColor.getColor(context, theme.isDarkMode),
                    )
                }
            }
        }
    }

    class AlternativePaymentMethod(
        private val binding: PaymentMethodItemVaultBinding,
        private val theme: PrimerTheme,
    ) : ViewHolder(binding.root) {
        private fun setPaymentMethodIcon(type: AlternativePaymentMethodType) {
            val iconView = binding.paymentMethodIcon
            when (type) {
                AlternativePaymentMethodType.PayPal ->
                    iconView.setImageResource(R.drawable.ic_paypal_card)

                AlternativePaymentMethodType.Klarna ->
                    iconView.setImageResource(R.drawable.ic_klarna_card)

                AlternativePaymentMethodType.Generic ->
                    iconView.setImageResource(R.drawable.ic_generic_card)
            }
        }

        fun bind(
            item: AlternativePaymentMethodData,
            status: PaymentItemStatus,
        ) {
            binding.bankLastFourLabel.isInvisible = true
            binding.bankNameLabel.isInvisible = true
            binding.lastFourLabel.isInvisible = true
            binding.expiryLabel.isInvisible = true
            binding.titleLabel.isVisible = true
            val titleLabel = binding.titleLabel
            titleLabel.text = item.title

            val textColor =
                theme.paymentMethodButton.text.defaultColor.getColor(
                    itemView.context,
                    theme.isDarkMode,
                )
            titleLabel.setTextColor(textColor)

            setPaymentMethodIcon(item.type)
            configureCheckIcon(binding, status, theme)
        }
    }

    class Card(private val binding: PaymentMethodItemVaultBinding, val theme: PrimerTheme) :
        ViewHolder(binding.root) {
        private fun setCardIcon(network: CardNetwork.Type) {
            val iconView = binding.paymentMethodIcon
            iconView.setImageDrawable(
                PrimerHeadlessUniversalCheckoutAssetsManager.getCardNetworkAsset(
                    binding.root.context,
                    network,
                ).cardImage,
            )
        }

        fun bind(
            item: CardData,
            status: PaymentItemStatus,
        ) {
            binding.bankLastFourLabel.isInvisible = true
            binding.bankNameLabel.isInvisible = true
            binding.titleLabel.isVisible = true
            binding.lastFourLabel.isVisible = true
            binding.expiryLabel.isVisible = true
            val titleLabel = binding.titleLabel
            val lastFourLabel = binding.lastFourLabel
            val expiryLabel: TextView = binding.expiryLabel
            titleLabel.text = item.title
            lastFourLabel.text = itemView.context.getString(R.string.last_four, item.lastFour)

            val expirationYear = "${item.expiryYear}"
            val expirationMonth = "${item.expiryMonth}".padStart(2, '0')
            expiryLabel.text =
                itemView.context
                    .getString(R.string.expiry_date, expirationMonth, expirationYear)

            val textColor =
                theme.paymentMethodButton.text.defaultColor.getColor(
                    itemView.context,
                    theme.isDarkMode,
                )
            titleLabel.setTextColor(textColor)
            lastFourLabel.setTextColor(textColor)
            expiryLabel.setTextColor(textColor)

            setCardIcon(item.network)
            configureCheckIcon(binding, status, theme)
        }
    }

    class Bank(private val binding: PaymentMethodItemVaultBinding, val theme: PrimerTheme) :
        ViewHolder(binding.root) {
        private fun setBankIcon() {
            binding.paymentMethodIcon.setImageResource(R.drawable.ic_bank_56)
        }

        fun bind(
            item: BankData,
            status: PaymentItemStatus,
        ) {
            binding.titleLabel.isInvisible = true
            binding.lastFourLabel.isInvisible = true
            binding.expiryLabel.isInvisible = true

            val textColor =
                theme.paymentMethodButton.text.defaultColor.getColor(
                    itemView.context,
                    theme.isDarkMode,
                )
            with(binding.bankNameLabel) {
                isVisible = true
                text = item.bankName
                setTextColor(textColor)
            }
            with(binding.bankLastFourLabel) {
                isVisible = true
                text = itemView.context.getString(R.string.last_four, item.lastFour)
            }
            setBankIcon()
            configureCheckIcon(binding, status, theme)
        }
    }
}

interface PaymentMethodItemData

internal data class AlternativePaymentMethodData(
    val title: String,
    val tokenId: String,
    val type: AlternativePaymentMethodType,
) : PaymentMethodItemData

internal data class CardData(
    val title: String,
    val lastFour: Int,
    val expiryMonth: Int,
    val expiryYear: Int,
    val network: CardNetwork.Type,
    val tokenId: String,
) : PaymentMethodItemData

internal data class BankData(
    val bankName: String,
    val lastFour: Int,
    val tokenId: String,
) : PaymentMethodItemData

private const val VIEW_TYPE_ALTERNATIVE_PAYMENT_METHOD = 1
private const val VIEW_TYPE_CARD = 2
private const val VIEW_TYPE_BANK = 3

internal enum class VaultViewAction {
    SELECT,
    DELETE,
}

internal class VaultedPaymentMethodRecyclerAdapter(
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

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_ALTERNATIVE_PAYMENT_METHOD -> {
                ViewHolder.AlternativePaymentMethod(
                    binding = createBinding(inflater, parent),
                    theme = theme,
                )
            }

            VIEW_TYPE_CARD -> {
                ViewHolder.Card(
                    binding = createBinding(inflater, parent),
                    theme = theme,
                )
            }

            VIEW_TYPE_BANK -> {
                ViewHolder.Bank(
                    binding = createBinding(inflater, parent),
                    theme = theme,
                )
            }

            else -> error("View type \"$viewType\" not valid")
        }
    }

    private fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
    ) = PaymentMethodItemVaultBinding.inflate(
        inflater,
        parent,
        false,
    )

    private fun invokeListener(id: String) {
        if (isEditing) {
            onClickWith(id, VaultViewAction.DELETE)
        } else {
            onClickWith(id, VaultViewAction.SELECT)
            selectedPaymentMethodId = id
        }
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        when (holder) {
            is ViewHolder.AlternativePaymentMethod -> {
                val item = itemData[position] as AlternativePaymentMethodData
                holder.itemView.setOnClickListener { invokeListener(item.tokenId) }
                holder.bind(item, getStatusForItemWith(item.tokenId))
            }

            is ViewHolder.Card -> {
                val item = itemData[position] as CardData
                holder.itemView.setOnClickListener { invokeListener(item.tokenId) }
                holder.bind(item, getStatusForItemWith(item.tokenId))
            }

            is ViewHolder.Bank -> {
                val item = itemData[position] as BankData
                holder.itemView.setOnClickListener { invokeListener(item.tokenId) }
                holder.bind(item, getStatusForItemWith(item.tokenId))
            }
        }
    }

    override fun getItemCount(): Int = itemData.size

    override fun getItemViewType(position: Int): Int =
        when (itemData[position]) {
            is CardData -> VIEW_TYPE_CARD
            is BankData -> VIEW_TYPE_BANK
            is AlternativePaymentMethodData -> VIEW_TYPE_ALTERNATIVE_PAYMENT_METHOD
            else -> error("Unexpected view type \"${itemData[position]}\"")
        }
}
