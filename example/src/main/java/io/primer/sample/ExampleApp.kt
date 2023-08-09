package io.primer.sample

import android.app.Application
import io.paperdb.Paper

class ExampleApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
    }
}
