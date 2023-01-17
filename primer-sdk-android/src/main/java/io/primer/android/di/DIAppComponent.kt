package io.primer.android.di

import io.primer.android.components.SdkUninitializedException
import org.koin.core.Koin
import org.koin.core.component.KoinComponent

internal interface DIAppComponent : KoinComponent {

    override fun getKoin(): Koin = DIAppContext.app?.koin ?: throw SdkUninitializedException()
}
