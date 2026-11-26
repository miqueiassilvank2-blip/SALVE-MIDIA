package com.salvemidia.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salvemidia.app.R
import com.salvemidia.app.ui.theme.CardBackground
import com.salvemidia.app.ui.theme.TextSecondary
import com.salvemidia.app.ui.theme.TextWhite

private data class SocialNetwork(val name: String, val icon: Int? = null)

@Composable
fun NetworkGrid() {
    val context = LocalContext.current
    val networks = listOf(
        SocialNetwork("YouTube", R.drawable.ic_youtube),
        SocialNetwork("TikTok", R.drawable.ic_tiktok),
        SocialNetwork("Instagram", R.drawable.ic_instagram),
        SocialNetwork("Facebook", R.drawable.ic_facebook),
        SocialNetwork("X (Twitter)", R.drawable.ic_x),
        SocialNetwork("Kwai", R.drawable.ic_kwai),
        SocialNetwork("Vimeo", R.drawable.ic_vimeo),
        SocialNetwork("E muito mais!")
    )
    Column(modifier = Modifier.padding(16.dp)) {
        Text("🔥 Redes Suportadas", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            networks.chunked(4).forEach { rowNetworks ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowNetworks.forEach { network ->
                        Column(
                            modifier = Modifier.weight(1f).height(96.dp)
                                .clip(RoundedCornerShape(16.dp)).background(CardBackground)
                                .border(1.dp, TextSecondary.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                                .clickable { Toast.makeText(context, "Em breve: suporte direto para ${network.name}", Toast.LENGTH_SHORT).show() }
                                .padding(horizontal = 4.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (network.icon != null) {
                                Image(painterResource(network.icon), contentDescription = network.name, modifier = Modifier.size(40.dp))
                            } else {
                                Icon(Icons.Default.Add, contentDescription = null, tint = TextWhite, modifier = Modifier.size(40.dp))
                            }
                            Text(network.name, color = TextSecondary, fontSize = 10.sp, modifier = Modifier.fillMaxWidth().padding(top = 4.dp), maxLines = 2, textAlign = TextAlign.Center, lineHeight = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
