package io.primer.android.threeds.data.repository

import android.content.Context
import com.netcetera.threeds.sdk.api.ThreeDS2Service
import com.netcetera.threeds.sdk.api.security.Warning
import com.netcetera.threeds.sdk.api.transaction.Transaction
import com.netcetera.threeds.sdk.api.utils.DsRidValues
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.data.configuration.models.Environment
import io.primer.android.threeds.data.exception.ThreeDsConfigurationException
import io.primer.android.threeds.data.exception.ThreeDsInitException
import io.primer.android.threeds.data.exception.ThreeDsMissingDirectoryServerException
import io.primer.android.threeds.data.models.common.CardNetwork
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.Locale
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class NetceteraThreeDsServiceRepositoryTest {

    @RelaxedMockK
    internal lateinit var context: Context

    @RelaxedMockK
    internal lateinit var threeDS2Service: ThreeDS2Service

    private lateinit var repository: NetceteraThreeDsServiceRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        repository = NetceteraThreeDsServiceRepository(context, threeDS2Service)
    }

    @Test
    fun `initializeProvider should throw ThreeDsConfigurationException KEYS_CONFIG_ERROR message when ThreeDsKeysParams are missing`() {
        assertThrows<ThreeDsConfigurationException>(
            NetceteraThreeDsServiceRepository.KEYS_CONFIG_ERROR
        ) {
            runTest {
                repository.initializeProvider(
                    false,
                    Locale.getDefault(),
                    true,
                    null
                ).first()
            }
        }
    }

    @Test
    fun `initializeProvider should throw ThreeDsConfigurationException LICENCE_CONFIG_ERROR message when ThreeDsKeysParams licence is missing`() {
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
        every { keysParams.licenceKey }.returns(null)

        assertThrows<ThreeDsConfigurationException>(
            NetceteraThreeDsServiceRepository.LICENCE_CONFIG_ERROR
        ) {
            runTest {
                repository.initializeProvider(
                    false,
                    Locale.getDefault(),
                    true,
                    keysParams
                ).first()
            }
        }
    }

    @Test
    fun `initializeProvider should continue flow when is3DSSanityCheckEnabled is not enabled `() {
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)

        runTest {
            val result = repository.initializeProvider(
                false,
                Locale.getDefault(),
                true,
                keysParams
            ).first()
            assertEquals(Unit, result)
        }
    }

    @Test
    fun `initializeProvider should continue flow when is3DSSanityCheckEnabled is enabled and there are no warnings`() {
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
        every { threeDS2Service.warnings }.returns(listOf())

        runTest {
            val result = repository.initializeProvider(
                true,
                Locale.getDefault(),
                true,
                keysParams
            ).first()
            assertEquals(Unit, result)
        }

        verify { threeDS2Service.warnings }
    }

    @Test
    fun `initializeProvider should throw ThreeDsInitException with correct message when is3DSSanityCheckEnabled is enabled`() {
        val keysParams = mockk<ThreeDsKeysParams>(relaxed = true)
        val warning = mock(Warning::class.java)

        Mockito.`when`(warning.id).thenReturn("S01")

        every { threeDS2Service.warnings }.returns(listOf(warning))

        val exception = assertThrows<ThreeDsInitException> {
            runTest {
                repository.initializeProvider(
                    true,
                    Locale.getDefault(),
                    true,
                    keysParams
                ).first()
            }
        }

        verify { threeDS2Service.warnings }

        assertEquals(
            exception.message,
            listOf(warning).joinToString(",") { "${it.severity}  ${it.message}" }
        )
    }

    @Test
    fun `performProviderAuth should return Transaction object`() {
        val cardNetwork = mockk<CardNetwork>(relaxed = true)
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        val transactionMock = mockk<Transaction>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(cardNetwork, protocolVersion, environment).first()
            assertEquals(transactionMock, transaction)
        }

        verify { threeDS2Service.createTransaction(any(), any()) }
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-VISA for CardNetwork-VISA`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(CardNetwork.VISA, protocolVersion, environment)
                    .first()
            assertEquals(transactionMock, transaction)
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.VISA, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-MASTERCARD for CardNetwork-MASTERCARD`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(CardNetwork.MASTERCARD, protocolVersion, environment)
                    .first()
            assertEquals(transactionMock, transaction)
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.MASTERCARD, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-AMEX for CardNetwork-AMEX`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(CardNetwork.AMEX, protocolVersion, environment)
                    .first()
            assertEquals(transactionMock, transaction)
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.AMEX, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-JCB for CardNetwork-JCB`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(CardNetwork.JCB, protocolVersion, environment)
                    .first()
            assertEquals(transactionMock, transaction)
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.JCB, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-DINERS for CardNetwork-DINERS_CLUB`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(
                    CardNetwork.DINERS_CLUB,
                    protocolVersion,
                    environment
                ).first()
            assertEquals(transactionMock, transaction)
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.DINERS, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-DINERS for CardNetwork-DISCOVER`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(CardNetwork.DISCOVER, protocolVersion, environment)
                    .first()
            assertEquals(transactionMock, transaction)
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.DINERS, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server DsRidValues-UNION for CardNetwork-UNIONPAY`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)
        val environment = mockk<Environment>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(CardNetwork.UNIONPAY, protocolVersion, environment)
                    .first()
            assertEquals(transactionMock, transaction)
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(DsRidValues.UNION, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should select directory server TEST_SCHEME_ID for any other card network if environment is SANDBOX`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        runTest {
            val transaction =
                repository.performProviderAuth(
                    CardNetwork.OTHER,
                    protocolVersion,
                    Environment.SANDBOX
                ).first()
            assertEquals(transactionMock, transaction)
        }

        val cardNetwork = slot<String>()
        verify { threeDS2Service.createTransaction(capture(cardNetwork), any()) }

        assertEquals(NetceteraThreeDsServiceRepository.TEST_SCHEME_ID, cardNetwork.captured)
    }

    @Test
    fun `performProviderAuth should throw ThreeDsMissingDirectoryServerException for any other card network if environment is PRODUCTION`() {
        val protocolVersion = mockk<ProtocolVersion>(relaxed = true)
        val transactionMock = mockk<Transaction>(relaxed = true)

        every { threeDS2Service.createTransaction(any(), any()) }.returns(transactionMock)

        assertThrows<ThreeDsMissingDirectoryServerException>() {
            runTest {
                repository.performProviderAuth(
                    CardNetwork.OTHER,
                    protocolVersion,
                    Environment.PRODUCTION
                ).first()
            }
        }
    }

    @Test
    fun `performCleanup should invoke threeDS2Service-cleanup`() {
        every { threeDS2Service.cleanup(any()) }.returns(Unit)

        runTest {
            repository.performCleanup()
        }

        verify { threeDS2Service.cleanup(any()) }
    }
}
