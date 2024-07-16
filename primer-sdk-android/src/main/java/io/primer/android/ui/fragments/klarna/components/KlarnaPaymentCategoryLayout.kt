package io.primer.android.ui.fragments.klarna.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Space
import io.primer.android.R
import io.primer.android.databinding.ItemKlarnaSelectedPaymentCategoryBinding
import io.primer.android.databinding.ItemKlarnaUnselectedPaymentCategoryBinding
import io.primer.android.ui.fragments.klarna.model.KlarnaPaymentCategory
import java.util.Timer
import java.util.TimerTask

private const val TIMER_DELAY = 300L

internal class KlarnaPaymentCategoryLayout(context: Context, attrs: AttributeSet?) :
    LinearLayout(context, attrs) {
    init {
        orientation = LinearLayout.VERTICAL
    }

    private val layoutInflater by lazy { LayoutInflater.from(context) }

    private var onItemClickListener: (Int) -> Unit = {}

    var dummyKlarnaPaymentViewContainer: FrameLayout? = null

    var klarnaPaymentCategories: List<KlarnaPaymentCategory> = emptyList()
        set(value) {
            field = value
            removeAllViews()
            addKlarnaPaymentCategories()
        }

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    private fun addKlarnaPaymentCategories() {
        klarnaPaymentCategories.forEachIndexed { index, klarnaPaymentCategory ->
            addView(
                when (klarnaPaymentCategory) {
                    is KlarnaPaymentCategory.UnselectedKlarnaPaymentCategory -> {
                        val binding =
                            ItemKlarnaUnselectedPaymentCategoryBinding.inflate(layoutInflater)
                        binding.bind(klarnaPaymentCategory)
                        binding.root
                    }

                    is KlarnaPaymentCategory.SelectedKlarnaPaymentCategory -> {
                        val binding =
                            ItemKlarnaSelectedPaymentCategoryBinding.inflate(layoutInflater)
                        binding.bind(klarnaPaymentCategory)
                        binding.root
                    }
                }.apply {
                    isClickable = true
                    this@apply.setOnClickListener { onItemClickListener(index) }
                }
            )
            if (index != klarnaPaymentCategories.lastIndex) {
                addView(
                    Space(context).apply {
                        layoutParams = LayoutParams(
                            0,
                            context.resources.getDimensionPixelSize(R.dimen.medium_padding)
                        )
                    }
                )
            }
        }
    }

    private fun ItemKlarnaUnselectedPaymentCategoryBinding.bind(
        category: KlarnaPaymentCategory.UnselectedKlarnaPaymentCategory
    ) {
        paymentCategory.paymentCategoryName.text = category.name
    }

    private fun ItemKlarnaSelectedPaymentCategoryBinding.bind(
        category: KlarnaPaymentCategory.SelectedKlarnaPaymentCategory
    ) {
        paymentCategory.paymentCategoryName.text = category.name
        with(klarnaPaymentViewContainer) {
            removeAllViews()
            lateinit var klarnaPaymentView: View
            val listener = object : View.OnLayoutChangeListener {
                private var timer: Timer? = null

                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    val listener = this
                    timer?.cancel()
                    timer = Timer().apply {
                        schedule(
                            object : TimerTask() {
                                override fun run() {
                                    timer?.cancel()
                                    timer = null
                                    klarnaPaymentView.removeOnLayoutChangeListener(listener)
                                    dummyKlarnaPaymentViewContainer?.post {
                                        dummyKlarnaPaymentViewContainer?.removeAllViews()
                                        addView(klarnaPaymentView)
                                    }
                                }
                            },
                            TIMER_DELAY
                        )
                    }
                }
            }
            klarnaPaymentView = category.view.apply {
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                )
            }.apply { addOnLayoutChangeListener(listener) }

            /*
            Initially add the view to a dummy invisible view to avoid flickering due the webview
            loading
            */
            dummyKlarnaPaymentViewContainer?.addView(klarnaPaymentView)
        }
    }
}
