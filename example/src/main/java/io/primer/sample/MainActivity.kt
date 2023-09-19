package io.primer.sample

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import io.primer.nolpay.PrimerNolPayNfcUtils
import io.primer.sample.databinding.ActivityMainBinding
import io.primer.sample.datamodels.AppCountryCode
import io.primer.sample.datasources.CountryDataSource
import io.primer.sample.repositories.AppApiKeyRepository
import io.primer.sample.repositories.CountryRepository
import io.primer.sample.viewmodels.MainViewModel
import io.primer.sample.viewmodels.MainViewModelFactory
import io.primer.sample.viewmodels.SettingsViewModel
import io.primer.sample.viewmodels.SettingsViewModelFactory
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var mainViewModel: MainViewModel
    private lateinit var settingsViewModel: SettingsViewModel

    private val apiKeyDataSource = AppApiKeyRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.mainToolbar)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val countryDataSource = CountryDataSource(AppCountryCode.AE)
        val countryRepository = CountryRepository(countryDataSource)

        mainViewModel = ViewModelProvider(
            this,
            MainViewModelFactory(WeakReference(this), countryRepository, apiKeyDataSource),
        )[MainViewModel::class.java]

        settingsViewModel = ViewModelProvider(
            this,
            SettingsViewModelFactory(countryRepository),
        )[SettingsViewModel::class.java]
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        return navController.navigateUp(appBarConfiguration)
            || super.onSupportNavigateUp()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        PrimerNolPayNfcUtils.getAvailableTag(intent)?.let { tag ->
            mainViewModel.setTag(tag)
        }

//        val collectDataComponent = NolPayCollectLinkDataComponent()
//        collectDataComponent.updateCollectedData(NolPayLinkCollectableData.NolPayOtpData("23456"))
//
//        val stepDataComponent =
//            NolPayCollectLinkDataComponent() as PrimerCollectDataStepable<PrimerCollectDataStep>
//
//
//        val nolPayStartPaymentComponent = NolPayStartPaymentComponent()
//        nolPayStartPaymentComponent.updateCollectedData(NolPayStartPaymentCollectableData.NolPayTagData())
//        val cardComponent = PrimerHeadlessComponentProvider().provideComponent("PAYMENT_CARD")
//        val nolComponet = PrimerHeadlessComponentProvider().provideComponent("NOL_PAY")
//
//        when (PrimerHeadlessComponentProvider().provideComponent("payment_method_type")) {
//            is PrimerHeadlessCollectDataComponent<*>, is PrimerCollectDataStepable<*> -> MyCollectAndStepWrapper()
//            is PrimerHeadlessCollectDataComponent<*> -> MyCollectDataWrapper()
//        }

    }
}

//class MyCollectAndStepWrapper :
//    PrimerHeadlessCollectDataComponent<PrimerCollectableData> by MyCollectDataWrapper(),
//    PrimerCollectDataStepable<PrimerCollectDataStep> by MyStepDataWrapper()
//
//class MyCollectDataWrapper : PrimerHeadlessCollectDataComponent<PrimerCollectableData> {
//    override val errorFlow: Flow<PrimerError>
//        get() = TODO("Not yet implemented")
//
//    override fun updateCollectedData(t: PrimerCollectableData) {
//        TODO("Not yet implemented")
//    }
//
//    override fun submit() {
//        TODO("Not yet implemented")
//    }
//
//    override val validationFlow: Flow<List<PrimerValidationError>>
//        get() = TODO("Not yet implemented")
//}
//
//class MyStepDataWrapper : PrimerCollectDataStepable<PrimerCollectDataStep> {
//    override val stepFlow: Flow<PrimerCollectDataStep>
//        get() = TODO("Not yet implemented")
//
//}
//
//
//interface PrimerHeadlessErrorable {
//
//    val errorFlow: Flow<PrimerError>
//}
//
//
//data class PaymentCardCollectableData(val cardNumber: String) : PrimerCollectableData
//
//class CardCollectDataComponent : PrimerHeadlessCollectDataComponent<PaymentCardCollectableData> {
//    override fun updateCollectedData(t: PaymentCardCollectableData) {
//    }
//
//    override val errorFlow: Flow<PrimerError>
//        get() = TODO("Not yet implemented")
//    override val validationFlow: Flow<List<PrimerValidationError>>
//        get() = TODO("Not yet implemented")
//
//    override fun submit() {
//        TODO("Not yet implemented")
//    }
//}
//
//interface PrimerInitData
//interface Initializable<T : PrimerInitData> {
//
//
//}
//
//data class BankListInitData(val banks: List<String>) : PrimerInitData
//
//
//class PrimerHeadlessNolPayManager : PrimerHeadlessPaymentMethodManager {
//
//    fun provideNolPayLinkComponent() = NolPayCollectLinkDataComponent()
//
//    fun provideStartPaymentComponent() = NolPayStartPaymentComponent()
//
//    fun provideNolPayLinkedCardsComponent() = NolPayLinkedCardsComponent()
//}
//
//class PrimerFormManager : PrimerHeadlessPaymentMethodManager {
//
//    fun provideCollectDataComponent(paymentMethod: String): PrimerHeadlessCollectDataComponent<PrimerCollectableData>
//}
