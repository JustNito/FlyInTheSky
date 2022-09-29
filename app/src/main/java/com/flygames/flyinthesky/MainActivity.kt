package com.flygames.flyinthesky

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.flygames.flyinthesky.ui.theme.FlyInTheSkyTheme
import com.flygames.flyinthesky.ui.theme.Red700
import com.flygames.flyinthesky.utils.GameStatus
import com.flygames.flyinthesky.utils.Star

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModelFactory((application as MyApp).storage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FlyInTheSkyTheme {
                Game(
                    rotate = gameViewModel.rotate,
                    changeRotate = gameViewModel::changeRotate,
                    planePosition = gameViewModel.planePosition,
                    initAreaSize = gameViewModel::initAreaSize,
                    initPlaneSize = gameViewModel::initPlaneSize,
                    gameStatus = gameViewModel.gameStatus,
                    planeBounds = gameViewModel.planeBounds,
                    gameStart = gameViewModel::start,
                    score = gameViewModel.score,
                    highScore = gameViewModel.highScore,
                    restartGame = gameViewModel::restartGame,
                    starsCord = gameViewModel.backgroundStars,
                    collectableStar = gameViewModel.collectableStar,
                    circleCenter = gameViewModel.point
                )
            }
        }
    }

    override fun onBackPressed() {}

}

@Composable
fun Game(
    rotate: Float,
    planePosition: Offset,
    circleCenter: Offset,
    planeBounds: List<Offset>,
    initPlaneSize: (Size) -> Unit,
    gameStart: () -> Unit,
    restartGame: () -> Unit,
    gameStatus: GameStatus,
    collectableStar: Offset,
    starsCord: List<Star>,
    score: Int,
    highScore: Int,
    initAreaSize: (Size) -> Unit,
    changeRotate: (Float) -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .scrollable(
                enabled = gameStatus != GameStatus.Stop,
                orientation = Orientation.Vertical,
                state = rememberScrollableState { delta ->
                    changeRotate(rotate + delta)
                    delta
                }
            )
            .background(Red700)
    ) {
        Canvas(modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .background(Color.Black)
        ) {
            val drawablePlane = context.getDrawable(R.drawable.airplane)
            val planeSize = .2f
            val planeBmp = drawablePlane!!.toBitmap(
                width = (drawablePlane.intrinsicWidth * planeSize).toInt(),
                height = (drawablePlane.intrinsicHeight * planeSize).toInt()
            )
            val drawableStar = context.getDrawable(R.drawable.star_1)
            val starSize = .05f
            val starBmp = drawableStar!!.toBitmap(
                width = (drawableStar.intrinsicWidth * starSize).toInt(),
                height = (drawableStar.intrinsicHeight * starSize).toInt()
            )
            starsCord.forEach {
                drawCircle(
                    color = Color.White,
                    center = it.cord,
                    radius = it.radius
                )
            }
            when(gameStatus) {
                GameStatus.WaitForStart -> {
                    initAreaSize(Size(size.width, size.height))
                    initPlaneSize(Size(planeBmp.width.toFloat(), planeBmp.height.toFloat()))
                }
                GameStatus.Game -> {
                    drawPlane(
                        planeBmp = planeBmp,
                        rotate = rotate,
                        planePosition = planePosition,
                    )
//                    drawCircle(
//                        color = Color.Green,
//                        radius = 20f,
//                        center = circleCenter
//                    )
//                    drawPoints(
//                        points = planeBounds,
//                        pointMode = PointMode.Lines,
//                        strokeWidth = 10f,
//                        color = Color.Blue
//                    )
                    drawImage(
                        image = starBmp.asImageBitmap(),
                        topLeft = collectableStar
                    )
                }
                GameStatus.Stop -> {
                    val explosionSize = .2f
                    val explosionDrawable = context.getDrawable(R.drawable.explosion)
                    val explosionBmp = explosionDrawable!!.toBitmap(
                        width = (explosionDrawable.intrinsicWidth * explosionSize).toInt(),
                        height = (explosionDrawable.intrinsicHeight * explosionSize).toInt()
                    )
                    drawImage(
                        topLeft = planePosition,
                        image = explosionBmp.asImageBitmap()
                    )
                }
            }
        }
        when(gameStatus) {
            GameStatus.WaitForStart -> {
                Button(
                    modifier = Modifier.align(Alignment.Center),
                    onClick = gameStart
                ) {
                    Text("Start")
                }
                ExtendedFloatingActionButton(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                    onClick = { openCustomTab(context) },
                    text = { Text("Privacy Policy") }
                )
            }
            GameStatus.Game -> {
                Text(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(8.dp),
                    text = "SCORE: $score",
                    style = MaterialTheme.typography.h4,
                    color = MaterialTheme.colors.secondary
                )
            }
            GameStatus.Stop -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Game Over",
                        style = MaterialTheme.typography.h3,
                        color = MaterialTheme.colors.secondary
                    )
                    Text(
                        text = "High Score $highScore",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.secondary
                    )
                    Text(
                        text = "Score: $score",
                        style = MaterialTheme.typography.h5,
                        color = MaterialTheme.colors.secondary
                    )
                    Button(onClick = restartGame) {
                        Text("Play Again")
                    }
                }
            }
        }
    }
}

fun DrawScope.drawPlane(
    planeBmp: Bitmap,
    rotate: Float,
    planePosition: Offset
) {
    val pivot = Offset(
        planePosition.x + planeBmp.width / 2,
        planePosition.y + planeBmp.height / 2
    )
    rotate(
        degrees = rotate,
        pivot = pivot
    ) {
        drawImage(
            topLeft = planePosition,
            image = planeBmp.asImageBitmap()
        )
    }

}

@Preview(showBackground = true)
@Composable
fun PreviewPlane() {
    FlyInTheSkyTheme {
        Game(
            rotate = 0f,
            changeRotate = {},
            planePosition = Offset(0f, 0f),
            circleCenter = Offset(100f, 100f),
            gameStatus = GameStatus.WaitForStart,
            initPlaneSize = {},
            initAreaSize = {},
            planeBounds = listOf(),
            starsCord = listOf(),
            score = 0,
            highScore = 0,
            collectableStar = Offset(0f, 0f),
            gameStart = {},
            restartGame = {}
        )
    }
}

fun openCustomTab(context: Context) {
    val url = context.getString(R.string.custom_tab_url)
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}
