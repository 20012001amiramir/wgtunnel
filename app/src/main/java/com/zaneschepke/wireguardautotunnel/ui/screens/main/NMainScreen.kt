package com.zaneschepke.wireguardautotunnel.ui.screens.main

import android.content.Context
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
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.ui.AppUiState
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.FlyingCats
import com.zaneschepke.wireguardautotunnel.ui.screens.main.components.LocationCards
import com.zaneschepke.wireguardautotunnel.ui.screens.settings.SettingsViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.JsonNull.content
import kotlin.math.cos
import kotlin.math.sin

val DarkColorPalette = darkColorScheme(
	primary = Color(0xFF4D456B),
	onPrimaryContainer = Color(0xFF9CEBA0),
	onTertiary = Color(0xFFA18BE8),
	onTertiaryContainer = Color(0xFF6E9970),
	onPrimary = Color.White,
	background = Color(0xFFEBC09B),
	surface = Color(0xFFEBC09B),
	onBackground = Color.White,
	onSurface = Color.White
)

var darkTheme: Boolean = false

val LightColorPalette = lightColorScheme(
	primary = Color(0xFF4D456B),
	onPrimaryContainer = Color(0xFF9CEBA0),
	onTertiary = Color(0xFFA18BE8),
	onTertiaryContainer = Color(0xFF6E9970),
	onPrimary = Color.Black,
	background = Color(0xFF6B5645),
	surface = Color(0xFFEBC09B),
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
fun VPNApp(
	viewModel: SettingsViewModel = hiltViewModel(),
) {
	// haze
	val hazeState = remember { HazeState() }
	var isConnecting by rememberSaveable { mutableStateOf(false) }
	val context = LocalContext.current
	val isDarkTheme by viewModel.getDarkThemePreference(context).collectAsState(
		initial = context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES
	)
	//TODO: Исправить мигание светлой черной темы, можно когда в будущем добавим лоадинг в самом начале
	darkTheme = isDarkTheme

	LaunchedEffect(Unit) {
		val savedTheme = viewModel.getDarkThemePreference(context).first()
		darkTheme = savedTheme
	}


	MyTheme(darkTheme = darkTheme) {
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

					TopBar(darkTheme) {
						viewModel.saveDarkThemePreference(context, !isDarkTheme)
						darkTheme = isDarkTheme
					}

					StatusBlock(hazeState, isConnecting)

					CentralArea(hazeState, isConnecting) { isConnecting = it }
//					LocationCards(hazeState)
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
			downloadSpeed.value = "${(5..1000).random()} Mbps"
			uploadSpeed.value = "${(2..500).random()} Mbps"
			ping.value = "${(10..1000).random()} ms"
		}
	}

	Row(
		modifier = Modifier
			.padding(25.dp, 10.dp)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween
	) {
		// Передаем hazeState иконкам для размытия фона
		StatusCard(130.dp, 130.dp, Icons.Default.Download, downloadSpeed.value, "Download", hazeState)
		StatusCard(130.dp, 130.dp, Icons.Default.Upload, uploadSpeed.value, "Upload", hazeState)
		StatusCard(130.dp, 130.dp, Icons.Default.SignalCellularAlt, ping.value, "Ping", hazeState)
	}
}

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
		Icon(
			Icons.Filled.Settings,
			contentDescription = "Settings",
			tint = MaterialTheme.colorScheme.background)

		// Текст "Protected"
		Text(
			text = "Protected",
			color = MaterialTheme.colorScheme.background,
			style = MaterialTheme.typography.bodyLarge.copy(
				fontWeight = FontWeight.SemiBold,
				fontSize = 20.sp
			),
			textAlign = TextAlign.Center
		)

		// Кнопка переключения темы с использованием иконки
		IconButton(onClick = onThemeToggle) {
			Icon(
				imageVector = if (isDarkTheme) Icons.Filled.WbSunny else Icons.Filled.Brightness2, // Солнце или луна
				contentDescription = "Toggle Theme",
				tint = MaterialTheme.colorScheme.background
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
	Row(
		modifier = Modifier
			.padding(25.dp, 0.dp)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.Top
	) {
		// Таймер как отдельная карточка

		Column (
			modifier = Modifier
				.height(200.dp)
				.wrapContentWidth(),
			verticalArrangement = Arrangement.SpaceBetween,
			horizontalAlignment = Alignment.Start
		){
			CentralCard(
				height = 95.dp,
				width = 200.dp,
				content = { TimerContent(isConnecting) },
				hazeState = hazeState,
				modifier = Modifier.weight(1f)
			)
			CentralCard(
				height = 95.dp,
				width = 200.dp,
				content = { TimerContent(isConnecting) },
				hazeState = hazeState,
				modifier = Modifier.weight(1f)
			)
		}

		// Кнопка подключения как отдельная карточка
		CentralCard(
			height = 200.dp,
			width = 200.dp,
			content = { ConnectButton(hazeState, isConnecting, onToggleConnecting) },
			hazeState = hazeState,
			modifier = Modifier.weight(1f) // Равномерное распределение пространства
		)
	}
}

@Composable
fun StatusCard(height: Dp,width: Dp,icon: ImageVector, value: String, label: String, hazeState: HazeState) {

	// Градиент от серого к белому
	val backgroundGradient = Brush.linearGradient(
		colors = listOf(
			Color.White.copy(alpha = 0.3f),
			Color.Gray.copy(alpha = 0.5f)
		)
	)

	Box(
		modifier = Modifier
			.width(width)
			.height(height)
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
						.size(32.dp)
				) {
					Icon(
						imageVector = icon,
						contentDescription = label,
						tint = MaterialTheme.colorScheme.background,
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
						color = MaterialTheme.colorScheme.background,
						style = MaterialTheme.typography.bodyLarge.copy(
							fontWeight = FontWeight.Bold,
							fontSize = 24.sp
						)
					)
					// Метка - меньшим размером и с приглушенным цветом
					Text(
						text = label,
						color = MaterialTheme.colorScheme.background,
						style = MaterialTheme.typography.bodyLarge.copy(
							fontWeight = FontWeight.W400,
							fontSize = 16.sp
						)
					)
				}
			}
		}
	}
}


@Composable
fun CentralCard(height : Dp, width: Dp, content: @Composable () -> Unit, hazeState: HazeState, modifier: Modifier = Modifier) {
	val backgroundGradient = Brush.linearGradient(
		colors = listOf(
			Color.White.copy(alpha = 0.3f),
			Color.Gray.copy(alpha = 0.5f)
		)
	)

	Box(
		modifier = Modifier
			.width(width)
			.height(height)
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
		)
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
			Column(
				modifier = Modifier
					.fillMaxSize()
					.padding(12.dp),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				// Контент карточки
				content()
			}
		}
	}
}


@Composable
fun TimerContent(isConnecting: Boolean) {
	var elapsedTime by remember { mutableLongStateOf(0L) }
	var startTime by remember { mutableStateOf(0L) }

	// Запуск таймера при подключении
	LaunchedEffect(isConnecting) {
		if (isConnecting) {
			startTime = System.currentTimeMillis()
			while (isActive) {
				elapsedTime = System.currentTimeMillis() - startTime
				kotlinx.coroutines.delay(16L)
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

	// Отображение таймера
	Text(
		text = displayTime,
		style = TextStyle(
			color = MaterialTheme.colorScheme.background,
			fontSize = 32.sp,
			fontWeight = FontWeight.Bold,
			textAlign = TextAlign.Center
		)
	)
}


@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ConnectButton(hazeState: HazeState, isConnecting: Boolean, onToggle: (Boolean) -> Unit) {

	val infiniteTransition = rememberInfiniteTransition(label = "BorderAnimation")
	val angle by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = if(isConnecting)360f else 0f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 25000, easing = LinearEasing),
			repeatMode = RepeatMode.Restart
		), label = "Border Animation"
	)

	// Градиент для границы карточки
	val density = LocalDensity.current
	val boxSize = 140.dp
	val boxSizePx = with(density) { boxSize.toPx() }
	val centerOffset = Offset(boxSizePx / 2, boxSizePx / 2)
	val gradientBrush = Brush.linearGradient(
		colors = listOf(
			Color(0xFF355CFF).copy(alpha = 0.9f),
			Color(0xFFFF3639).copy(alpha = 0.9f),
			Color(0xFF37FFC6).copy(alpha = 0.9f),
			Color(0xFFFF4E9A).copy(alpha = 0.9f)),
		start = Offset(0f, 0f).rotate(angle, centerOffset),
		end = Offset(boxSizePx, boxSizePx).rotate(angle, centerOffset)
	)

	Box(
		modifier = Modifier
			.size(140.dp)
			.clip(RoundedCornerShape(50))
	) {
		// Blurred background effect for the card
		Box(
			modifier = Modifier
				.fillMaxSize()
				.haze(hazeState)
				.background(
					brush = gradientBrush,
				)
				.size(140.dp)
				.clip(RoundedCornerShape(50))
		)

		Box(
			contentAlignment = Alignment.Center,
			modifier = Modifier
				.size(140.dp)
				.clip(RoundedCornerShape(50)) // Circular shape
				.hazeChild(
					state = hazeState,
					style = HazeMaterials.ultraThin(),
					shape = RoundedCornerShape(70.dp)
				)
				.clickable {
					onToggle(!isConnecting) // Toggle connection state
				}
		) {

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
}



