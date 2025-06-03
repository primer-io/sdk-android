package io.primer.composable.internal.presentation.screens.card.components.input

interface InputFormatter {
    fun format(value: String): String
    fun clean(input: String): String
}

object NoOpFormatter : InputFormatter {
    override fun format(value: String): String = value
    override fun clean(input: String): String = input
}
