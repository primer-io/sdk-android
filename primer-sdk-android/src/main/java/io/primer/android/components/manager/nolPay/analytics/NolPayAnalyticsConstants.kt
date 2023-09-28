package io.primer.android.components.manager.nolPay.analytics

internal object NolPayAnalyticsConstants {

    const val COLLECTED_DATA_SDK_PARAMS = "collectedData"

    // link component
    const val LINK_CARD_START_METHOD = "NolPayLinkCardComponent.start()"
    const val LINK_CARD_UPDATE_COLLECTED_DATA_METHOD =
        "NolPayLinkCardComponent.updateCollectedData()"
    const val LINK_CARD_SUBMIT_DATA_METHOD = "NolPayLinkCardComponent.submit()"

    // unlink component
    const val UNLINK_CARD_START_METHOD = "NolPayUnlinkCardComponent.start()"
    const val UNLINK_CARD_UPDATE_COLLECTED_DATA_METHOD =
        "NolPayUnlinkCardComponent.updateCollectedData()"
    const val UNLINK_CARD_SUBMIT_DATA_METHOD = "NolPayUnlinkCardComponent.submit()"

    // payment component
    const val PAYMENT_START_METHOD = "NolPayStartPaymentComponent.start()"
    const val PAYMENT_UPDATE_COLLECTED_DATA_METHOD =
        "NolPayStartPaymentComponent.updateCollectedData()"
    const val PAYMENT_SUBMIT_DATA_METHOD = "NolPayStartPaymentComponent.submit()"

    // list cards component
    const val LINKED_CARDS_GET_CARDS_METHOD = "NolPayLinkedCardsComponent.getLinkedCards()"
}
