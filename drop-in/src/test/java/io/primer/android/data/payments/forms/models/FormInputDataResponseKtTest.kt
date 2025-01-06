package io.primer.android.data.payments.forms.models

import android.text.InputType
import io.mockk.mockk
import io.primer.android.data.payments.forms.models.helper.DialCodeCountryPrefix
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FormInputDataResponseKtTest {
    @Test
    fun `toInputType() returns correct input type for FormType TEXT`() {
        val result = FormType.TEXT.toInputType()
        assertEquals(InputType.TYPE_CLASS_TEXT, result)
    }

    @Test
    fun `toInputType() returns correct input type for FormType PHONE`() {
        val result = FormType.PHONE.toInputType()
        assertEquals(InputType.TYPE_CLASS_PHONE, result)
    }

    @Test
    fun `toInputType() returns correct input type for FormType NUMBER`() {
        val result = FormType.NUMBER.toInputType()
        assertEquals(InputType.TYPE_CLASS_NUMBER, result)
    }

    @Test
    fun `toInputType() returns correct input type for FormType IBAN`() {
        val result = FormType.IBAN.toInputType()
        assertEquals(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS, result)
    }

    @Test
    fun `toForm() maps FormDataResponse to Form correctly`() {
        val formDataResponse =
            FormDataResponse(
                title = 123,
                logo = 456,
                buttonType = ButtonType.CONFIRM,
                description = 789,
                inputs =
                    listOf(
                        FormInputDataResponse(
                            type = FormType.TEXT,
                            id = "input1",
                            hint = 111,
                            inputCharacters = "abc",
                            maxInputLength = 10,
                            validation = "regex",
                            inputPrefix = mockk<DialCodeCountryPrefix>(),
                        ),
                    ),
                accountNumber = "123456",
                expiration = "12/34",
                inputPrefix = mockk<DialCodeCountryPrefix>(),
            )

        val form = formDataResponse.toForm()

        assertEquals(formDataResponse.title, form.title)
        assertEquals(formDataResponse.logo, form.logo)
        assertEquals(formDataResponse.buttonType, form.buttonType)
        assertEquals(formDataResponse.description, form.description)
        assertEquals(formDataResponse.accountNumber, form.accountNumber)
        assertEquals(formDataResponse.expiration, form.expiration)
        assertEquals(formDataResponse.inputPrefix, form.inputPrefix)

        val formInput = form.inputs?.single()
        val formInputDataResponse = formDataResponse.inputs?.single()

        assertEquals(formInputDataResponse?.type, formInput?.formType)
        assertEquals(formInputDataResponse?.type?.toInputType(), formInput?.inputType)
        assertEquals(formInputDataResponse?.id, formInput?.id)
        assertEquals(formInputDataResponse?.hint, formInput?.hint)
        assertEquals(formInputDataResponse?.inputCharacters, formInput?.inputCharacters)
        assertEquals(formInputDataResponse?.maxInputLength, formInput?.maxInputLength)
        assertEquals(formInputDataResponse?.validation, formInput?.regex?.pattern)
        assertEquals(formInputDataResponse?.inputPrefix, formInput?.inputPrefix)
    }

    @Test
    fun `toFormInput() maps FormInputDataResponse to FormInput correctly`() {
        val formInputDataResponse =
            FormInputDataResponse(
                type = FormType.NUMBER,
                id = "input1",
                hint = 222,
                inputCharacters = "123",
                maxInputLength = 5,
                validation = "regex",
                inputPrefix = mockk<DialCodeCountryPrefix>(),
            )

        val formInput = formInputDataResponse.toFormInput()

        assertEquals(formInputDataResponse.type, formInput.formType)
        assertEquals(formInputDataResponse.type.toInputType(), formInput.inputType)
        assertEquals(formInputDataResponse.id, formInput.id)
        assertEquals(formInputDataResponse.hint, formInput.hint)
        assertEquals(formInputDataResponse.inputCharacters, formInput.inputCharacters)
        assertEquals(formInputDataResponse.maxInputLength, formInput.maxInputLength)
        assertEquals(formInputDataResponse.validation, formInput.regex?.pattern)
        assertEquals(formInputDataResponse.inputPrefix, formInput.inputPrefix)
    }
}
