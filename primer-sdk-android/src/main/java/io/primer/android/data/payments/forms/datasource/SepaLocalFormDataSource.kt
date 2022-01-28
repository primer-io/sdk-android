package io.primer.android.data.payments.forms.datasource

import io.primer.android.PrimerTheme
import io.primer.android.R
import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.payments.forms.models.ButtonType
import io.primer.android.data.payments.forms.models.FormDataResponse
import io.primer.android.data.payments.forms.models.FormInputDataResponse
import io.primer.android.data.payments.forms.models.FormType
import kotlinx.coroutines.flow.flowOf

internal class SepaLocalFormDataSource(private val theme: PrimerTheme) :
    BaseFlowCacheDataSource<FormDataResponse, String> {

    override fun get() = flowOf(
        FormDataResponse(
            R.string.input_title_bank_details,
            if (theme.isDarkMode == true) R.drawable.ic_logo_sepa_dark
            else R.drawable.ic_logo_sepa_light,
            ButtonType.CONFIRM,
            null,
            listOf(
                FormInputDataResponse(
                    FormType.IBAN,
                    FORM_IBAN_ID,
                    R.string.iban,
                    null,
                    null,
                    null,
                    null,
                    FORM_IBAN_VALIDATION
                ),
                FormInputDataResponse(
                    FormType.TEXT,
                    FORM_NAME_ID,
                    R.string.first_last_name,
                    null,
                    null,
                    null,
                    null,
                    FORM_NAME_VALIDATION
                )
            )
        )
    )

    private companion object {

        const val FORM_IBAN_ID = "ibanNumber"
        const val FORM_NAME_ID = "ownerName"
        const val FORM_NAME_VALIDATION = "^(\\w[\\.,\\-]*)+(\\s+(\\w[\\.,\\-]*)+)+"
        private val FORM_IBAN_VALIDATION: String = arrayOf(
            "AD\\d{10}[A-Z0-9]{12}", // Andorra
            "AT\\d{18}", // Austria
            "BE\\d{14}", // Belgium
            "BG\\d{2}[A-Z]{4}\\d{6}[A-Z0-9]{8}", // Bulgaria
            "CH\\d{7}[A-Z0-9]{12}", // Switzerland
            "CY\\d{10}[A-Z0-9]{16}", // Cyprus
            "CZ\\d{22}", // Czech Republic
            "DE\\d{20}", // Germany
            "DK\\d{16}", // Denmark
            "EE\\d{18}", // Estonia
            "ES\\d{22}", // Spain
            "FI\\d{16}", // Finland
            "FO\\d{16}", // Denmark (Faroes)
            "FR\\d{12}[A-Z0-9]{11}\\d{2}", // France
            "GB\\d{2}[A-Z]{4}\\d{14}", // United Kingdom
            "GL\\d{16}", // Denmark (Greenland)
            "GR\\d{9}[A-Z0-9]{16}", // Greece
            "HR\\d{19}", // Croatia
            "HU\\d{26}", // Hungary
            "IE\\d{2}[A-Z]{4}\\d{14}", // Ireland
            "IL\\d{21}", // Israel
            "IS\\d{24}", // Iceland
            "IT\\d{2}[A-Z]{1}\\d{10}[A-Z0-9]{12}", // Italy
            "LI\\d{7}[A-Z0-9]{12}", // Liechtenstein (Principality of)
            "LT\\d{18}", // Lithuania
            "LU\\d{5}[A-Z0-9]{13}", // Luxembourg
            "LV\\d{2}[A-Z]{4}[A-Z0-9]{13}", // Latvia
            "MC\\d{12}[A-Z0-9]{11}\\d{2}", // Monaco
            "MT\\d{2}[A-Z]{4}\\d{5}[A-Z0-9]{18}", // Malta
            "NL\\d{2}[A-Z]{4}\\d{10}", // The Netherlands
            "NO\\d{13}", // Norway
            "PL\\d{26}", // Poland
            "PT\\d{23}", // Portugal
            "RO\\d{2}[A-Z]{4}[A-Z0-9]{16}", // Romania
            "SE\\d{22}", // Sweden
            "SI\\d{17}", // Slovenia
            "SK\\d{22}", // Slovak Republic
            "SM\\d{2}[A-Z]{1}\\d{10}[A-Z0-9]{12}", // San Marino
        ).joinToString("|")
    }
}
