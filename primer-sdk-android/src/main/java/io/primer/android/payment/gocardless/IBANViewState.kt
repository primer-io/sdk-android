package io.primer.android.payment.gocardless

import io.primer.android.R
import io.primer.android.ui.ButtonState
import io.primer.android.ui.FormField
import io.primer.android.ui.FormProgressState
import io.primer.android.ui.FormTitleState

class IBANViewState(
    buttonLabelId: Int,
    cancelBehaviour: FormTitleState.CancelBehaviour,
    showProgress: Boolean,
    initialValues: Map<String, String>? = null,
) : GoCardlessFormSceneState(
    scene = Scene.IBAN,
    title = FormTitleState(
        titleId = R.string.add_bank_account,
        descriptionId = R.string.sepa_core_description,
        cancelBehaviour = cancelBehaviour,
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
        labelId = buttonLabelId,
    ),
    progress = if (showProgress) FormProgressState(current = 1, max = 5) else null,
    initialValues = initialValues,
)
