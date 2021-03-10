package io.primer.android.di

import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
interface DIAppComponent : KoinComponent {

    override fun getKoin() = DIAppContext.app!!.koin
}
