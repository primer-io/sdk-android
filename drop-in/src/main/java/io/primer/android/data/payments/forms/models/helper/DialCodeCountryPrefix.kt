package io.primer.android.data.payments.forms.models.helper

import io.primer.android.clientSessionActions.domain.models.PrimerPhoneCode
import io.primer.android.domain.payments.forms.models.FormInputPrefix

internal data class DialCodeCountryPrefix(val phoneCode: PrimerPhoneCode) : FormInputPrefix
