package io.primer.android.ui

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.primer.android.api.APIClient
import io.primer.android.api.IAPIClient
import io.primer.android.session.ClientSession
import io.primer.android.session.ClientToken
import io.primer.android.session.SessionFactory

class PrimerViewModel(context: Context, token: String): ViewModel() {
  private val clientToken = ClientToken(token)
  private val api = APIClient(context, clientToken)
  private val sessionFactory = SessionFactory(api, clientToken)
  private var session: ClientSession? = null

  private val loading: MutableLiveData<Boolean> = MutableLiveData(true)

  init {
    sessionFactory.create {
      this.session = it
    }
  }
}