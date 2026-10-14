package io.primer.nolpay.api.exceptions

class NolPaySdkException(val errorCode: String, message: String?) : Exception(message)
