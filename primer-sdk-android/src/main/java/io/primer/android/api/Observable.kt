package io.primer.android.api

import io.primer.android.model.json
import kotlinx.serialization.serializer
import org.json.JSONObject

class Observable {
  enum class Status {
    LOADING, SUCCESS, ERROR
  }

  abstract class ObservableEvent(val status: Status)

  class ObservableLoadingEvent(): ObservableEvent(Status.LOADING)
  class ObservableErrorEvent(val error: APIError): ObservableEvent(Status.ERROR)
  class ObservableSuccessEvent(val data: JSONObject): ObservableEvent(Status.SUCCESS) {
    inline fun <reified T> cast(): T {
      return json.decodeFromString(serializer(), data.toString())
    }
  }

  private val observers: MutableList<((ObservableEvent) -> Unit)> = ArrayList()
  private var result: ObservableEvent = ObservableLoadingEvent()

  fun observe(observer: ((ObservableEvent) -> Unit)): Observable {
    observers.add(observer)
    observer(result)
    return this
  }

  fun setSuccess(data: JSONObject) {
    setResult(ObservableSuccessEvent(data))
  }

  fun setError(error: APIError) {
    setResult(ObservableErrorEvent(error))
  }

  private fun setResult(result: ObservableEvent) {
    this.result = result
    observers.forEach {
      it(result)
    }
    observers.clear()
  }
}