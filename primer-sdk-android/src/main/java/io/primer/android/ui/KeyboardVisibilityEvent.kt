package io.primer.android.ui

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.primer.android.logging.Logger
import java.lang.ref.WeakReference

private const val KEYBOARD_MIN_HEIGHT_RATIO = 0.15

internal object KeyboardVisibilityEvent {

    interface OnChangedListener {

        fun onKeyboardVisibilityChanged(visible: Boolean)
    }

    private class Subscription(view: View, listener: ViewTreeObserver.OnGlobalLayoutListener) {

        private val viewRef = WeakReference(view)
        private val listenerRef = WeakReference(listener)

        fun register() {
            val view = viewRef.get()
            val listener = listenerRef.get()

            if (view != null && listener != null) {
                view.viewTreeObserver.addOnGlobalLayoutListener(listener)
            }
        }

        fun unregister() {
            val view = viewRef.get()
            val listener = listenerRef.get()

            if (view != null && listener != null) {
                view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
            }
        }
    }

    fun subscribe(
        contentView: View,
        lifecycleOwner: LifecycleOwner,
        listener: OnChangedListener,
    ) {
        val layoutListener = object : ViewTreeObserver.OnGlobalLayoutListener {
            private var isVisible = false

            override fun onGlobalLayout() {
                val nextIsVisible = isKeyboardVisible(contentView)

                if (nextIsVisible == isVisible) {
                    return
                }

                isVisible = nextIsVisible

                listener.onKeyboardVisibilityChanged(isVisible)
            }
        }

        val subscription = Subscription(contentView, layoutListener)

        lifecycleOwner.lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    subscription.unregister()
                }
            }
        )

        subscription.register()
    }

    fun isKeyboardVisible(contentView: View): Boolean {
        val r = Rect()

        contentView.getWindowVisibleDisplayFrame(r)

        val screenHeight = contentView.rootView.height
        val heightDiff = screenHeight - r.height()

        return heightDiff > screenHeight * KEYBOARD_MIN_HEIGHT_RATIO
    }

    fun getContentRoot(activity: Activity): ViewGroup {
        return activity.findViewById(android.R.id.content)
    }
}
