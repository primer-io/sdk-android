package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.*

class SummaryViewState(
  val getCustomerName: () -> String,
  val getCustomerAddress: () -> String,
  val getBankDetails: () -> String,
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
        getLabel = getCustomerName,
      ),
      InteractiveSummaryItem(
        name = "address",
        iconId = R.drawable.icon_location_pin,
        getLabel = getCustomerAddress,
      ),
      InteractiveSummaryItem(
        name = "bank",
        iconId = R.drawable.icon_bank,
        getLabel = getBankDetails,
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