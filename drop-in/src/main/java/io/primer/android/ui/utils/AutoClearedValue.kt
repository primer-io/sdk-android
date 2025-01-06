package io.primer.android.ui.utils

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

internal class AutoClearedValue<T : Any>(
    fragment: Fragment,
    private val initializer: (() -> T)?,
) : ReadWriteProperty<Fragment, T> {
    @Suppress("ktlint:standard:property-naming")
    private var _value: T? = null

    init {
        fragment.lifecycle.addObserver(
            object : LifecycleEventObserver {
                val viewLifecycleOwnerObserver =
                    Observer<LifecycleOwner?> { viewLifecycleOwner ->

                        viewLifecycleOwner?.lifecycle?.addObserver(
                            object : LifecycleEventObserver {
                                override fun onStateChanged(
                                    source: LifecycleOwner,
                                    event: Lifecycle.Event,
                                ) {
                                    when (event) {
                                        Lifecycle.Event.ON_DESTROY -> _value = null
                                        else -> Unit
                                    }
                                }
                            },
                        )
                    }

                override fun onStateChanged(
                    source: LifecycleOwner,
                    event: Lifecycle.Event,
                ) {
                    when (event) {
                        Lifecycle.Event.ON_CREATE ->
                            fragment.viewLifecycleOwnerLiveData.observeForever(
                                viewLifecycleOwnerObserver,
                            )
                        Lifecycle.Event.ON_DESTROY ->
                            fragment.viewLifecycleOwnerLiveData.removeObserver(
                                viewLifecycleOwnerObserver,
                            )
                        else -> Unit
                    }
                }
            },
        )
    }

    override fun getValue(
        thisRef: Fragment,
        property: KProperty<*>,
    ): T {
        val value = _value

        if (value != null) {
            return value
        }

        if (thisRef.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)
        ) {
            return initializer?.invoke().also { _value = it }
                ?: throw IllegalStateException(
                    "The value has not yet been set or no default initializer provided",
                )
        } else {
            throw IllegalStateException(
                "Fragment might have been destroyed or not initialized yet",
            )
        }
    }

    override fun setValue(
        thisRef: Fragment,
        property: KProperty<*>,
        value: T,
    ) {
        _value = value
    }
}
