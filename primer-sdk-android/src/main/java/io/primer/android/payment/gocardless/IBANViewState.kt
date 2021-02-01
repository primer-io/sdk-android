package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormField
import io.primer.android.ui.FormTitleState


class IBANViewState(initialValues: Map<String, String>? = null) : GoCardlessFormSceneState(
  scene = Scene.IBAN,
  title = FormTitleState(
    titleId = R.string.add_bank_account,
    descriptionId = R.string.sepa_core_description
  ),
  fields = listOf(
    FormField(
      name = DD_FIELD_NAME_IBAN,
      labelId = R.string.iban,
      inputType = FormField.Type.TEXT,
      required = true,
      autoFocus = true,
      minLength = 15
    )
  ),
  button = ButtonState(
    labelId = R.string.next,
    placement = ButtonState.Placement.RIGHT,
  ),
  initialValues = initialValues,
)