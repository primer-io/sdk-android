package io.primer.android.webRedirectShared.implementation.composer.presentation.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.core.InstantExecutorExtension
import io.primer.android.analytics.domain.AnalyticsInteractor
import io.primer.android.analytics.domain.models.BaseAnalyticsParams
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.rules.TestRule

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
internal class WebRedirectViewModelTest {
    @get:Rule
    var instantExecutorRule: TestRule = InstantTaskExecutorRule()

    private lateinit var viewModel: WebRedirectViewModel
    private val analyticsInteractor: AnalyticsInteractor = mockk(relaxed = true)

    @BeforeEach
    fun setUp() {
        viewModel = WebRedirectViewModel(analyticsInteractor)
    }

    @Test
    fun `addAnalyticsEvent should call analyticsInteractor with correct params`() {
        val params = mockk<BaseAnalyticsParams>(relaxed = true)

        runTest {
            viewModel.addAnalyticsEvent(params)
        }

        coVerify { analyticsInteractor(any()) }
    }
}
