package io.primer.paymentMethodCoreUi.core.ui.mock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import io.primer.android.core.di.extensions.viewModel
import io.primer.android.paymentMethodCoreUi.R
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.paymentMethodCoreUi.core.ui.BaseCheckoutActivity
import kotlinx.coroutines.launch

/**
 * An Activity used to only simulate 3rd party SDKs during UI tests.
 * Should never be called in production code.
 **/
class PaymentMethodMockActivity : BaseCheckoutActivity() {

    private val viewModel: PaymentMethodMockViewModel
        by viewModel<PaymentMethodMockViewModel, PaymentMethodMockViewModelFactory>()

    private val paymentMethodType by lazy {
        intent.extras?.getString(PAYMENT_METHOD_TYPE_KEY).orEmpty()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_primer_payment_method_mock)
        setupViews()
        setupListeners()
        setupObservers()
    }

    private fun setupViews() {
        val toolbar = findViewById<Toolbar>(R.id.primerWebviewToolbar)
        toolbar.title = paymentMethodType
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        findViewById<TextView>(R.id.test_payment_method_title).text = getTestTitle()
    }

    private fun setupListeners() {
        findViewById<TextView>(R.id.send_credentials_btn).setOnClickListener {
            when (paymentMethodType) {
                PaymentMethodType.IPAY88_CARD.name -> {
                    viewModel.finaliseMockedFlow()
                }

                PaymentMethodType.PAYMENT_CARD.name -> lifecycleScope.launch {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.finalizeMocked.observe(this) {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun getTestTitle() = when (paymentMethodType) {
        PaymentMethodType.PAYMENT_CARD.name -> THREE_DS_DEMO_TEST
        else -> PAYMENT_METHOD_TEST.format(paymentMethodType)
    }

    companion object {

        private const val THREE_DS_DEMO_TEST = "Demo 3DS"
        private const val PAYMENT_METHOD_TEST = "Testing %s"
        private const val PAYMENT_METHOD_TYPE_KEY = "PAYMENT_METHOD_TYPE"

        fun getLaunchIntent(context: Context, paymentMethodType: String) = Intent(
            context,
            PaymentMethodMockActivity::class.java
        ).apply {
            putExtra(PAYMENT_METHOD_TYPE_KEY, paymentMethodType)
        }
    }
}
