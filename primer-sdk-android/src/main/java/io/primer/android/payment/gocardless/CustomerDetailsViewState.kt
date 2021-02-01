package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormField
import io.primer.android.ui.FormTitleState

class CustomerDetailsViewState : GoCardlessFormSceneState(
  Scene.CUSTOMER,
  title = FormTitleState(
    titleId = R.string.confirm_dd_mandate,
  ),
  fields = listOf(
    FormField(
      name = DD_FIELD_NAME_CUSTOMER_NAME,
      labelId = R.string.name,
      required = true,
      autoFocus = true,
      inputType = FormField.Type.PERSON_NAME,
      minWordCount = 2,
    ),
    FormField(
      name = DD_FIELD_NAME_CUSTOMER_EMAIL,
      labelId = R.string.email,
      required = true,
      inputType = FormField.Type.EMAIL,
    ),
  ),
  button = ButtonState(
    labelId = R.string.confirm,
    placement = ButtonState.Placement.LEFT,
  )
)