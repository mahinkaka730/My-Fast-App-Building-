package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun SmsDraftDialog(
    initialRecipient: String,
    initialBody: String,
    onDismiss: () -> Unit,
    onConfirm: (recipient: String, body: String) -> Unit
) {
    var recipient by remember { mutableStateOf(initialRecipient) }
    var body by remember { mutableStateOf(initialBody) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        shape = RoundedCornerShape(18.dp),
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Message,
                contentDescription = "SMS Draft",
                tint = ElectricBlue
            )
        },
        title = {
            Text(
                text = "এসএমএস প্রেরণের খসড়া",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "KAKA AI আপনার অনুরোধে এই বার্তাটি প্রস্তুত করেছে। প্রাপকের নম্বর ও মেসেজ যাচাই করে এসএমএস অ্যাপে খুলুন। কোনো বার্তা স্বয়ংক্রিয়ভাবে পাঠানো হবে না।",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = recipient,
                    onValueChange = { recipient = it },
                    label = { Text("প্রাপকের ফোন নম্বর (ঐচ্ছিক)") },
                    placeholder = { Text("+8801...") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = ElectricBlue)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("sms_recipient_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = { Text("বার্তা / Message Text") },
                    placeholder = { Text("আপনার মেসেজ লিখুন...") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth().testTag("sms_body_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(recipient, body) },
                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                modifier = Modifier.testTag("sms_confirm_button")
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Text(" এসএমএস অ্যাপে পাঠান", color = SurfaceDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("sms_cancel_button")
            ) {
                Text("বাতিল করুন", color = TextSecondary)
            }
        }
    )
}
