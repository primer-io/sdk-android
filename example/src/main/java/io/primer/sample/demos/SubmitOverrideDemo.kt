package io.primer.sample.demos

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object SubmitOverrideDemo : CheckoutDemo(
    title = "Submit Button Override",
    description = "Replace submit button with a full-width floating action button with enhanced styling",
    customizationLevel = 2,
    render = {
        cardForm.submitButton = { modifier, text ->
            ExtendedFloatingActionButton(
                onClick = { cardForm.onSubmit() },
                modifier = modifier
                    .fillMaxWidth(),
                containerColor = Color(0xFF4CAF50),
                contentColor = Color.White,
                elevation = androidx.compose.material3.FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Gimme your moni",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
)
