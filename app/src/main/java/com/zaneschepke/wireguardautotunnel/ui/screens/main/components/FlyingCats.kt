package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.random.Random
@Composable
fun FlyingCats(catImages: List<Painter>) {
	// Получаем размер экрана текущего устройства
	val configuration = LocalConfiguration.current
	val screenWidth = configuration.screenWidthDp.toFloat()
	val screenHeight = configuration.screenHeightDp.toFloat()

	// Список для отслеживания активных котов
	val activeCats = remember { mutableStateListOf<Int>() }

	catImages.forEachIndexed { index, catImage ->
		// Размер кота
		val catSize = (120).dp

		// Создаем Animatable для позиции, вращения и прозрачности
		val offsetX = remember { Animatable(0f) }
		val offsetY = remember { Animatable(0f) }
		val rotation = remember { Animatable(0f) }
		val alpha = remember { Animatable(0f) }

		// Определяем направление вращения (-1 для против часовой стрелки, 1 для по часовой)
		val rotationDirection = if (Random.nextBoolean()) 1 else -1

		LaunchedEffect(Unit) {
			while (true) {
				// Случайная задержка для появления нового кота
				val delayMillis = Random.nextLong(0, 20000)
				kotlinx.coroutines.delay(delayMillis)

				// Определяем начальные и конечные позиции кота
				val startX: Float
				val startY: Float
				val targetX: Float
				val targetY: Float

				// Определение направления движения
				when (Random.nextInt(4)) {
					0 -> { // Летит слева направо
						startX = -200f // Начинаем за границей экрана
						startY = Random.nextFloat() * screenHeight
						targetX = screenWidth + 200f // Летим за противоположную границу
						targetY = Random.nextFloat() * screenHeight
					}
					1 -> { // Летит справа налево
						startX = screenWidth + 200f
						startY = Random.nextFloat() * screenHeight
						targetX = -200f
						targetY = Random.nextFloat() * screenHeight
					}
					2 -> { // Летит сверху вниз
						startX = Random.nextFloat() * screenWidth
						startY = -200f
						targetX = Random.nextFloat() * screenWidth
						targetY = screenHeight + 200f
					}
					else -> { // Летит снизу вверх
						startX = Random.nextFloat() * screenWidth
						startY = screenHeight + 200f
						targetX = Random.nextFloat() * screenWidth
						targetY = -200f
					}
				}

				// Добавляем кота в активные
				activeCats.add(index)

				// Устанавливаем начальные значения
				offsetX.snapTo(startX)
				offsetY.snapTo(startY)
				rotation.snapTo(0f)
				alpha.snapTo(0f)

				// Появление кота перед полетом
				alpha.animateTo(
					targetValue = 1f, // Появление кота
					animationSpec = tween(durationMillis = 500)
				)
				val offY = Random.nextInt(10000, 45000)
				val offX = Random.nextInt(10000, 45000)

				// Параллельная анимация по X, Y и вращения
				launch {
					offsetX.animateTo(
						targetValue = targetX,
						animationSpec = tween(
							durationMillis = offX, // Длительность от 10 до 15 секунд
							easing = LinearEasing
						)
					)
				}

				launch {
					offsetY.animateTo(
						targetValue = targetY,
						animationSpec = tween(
							durationMillis = offY, // Длительность от 10 до 15 секунд
							easing = LinearEasing
						)
					)
				}

				// Вращение с разным направлением
				launch {
					rotation.animateTo(
						targetValue = 360f * rotationDirection,
						animationSpec = tween(
							durationMillis = max(offX, offY), // Вращение за весь полет
							easing = LinearEasing
						)
					)
				}

				// Ждем, пока кот закончит полет
				kotlinx.coroutines.delay(max(offX, offY).toLong())

				// Скрываем кота после завершения полета и удаляем из активных
				alpha.animateTo(targetValue = 0f, animationSpec = tween(durationMillis = 500))
				activeCats.remove(index) // Убираем кота из активных после завершения полета
			}
		}

		// Отрисовываем кота только если он в списке активных
		if (activeCats.contains(index)) {
			Image(
				painter = catImage,
				contentDescription = "Flying Cat",
				modifier = Modifier
					.offset(x = offsetX.value.dp, y = offsetY.value.dp)
					.size(catSize) // Фиксированный размер кота
					.graphicsLayer(
						rotationZ = rotation.value, // Вращение кота
						alpha = alpha.value // Контроль прозрачности
					)
			)
		}
	}
}




