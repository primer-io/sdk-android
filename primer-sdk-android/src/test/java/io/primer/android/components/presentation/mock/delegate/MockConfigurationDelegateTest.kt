package io.primer.android.components.presentation.mock.delegate

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.domain.base.None
import io.primer.android.domain.mock.MockConfigurationInteractor
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class MockConfigurationDelegateTest {
    private val interactor = mockk<MockConfigurationInteractor> {
        every { execute(any()) } returns false
    }

    private val delegate = MockConfigurationDelegate(interactor)

    @Test
    fun `isMockedFlow() should return value returned by repository`() {
        val actual = delegate.isMockedFlow()

        verify(exactly = 1) {
            interactor.execute(any<None>())
        }
        assertEquals(false, actual)
    }
}
