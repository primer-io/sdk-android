package io.primer.android.data.configuration.models

import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ConfigurationDataResponseTest {

    private val configurationDataResponse by lazy {
        ConfigurationDataResponse.deserializer.deserialize(
            JSONObject(JSON_OBJECT)
        )
    }

    @Test
    fun `toConfigurationData() maps correctly`() {
        val configurationData = with(configurationDataResponse) {
            ConfigurationData(
                pciUrl,
                coreUrl,
                assetsUrl,
                paymentMethods,
                checkoutModules,
                keys,
                clientSession,
                environment,
                primerAccountId,
                listOf()
            )
        }

        assertEquals(configurationData, configurationDataResponse.toConfigurationData(listOf()))
    }

    @Test
    fun `'coreUrl' should be deserialized correctly`() {
        assertEquals(CORE_URL, configurationDataResponse.coreUrl)
    }

    @Test
    fun `'pciUrl' should be deserialized correctly`() {
        assertEquals(PCI_URL, configurationDataResponse.pciUrl)
    }

    @Test
    fun `'assetsUrl' should be deserialized correctly`() {
        assertEquals(ASSETS_URL, configurationDataResponse.assetsUrl)
    }

    @Test
    fun `'paymentMethod-id' should be deserialized correctly`() {
        assertEquals(PAYMENT_METHOD_ID, configurationDataResponse.paymentMethods.first().id)
    }

    @Test
    fun `'paymentMethod-type' should be deserialized correctly`() {
        assertEquals(PAYMENT_METHOD_TYPE, configurationDataResponse.paymentMethods.first().type)
    }

    @Test
    fun `'paymentMethod-options' should be deserialized correctly`() {
        val paymentMethodRemoteConfigOptions = listOf(
            PaymentMethodRemoteConfigOptions(
                PAYMENT_METHOD_OPTIONS_MERCHANT_ID,
                PAYMENT_METHOD_OPTIONS_MERCHANT_ACCOUNT_ID,
                null,
                null
            )
        )
        assertEquals(
            paymentMethodRemoteConfigOptions,
            configurationDataResponse.paymentMethods.map { it.options }
        )
    }

    @Test
    fun `'paymentMethod-implementationType' should be deserialized correctly`() {
        assertEquals(
            PaymentMethodImplementationType.NATIVE_SDK,
            configurationDataResponse.paymentMethods.first().implementationType
        )
    }

    @Test
    fun `'paymentMethod-name' should be deserialized correctly`() {
        assertEquals(
            PAYMENT_METHOD_NAME,
            configurationDataResponse.paymentMethods.first().name
        )
    }

    @Test
    fun `'paymentMethod-displayMetadata-button-iconUrl' should be deserialized correctly`() {
        val iconUrlDataResponse =
            PaymentMethodDisplayMetadataResponse.ButtonDataResponse.IconUrlDataResponse(
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_COLORED,
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_DARK,
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_LIGHT
            )
        assertEquals(
            iconUrlDataResponse,
            configurationDataResponse.paymentMethods.first().displayMetadata?.buttonData?.iconUrl
        )
    }

    @Test
    fun `'paymentMethod-displayMetadata-button-backgroundColorData' should be deserialized correctly`() {
        val colorDataResponse =
            PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse(
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_COLORED,
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_LIGHT,
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_DARK
            )
        assertEquals(
            colorDataResponse,
            configurationDataResponse.paymentMethods
                .first().displayMetadata?.buttonData?.backgroundColorData
        )
    }

    @Test
    fun `'paymentMethod-displayMetadata-button-cornerRadius' should be deserialized correctly`() {
        assertEquals(
            PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_RADIUS,
            configurationDataResponse.paymentMethods
                .first().displayMetadata?.buttonData?.cornerRadius
        )
    }

    @Test
    fun `'paymentMethod-displayMetadata-button-borderWidthData' should be deserialized correctly`() {
        val colorDataResponse =
            PaymentMethodDisplayMetadataResponse.ButtonDataResponse.BorderWidthDataResponse(
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_COLORED,
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_LIGHT,
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_DARK
            )
        assertEquals(
            colorDataResponse,
            configurationDataResponse.paymentMethods
                .first().displayMetadata?.buttonData?.borderWidthData
        )
    }

    @Test
    fun `'paymentMethod-displayMetadata-button-borderColorData' should be deserialized correctly`() {
        val colorDataResponse =
            PaymentMethodDisplayMetadataResponse.ButtonDataResponse.ColorDataResponse(
                null,
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_COLOR_LIGHT,
                PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_COLOR_DARK
            )
        assertEquals(
            colorDataResponse,
            configurationDataResponse.paymentMethods
                .first().displayMetadata?.buttonData?.borderColorData
        )
    }

    @Test
    fun `'checkout-module-card-information-type' should be deserialized correctly`() {
        assertEquals(
            CheckoutModuleType.CARD_INFORMATION,
            configurationDataResponse.checkoutModules[0].type
        )
    }

    @Test
    fun `'checkout-module-card-information-options' should be deserialized correctly`() {
        assertEquals(
            mapOf("cardHolderName" to true),
            configurationDataResponse.checkoutModules[0].options
        )
    }

    @Test
    fun `'checkout-module-billing-address-type' should be deserialized correctly`() {
        assertEquals(
            CheckoutModuleType.BILLING_ADDRESS,
            configurationDataResponse.checkoutModules[1].type
        )
    }

    @Test
    fun `'checkout-module-billing-address-options' should be deserialized correctly`() {
        assertEquals(
            mapOf(
                "countryCode" to true,
                "city" to true,
                "state" to true,
                "postalCode" to true,
                "addressLine1" to true,
                "addressLine2" to true,
                "phoneNumber" to true,
                "firstName" to true,
                "lastName" to true
            ),
            configurationDataResponse.checkoutModules[1].options
        )
    }

    @Test
    fun `'client-session-id' should be deserialized correctly`() {
        assertEquals(
            CLIENT_SESSION_ID,
            configurationDataResponse.clientSession?.clientSessionId
        )
    }

    @Test
    fun `'client-session-order-id' should be deserialized correctly`() {
        assertEquals(
            CLIENT_SESSION_ORDER_ID,
            configurationDataResponse.clientSession?.order?.orderId
        )
    }

    @Test
    fun `'client-session-order-countryCode' should be deserialized correctly`() {
        assertEquals(
            CountryCode.GB,
            configurationDataResponse.clientSession?.order?.countryCode
        )
    }

    @Test
    fun `'client-session-order-currencyCode' should be deserialized correctly`() {
        assertEquals(
            CLIENT_SESSION_ORDER_CURRENCY,
            configurationDataResponse.clientSession?.order?.currencyCode
        )
    }

    @Test
    fun `'client-session-order-totalOrderAmount' should be deserialized correctly`() {
        assertEquals(
            CLIENT_SESSION_ORDER_TOTAL_AMOUNT,
            configurationDataResponse.clientSession?.order?.totalOrderAmount
        )
    }

    @Test
    fun `'client-session-order-line-items' should be deserialized correctly`() {
        assertEquals(
            listOf(
                OrderDataResponse.LineItemDataResponse(
                    unitAmount = CLIENT_SESSION_ORDER_LINE_ITEMS_AMOUNT,
                    quantity = CLIENT_SESSION_ORDER_LINE_ITEMS_QUANTITY,
                    discountAmount = CLIENT_SESSION_ORDER_LINE_ITEMS_DISCOUNT_AMOUNT,
                    itemId = CLIENT_SESSION_ORDER_LINE_ITEMS_ITEM_ID,
                    description = CLIENT_SESSION_ORDER_LINE_ITEMS_ITEM_DESCRIPTION
                )
            ),
            configurationDataResponse.clientSession?.order?.lineItems
        )
    }

    @Test
    fun `'client-session-order-fees' should be deserialized correctly`() {
        assertEquals(
            listOf(
                OrderDataResponse.FeeDataResponse(
                    CLIENT_SESSION_ORDER_FEE_TYPE,
                    CLIENT_SESSION_ORDER_FEE_AMOUNT
                )
            ),
            configurationDataResponse.clientSession?.order?.fees
        )
    }

    @Test
    fun `'client-session-customer' should be deserialized correctly`() {
        assertEquals(
            CustomerDataResponse(
                customerId = CLIENT_SESSION_CUSTOMER_ID,
                mobileNumber = CLIENT_SESSION_CUSTOMER_MOBILE_NUMBER,
                firstName = CLIENT_SESSION_CUSTOMER_FIRST_NAME,
                lastName = CLIENT_SESSION_CUSTOMER_LAST_NAME,
                emailAddress = CLIENT_SESSION_CUSTOMER_EMAIL,
                billingAddress = AddressDataResponse(
                    CLIENT_SESSION_CUSTOMER_FIRST_NAME,
                    CLIENT_SESSION_CUSTOMER_LAST_NAME,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_LINE,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_LINE,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_POSTAL_CODE,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_CITY,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_STATE,
                    CountryCode.GB
                ),
                shippingAddress = AddressDataResponse(
                    CLIENT_SESSION_CUSTOMER_FIRST_NAME,
                    CLIENT_SESSION_CUSTOMER_LAST_NAME,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_LINE,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_LINE,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_POSTAL_CODE,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_CITY,
                    CLIENT_SESSION_CUSTOMER_ADDRESS_STATE,
                    CountryCode.GB
                ),
                nationalDocumentId = CLIENT_SESSION_CUSTOMER_NATIONAL_ID
            ),
            configurationDataResponse.clientSession?.customer
        )
    }

    @Test
    fun `'client-session-payment-method' should be deserialized correctly`() {
        assertEquals(
            ClientSessionDataResponse.PaymentMethodDataResponse(
                false,
                listOf(
                    ClientSessionDataResponse.PaymentMethodOptionDataResponse(
                        PaymentMethodType.PAYMENT_CARD.name,
                        null,
                        listOf(
                            ClientSessionDataResponse.NetworkOptionDataResponse(
                                "VISA",
                                100
                            )
                        )
                    ),
                    ClientSessionDataResponse.PaymentMethodOptionDataResponse(
                        PaymentMethodType.PAYPAL.name,
                        50,
                        null
                    )
                )
            ),
            configurationDataResponse.clientSession?.paymentMethod
        )
    }

    @Test
    fun `'primer-account-id' should be deserialized correctly`() {
        assertEquals(
            PRIMER_ACCOUNT_ID,
            configurationDataResponse.primerAccountId
        )
    }

    @Test
    fun `'env' should be deserialized correctly`() {
        assertEquals(
            Environment.STAGING,
            configurationDataResponse.environment
        )
    }

    private companion object {

        const val CORE_URL = "https://api.staging.primer.io"
        const val PCI_URL = "https://sdk.api.staging.primer.io"
        const val ASSETS_URL = "https://assets.staging.core.primer.io"
        const val PAYMENT_METHOD_ID = "a02e7d8b-8749-4bf6-a1d6"
        const val PAYMENT_METHOD_TYPE = "XENDIT_OVO"
        const val PAYMENT_METHOD_OPTIONS_MERCHANT_ID = "364218b4-8d33-48d1-a849"
        const val PAYMENT_METHOD_OPTIONS_MERCHANT_ACCOUNT_ID = "d09d6311-3a3b-5f9b-aef3"
        const val PAYMENT_METHOD_IMPLEMENTATION_TYPE = "NATIVE_SDK"
        const val PAYMENT_METHOD_NAME = "Ovo"
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_COLORED =
            "https://assets.staging.core.primer.io/OVO/ovo-logo-dark@3x.png"
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_DARK =
            "https://assets.staging.core.primer.io/OVO/ovo-logo-dark@3x.png"
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_LIGHT =
            "https://assets.staging.core.primer.io/OVO/ovo-logo-dark@3x.png"
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_COLORED = "#4B2489"
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_DARK = "#000000"
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_LIGHT = "#FFFFFF"
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_RADIUS = 4f
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_COLORED = 0f
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_DARK = 1f
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_LIGHT = 1f
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_COLOR_DARK = "#FFFFFF"
        const val PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_COLOR_LIGHT = "#212121"
        const val CLIENT_SESSION_ID = "ea59c16b-18e9-436e-bc99"
        const val CLIENT_SESSION_ORDER_ID = "android-test-7a8d891e-09cf-48d6-a68f"
        const val CLIENT_SESSION_ORDER_CURRENCY = "GBP"
        const val CLIENT_SESSION_ORDER_TOTAL_AMOUNT = 10160
        const val CLIENT_SESSION_ORDER_LINE_ITEMS_AMOUNT = 10100
        const val CLIENT_SESSION_ORDER_LINE_ITEMS_QUANTITY = 1
        const val CLIENT_SESSION_ORDER_LINE_ITEMS_DISCOUNT_AMOUNT = 0
        const val CLIENT_SESSION_ORDER_LINE_ITEMS_ITEM_ID = "item-123"
        const val CLIENT_SESSION_ORDER_LINE_ITEMS_ITEM_DESCRIPTION = "item"
        const val CLIENT_SESSION_ORDER_FEE_TYPE = "SURCHARGE"
        const val CLIENT_SESSION_ORDER_FEE_AMOUNT = 60

        const val CLIENT_SESSION_CUSTOMER_ID = "customer8"
        const val CLIENT_SESSION_CUSTOMER_EMAIL = "test@mail.co"
        const val CLIENT_SESSION_CUSTOMER_MOBILE_NUMBER = "80002026"
        const val CLIENT_SESSION_CUSTOMER_FIRST_NAME = "John"
        const val CLIENT_SESSION_CUSTOMER_LAST_NAME = "Doe"
        const val CLIENT_SESSION_CUSTOMER_ADDRESS_LINE = "Address line"
        const val CLIENT_SESSION_CUSTOMER_ADDRESS_POSTAL_CODE = "71999"
        const val CLIENT_SESSION_CUSTOMER_ADDRESS_CITY = "test"
        const val CLIENT_SESSION_CUSTOMER_ADDRESS_STATE = "test"
        const val CLIENT_SESSION_CUSTOMER_NATIONAL_ID = "9011211234567"

        const val PRIMER_ACCOUNT_ID = "d634b2c6-17d8-4347-bac0"
        const val ENV = "STAGING"

        const val JSON_OBJECT =
            """
       {
           "coreUrl":"$CORE_URL",
           "pciUrl":"$PCI_URL",
           "assetsUrl":"$ASSETS_URL",
           "paymentMethods":[
              {
                 "id":"$PAYMENT_METHOD_ID",
                 "type":"$PAYMENT_METHOD_TYPE",
                 "options":{
                    "merchantId":"$PAYMENT_METHOD_OPTIONS_MERCHANT_ID",
                    "merchantAccountId":"$PAYMENT_METHOD_OPTIONS_MERCHANT_ACCOUNT_ID"
                 },
                 "implementationType":"$PAYMENT_METHOD_IMPLEMENTATION_TYPE",
                 "name":"$PAYMENT_METHOD_NAME",
                 "displayMetadata":{
                    "button":{
                       "iconUrl":{
                          "colored":"$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_COLORED",
                          "dark":"$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_DARK",
                          "light":"$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_ICON_URL_LIGHT"
                       },
                       "backgroundColor":{
                          "colored":"$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_COLORED",
                          "dark":"$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_DARK",
                          "light":"$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BACKGROUND_COLOR_LIGHT"
                       },
                       "cornerRadius":$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_RADIUS,
                       "borderWidth":{
                          "colored":$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_COLORED,
                          "dark":$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_DARK,
                          "light":$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_WIDTH_LIGHT
                       },
                       "borderColor":{
                          "dark":"$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_COLOR_DARK",
                          "light":"$PAYMENT_METHOD_DISPLAY_METADATA_BUTTON_BORDER_COLOR_LIGHT"
                       }
                    }
                 }
              },
           ],
           "checkoutModules":[
              {
                 "type":"CARD_INFORMATION",
                 "options":{
                    "cardHolderName":true
                 }
              },
              {
                 "type":"BILLING_ADDRESS",
                 "options":{
                    "countryCode":true,
                    "city":true,
                    "state":true,
                    "postalCode":true,
                    "addressLine1":true,
                    "addressLine2":true,
                    "phoneNumber":true,
                    "firstName":true,
                    "lastName":true
                 }
              }
           ],
           "clientSession":{
              "clientSessionId":"$CLIENT_SESSION_ID",
              "order":{
                 "orderId":"$CLIENT_SESSION_ORDER_ID",
                 "currencyCode":"$CLIENT_SESSION_ORDER_CURRENCY",
                 "countryCode":"GB",
                 "totalOrderAmount":$CLIENT_SESSION_ORDER_TOTAL_AMOUNT,
                 "lineItems":[
                    {
                       "amount":$CLIENT_SESSION_ORDER_LINE_ITEMS_AMOUNT,
                       "quantity":$CLIENT_SESSION_ORDER_LINE_ITEMS_QUANTITY,
                       "discountAmount":$CLIENT_SESSION_ORDER_LINE_ITEMS_DISCOUNT_AMOUNT,
                       "itemId":"$CLIENT_SESSION_ORDER_LINE_ITEMS_ITEM_ID",
                       "description":"$CLIENT_SESSION_ORDER_LINE_ITEMS_ITEM_DESCRIPTION"
                    }
                 ],
                 "fees":[
                    {
                       "type":$CLIENT_SESSION_ORDER_FEE_TYPE,
                       "amount":$CLIENT_SESSION_ORDER_FEE_AMOUNT
                    }
                 ]
              },
              "customer":{
                 "customerId":"$CLIENT_SESSION_CUSTOMER_ID",
                 "emailAddress":"$CLIENT_SESSION_CUSTOMER_EMAIL",
                 "mobileNumber":"$CLIENT_SESSION_CUSTOMER_MOBILE_NUMBER",
                 "firstName":"$CLIENT_SESSION_CUSTOMER_FIRST_NAME",
                 "lastName":"$CLIENT_SESSION_CUSTOMER_LAST_NAME",
                 "billingAddress":{
                    "firstName":"$CLIENT_SESSION_CUSTOMER_FIRST_NAME",
                    "lastName":"$CLIENT_SESSION_CUSTOMER_LAST_NAME",
                    "postalCode":"$CLIENT_SESSION_CUSTOMER_ADDRESS_POSTAL_CODE",
                    "addressLine1":"$CLIENT_SESSION_CUSTOMER_ADDRESS_LINE",
                    "addressLine2":"$CLIENT_SESSION_CUSTOMER_ADDRESS_LINE",
                    "countryCode":"GB",
                    "city":"$CLIENT_SESSION_CUSTOMER_ADDRESS_CITY",
                    "state":"$CLIENT_SESSION_CUSTOMER_ADDRESS_STATE"
                 },
                 "shippingAddress":{
                    "firstName":"$CLIENT_SESSION_CUSTOMER_FIRST_NAME",
                    "lastName":"$CLIENT_SESSION_CUSTOMER_LAST_NAME",
                    "postalCode":"$CLIENT_SESSION_CUSTOMER_ADDRESS_POSTAL_CODE",
                    "addressLine1":"$CLIENT_SESSION_CUSTOMER_ADDRESS_LINE",
                    "addressLine2":"$CLIENT_SESSION_CUSTOMER_ADDRESS_LINE",
                    "countryCode":"GB",
                    "city":"$CLIENT_SESSION_CUSTOMER_ADDRESS_CITY",
                    "state":"$CLIENT_SESSION_CUSTOMER_ADDRESS_STATE"
                 },
                 "nationalDocumentId":"$CLIENT_SESSION_CUSTOMER_NATIONAL_ID"
              },
              "paymentMethod":{
                 "vaultOnSuccess":false,
                 "options":[
                    {
                       "type":"PAYMENT_CARD",
                       "networks":[
                          {
                             "type":"VISA",
                             "surcharge":100
                          },
                       ]
                    },
                    {
                       "type":"PAYPAL",
                       "surcharge":50
                    },
                 ]
              }
           },
           "primerAccountId":"$PRIMER_ACCOUNT_ID",
           "env":"$ENV"
        }
        """
    }
}
