package io.primer.android.ui.main

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior


class CheckoutView(context: Context): CoordinatorLayout(context) {
  private val behaviour = BottomSheetBehavior<View>()

  init {
    initLayout()

    val sheet = createSheet()
    addView(sheet)

    visibility = View.VISIBLE
    behaviour.state = BottomSheetBehavior.STATE_EXPANDED
  }

  private fun initLayout() {
    id = ViewCompat.generateViewId()
    layoutParams = CoordinatorLayout.LayoutParams(
      LayoutParams.MATCH_PARENT,
      ViewGroup.LayoutParams.MATCH_PARENT,
    )
    setBackgroundColor(Color.parseColor("#1F000000"))
    setOnClickListener {
      behaviour.state = BottomSheetBehavior.STATE_COLLAPSED
      visibility = View.INVISIBLE
    }
  }

  private fun createSheet(): View {
    val sheet = View(context)

    sheet.id = ViewCompat.generateViewId()
    sheet.setBackgroundColor(Color.parseColor("#FFFFFFFF"))

    if (Build.VERSION.SDK_INT >= 21) {
      sheet.elevation = 5.0f
    }

    val params = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.MATCH_PARENT, 100) as CoordinatorLayout.LayoutParams

    params.behavior = behaviour
    sheet.layoutParams = params
    sheet.requestLayout()

    return sheet
  }
}