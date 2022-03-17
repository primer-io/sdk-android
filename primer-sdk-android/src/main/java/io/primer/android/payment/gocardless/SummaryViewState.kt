package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormSummaryState
import io.primer.android.ui.FormTitleState
import io.primer.android.ui.InteractiveSummaryItem
import io.primer.android.ui.TextSummaryItem

class SummaryViewState(
    val getCustomerName: () -> String,
    val getCustomerEmail: () -> String,
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
                name = "customer-name",
                iconId = R.drawable.ic_person,
                getLabel = getCustomerName,
            ),
            InteractiveSummaryItem(
                name = "customer-email",
                iconId = R.drawable.ic_mail,
                getLabel = getCustomerEmail,
            ),
            InteractiveSummaryItem(
                name = "address",
                iconId = R.drawable.ic_location_pin,
                getLabel = getCustomerAddress,
            ),
            InteractiveSummaryItem(
                name = "bank",
                iconId = R.drawable.ic_logo_gocardless,
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
