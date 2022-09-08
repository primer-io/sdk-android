package io.primer.android.data.configuration.models

import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class OrderDataResponseTest {

    private val orderDataResponse by lazy {
        OrderDataResponse.deserializer.deserialize(
            JSONObject(JSON_OBJECT)
        )
    }

    @Test
    fun `'order-line-items' should be deserialized correctly`() {
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
            orderDataResponse.lineItems
        )
    }

    @Test
    fun `'order-line-fees' should be deserialized correctly`() {
        assertEquals(
            listOf(
                OrderDataResponse.FeeDataResponse(
                    CLIENT_SESSION_ORDER_FEE_TYPE,
                    CLIENT_SESSION_ORDER_FEE_AMOUNT
                )
            ),
            orderDataResponse.fees
        )
    }

    private companion object {

        const val CLIENT_SESSION_ORDER_NAME = "test-name"
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

        const val JSON_OBJECT =
            """
         {
           "name":"$CLIENT_SESSION_ORDER_NAME",
           "orderId":"$CLIENT_SESSION_ORDER_ID",
           "currencyCode":"$CLIENT_SESSION_ORDER_CURRENCY",
           "countryCode":"GB",
           "totalOrderAmount":"$CLIENT_SESSION_ORDER_TOTAL_AMOUNT",
           "lineItems":[
              {
                 "amount":"$CLIENT_SESSION_ORDER_LINE_ITEMS_AMOUNT",
                 "quantity":"$CLIENT_SESSION_ORDER_LINE_ITEMS_QUANTITY",
                 "discountAmount":"$CLIENT_SESSION_ORDER_LINE_ITEMS_DISCOUNT_AMOUNT",
                 "itemId":"$CLIENT_SESSION_ORDER_LINE_ITEMS_ITEM_ID",
                 "description":"$CLIENT_SESSION_ORDER_LINE_ITEMS_ITEM_DESCRIPTION"
              }
           ],
           "fees":[
              {
                 "type":"$CLIENT_SESSION_ORDER_FEE_TYPE",
                 "amount":"$CLIENT_SESSION_ORDER_FEE_AMOUNT"
              }
           ]
        }
        """
    }
}
