package com.example.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPink
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmLabel: String = "নিশ্চিত করুন",
    dismissLabel: String = "বাতিল",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(18.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = AccentPink
            )
        },
        title = {
            Text(
                text = title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = message,
                color = TextSecondary,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentPink),
                modifier = Modifier.testTag("confirm_action_button")
            ) {
                Text(confirmLabel, color = TextPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_action_button")
            ) {
                Text(dismissLabel, color = TextSecondary)
            }
        }
    )
}
