package io.primer.android

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout

class DebugActivity : AppCompatActivity() {

    private lateinit var motionLayout: MotionLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        findViewById<ConstraintLayout>(R.id.saved_payment_method).setOnClickListener {
            when (motionLayout.currentState) {
                motionLayout.startState -> motionLayout.transitionToEnd()
                else -> motionLayout.transitionToStart()
            }
        }
    }
}
