package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.*

class SummaryViewState(
  val customerName: String,
  val customerAddress: String,
  val bankDetails: String,
  val companyAddress: String,
  val legalText: String,
) : GoCardlessFormSceneState(
  scene = Scene.SUMMARY,
  title = FormTitleState(
    titleId = R.string.confirm_dd_mandate,
  ),
  button = ButtonState(
    labelId = R.string.confirm,
  ),
  summary = FormSummaryState(
    items = listOf(
      InteractiveSummaryItem(
        name = "customer",
        iconId = R.drawable.icon_user,
        label = customerName,
      ),
      InteractiveSummaryItem(
        name = "address",
        iconId = R.drawable.icon_location_pin,
        label = customerAddress,
      ),
      InteractiveSummaryItem(
        name = "bank",
        iconId = R.drawable.icon_bank,
        label = bankDetails,
      )
    ),
    text = listOf(
      TextSummaryItem(
        content = companyAddress
      ),
      TextSummaryItem(
        content = legalText,
        styleId = R.style.Primer_Text_SmallPrint
      )
    )
  )
)