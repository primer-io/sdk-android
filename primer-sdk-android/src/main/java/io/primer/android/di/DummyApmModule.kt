package io.primer.android.di

import io.primer.android.ui.fragments.dummy.DummyResultSelectorViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val dummyApmModule = {
    module {
        viewModel {
            DummyResultSelectorViewModel(get())
        }
    }
}
