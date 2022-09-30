package io.primer.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.di.DIAppComponent
import io.primer.android.di.DIAppContext

internal open class BaseCheckoutActivity : AppCompatActivity(), DIAppComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }

    protected fun isInitialized() = DIAppContext.app != null
}
