package io.primer.android.api

import org.json.JSONObject

class Observable {
  enum class Status {
    LOADING, SUCCESS, ERROR
  }

  abstract class ObservableResult(val status: Status)

  class PendingResult(): ObservableResult(Status.LOADING)
  class SuccessResult(val data: JSONObject): ObservableResult(Status.SUCCESS)
  class ErrorResult(val error: APIError): ObservableResult(Status.ERROR)

  private val observers: MutableList<((ObservableResult) -> Unit)> = ArrayList()
  private var result: ObservableResult = PendingResult()

  fun observe(observer: ((ObservableResult) -> Unit)): Observable {
    observers.add(observer)
    observer(result)
    return this
  }

  fun setSuccess(data: JSONObject) {
    setResult(SuccessResult(data))
  }

  fun setError(error: APIError) {
    setResult(ErrorResult(error))
  }

  private fun setResult(result: ObservableResult) {
    this.result = result
    observers.forEach {
      it(result)
    }
    observers.clear()
  }
}