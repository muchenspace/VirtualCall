package com.muchen.virtualcall.presentation.call.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.muchen.virtualcall.R
import com.muchen.virtualcall.domain.model.Caller
import com.muchen.virtualcall.ui.theme.CallAnswerGreen
import com.muchen.virtualcall.ui.theme.CallDeclineRed
import com.muchen.virtualcall.ui.theme.CallPhoneNumber

@Composable
internal fun IncomingContent(
    caller: Caller,
    onAnswer: () -> Unit,
    onDecline: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp)
            .padding(top = 80.dp, bottom = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 联系人信息区：姓名 + 号码+位置 + 来电
        Text(
            text = caller.name,
            color = Color.White,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "${caller.formattedNumber}  ${caller.carrier}",
            color = CallPhoneNumber,
            fontSize = 18.sp,
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.SimCard,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = stringResource(R.string.incoming_call_label),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
            )
        }

        Spacer(Modifier.weight(1f))

        // 两个可上滑大圆形按钮：挂断（左）+ 接听（右），上方带三重箭头
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom,
        ) {
            SlideCallButton(
                icon = Icons.Filled.CallEnd,
                backgroundColor = CallDeclineRed,
                label = stringResource(R.string.action_decline),
                onClick = onDecline,
            )
            SlideCallButton(
                icon = Icons.Filled.Call,
                backgroundColor = CallAnswerGreen,
                label = stringResource(R.string.action_answer),
                onClick = onAnswer,
            )
        }
    }
}
