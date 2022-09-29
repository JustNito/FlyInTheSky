package com.flygames.flyinthesky

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flygames.flyinthesky.utils.GameStatus
import com.flygames.flyinthesky.utils.Star
import com.flygames.flyinthesky.utils.Vector
import com.travelgames.roadrace.data.Storage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class GameViewModel(private val storage: Storage): ViewModel() {
    private var _rotate by mutableStateOf(0f)
    val rotate
        get() = _rotate

    private var _planePosition by mutableStateOf(Offset(500f,500f))
    val planePosition
        get() = _planePosition

    private var _point by mutableStateOf(Offset(0f, 0f))
    val point
        get() = _point

    private var _gameStatus by mutableStateOf(GameStatus.WaitForStart)
    val gameStatus
        get() = _gameStatus

    private var _score by mutableStateOf(0)
    val score
        get() = _score

    private var _highScore by mutableStateOf(storage.getScore())
    val highScore
        get() = _highScore

    private var _collectableStar by mutableStateOf(Offset(-1f,-1f))
    val collectableStar
        get() = _collectableStar

    private val _backgroundStars = mutableStateListOf<Star>()
    val backgroundStars
        get() = _backgroundStars

    private var planeSpeed = 0f

    private var _planeBounds by mutableStateOf(listOf<Offset>())
    val planeBounds
        get() = _planeBounds

    private var areaSize = Size(0f,0f)

    private var planeSize = Size(0f,0f)

    fun changeRotate(rotate: Float) {
        _rotate = rotate
        updatePointPosition()
        changePlanePos()
    }

    private fun spawnStar() {
        val verticalBound = areaSize.height * 0.1f
        val horizontalBound = areaSize.width * 0.1f
        val y = Random.nextInt(
            verticalBound.toInt(),
            areaSize.width.toInt() - verticalBound.toInt()
        )
        val x = Random.nextInt(
            horizontalBound.toInt(),
            areaSize.width.toInt() - horizontalBound.toInt()
        )
        _collectableStar = Offset(x.toFloat(), y.toFloat())
    }

    private fun changePlaneBounds() {
        val planeStart = Offset(planePosition.x + planeSize.width / 2, planePosition.y + planeSize.height / 2)
        val bounds = mutableListOf<Offset>()
        val vector1 = Vector(
            start = getVectorPoint(rotate, planeStart, planeSize.width / 2),
            end = getVectorPoint(rotate - 180f, planeStart, planeSize.width / 2)
        )
        val vector2 = Vector(
            start = vector1.start,
            end = getVectorPoint(rotate - 90f, planeStart, planeSize.width / 3)
        )
        val vector3 = Vector(
            start = vector1.start,
            end = getVectorPoint(rotate + 90f, planeStart, planeSize.width / 3)
        )
        val vector4 = Vector(
            start = vector1.end,
            end = vector3.start
        )
        val vector5 = Vector(
            start = vector1.end,
            end = vector2.end
        )
        bounds.apply {
            addAll(getVectorPoints(vector2))
            addAll(getVectorPoints(vector3))
            addAll(getVectorPoints(vector4))
            addAll(getVectorPoints(vector5))
        }
        _planeBounds = bounds
    }

    private fun isWentBeyond(): Boolean =
        planeBounds.any {
            it.y > areaSize.height
        } || planeBounds.any {
            it.x > areaSize.width
        } || planeBounds.any {
            it.y < 0
        } || planeBounds.any {
            it.x < 0
        }


    private fun getVectorPoints(vector: Vector): List<Offset> {
        val normal = normal(vector.end.x - vector.start.x, vector.end.y - vector.start.y)
        var t = 0f
        val points = mutableListOf<Offset>()
        do {
            val g = normal * t
            val point = Offset(vector.start.x + g.x, vector.start.y + g.y)
            points.add(point)
            t += 1f
        }
        while (point.x.toInt() != vector.end.x.toInt() && point.y.toInt() != vector.end.y.toInt())
        return points
    }

    private fun getVectorPoint(angle: Float, startPoint: Offset, len: Float): Offset {
        val radians = angle * Math.PI / 180
        return Offset(
            (startPoint.x + len * cos(radians)).toFloat(),
            (startPoint.y + len * sin(radians)).toFloat()
        )
    }

    fun initAreaSize(size: Size) {
        areaSize = size
        if(_backgroundStars.isEmpty())
            populateStarsList()
    }

    fun initPlaneSize(size: Size) {
        planeSize = size
    }

    private suspend fun increaseInSpeed() {
        while(planeSpeed < 10f) {
            planeSpeed += 1
            delay(200)
        }
    }

    private fun updatePointPosition() {
        val r = planeSize.width
        val radians = rotate * Math.PI / 180
        _point = Offset(
            (planePosition.x + r * cos(radians) + planeSize.width / 2).toFloat(),
            (planePosition.y + r * sin(radians) + planeSize.height / 2).toFloat()
        )
    }

    private fun populateStarsList() {
        repeat(1000) {
            val radius = Random.nextFloat() * 2f
            val x = Random.nextInt(0, areaSize.width.toInt()).toFloat()
            val y = Random.nextInt(0, areaSize.height.toInt()).toFloat()
            _backgroundStars.add(Star(cord = Offset(x,y), radius = radius))
        }
    }

    private fun gameOver() {
        if(score > storage.getScore()) {
            _highScore = _score
            storage.setScore(_score)
        }
    }

    private fun isStarCollect(): Boolean =
        planeBounds.any {
            it.y.toInt() == collectableStar.y.toInt()
        } && planeBounds.any {
            it.x.toInt() == collectableStar.x.toInt()
        }

    fun start() = viewModelScope.launch{
        _rotate = 0f
        spawnStar()
        _planePosition = Offset(
            (areaSize.width / 2) - (planeSize.width / 2),
            (areaSize.height / 2) - (planeSize.height / 2)
        )
        updatePointPosition()
        _gameStatus = GameStatus.Game
        launch {
            increaseInSpeed()
        }
        while (true) {
            changePlanePos()
            updatePointPosition()
            delay(10)
            if(gameStatus == GameStatus.Stop) {
                break
            }
        }
    }

    fun restartGame() {
        _rotate = 0f
        _score = 0
        planeSpeed = 0f
        start()
    }

    private fun changePlanePos() {
        val x1 = _planePosition.x
        val y1 = _planePosition.y
        val x2 = _point.x
        val y2 = _point.y
        val normal = normal(x2 - x1, y2 - y1)
        var t = 0f
        val g = normal * planeSpeed
        _planePosition = Offset(x1 + g.x, y1 + g.y)
        if(isStarCollect()) {
            _score++
            spawnStar()
        }
        changePlaneBounds()
        t += 1f
        if(isWentBeyond()) {
            gameOver()
            _gameStatus = GameStatus.Stop
        }
    }

    private fun normal(a: Float, b: Float): Offset =
        Offset(a / sqrt(a.pow(2) + b.pow(2)), b / sqrt(a.pow(2) + b.pow(2)))
}

