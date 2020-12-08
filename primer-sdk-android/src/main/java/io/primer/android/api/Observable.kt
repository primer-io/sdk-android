package io.primer.android.api

import io.primer.android.model.json
import kotlinx.serialization.decodeFromString
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
      return json.decodeFromString(data.toString())
//      return json.decodeFromString(serializer(), data.toString())
    }

    inline fun <reified T> cast(key: String, defaultValue: T): T {
      if (!data.has(key)) {
        return defaultValue
      }

      val serialized = if (defaultValue is List<*>) {
        data.getJSONArray(key).toString()
      } else {
        data.getJSONObject(key).toString()
      }

      return json.decodeFromString(serialized)
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