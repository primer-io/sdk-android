package io.primer.android.di

import org.koin.core.Koin
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@KoinApiExtension
internal interface DIAppComponent : KoinComponent {

    override fun getKoin(): Koin = DIAppContext.app?.koin!!
}
