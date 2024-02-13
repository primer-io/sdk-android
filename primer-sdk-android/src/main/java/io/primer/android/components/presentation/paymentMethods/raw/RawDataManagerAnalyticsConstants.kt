package io.primer.android.components.presentation.paymentMethods.raw

internal object RawDataManagerAnalyticsConstants {

    // PrimerHeadlessUniversalCheckoutRawDataManager
    const val CONFIGURE_METHOD = "PrimerHeadlessUniversalCheckoutRawDataManager.configure"
    const val SET_LISTENER_METHOD = "PrimerHeadlessUniversalCheckoutRawDataManager.setListener"
    const val SET_RAW_DATA_METHOD = "PrimerHeadlessUniversalCheckoutRawDataManager.setRawData"
    const val SUBMIT_METHOD = "PrimerHeadlessUniversalCheckoutRawDataManager.submit"
    const val GET_INPUT_ELEMENT_TYPES_METHOD =
        "PrimerHeadlessUniversalCheckoutRawDataManager.getRequiredInputElementTypes"
    const val CLEANUP_METHOD =
        "PrimerHeadlessUniversalCheckoutRawDataManager.cleanup"

    // Params
    const val PAYMENT_METHOD_TYPE_PARAM = "paymentMethodType"
    const val PREFERRED_NETWORK_PARAM = "preferredNetwork"

    // PrimerHeadlessUniversalCheckoutRawDataManagerListener
    const val ON_VALIDATION_CHANGED =
        "PrimerHeadlessUniversalCheckoutRawDataManagerListener.onValidationChanged"
    const val ON_METADATA_STATE_CHANGED =
        "PrimerHeadlessUniversalCheckoutRawDataManagerListener.onMetadataStateChanged"

    // Params
    const val ON_VALIDATION_IS_VALID_PARAM = "isValid"
    const val ON_VALIDATION_IS_VALIDATION_ERRORS_PARAM = "errors"
    const val ON_METADATA_STATE_STATE_PARAM = "metadataState"
}
