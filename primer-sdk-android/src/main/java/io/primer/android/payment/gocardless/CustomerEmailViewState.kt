package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormField
import io.primer.android.ui.FormTitleState

class CustomerEmailViewState(buttonLabelId: Int) : GoCardlessFormSceneState(
  Scene.CUSTOMER_EMAIL,
  title = FormTitleState(
    titleId = R.string.enter_email,
  ),
  fields = listOf(
    FormField(
      name = DD_FIELD_NAME_CUSTOMER_EMAIL,
      labelId = R.string.email,
      required = true,
      autoFocus = true,
      inputType = FormField.Type.EMAIL,
    ),
  ),
  button = ButtonState(
    labelId = buttonLabelId,
  )
)