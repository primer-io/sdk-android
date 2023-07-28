package io.primer.android.ui.mock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import io.primer.android.BaseCheckoutActivity
import io.primer.android.R
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.klarna.NativeKlarnaActivity
import io.primer.android.presentation.mock.PaymentMethodMockViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.inject
import java.util.UUID

/**
 * An Activity used to only simulate 3rd party SDKs during UI tests.
 * Should never be called in production code.
 **/
internal class PaymentMethodMockActivity : BaseCheckoutActivity() {

    private val eventResolver: ResumeEventResolver by inject()
    private val viewModel: PaymentMethodMockViewModel by viewModel()

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
                PaymentMethodType.KLARNA.name -> setResult(
                    RESULT_OK,
                    Intent().apply {
                        putExtra(NativeKlarnaActivity.AUTH_TOKEN_KEY, UUID.randomUUID().toString())
                    }
                ).also { finish() }
                PaymentMethodType.IPAY88_CARD.name -> viewModel.finaliseMockedFlow()
                PaymentMethodType.PAYMENT_CARD.name ->
                    eventResolver.resolve(
                        paymentMethodType,
                        false,
                        UUID.randomUUID().toString()
                    ).also { finish() }
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
