package com.flygames.flyinthesky

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.flygames.flyinthesky.ui.theme.FlyInTheSkyTheme
import com.flygames.flyinthesky.R

class PreloaderActivity : ComponentActivity() {

    private val preloaderViewModel: PreloaderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preloaderViewModel.wait()
        setContent {
            FlyInTheSkyTheme {
                val context = LocalContext.current
                if(preloaderViewModel.status == PreloaderStatus.Loading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black),

                    ) {
                        LoadingScreen()
                    }
                } else {
                    LaunchedEffect(key1 = preloaderViewModel.status) {
                        context.startActivity(Intent(context, MainActivity::class.java))
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(60.dp)
            )
            Image(
                modifier = Modifier.size(45.dp),
                painter = painterResource(id = R.drawable.star_1),
                contentDescription = "ball"
            )
        }
        Text(
            modifier = Modifier.padding(4.dp),
            color = Color.Black,
            text = "Loading",
        )
    }
}