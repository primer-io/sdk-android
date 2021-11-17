package io.primer.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.extensions.setCompoundDrawablesWithIntrinsicBoundsTinted
import io.primer.android.ui.base.recyclerview.BaseAdapterItem
import io.primer.android.ui.base.recyclerview.BaseRecyclerViewAdapter
import io.primer.android.ui.base.recyclerview.BaseViewHolder
import io.primer.android.utils.ImageLoader

internal interface BankSelectionAdapterListener {

    fun onBankSelected(issuerId: String)
}

internal enum class BankItemType {
    BANK_ITEM_ENABLED,
    BANK_ITEM_DISABLED,
    BANK_ITEM_LOADING
}

internal abstract class BaseBankItem(
    open val id: String,
    open val name: String,
    open val logoUrl: String
) : BaseAdapterItem()

internal data class BankItem(
    override val id: String,
    override val name: String,
    override val logoUrl: String
) : BaseBankItem(id, name, logoUrl) {

    override fun getType() = BankItemType.BANK_ITEM_ENABLED.ordinal

    fun toDisabledBankItem() = BankItemDisabled(id, name, logoUrl)

    fun toLoadingBankItem() = BankItemLoading(id, name, logoUrl)
}

internal data class BankItemDisabled(
    override val id: String,
    override val name: String,
    override val logoUrl: String
) : BaseBankItem(id, name, logoUrl) {

    override fun getType() = BankItemType.BANK_ITEM_DISABLED.ordinal
}

internal data class BankItemLoading(
    override val id: String,
    override val name: String,
    override val logoUrl: String
) : BaseBankItem(id, name, logoUrl) {

    override fun getType() = BankItemType.BANK_ITEM_LOADING.ordinal
}

internal open class BaseBankViewHolder(
    itemView: View,
    private val imageLoader: ImageLoader,
    protected val theme: PrimerTheme,
) : BaseViewHolder<BaseBankItem>(itemView) {

    override fun bind(item: BaseBankItem) {
        val bankName = itemView.findViewById<TextView>(R.id.name)
        bankName.text = item.name
        bankName
            .setTextColor(theme.titleText.defaultColor.getColor(itemView.context, theme.isDarkMode))

        imageLoader.loadImage(
            item.logoUrl, TextDrawable(itemView.resources, item.name.first().toString()),
            itemView.findViewById(R.id.icon)
        )
    }

    override fun unbind() {
        itemView.findViewById<ImageView>(R.id.icon).setImageBitmap(null)
        imageLoader.clear(itemView.findViewById(R.id.icon))
    }
}

internal class BankViewHolder(
    itemView: View,
    imageLoader: ImageLoader,
    theme: PrimerTheme,
    private val listener: BankSelectionAdapterListener
) : BaseBankViewHolder(itemView, imageLoader, theme) {

    override fun bind(item: BaseBankItem) {
        super.bind(item)
        itemView.findViewById<TextView>(R.id.name).setCompoundDrawablesWithIntrinsicBoundsTinted(
            0,
            0,
            R.drawable.ic_arrow_right,
            0,
            theme.input.text.defaultColor.getColor(itemView.context, theme.isDarkMode)
        )
        itemView.setOnClickListener {
            listener.onBankSelected(item.id)
        }
    }
}

internal class BankViewDisabledHolder(
    itemView: View,
    imageLoader: ImageLoader,
    theme: PrimerTheme,
) : BaseBankViewHolder(itemView, imageLoader, theme) {

    override fun bind(item: BaseBankItem) {
        super.bind(item)
        itemView.findViewById<TextView>(R.id.name).setCompoundDrawablesWithIntrinsicBoundsTinted(
            0,
            0,
            R.drawable.ic_arrow_right,
            0,
            theme.input.text.defaultColor.getColor(itemView.context, theme.isDarkMode)
        )
    }
}

internal class BankViewLoadingHolder(
    itemView: View,
    imageLoader: ImageLoader,
    theme: PrimerTheme,
) : BaseBankViewHolder(itemView, imageLoader, theme) {
    override fun bind(item: BaseBankItem) {
        super.bind(item)
        val progressBar = itemView.findViewById<ProgressBar>(R.id.progress_bar)
        progressBar.indeterminateDrawable.setTint(
            theme.primaryColor.getColor(
                itemView.context,
                theme.isDarkMode
            )
        )
    }
}

internal class BankSelectionAdapter(
    private val listener: BankSelectionAdapterListener,
    private val imageLoader: ImageLoader,
    private val theme: PrimerTheme
) : BaseRecyclerViewAdapter<BaseBankItem>(::compare) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        BankItemType.BANK_ITEM_ENABLED.ordinal -> BankViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_bank_select, parent, false,
            ),
            imageLoader,
            theme,
            listener
        )
        BankItemType.BANK_ITEM_DISABLED.ordinal -> BankViewDisabledHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_bank_select_disabled, parent, false,
            ),
            imageLoader,
            theme
        )
        BankItemType.BANK_ITEM_LOADING.ordinal -> BankViewLoadingHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_bank_select_loading, parent, false,
            ),
            imageLoader,
            theme,
        )
        else -> throw IllegalStateException("Invalid $viewType.")
    }
}

internal fun compare(old: BaseBankItem, new: BaseBankItem) = old.id == new.id
