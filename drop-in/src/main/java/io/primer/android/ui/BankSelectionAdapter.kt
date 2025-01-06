package io.primer.android.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import io.primer.android.R
import io.primer.android.components.utils.ImageLoader
import io.primer.android.databinding.ItemBankSelectBinding
import io.primer.android.databinding.ItemBankSelectDisabledBinding
import io.primer.android.databinding.ItemBankSelectLoadingBinding
import io.primer.android.ui.base.recyclerview.BaseAdapterItem
import io.primer.android.ui.base.recyclerview.BaseRecyclerViewAdapter
import io.primer.android.ui.base.recyclerview.BaseViewHolder
import io.primer.android.ui.extensions.setCompoundDrawablesWithIntrinsicBoundsTinted
import io.primer.android.ui.settings.PrimerTheme

internal interface BankSelectionAdapterListener {
    fun onBankSelected(issuerId: String)
}

internal enum class BankItemType {
    BANK_ITEM_ENABLED,
    BANK_ITEM_DISABLED,
    BANK_ITEM_LOADING,
}

internal abstract class BaseBankItem(
    open val id: String,
    open val name: String,
    open val logoUrl: String,
) : BaseAdapterItem

internal data class BankItem(
    override val id: String,
    override val name: String,
    override val logoUrl: String,
) : BaseBankItem(id, name, logoUrl) {
    override fun getType() = BankItemType.BANK_ITEM_ENABLED.ordinal

    fun toDisabledBankItem() = BankItemDisabled(id, name, logoUrl)

    fun toLoadingBankItem() = BankItemLoading(id, name, logoUrl)
}

internal data class BankItemDisabled(
    override val id: String,
    override val name: String,
    override val logoUrl: String,
) : BaseBankItem(id, name, logoUrl) {
    override fun getType() = BankItemType.BANK_ITEM_DISABLED.ordinal
}

internal data class BankItemLoading(
    override val id: String,
    override val name: String,
    override val logoUrl: String,
) : BaseBankItem(id, name, logoUrl) {
    override fun getType() = BankItemType.BANK_ITEM_LOADING.ordinal
}

internal class BaseBankBinding(
    val name: TextView,
    val icon: ImageView,
    val root: View,
)

internal fun ItemBankSelectBinding.toBaseBankBinding() = BaseBankBinding(name, icon, root)

internal fun ItemBankSelectDisabledBinding.toBaseBankBinding() = BaseBankBinding(name, icon, root)

internal fun ItemBankSelectLoadingBinding.toBaseBankBinding() = BaseBankBinding(name, icon, root)

internal open class BaseBankViewHolder(
    private val binding: BaseBankBinding,
    private val imageLoader: ImageLoader,
    protected val theme: PrimerTheme,
) : BaseViewHolder<BaseBankItem>(binding.root) {
    override fun bind(item: BaseBankItem) {
        val bankName = binding.name
        bankName.text = item.name
        bankName.setTextColor(
            theme.titleText.defaultColor.getColor(
                binding.root.context,
                theme.isDarkMode,
            ),
        )

        imageLoader.loadImage(
            item.logoUrl,
            TextDrawable(binding.root.resources, item.name.first().toString()),
            binding.icon,
        )
    }

    override fun unbind() {
        binding.icon.setImageBitmap(null)
        imageLoader.clear(binding.icon)
    }
}

internal class BankViewHolder(
    private val binding: ItemBankSelectBinding,
    imageLoader: ImageLoader,
    theme: PrimerTheme,
    private val listener: BankSelectionAdapterListener,
) : BaseBankViewHolder(binding.toBaseBankBinding(), imageLoader, theme) {
    override fun bind(item: BaseBankItem) {
        super.bind(item)
        binding.name.setCompoundDrawablesWithIntrinsicBoundsTinted(
            0,
            0,
            R.drawable.ic_arrow_right,
            0,
            theme.input.text.defaultColor.getColor(binding.root.context, theme.isDarkMode),
        )
        itemView.setOnClickListener {
            listener.onBankSelected(item.id)
        }
    }
}

internal class BankViewDisabledHolder(
    private val binding: ItemBankSelectDisabledBinding,
    imageLoader: ImageLoader,
    theme: PrimerTheme,
) : BaseBankViewHolder(binding.toBaseBankBinding(), imageLoader, theme) {
    override fun bind(item: BaseBankItem) {
        super.bind(item)
        binding.name.setCompoundDrawablesWithIntrinsicBoundsTinted(
            0,
            0,
            R.drawable.ic_arrow_right,
            0,
            theme.input.text.defaultColor.getColor(binding.root.context, theme.isDarkMode),
        )
    }
}

internal class BankViewLoadingHolder(
    private val binding: ItemBankSelectLoadingBinding,
    imageLoader: ImageLoader,
    theme: PrimerTheme,
) : BaseBankViewHolder(binding.toBaseBankBinding(), imageLoader, theme) {
    override fun bind(item: BaseBankItem) {
        super.bind(item)
        binding.progressBar.indeterminateDrawable.setTint(
            theme.primaryColor.getColor(
                binding.root.context,
                theme.isDarkMode,
            ),
        )
    }
}

internal class BankSelectionAdapter(
    private val listener: BankSelectionAdapterListener,
    private val imageLoader: ImageLoader,
    private val theme: PrimerTheme,
) : BaseRecyclerViewAdapter<BaseBankItem>(::compare) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ) = when (viewType) {
        BankItemType.BANK_ITEM_ENABLED.ordinal ->
            BankViewHolder(
                ItemBankSelectBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                ),
                imageLoader,
                theme,
                listener,
            )
        BankItemType.BANK_ITEM_DISABLED.ordinal ->
            BankViewDisabledHolder(
                ItemBankSelectDisabledBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                ),
                imageLoader,
                theme,
            )
        BankItemType.BANK_ITEM_LOADING.ordinal ->
            BankViewLoadingHolder(
                ItemBankSelectLoadingBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false,
                ),
                imageLoader,
                theme,
            )
        else -> throw IllegalStateException("Invalid $viewType.")
    }
}

internal fun compare(
    old: BaseBankItem,
    new: BaseBankItem,
) = old.id == new.id
