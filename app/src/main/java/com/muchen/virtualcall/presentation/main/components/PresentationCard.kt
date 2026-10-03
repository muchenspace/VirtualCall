package com.muchen.virtualcall.presentation.main.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.R
import com.muchen.virtualcall.domain.model.PresentationMode
import com.muchen.virtualcall.ui.components.RadioOption
import com.muchen.virtualcall.ui.components.SecondaryButton
import com.muchen.virtualcall.ui.components.SectionCard
import com.muchen.virtualcall.ui.theme.LocalAppTheme

@Composable
internal fun PresentationCard(
    presentationMode: PresentationMode,
    overlayPermissionGranted: Boolean,
    presentationStatusText: String,
    onModeChange: (PresentationMode) -> Unit,
    onRequestOverlayPermission: () -> Unit,
) {
    val colors = LocalAppTheme.current
    SectionCard(
        title = stringResource(R.string.label_presentation_mode),
        subtitle = stringResource(R.string.subtitle_presentation_mode),
        icon = Icons.Outlined.Tune,
    ) {
        RadioOption(
            label = stringResource(R.string.option_presentation_fullscreen),
            selected = presentationMode == PresentationMode.FULLSCREEN,
            onSelect = { onModeChange(PresentationMode.FULLSCREEN) },
        )
        Spacer(Modifier.height(10.dp))
        RadioOption(
            label = stringResource(R.string.option_presentation_overlay),
            selected = presentationMode == PresentationMode.OVERLAY,
            onSelect = { onModeChange(PresentationMode.OVERLAY) },
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = presentationStatusText,
            color = colors.textSecondary,
            fontSize = 12.sp,
        )
        if (presentationMode == PresentationMode.OVERLAY && !overlayPermissionGranted) {
            Spacer(Modifier.height(12.dp))
            SecondaryButton(
                text = stringResource(R.string.btn_overlay_permission),
                icon = Icons.Outlined.VerifiedUser,
                onClick = onRequestOverlayPermission,
                tint = colors.accent,
            )
        }
    }
}
