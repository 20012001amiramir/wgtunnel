package com.zaneschepke.wireguardautotunnel.ui.screens.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.FlyingCats
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin

val DarkColorPalette = darkColorScheme(
	primary = Color(0xFF509A1B),
	onPrimaryContainer = Color(0xFF2EB16D),
	onTertiary = Color(0xFF571E4D),
	onTertiaryContainer = Color(0xFF2EB16D),
	onPrimary = Color.White,
	background = Color(0xFFFBF2E3),
	surface = Color(0xFF2B2D30),
	onBackground = Color.White,
	onSurface = Color.White
)

var darkTheme: Boolean = false

val LightColorPalette = lightColorScheme(
	primary = Color(0xFF509A1B),
	onPrimaryContainer = Color(0xFF2EB16D),
	onTertiary = Color(0xFF571E4D),
	onTertiaryContainer = Color(0xFF2EB16D),
	onPrimary = Color.Black,
	background = Color(0xFF2B2D30),
	surface = Color(0xFFFBF2E3),
	onBackground = Color.Black,
	onSurface = Color.Black
)



@Composable
fun MyTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
	val colors = if (darkTheme) DarkColorPalette else LightColorPalette

	MaterialTheme(
		colorScheme = colors,
		typography = Typography(),
		content = content
	)
}

@Composable
fun VPNApp() {
	// haze
	val hazeState = remember { HazeState() }
	var isConnecting by rememberSaveable { mutableStateOf(false) }

	// Управляем состоянием темы с помощью rememberSaveable
	var isDarkTheme by rememberSaveable { mutableStateOf(false) }
	darkTheme = isDarkTheme

	MyTheme(darkTheme = isDarkTheme) {
		Box(
			modifier = Modifier.fillMaxSize()
		) {
			// Добавляем размытие фона
			Box(
				modifier = Modifier
					.fillMaxSize()
			) {
				// Все содержимое, которое должно размываться, переносим сюда
				BlurryCardContent(hazeState, darkTheme)


				// Остальные элементы (которые не должны размываться)
				Column(
					modifier = Modifier
						.padding(0.dp, 32.dp)
						.fillMaxSize()
				) {

					TopBar(isDarkTheme) {
						isDarkTheme = !isDarkTheme
						darkTheme = isDarkTheme
					}


					StatusBlock(hazeState, isConnecting)

					CentralArea(hazeState, isConnecting) { isConnecting = it }
					LocationCards(hazeState)
				}

			}

		}
	}
}

@Composable
fun StatusBlock(hazeState: HazeState, isConnecting: Boolean) {
	// Мокированные данные для отображения, замените их на реальное получение данных
	val downloadSpeed = remember { mutableStateOf("0 Mbps") }
	val uploadSpeed = remember { mutableStateOf("0 Mbps") }
	val ping = remember { mutableStateOf("0 ms") }

	// Обновление данных в реальном времени только когда подключение активно
	LaunchedEffect(isConnecting) {
		while (isConnecting) {
			// Здесь добавьте получение реальных данных скорости загрузки, отправки и пинга
			kotlinx.coroutines.delay(1000L) // Обновляем каждую секунду

			// Мокированные значения для демонстрации
			downloadSpeed.value = "${(5..100).random()} Mbps"
			uploadSpeed.value = "${(2..50).random()} Mbps"
			ping.value = "${(10..100).random()} ms"
		}
	}

	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		horizontalArrangement = Arrangement.SpaceEvenly
	) {
		// Передаем hazeState иконкам для размытия фона
		StatusCard(Icons.Default.Download, downloadSpeed.value, "Download", hazeState)
		StatusCard(Icons.Default.Upload, uploadSpeed.value, "Upload", hazeState)
		StatusCard(Icons.Default.SignalCellularAlt, ping.value, "Ping", hazeState)
	}
}

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun StatusCard(icon: ImageVector, value: String, label: String, hazeState: HazeState) {


	val infiniteTransition = rememberInfiniteTransition(label = "SampleTransitionEffect")
	val angle by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 360f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 2500, easing = LinearEasing),
			repeatMode = RepeatMode.Restart
		), label = "Sample Border Animation"
	)

	val density = LocalDensity.current
	val boxSize = 200.dp
	val boxSizePx = with(density) { boxSize.toPx() }
	val centerOffset = Offset(boxSizePx / 2, boxSizePx / 2)

	val gradientBrush = Brush.linearGradient(
		colors = listOf(
			Color.White,
			Color.White,
			Color.White,
			Color.White
		),
		start = Offset(0f, 0f).rotate(angle, centerOffset),
		end = Offset(boxSizePx, boxSizePx).rotate(angle, centerOffset)
	)

	// Градиент от серого к белому
	val backgroundGradient = Brush.linearGradient(
		colors = listOf(
			Color.White.copy(alpha = 0.3f),
			Color.Gray.copy(alpha = 0.5f)
		)
	)

	Box(
		modifier = Modifier
			.size(120.dp, 120.dp)
	) {
		// Добавляем эффект размытия только для фона
		Box(
			modifier = Modifier
				.fillMaxSize()
				.haze(hazeState)
				.border(
					width = 2.dp,
					brush = backgroundGradient,
					shape = RoundedCornerShape(16.dp)
				)
				.background(
					brush = backgroundGradient,
					shape = RoundedCornerShape(16.dp)
				)
				.size(width = 140.dp, height = 200.dp)
		)

		// Поверх размытого фона размещаем текст и иконку
		Card(
			modifier = Modifier
				.fillMaxSize()
				.hazeChild(
					state = hazeState,
					style = HazeStyle(
						blurRadius = 4.dp
					),
					shape = RoundedCornerShape(16.dp)
				),
			shape = RoundedCornerShape(16.dp),
			colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.3f)),
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp)
			) {
				// Круг вокруг иконки
				Box(
					modifier = Modifier
						.align(Alignment.TopEnd)
						.size(30.dp)
				) {
					Icon(
						imageVector = icon,
						contentDescription = label,
						tint = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
						modifier = Modifier.fillMaxSize()
					)
				}
				// Текст поверх размытого фона
				Column(
					modifier = Modifier
						.fillMaxSize()
						.padding(top = 8.dp),
					horizontalAlignment = Alignment.Start,
					verticalArrangement = Arrangement.Bottom
				) {
					// Значение - жирным шрифтом
					Text(
						text = value,
						color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
						style = MaterialTheme.typography.bodyLarge.copy(
							fontWeight = FontWeight.Bold,
							fontSize = 16.sp
						)
					)
					// Метка - меньшим размером и с приглушенным цветом
					Text(
						text = label,
						color = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
						style = MaterialTheme.typography.bodySmall
					)
				}
			}
		}
	}
}




// Функция, которая включает в себя размываемые объекты
@Composable
fun BlurryCardContent(hazeState: HazeState, darkTheme: Boolean) {
	// Фон и коты
	Box(
		modifier = Modifier
			.fillMaxSize()
			.haze(hazeState)
	) {

		Image(
			painter = painterResource(id = if (darkTheme) R.drawable.b_space else R.drawable.l_space),
			contentDescription = "Background Space",
			modifier = Modifier
				.fillMaxWidth()
				.align(Alignment.BottomCenter),
			contentScale = ContentScale.Crop
		)

		// Летающие коты
		val catImages = listOf(
			painterResource(id = R.drawable.cat_1),
			painterResource(id = R.drawable.cat_2),
			painterResource(id = R.drawable.cat_9),
			painterResource(id = R.drawable.cat_10),
			painterResource(id = R.drawable.cat_11),
			painterResource(id = R.drawable.cat_12),
			painterResource(id = R.drawable.cat_13),
			painterResource(id = R.drawable.cat_16),
			painterResource(id = R.drawable.cat_17),
			painterResource(id = R.drawable.cat_18),
			painterResource(id = R.drawable.cat_19),
		)

		FlyingCats(catImages)
	}
}


@Composable
fun TopBar(isDarkTheme: Boolean, onThemeToggle: () -> Unit) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		// Иконка настроек
		Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)

		// Текст "Protected"
		Text(
			text = "Protected",
			style = TextStyle(
				color = MaterialTheme.colorScheme.onBackground,
				fontSize = 20.sp,
				fontWeight = FontWeight.Bold
			),
			textAlign = TextAlign.Center
		)

		// Кнопка переключения темы с использованием иконки
		IconButton(onClick = onThemeToggle) {
			Icon(
				imageVector = if (isDarkTheme) Icons.Filled.WbSunny else Icons.Filled.Brightness2, // Солнце или луна
				contentDescription = "Toggle Theme",
				tint = MaterialTheme.colorScheme.onBackground
			)
		}
	}
}

fun Offset.rotate(degrees: Float, pivot: Offset): Offset {
	val radians = Math.toRadians(degrees.toDouble())
	val cos = cos(radians)
	val sin = sin(radians)
	return Offset(
		x = (cos * (x - pivot.x) - sin * (y - pivot.y) + pivot.x).toFloat(),
		y = (sin * (x - pivot.x) + cos * (y - pivot.y) + pivot.y).toFloat()
	)
}

@Composable
fun CentralArea(hazeState: HazeState, isConnecting: Boolean, onToggleConnecting: (Boolean) -> Unit) {
	// Стейт для отслеживания времени в миллисекундах
	var elapsedTime by remember { mutableLongStateOf(0L) }
	var startTime by remember { mutableStateOf(0L) }

	// Запуск таймера при подключении
	LaunchedEffect(isConnecting) {
		if (isConnecting) {
			startTime = System.currentTimeMillis()
			while (isActive) {
				// Рассчитываем разницу во времени с момента запуска таймера
				elapsedTime = System.currentTimeMillis() - startTime
				kotlinx.coroutines.delay(16L) // Обновляем примерно каждые 16 мс (60 FPS)
			}
		} else {
			elapsedTime = 0L
		}
	}

	// Форматируем отображаемое время
	val displayTime = if (isConnecting) {
		val minutes = (elapsedTime / 60000).toString().padStart(2, '0')
		val seconds = ((elapsedTime / 1000) % 60).toString().padStart(2, '0')
		val millis = ((elapsedTime % 1000) / 10).toString().padStart(2, '0')
		"$minutes:$seconds:$millis"
	} else {
		"00:00:00"
	}

	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp)
			.background(
				color = Color.Transparent,
				shape = RoundedCornerShape(16.dp)
			)
			.padding(32.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		// Отображение времени с соответствующим стилем
		Text(
			text = displayTime,
			style = TextStyle(
				color = MaterialTheme.colorScheme.background,
				fontSize = 48.sp,
				fontWeight = FontWeight.Bold,
				textAlign = TextAlign.Center
			),
			modifier = Modifier.padding(bottom = 16.dp)
		)

		// Кнопка подключения
		ConnectButton(hazeState, isConnecting) {
			onToggleConnecting(it)
		}
	}
}

@Composable
fun ConnectButton(hazeState: HazeState, isConnecting: Boolean, onToggle: (Boolean) -> Unit) {
	val buttonGradient = Brush.linearGradient(
		colors = if (isConnecting) {
			listOf(
				MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
				MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
			)
		} else {
			listOf(
				MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
				MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
			)
		}
	)

	Box(
		contentAlignment = Alignment.Center,
		modifier = Modifier
			.size(150.dp)
			.clip(RoundedCornerShape(50)) // Circular shape
			.background(buttonGradient)
			.hazeChild(
				state = hazeState,
				style = HazeStyle(
					blurRadius = 4.dp
				),
				shape = RoundedCornerShape(50) // Circular shape
			)
			.clickable {
				onToggle(!isConnecting) // Toggle connection state
			}
	) {
		// Background circle with white border when connected
		if (isConnecting) {
			Icon(
				painter = painterResource(id = R.drawable.ic_square), // Your custom square icon
				contentDescription = "Disconnect",
				tint = MaterialTheme.colorScheme.surface,
				modifier = Modifier.size(60.dp) // Icon size
			)
		} else {
			// Icon for "Connect"
			Icon(
				painter = painterResource(id = R.drawable.ic_on_button), // Your custom circle icon
				contentDescription = "Connect",
				tint = MaterialTheme.colorScheme.surface,
				modifier = Modifier.size(60.dp) // Icon size
			)
		}
	}
}


@Composable
fun LocationCards(hazeState: HazeState) {
	val locations = listOf(
		"Auto" to "",
		"Australia" to "🇦🇺",
		"China" to "🇨🇳",
		"USA" to "🇺🇸"
	)

	val pagerState = rememberPagerState { locations.size }

	Box(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 32.dp)
	) {
		// Horizontal pager with the padding for the centering effect
		HorizontalPager(
			state = pagerState,
			contentPadding = PaddingValues(horizontal = 128.dp),
			modifier = Modifier
				.fillMaxWidth()
				.align(Alignment.BottomCenter)
		) { page ->
			// Get the location and flag for each page
			val (location, flag) = locations[page]

			// Check if the current page is the centered one
			val isSelected = pagerState.currentPage == page

			LocationCard(location, flag, hazeState, isSelected)
		}
	}
}

@Composable
fun LocationCard(location: String, flag: String, hazeState: HazeState, isSelected: Boolean) {
	val cardSize = if (isSelected) 220.dp else 200.dp

	val backgroundGradient = Brush.linearGradient(
		colors = if (!isSelected) {
			listOf(
				Color.White.copy(alpha = 0.3f),
				Color.Gray.copy(alpha = 0.5f)
			)
		}
		else {
			listOf(
				MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
				MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
			)
		}
	)

	Box(
		modifier = Modifier
			.size(cardSize)
			.padding(4.dp)
	) {
		// Blurred background effect for the card
		Box(
			modifier = Modifier
				.fillMaxSize()
				.haze(hazeState)
				.background(
					brush = backgroundGradient,
					shape = RoundedCornerShape(16.dp)
				)
				.clip(RoundedCornerShape(16.dp))
		)

		// Card with the flag and location
		Card(
			modifier = Modifier
				.fillMaxSize()
				.hazeChild(
					state = hazeState,
					style = HazeStyle(
						blurRadius = 4.dp
					),
					shape = RoundedCornerShape(16.dp)
				),
			shape = RoundedCornerShape(16.dp),

			colors = CardDefaults.cardColors(
				containerColor = Color.Transparent
			)
		) {
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(16.dp),
				verticalArrangement = Arrangement.SpaceBetween,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Icon(
					modifier = Modifier
						.fillMaxWidth()
						.align(Alignment.Start)
					,
					imageVector = Icons.Default.SignalCellularAlt,
					contentDescription = "Toggle Theme",
					tint = Color.White.copy(alpha = 0.5f)
				)

				Text(
					modifier = Modifier
						.align(Alignment.CenterHorizontally)
					,
					text = flag.ifEmpty { "\uD83C\uDFF3\uFE0F" },
					fontSize = 48.sp
				)


				// Location Name
				Text(
					modifier = Modifier
						.padding(0.dp,0.dp,0.dp,20.dp),
					text = location,
					fontSize = 24.sp,
					color = Color.White,
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
				)

			}
		}
	}
}
@Preview(showBackground = true)
@Composable
fun PreviewVPNApp() {
	VPNApp()
}
