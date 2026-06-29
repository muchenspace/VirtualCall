package com.muchen.virtualcall.presentation.main.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.R
import com.muchen.virtualcall.ui.components.BadgeType
import com.muchen.virtualcall.ui.components.PrimaryButton
import com.muchen.virtualcall.ui.components.SecondaryButton
import com.muchen.virtualcall.ui.components.SectionCard
import com.muchen.virtualcall.ui.components.StatusBadge
import com.muchen.virtualcall.ui.theme.LocalAppTheme

@Composable
internal fun CustomizeCard(
    contactName: String,
    contactNumber: String,
    contactCarrier: String,
    ringtoneLabel: String,
    recordingLabel: String,
    hasRecording: Boolean,
    onSaveContact: (String, String, String) -> Unit,
    onPreviewRingtone: () -> Unit,
    onSelectRecording: () -> Unit,
    onClearRecording: () -> Unit,
    onRestoreDefaults: () -> Unit,
) {
    val colors = LocalAppTheme.current
    // 局部 state 管理输入框文本，按键仅更新本地状态，不触发父级重组
    var localName by remember(contactName) { mutableStateOf(contactName) }
    var localNumber by remember(contactNumber) { mutableStateOf(contactNumber) }
    var localCarrier by remember(contactCarrier) { mutableStateOf(contactCarrier) }

    SectionCard(
        title = stringResource(R.string.label_customize),
        subtitle = stringResource(R.string.subtitle_customize),
        icon = Icons.Outlined.Call,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = localName.ifBlank { stringResource(R.string.default_contact_name) },
                    color = colors.textPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = localNumber.ifBlank { stringResource(R.string.default_phone_number) },
                    color = colors.textSecondary,
                    fontSize = 13.sp,
                )
                Spacer(Modifier.height(8.dp))
                StatusBadge(text = ringtoneLabel, type = BadgeType.SUCCESS)
            }
        }
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(
            value = localName,
            onValueChange = { localName = it },
            label = { Text(stringResource(R.string.hint_contact_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.borderSubtle,
                focusedLabelColor = colors.accent,
                cursorColor = colors.accent,
            ),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = localNumber,
            onValueChange = { localNumber = it },
            label = { Text(stringResource(R.string.hint_phone_number)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.borderSubtle,
                focusedLabelColor = colors.accent,
                cursorColor = colors.accent,
            ),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = localCarrier,
            onValueChange = { localCarrier = it },
            label = { Text(stringResource(R.string.hint_carrier)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = colors.accent,
                unfocusedBorderColor = colors.borderSubtle,
                focusedLabelColor = colors.accent,
                cursorColor = colors.accent,
            ),
        )
        Spacer(Modifier.height(18.dp))
        PrimaryButton(
            text = stringResource(R.string.btn_save_contact),
            icon = Icons.Outlined.Save,
            onClick = { onSaveContact(localName, localNumber, localCarrier) },
        )
        Spacer(Modifier.height(10.dp))
        SecondaryButton(
            text = stringResource(R.string.btn_preview_ringtone),
            icon = Icons.AutoMirrored.Outlined.VolumeUp,
            onClick = onPreviewRingtone,
            modifier = Modifier.fillMaxWidth(),
            tint = colors.accent,
        )
        Spacer(Modifier.height(10.dp))
        SecondaryButton(
            text = stringResource(R.string.btn_select_recording),
            icon = Icons.Outlined.GraphicEq,
            onClick = onSelectRecording,
            modifier = Modifier.fillMaxWidth(),
            tint = colors.accent,
        )
        // 已选择录音时显示状态徽章和清除按钮
        if (hasRecording) {
            Spacer(Modifier.height(8.dp))
            StatusBadge(
                text = recordingLabel,
                type = BadgeType.SUCCESS,
            )
            Spacer(Modifier.height(8.dp))
            SecondaryButton(
                text = stringResource(R.string.btn_clear_recording),
                icon = Icons.Outlined.Refresh,
                onClick = onClearRecording,
                modifier = Modifier.fillMaxWidth(),
                tint = colors.textSecondary,
            )
        }
        Spacer(Modifier.height(10.dp))
        SecondaryButton(
            text = stringResource(R.string.btn_restore_defaults),
            icon = Icons.Outlined.Refresh,
            onClick = onRestoreDefaults,
            tint = colors.textSecondary,
        )
    }
}
