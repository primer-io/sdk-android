package io.primer.android.di

import io.primer.android.UniversalCheckoutTheme
import org.koin.dsl.module

val ThemeModule = { theme: UniversalCheckoutTheme ->
  module {
    single<UniversalCheckoutTheme> { theme }
  }
}