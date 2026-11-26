package com.salvemidia.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.salvemidia.app.ui.navigation.BottomNavigationBar
import com.salvemidia.app.ui.navigation.NavigationGraph
import com.salvemidia.app.ui.theme.SalveMidiaTheme
import java.io.File

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val crashFile = File(filesDir, "crash.txt")
        setContent {
            SalveMidiaTheme {
                var crash by remember { mutableStateOf(if (crashFile.exists()) crashFile.readText() else null) }
                if (crash != null) {
                    Column(Modifier.fillMaxSize().background(Color.Black).padding(16.dp).verticalScroll(rememberScrollState())) {
                        Text("ERRO ANTERIOR (me mande print):", color = Color.Yellow)
                        Text(crash!!.take(2500), color = Color.White, fontSize = 11.sp)
                        Button(onClick = { crashFile.delete(); crash = null }) { Text("Continuar") }
                    }
                } else {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = { BottomNavigationBar(navController) },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        NavigationGraph(navController, Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}
