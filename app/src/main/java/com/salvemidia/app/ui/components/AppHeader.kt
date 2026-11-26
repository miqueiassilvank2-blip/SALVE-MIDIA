package com.salvemidia.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salvemidia.app.ui.theme.PurpleMain
import com.salvemidia.app.ui.theme.TextSecondary
import com.salvemidia.app.ui.theme.TextWhite
import com.salvemidia.app.R

@Composable
fun AppHeader(onSettingsClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(R.drawable.ic_header),
            contentDescription = null,
            modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = TextWhite, fontWeight = FontWeight.Bold)) {
                        append("Salve")
                    }
                    withStyle(style = SpanStyle(color = PurpleMain, fontWeight = FontWeight.Bold)) {
                        append("Mídia")
                    }
                },
                fontSize = 32.sp
            )
            Text(
                text = "Baixe vídeos e áudios\ndas suas redes favoritas!",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurações",
                tint = TextWhite
            )
        }
    }
}
