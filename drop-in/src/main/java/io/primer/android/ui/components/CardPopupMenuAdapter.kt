package io.primer.android.ui.components

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import io.primer.android.R
import io.primer.android.components.assets.ui.getCardImageAsset
import io.primer.android.configuration.data.model.CardNetwork
import io.primer.android.databinding.DropdownItemWithImageAndCheckBinding
import io.primer.android.displayMetadata.domain.model.ImageColor

internal class CardPopupMenuAdapter(
    private val context: Context,
    private val menuList: List<CardNetwork.Type>,
    private val selectedMethod: CardNetwork.Type? = null
) : ArrayAdapter<CardNetwork.Type>(context, 0, menuList) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: DropdownItemWithImageAndCheckBinding
        val view: View
        if (convertView == null) {
            binding = DropdownItemWithImageAndCheckBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as DropdownItemWithImageAndCheckBinding
            view = convertView
        }

        val isSelected = menuList[position] == selectedMethod
        with(binding) {
            imageViewCardNetworkLogo.setImageResource(menuList[position].getCardImageAsset(ImageColor.COLORED))
            textViewCardNetworkName.text = menuList[position].displayName
            imageViewCheckmark.isVisible = isSelected
            linearLayoutCardNetworkDescription.background = ColorDrawable(
                ContextCompat.getColor(
                    context,
                    if (isSelected) {
                        R.color.primer_gray_100
                    } else {
                        R.color.design_default_color_background
                    }
                )
            )
        }
        return view
    }
}
