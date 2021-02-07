package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormField
import io.primer.android.ui.FormProgressState
import io.primer.android.ui.FormTitleState

class CustomerAddressViewState(buttonLabelId: Int, showProgress: Boolean) : GoCardlessFormSceneState(
  Scene.ADDRESS,
  title = FormTitleState(
    titleId = R.string.enter_address,
  ),
  fields = listOf(
    FormField(
      name = DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_1,
      labelId = R.string.address_line_1,
      required = true,
      autoFocus = true,
      inputType = FormField.Type.POSTAL_ADDRESS,
    ),
    FormField(
      name = DD_FIELD_NAME_CUSTOMER_ADDRESS_LINE_2,
      labelId = R.string.address_line_2,
      required = false,
      inputType = FormField.Type.POSTAL_ADDRESS,
    ),
    FormField(
      name = DD_FIELD_NAME_CUSTOMER_ADDRESS_POSTAL_CODE,
      labelId = R.string.address_postal_code,
      required = true,
      inputType = FormField.Type.TEXT,
    ),
    FormField(
      name = DD_FIELD_NAME_CUSTOMER_ADDRESS_CITY,
      labelId = R.string.address_city,
      required = true,
      inputType = FormField.Type.TEXT,
    ),
    FormField(
      name = DD_FIELD_NAME_CUSTOMER_ADDRESS_COUNTRY_CODE,
      labelId = R.string.address_country_code,
      required = true,
      inputType = FormField.Type.COUNTRY_CODE,
      minLength = 2,
    ),
  ),
  button = ButtonState(
    labelId = buttonLabelId,
  ),
  progress = if (showProgress) FormProgressState(current = 4, max = 5) else null,
)