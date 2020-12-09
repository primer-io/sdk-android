package io.primer.android.ui

import android.content.Context
import android.media.Image
import android.util.AttributeSet
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import io.primer.android.R
import io.primer.android.payment.TokenAttributes

internal class SavedPaymentMethodView(context: Context, attributes: TokenAttributes) : LinearLayout(context) {
  private var mEditable = false
  private val mContent: LinearLayout
  private val mDeleteButton: Button

  init {
    // Set the view group layout paramms
    val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

    params.gravity = Gravity.CENTER_VERTICAL

    orientation = LinearLayout.HORIZONTAL
    layoutParams = params

    // Add the icon
    val icon = ImageView(context)
    icon.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0f)
    icon.setImageDrawable(ResourcesCompat.getDrawable(resources, attributes.icon, null))
    addView(icon)

    // Create the layout to contain the other items
    mContent = LinearLayout(context)
    mContent.layoutParams = LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
    (mContent.layoutParams as LayoutParams).gravity = Gravity.CENTER_VERTICAL
    mContent.elevation = 2f
    addView(mContent)

    // Add the text and delete button to the
    val description = TextView(context)
    description.layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
    description.text = attributes.description
    mContent.addView(description)

    mDeleteButton = Button(context)
    mDeleteButton.layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 0f)
    mDeleteButton.text = context.getString(R.string.delete)
    mContent.addView(mDeleteButton)
  }

  var isEditing: Boolean
    get() = mEditable
    set(value) = setEditable(value)

  private fun setEditable(editing: Boolean) {
    mEditable = editing

    mDeleteButton.isEnabled = mEditable
    mContent.isEnabled = mEditable
  }
}