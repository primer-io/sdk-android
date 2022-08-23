package io.primer.android.data.payments.forms.models.helper

import io.primer.android.domain.action.models.PrimerPhoneCode
import io.primer.android.domain.payments.forms.models.FormInputPrefix

internal data class DialCodeCountryPrefix(val phoneCode: PrimerPhoneCode) : FormInputPrefix
