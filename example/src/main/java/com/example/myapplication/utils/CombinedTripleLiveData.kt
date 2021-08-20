package com.example.myapplication.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class CombinedTripleLiveData<T, K, M, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    source3: LiveData<M>,
    private val combine: (data1: T?, data2: K?, data3: M?) -> S,
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null
    private var data3: M? = null

    init {
        super.addSource(source1) {
            data1 = it
            value = combine(data1, data2, data3)
        }
        super.addSource(source2) {
            data2 = it
            value = combine(data1, data2, data3)
        }
        super.addSource(source3) {
            data3 = it
            value = combine(data1, data2, data3)
        }
    }

    override fun <T : Any?> addSource(source: LiveData<T>, onChanged: Observer<in T>) {
        throw UnsupportedOperationException()
    }

    override fun <T : Any?> removeSource(toRemote: LiveData<T>) {
        throw UnsupportedOperationException()
    }
}