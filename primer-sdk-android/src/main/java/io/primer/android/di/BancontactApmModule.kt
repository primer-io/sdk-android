package io.primer.android.di

import io.primer.android.ui.fragments.bancontact.BancontactCardViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal val bancontactApmModule = {
    module {
        viewModel {
            BancontactCardViewModel(get())
        }
    }
}
