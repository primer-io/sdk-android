package io.primer.android.payment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.ui.fragments.dummy.DummyResultSelectorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class NewFragmentBehaviourTest {

    @MockK
    private lateinit var parentFragment: Fragment

    @MockK
    private lateinit var fragment: Fragment

    @MockK
    private lateinit var fragmentTransaction: FragmentTransaction

    @MockK
    private lateinit var fragmentManager: FragmentManager

    @BeforeEach
    fun setUp() {
        every { parentFragment.childFragmentManager } returns fragmentManager
        every { fragmentManager.beginTransaction() } returns fragmentTransaction
        every { fragmentTransaction.replace(any(), any()) } returns fragmentTransaction
        every { fragmentTransaction.addToBackStack(any()) } returns fragmentTransaction
        every { fragmentTransaction.commit() } returns 0
        confirmVerified(fragmentManager, fragmentTransaction)
    }

    @Test
    fun `execute() should add fragment to back stack when returnToPreviousOnBack is true`() {
        val behaviour = NewFragmentBehaviour(factory = { fragment }, returnToPreviousOnBack = true)

        behaviour.execute(parentFragment)

        verify {
            fragmentManager.beginTransaction()
            fragmentTransaction.replace(any(), fragment)
            fragmentTransaction.addToBackStack(null)
            fragmentTransaction.commit()
        }
    }

    @Test
    fun `execute() should not add fragment to back stack when returnToPreviousOnBack is false`() {
        val behaviour = NewFragmentBehaviour(factory = { fragment }, returnToPreviousOnBack = false)

        behaviour.execute(parentFragment)

        verify {
            fragmentManager.beginTransaction()
            fragmentTransaction.replace(any(), fragment)
            fragmentTransaction.commit()
        }
        verify(exactly = 0) {
            fragmentTransaction.addToBackStack(any())
        }
    }

    @Test
    fun `execute() should call onProvideActionContinue when fragment is OnActionContinueCallback`() {
        val onActionContinueCallback = mockk<DummyResultSelectorFragment>(relaxed = true)
        val behaviour =
            NewFragmentBehaviour(factory = { onActionContinueCallback as Fragment }, returnToPreviousOnBack = false)

        behaviour.execute(parentFragment)

        verify {
            onActionContinueCallback.onProvideActionContinue(any())
            fragmentManager.beginTransaction()
            fragmentTransaction.replace(any(), onActionContinueCallback as Fragment)
            fragmentTransaction.commit()
        }
        verify(exactly = 0) {
            fragmentTransaction.addToBackStack(any())
        }
    }
}
