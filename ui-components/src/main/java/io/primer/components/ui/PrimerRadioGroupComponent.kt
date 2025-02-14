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

@Composable
fun PrimerRadioGroupComponent(options: List<String>, modifier: Modifier, onOptionSelected: (String) -> Unit) {
    // State to keep track of the selected option
    var selectedOption by remember { mutableStateOf(options.firstOrNull()) }

    Column(modifier = modifier.border(border = BorderStroke(2.dp, Color.Blue), shape = CutCornerShape(8.dp))) {
        options.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (option == selectedOption),
                        onClick = {
                            selectedOption = option
                            onOptionSelected(option)
                        },
                    )
                    .padding(8.dp),
            ) {
                RadioButton(
                    selected = (option == selectedOption),
                    onClick = {
                        selectedOption = option
                        onOptionSelected(option)
                    },
                )
                Text(
                    text = option,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
        }
    }
}
