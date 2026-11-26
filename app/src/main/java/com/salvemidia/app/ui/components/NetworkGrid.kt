package com.salvemidia.app.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.salvemidia.app.ui.theme.CardBackground
import com.salvemidia.app.ui.theme.TextSecondary
import com.salvemidia.app.ui.theme.TextWhite

data class SocialNetwork(val name: String, val icon: Int)

@Composable
fun NetworkGrid() {
    val context = LocalContext.current
    val networks = listOf(
        SocialNetwork("YouTube", android.R.drawable.ic_menu_slideshow),
        SocialNetwork("TikTok", android.R.drawable.ic_menu_camera),
        SocialNetwork("Instagram", android.R.drawable.ic_menu_gallery),
        SocialNetwork("Facebook", android.R.drawable.ic_menu_share),
        SocialNetwork("X (Twitter)", android.R.drawable.ic_menu_info_details),
        SocialNetwork("Kwai", android.R.drawable.ic_menu_send),
        SocialNetwork("Vimeo", android.R.drawable.ic_menu_view),
        SocialNetwork("E muito mais!", android.R.drawable.ic_menu_add)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Redes Suportadas",
            color = TextWhite,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            networks.chunked(4).forEach { rowNetworks ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowNetworks.forEach { network ->
                        NetworkItem(
                            network = network,
                            modifier = Modifier.weight(1f)
                        ) {
                            Toast.makeText(context, "Em breve: suporte direto para ${network.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkItem(network: SocialNetwork, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardBackground)
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(network.icon),
            contentDescription = network.name,
            tint = TextWhite,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = network.name,
            color = TextSecondary,
            fontSize = 10.sp,
            modifier = Modifier.padding(top = 4.dp),
            maxLines = 1
        )
    }
}
