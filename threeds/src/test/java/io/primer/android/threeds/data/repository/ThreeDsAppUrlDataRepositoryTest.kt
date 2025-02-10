package io.primer.android.threeds.data.repository

import android.net.Uri
import android.webkit.URLUtil
import androidx.core.util.PatternsCompat
import com.netcetera.threeds.sdk.api.transaction.AuthenticationRequestParameters
import com.netcetera.threeds.sdk.api.transaction.Transaction
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.verify
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.data.settings.PrimerThreeDsOptions
import io.primer.android.threeds.domain.repository.ThreeDsAppUrlRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.util.UUID
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ThreeDsAppUrlDataRepositoryTest {
    @RelaxedMockK
    internal lateinit var primerSettings: PrimerSettings

    private lateinit var repository: ThreeDsAppUrlRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        repository = ThreeDsAppUrlDataRepository(primerSettings)
    }

    @Test
    fun `getAppUrl() should return correct threeDAppRequestorUrl when PrimerThreeDsOptions contains valid App link`() {
        val threeDsOptions = mockk<PrimerThreeDsOptions>(relaxed = true)
        every { threeDsOptions.threeDsAppRequestorUrl }.returns(VALID_APP_LINK_URL)
        every { primerSettings.paymentMethodOptions.threeDsOptions }.returns(threeDsOptions)

        val transaction = mockk<Transaction>(relaxed = true)
        val requestParameters = mock(AuthenticationRequestParameters::class.java)
        `when`(requestParameters.sdkTransactionID).thenReturn(SDK_TRANSACTION_ID)
        every { transaction.authenticationRequestParameters }.returns(requestParameters)

        mockkObject(PatternsCompat.WEB_URL).also {
            every {
                PatternsCompat.WEB_URL.matcher(VALID_APP_LINK_URL).matches()
            } returns true
        }

        mockkStatic(URLUtil::class).also {
            every { URLUtil.isHttpsUrl(VALID_APP_LINK_URL) }.returns(true)
        }

        mockkStatic(Uri::class).also {
            val uriMock = mockk<Uri>()
            val uriBuilder = mockk<Uri.Builder>()
            every { uriBuilder.appendQueryParameter(any(), SDK_TRANSACTION_ID) }.returns(uriBuilder)
            every { uriBuilder.build() }.returns(uriMock)

            every { Uri.parse(VALID_APP_LINK_URL) }.returns(uriMock)
            every { uriMock.buildUpon() }.returns(uriBuilder)
            every { uriMock.toString() }
                .returns("$VALID_APP_LINK_URL?transID=$SDK_TRANSACTION_ID")
        }
        runTest {
            val threeDAppRequestorUrl = repository.getAppUrl(transaction)
            assertEquals(
                "$VALID_APP_LINK_URL?transID=$SDK_TRANSACTION_ID",
                threeDAppRequestorUrl,
            )
        }

        verify { threeDsOptions.threeDsAppRequestorUrl }
        verify { primerSettings.paymentMethodOptions.threeDsOptions }
    }

    @Test
    fun `getAppUrl() should return null when PrimerThreeDsOptions contains invalid https App link`() {
        val threeDsOptions = mockk<PrimerThreeDsOptions>(relaxed = true)
        every { threeDsOptions.threeDsAppRequestorUrl }.returns(INVALID_HTTPS_APP_LINK_URL)
        every { primerSettings.paymentMethodOptions.threeDsOptions }.returns(threeDsOptions)

        val transaction = mockk<Transaction>(relaxed = true)
        val requestParameters = mock(AuthenticationRequestParameters::class.java)
        `when`(requestParameters.sdkTransactionID).thenReturn(SDK_TRANSACTION_ID)
        every { transaction.authenticationRequestParameters }.returns(requestParameters)

        mockkObject(PatternsCompat.WEB_URL).also {
            every {
                PatternsCompat.WEB_URL.matcher(INVALID_HTTPS_APP_LINK_URL).matches()
            } returns false
        }

        mockkStatic(URLUtil::class).also {
            every { URLUtil.isHttpsUrl(INVALID_HTTPS_APP_LINK_URL) }.returns(true)
        }

        runTest {
            val threeDAppRequestorUrl = repository.getAppUrl(transaction)
            assertEquals(
                null,
                threeDAppRequestorUrl,
            )
        }

        verify { threeDsOptions.threeDsAppRequestorUrl }
        verify { primerSettings.paymentMethodOptions.threeDsOptions }
    }

    @Test
    fun `getAppUrl() should return null when PrimerThreeDsOptions contains empty App link`() {
        val threeDsOptions = mockk<PrimerThreeDsOptions>()
        every { threeDsOptions.threeDsAppRequestorUrl }.returns("")
        every { primerSettings.paymentMethodOptions.threeDsOptions }.returns(threeDsOptions)

        val transaction = mockk<Transaction>()
        every { transaction.authenticationRequestParameters.sdkTransactionID }.returns(
            SDK_TRANSACTION_ID,
        )

        runTest {
            val threeDAppRequestorUrl = repository.getAppUrl(transaction)
            assertEquals(
                null,
                threeDAppRequestorUrl,
            )
        }

        verify { threeDsOptions.threeDsAppRequestorUrl }
        verify { primerSettings.paymentMethodOptions.threeDsOptions }
    }

    @Test
    fun `getAppUrl() should return null when PrimerThreeDsOptions contains invalid App link`() {
        val threeDsOptions =
            mockk<PrimerThreeDsOptions>(relaxed = true) {
                every { threeDsAppRequestorUrl }.returns(DEEPLINK_URL)
            }
        every { primerSettings.paymentMethodOptions.threeDsOptions }.returns(threeDsOptions)

        mockkStatic(URLUtil::class).also {
            every { URLUtil.isHttpsUrl(DEEPLINK_URL) }.returns(false)
        }

        val transaction = mockk<Transaction>(relaxed = true)
        val authenticationRequestParameters = mockk<AuthenticationRequestParameters>(relaxed = true)
        every { transaction.authenticationRequestParameters }.returns(
            authenticationRequestParameters,
        )

        runTest {
            val threeDsAppRequestorUrl = repository.getAppUrl(transaction)
            assertEquals(
                null,
                threeDsAppRequestorUrl,
            )
        }

        verify { threeDsOptions.threeDsAppRequestorUrl }
        verify { primerSettings.paymentMethodOptions.threeDsOptions }
    }

    private companion object {
        val SDK_TRANSACTION_ID = UUID.randomUUID().toString()
        const val VALID_APP_LINK_URL = "https://primer.io/"
        const val INVALID_HTTPS_APP_LINK_URL = "https://prime r.io/"
        const val DEEPLINK_URL = "primer://"
    }
}
