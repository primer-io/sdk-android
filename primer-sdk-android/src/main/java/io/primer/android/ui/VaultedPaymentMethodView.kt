package io.primer.android.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import io.primer.android.R
import io.primer.android.payment.TokenAttributes

// FIXME this should, itself, be a View
internal class VaultedPaymentMethodView(context: Context, private val attributes: TokenAttributes) {

    private val view: ViewGroup =
        View.inflate(context, R.layout.vaulted_payment_method_card, null) as ViewGroup
    private val listItemNormal =
        ResourcesCompat.getDrawable(context.resources, R.drawable.list_item_normal, null)
    private val listItemEditable =
        ResourcesCompat.getDrawable(context.resources, R.drawable.list_item_editable, null)

    private val deleteButton: Button
        get() = view.findViewById(R.id.item_delete_button)

    private val iconImageView: ImageView
        get() = view.findViewById(R.id.item_icon_left)

    private val contentLayout: ViewGroup
        get() = view.findViewById(R.id.item_content_layout)

    private val descriptionTextView: TextView
        get() = view.findViewById(R.id.item_description)

    init {
        iconImageView.setImageDrawable(
            ResourcesCompat.getDrawable(view.context.resources, attributes.icon, null)
        )
        iconImageView.scaleType = ImageView.ScaleType.CENTER_CROP
        iconImageView.scaleX = attributes.iconScale
        iconImageView.scaleY = attributes.iconScale
        descriptionTextView.text = attributes.getDescription(context)
        setEditable(false)
    }

    fun setOnDeleteListener(l: ((v: View) -> Unit)) {
        deleteButton.setOnClickListener(l)
    }

    fun setEditable(isEditable: Boolean) {
        deleteButton.visibility = if (isEditable) View.VISIBLE else View.INVISIBLE
        contentLayout.background = if (isEditable) listItemEditable else listItemNormal
        contentLayout.elevation =
            if (isEditable) EDITABLE_VIEW_ELEVATION else DEFAULT_VIEW_ELEVATION
    }

    private companion object {

        const val EDITABLE_VIEW_ELEVATION = 12f
        const val DEFAULT_VIEW_ELEVATION = 0f
    }
}
