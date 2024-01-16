package io.primer.sample.adyen.ideal

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import io.primer.sample.databinding.ItemBankBinding
import io.primer.sample.utils.ImageLoader

data class BankItem(
    val id: String,
    val name: String,
    val logoUrl: String,
    val isLoading: Boolean,
    val isDisabled: Boolean,
)

internal class BankSelectionAdapter(
    private val onClick: (BankItem) -> Unit,
    private val imageLoader: ImageLoader,
) : RecyclerView.Adapter<BankSelectionAdapter.ViewHolder>() {

    var items: List<BankItem> = emptyList()
        @SuppressLint("NotifyDataSetChanged")
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            binding = ItemBankBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            imageLoader = imageLoader,
            onClick = onClick,
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(
        private val binding: ItemBankBinding,
        private val imageLoader: ImageLoader,
        private val onClick: (BankItem) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(bankItem: BankItem) {
            with(binding) {
                val isDisabledOrLoading = bankItem.isDisabled || bankItem.isLoading
                name.text = bankItem.name
                imageLoader.loadImage(bankItem.logoUrl, binding.icon)
                progressBar.isVisible = bankItem.isLoading
                rootLayout.setOnClickListener { onClick(bankItem) }
                rootLayout.isClickable = !isDisabledOrLoading
                name.setTextColor(
                    binding.root.context.resources.getColor(
                        if (bankItem.isDisabled) {
                            android.R.color.darker_gray
                        } else {
                            android.R.color.black
                        }
                    )
                )
            }
        }
    }
}