package io.primer.android.ui

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.Window
import io.primer.android.logging.Logger

class KeyboardVisibilityListener private constructor(activity: Activity, root: View) {
  private val log = Logger("keyboard-listener")
  private val listeners: MutableList<((Boolean) -> Unit)> = ArrayList()
  private var visible = false
  private val KEYBOARD_OBJECT_HEIGHT = 100

  init {
    root.viewTreeObserver.addOnGlobalLayoutListener {
      val bounds = Rect()

      root.getWindowVisibleDisplayFrame(bounds)

      val delta = root.rootView.height - (bounds.bottom - bounds.top);
      val contentViewTop = activity.window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
      val isVisible = delta > KEYBOARD_OBJECT_HEIGHT

      log("Listener: delta: $delta, " + bounds + ", " + root.rootView.height + ", top: " + contentViewTop)


      if (isVisible != visible) {
        visible = isVisible
        listeners.forEach { it(visible) }
      }
    }
  }

  fun addListener(listener: ((Boolean) -> Unit)) {
    listeners.add(listener)
  }

  companion object {
    private var instance: KeyboardVisibilityListener? = null

    fun init(activity: Activity, view: View) {
      instance = KeyboardVisibilityListener(activity, view)
    }

    fun addListener(listener: ((Boolean) -> Unit)) {
      instance?.addListener(listener)
    }
  }
}