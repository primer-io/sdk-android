package io.primer.components.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * A composable radio group component that allows users to select an option from a list.
 *
 * @param modifier The [Modifier] to be applied to the radio group container.
 * @param options A list of string options to be displayed as selectable items.
 * @param onSelectionChange A callback triggered when an option is selected, providing the selected value.
 */
@Composable
fun PrimerRadioGroupComponent(
    modifier: Modifier = Modifier,
    options: List<String>,
    onSelectionChange: (String) -> Unit,
) {
    // State to keep track of the selected option
    var selectedOption by remember { mutableStateOf(options.firstOrNull()) }

    Column(
        modifier = modifier.border(border = BorderStroke(2.dp, Color.Blue), shape = CutCornerShape(8.dp)),
    ) { // TODO TWS: don't hardcode colors or dimens
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier // TODO TWS: modifiers shouldn't be reused like this
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            selectedOption = option
                            onSelectionChange(option)
                        },
                    )
                    .padding(8.dp), // TODO TWS: don't hardcode dimens
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = {
                        selectedOption = option
                        onSelectionChange(option)
                    },
                )
                Text(
                    text = option,
                    modifier = Modifier.padding(start = 8.dp), // TODO TWS: don't hardcode dimens
                )
            }
        }
    }
}
