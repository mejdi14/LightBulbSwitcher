import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

import data.BulbStringConfig
import data.BulbSwitcherActionListener
import kotlinx.coroutines.launch
import lightbulbswitcher.composeapp.generated.resources.Res
import lightbulbswitcher.composeapp.generated.resources.bulb_switcher_off
import lightbulbswitcher.composeapp.generated.resources.bulb_switcher_on
import theme.AppTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@Composable
@Preview
fun App() {
    val isDark = remember { mutableStateOf(false) }

    AppTheme(darkTheme = isDark.value) {

        BulbSwitcher(listener = object : BulbSwitcherActionListener {
            override fun onPull(position: Offset) {
                // when you start pulling the string
            }

            override fun onRelease(position: Offset) {
                // when you release the string
                isDark.value = !isDark.value
            }

            override fun onEndRelease() {
            }

            override fun onClickListener() {
                isDark.value = !isDark.value
            }

        }, modifier = Modifier.background(color = MaterialTheme.colors.background))
        Box(modifier = Modifier.fillMaxSize().padding(horizontal = 100.dp).padding(top = 200.dp)) {
            Column (horizontalAlignment = Alignment.CenterHorizontally){
                Text("Lorem Ipsum is simply dummy text of the printing and typesetting industry." +
                        " Lorem Ipsum has been the industry's standard dummy text ever since the 1500s," +
                        " when an unknown printer took a galley of type and scrambled it to make a type specimen book." +
                        " It has survived not only five centuries, but also the leap into electronic typesetting," +
                        " remaining essentially unchanged." +
                        " It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages," +
                        " and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.", color = MaterialTheme.colors.primary)
                Spacer(Modifier.height(100.dp))
                Box(Modifier.clip(shape =
                    RoundedCornerShape(15.dp)
                ).background(color = MaterialTheme.colors.primary).height(60.dp).width(140.dp)){
                    Text("Button", Modifier.align(Alignment.Center), color = MaterialTheme.colors.secondary)
                }

            }
        }
    }
}

@Composable
private fun BulbSwitcher(
    config: BulbStringConfig = BulbStringConfig(),
    listener: BulbSwitcherActionListener,
    modifier: Modifier = Modifier
) {
    val bulbState = remember { mutableStateOf(config.initialLightState.value) }
    var touchPosition by remember { mutableStateOf(config.initialTouchPosition) }
    var isTouching by remember { mutableStateOf(false) }
    val bulbCenterX = remember { config.bulbCenterX }
    val endPoint = remember { mutableStateOf(Offset(bulbCenterX, config.initialYOffset)) }
    val waveAmplitude = remember { Animatable(0f) }
    val yOffset = remember { Animatable(config.initialYOffset) }
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(isTouching) {
        if (!isTouching) {
            val waveSequence = config.waveSequence
            val lengthSequence = config.lengthSequence


            coroutineScope.launch {
                val lengthAnimations = launch {
                    lengthSequence.forEach { length ->
                        yOffset.animateTo(
                            targetValue = length,
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    }
                }
                val waveAnimations = launch {
                    waveSequence.forEach { (amplitude, direction) ->
                        waveAmplitude.animateTo(
                            targetValue = amplitude * direction,  // Positive for right, negative for left
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                        waveAmplitude.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 100, easing = LinearEasing)
                        )
                    }
                }
                lengthAnimations.join()
                waveAnimations.join()
                listener.onEndRelease()
                waveAmplitude.snapTo(0f)
            }
        }
    }

    MaterialTheme
    Box {
        Column(
            modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colors.background),
            horizontalAlignment = Alignment.End
        ) {
            Spacer(Modifier.height(20.dp))
            Image(
                painterResource(if (bulbState.value) Res.drawable.bulb_switcher_on else Res.drawable.bulb_switcher_off),
                contentDescription = null,
                modifier = Modifier.graphicsLayer {
                    rotationZ = 180f
                    translationX = -30f
                    translationY = 4f
                }.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    bulbState.value = !bulbState.value
                    listener.onClickListener()
                }
            )
            Canvas(modifier = Modifier.weight(1f).size(width = 100.dp, height = 50.dp)
                .pointerInput(Unit) {
                    forEachGesture {
                        awaitPointerEventScope {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            if (abs(down.position.x - endPoint.value.x) <= config.touchThreshold && abs(
                                    down.position.y - endPoint.value.y
                                ) <= config.touchThreshold
                            ) {
                                touchPosition = down.position
                                isTouching = true
                                do {
                                    val event = awaitPointerEvent()
                                    touchPosition = event.changes.first().position
                                } while (event.changes.any { it.pressed })
                                listener.onRelease(touchPosition)
                                bulbState.value = !bulbState.value
                                isTouching = false
                            }
                        }
                    }
                }) {
                val path = Path().apply {
                    moveTo(bulbCenterX, 0f)
                    if (isTouching) {
                        lineTo(touchPosition.x, touchPosition.y)
                    } else {
                        var x = bulbCenterX
                        var y = 0f
                        for (i in 0..yOffset.value.toInt() step 5) {
                            y = i.toFloat()
                            val phase = 2 * (yOffset.value - y) / yOffset.value * PI
                            val dx = waveAmplitude.value * sin(phase).toFloat()
                            lineTo(x + dx, y)
                        }
                    }
                }
                drawPath(
                    path = path,
                    color = config.lineColor,
                    style = Stroke(width = config.strokeWidth.dp.toPx())
                )
                drawCircle(
                    color = config.circleColor,
                    radius = config.circleRadius.dp.toPx(),
                    center = if (isTouching) touchPosition else Offset(bulbCenterX, yOffset.value)
                )
            }
        }
        Column {
            Box(
                modifier = Modifier.width(200.dp).height(90.dp),
            )
        }
    }
}
