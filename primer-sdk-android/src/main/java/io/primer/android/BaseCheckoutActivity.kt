package io.primer.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.primer.android.di.DIAppComponent

internal open class BaseCheckoutActivity : AppCompatActivity(), DIAppComponent {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
    }
}
