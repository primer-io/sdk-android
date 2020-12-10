package io.primer.android.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import io.primer.android.R
import io.primer.android.logging.Logger
import io.primer.android.payment.TokenAttributes

internal class VaultedPaymentMethodView(context: Context, private val attributes: TokenAttributes) {
  private val log = Logger("vaulted-payment-method")

  private val mView: ViewGroup =
    View.inflate(context, R.layout.vaulted_payment_method_card, null) as ViewGroup
  private val mListItemNormal =
    ResourcesCompat.getDrawable(context.resources, R.drawable.list_item_normal, null)
  private val mListItemEditable =
    ResourcesCompat.getDrawable(context.resources, R.drawable.list_item_editable, null)


  private val mDelete: Button
    get () = mView.findViewById(R.id.item_delete_button)

  private val mIcon: ImageView
    get () = mView.findViewById(R.id.item_icon_left)

  private val mContentLayout: ViewGroup
    get () = mView.findViewById(R.id.item_content_layout)

  private val mDescription: TextView
    get () = mView.findViewById(R.id.item_description)

  init {
    mIcon.setImageDrawable(
      ResourcesCompat.getDrawable(mView.context.resources, attributes.icon, null)
    )
    mDescription.text = attributes.getDescription(context)
    setEditable(false)
  }

  fun setOnDeleteListener(l: ((v: View) -> Unit)) {
    mDelete.setOnClickListener(l)
  }

  fun setEditable(isEditable: Boolean) {
    mDelete.visibility = if (isEditable) View.VISIBLE else View.INVISIBLE
    mContentLayout.background = if (isEditable) mListItemEditable else mListItemNormal
    mContentLayout.elevation = if (isEditable) 12f else 0f
  }

  fun getView(): View {
    return mView
  }
}