package io.primer.android.internal.data.mappers

import io.mockk.mockk
import io.primer.android.internal.domain.models.KlarnaCategory
import io.primer.android.internal.domain.models.KlarnaStep
import io.primer.android.klarna.api.composable.KlarnaPaymentStep
import io.primer.android.klarna.api.ui.PrimerKlarnaPaymentView
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KlarnaMapperTest {

    private lateinit var mapper: KlarnaMapper

    @BeforeEach
    fun setUp() {
        mapper = KlarnaMapper()
    }

    @Test
    fun `mapCategory maps all fields correctly`() {
        val klarnaCategory = KlarnaPaymentCategory(
            identifier = "pay_now",
            name = "Pay Now",
            descriptiveAssetUrl = "https://example.com/descriptive.png",
            standardAssetUrl = "https://example.com/standard.png",
        )

        val result = mapper.mapCategory(klarnaCategory)

        assertEquals("pay_now", result.id)
        assertEquals("Pay Now", result.displayName)
        assertEquals("https://example.com/descriptive.png", result.descriptiveAssetUrl)
        assertEquals("https://example.com/standard.png", result.standardAssetUrl)
    }

    @Test
    fun `mapCategories maps multiple categories correctly`() {
        val category1 = KlarnaPaymentCategory(
            identifier = "pay_now",
            name = "Pay Now",
            descriptiveAssetUrl = "https://example.com/desc1.png",
            standardAssetUrl = "https://example.com/std1.png",
        )
        val category2 = KlarnaPaymentCategory(
            identifier = "pay_later",
            name = "Pay Later",
            descriptiveAssetUrl = "https://example.com/desc2.png",
            standardAssetUrl = "https://example.com/std2.png",
        )

        val result = mapper.mapCategories(listOf(category1, category2))

        assertEquals(2, result.size)
        assertEquals("pay_now", result[0].id)
        assertEquals("Pay Now", result[0].displayName)
        assertEquals("pay_later", result[1].id)
        assertEquals("Pay Later", result[1].displayName)
    }

    @Test
    fun `mapCategories handles empty list`() {
        val result = mapper.mapCategories(emptyList())

        assertEquals(0, result.size)
    }

    @Test
    fun `mapCategoryToDomain maps all fields correctly`() {
        val domainCategory = KlarnaCategory(
            id = "pay_over_time",
            displayName = "Pay Over Time",
            descriptiveAssetUrl = "https://example.com/desc.png",
            standardAssetUrl = "https://example.com/std.png",
        )

        val result = mapper.mapCategoryToDomain(domainCategory)

        assertEquals("pay_over_time", result.identifier)
        assertEquals("Pay Over Time", result.name)
        assertEquals("https://example.com/desc.png", result.descriptiveAssetUrl)
        assertEquals("https://example.com/std.png", result.standardAssetUrl)
    }

    @Test
    fun `mapStep maps PaymentSessionCreated to CategoriesAvailable`() {
        val categories = listOf(
            KlarnaPaymentCategory(
                identifier = "pay_now",
                name = "Pay Now",
                descriptiveAssetUrl = "https://example.com/desc.png",
                standardAssetUrl = "https://example.com/std.png",
            ),
        )
        val step = KlarnaPaymentStep.PaymentSessionCreated(paymentCategories = categories)

        val result = mapper.mapStep(step)

        assertTrue(result is KlarnaStep.CategoriesAvailable)
        val categoriesAvailable = result as KlarnaStep.CategoriesAvailable
        assertEquals(1, categoriesAvailable.categories.size)
        assertEquals("pay_now", categoriesAvailable.categories[0].id)
        assertEquals("Pay Now", categoriesAvailable.categories[0].displayName)
    }

    @Test
    fun `mapStep maps PaymentViewLoaded to ViewLoaded`() {
        val mockView = mockk<PrimerKlarnaPaymentView>()
        val step = KlarnaPaymentStep.PaymentViewLoaded(paymentView = mockView)

        val result = mapper.mapStep(step)

        assertTrue(result is KlarnaStep.ViewLoaded)
        val viewLoaded = result as KlarnaStep.ViewLoaded
        assertEquals(mockView, viewLoaded.viewData.nativeView)
    }

    @Test
    fun `mapStep maps PaymentSessionAuthorized with finalized=true to Authorized with needsFinalization=false`() {
        val step = KlarnaPaymentStep.PaymentSessionAuthorized(isFinalized = true)

        val result = mapper.mapStep(step)

        assertTrue(result is KlarnaStep.Authorized)
        val authorized = result as KlarnaStep.Authorized
        assertFalse(authorized.needsFinalization)
    }

    @Test
    fun `mapStep maps PaymentSessionAuthorized with finalized=false to Authorized with needsFinalization=true`() {
        val step = KlarnaPaymentStep.PaymentSessionAuthorized(isFinalized = false)

        val result = mapper.mapStep(step)

        assertTrue(result is KlarnaStep.Authorized)
        val authorized = result as KlarnaStep.Authorized
        assertTrue(authorized.needsFinalization)
    }

    @Test
    fun `mapStep maps PaymentSessionFinalized to Finalized`() {
        val step = KlarnaPaymentStep.PaymentSessionFinalized

        val result = mapper.mapStep(step)

        assertTrue(result is KlarnaStep.Finalized)
    }

    @Test
    fun `mapStep handles PaymentSessionCreated with empty categories list`() {
        val step = KlarnaPaymentStep.PaymentSessionCreated(paymentCategories = emptyList())

        val result = mapper.mapStep(step)

        assertTrue(result is KlarnaStep.CategoriesAvailable)
        val categoriesAvailable = result as KlarnaStep.CategoriesAvailable
        assertEquals(0, categoriesAvailable.categories.size)
    }

    @Test
    fun `mapStep handles PaymentSessionCreated with multiple categories`() {
        val categories = listOf(
            KlarnaPaymentCategory(
                identifier = "pay_now",
                name = "Pay Now",
                descriptiveAssetUrl = "https://example.com/desc1.png",
                standardAssetUrl = "https://example.com/std1.png",
            ),
            KlarnaPaymentCategory(
                identifier = "pay_later",
                name = "Pay Later",
                descriptiveAssetUrl = "https://example.com/desc2.png",
                standardAssetUrl = "https://example.com/std2.png",
            ),
            KlarnaPaymentCategory(
                identifier = "pay_over_time",
                name = "Pay Over Time",
                descriptiveAssetUrl = "https://example.com/desc3.png",
                standardAssetUrl = "https://example.com/std3.png",
            ),
        )
        val step = KlarnaPaymentStep.PaymentSessionCreated(paymentCategories = categories)

        val result = mapper.mapStep(step)

        assertTrue(result is KlarnaStep.CategoriesAvailable)
        val categoriesAvailable = result as KlarnaStep.CategoriesAvailable
        assertEquals(3, categoriesAvailable.categories.size)
        assertEquals("pay_now", categoriesAvailable.categories[0].id)
        assertEquals("pay_later", categoriesAvailable.categories[1].id)
        assertEquals("pay_over_time", categoriesAvailable.categories[2].id)
    }
}
