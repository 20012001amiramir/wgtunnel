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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.math.cos
import kotlin.math.sin

val DarkColorPalette = darkColorScheme(
	primary = Color(0xFF6A1B9A),
	onPrimaryContainer = Color(0xFFB12E61),
	onTertiary = Color(0xFF571E4D),
	onTertiaryContainer = Color(0xFFA72E4D),
	onPrimary = Color.White,
	background = Color(0xFF0D0D0D),
	surface = Color(0xFF121212),
	onBackground = Color.White,
	onSurface = Color.White
)

var darkTheme: Boolean = false

val LightColorPalette = lightColorScheme(
	primary = Color(0xFF6A1B9A),
	onPrimaryContainer = Color(0xFFB12E61),
	onTertiary = Color(0xFF571E4D),
	onTertiaryContainer = Color(0xFFA72E4D),
	onPrimary = Color.Black,
	background = Color(0xFFF2F2F2),
	surface = Color(0xFFFFFFFF),
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

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun VPNApp() {
	// haze
	val hazeState = remember { HazeState() }

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
						.fillMaxSize()
				) {

					TopBar(isDarkTheme) {
						isDarkTheme = !isDarkTheme
						darkTheme = isDarkTheme
					}


					StatusBlock(hazeState, isDarkTheme)

					CentralArea()
					LocationCards(hazeState)
				}

			}

		}
	}
}

@Composable
fun StatusBlock(hazeState: HazeState, isDarkTheme: Boolean) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		horizontalArrangement = Arrangement.SpaceEvenly
	) {
		// Передаем hazeState иконкам для размытия фона
		StatusCard(Icons.Default.Download, "16.7 Mbps", "Download", hazeState)
		StatusCard(Icons.Default.Upload, "24.2 Mbps", "Upload", hazeState)
		StatusCard(Icons.Default.SignalCellularAlt, "16 ms", "Ping", hazeState)
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
						.background(
							color = Color.Gray.copy(alpha = 0.7f),
							shape = CircleShape
						)
						.padding(6.dp)
				) {
					Icon(
						imageVector = icon,
						contentDescription = label,
						tint = Color.White,
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
						color = Color.White,
						style = MaterialTheme.typography.bodyLarge.copy(
							fontWeight = FontWeight.Bold,
							fontSize = 16.sp
						)
					)
					// Метка - меньшим размером и с приглушенным цветом
					Text(
						text = label,
						color = Color.White,
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
		GradientBackground(darkTheme)

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

@Composable
fun GradientBackground(darkTheme: Boolean, modifier: Modifier = Modifier) {
	val colors = if (darkTheme) {
		listOf(Color(0xFF0D0D0D), Color(0xFF4A148C))
	} else {
		listOf(Color(0xFFF2F2F2), Color(0xFF6A1B9A))
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(
				brush = Brush.verticalGradient(colors = colors)
			)
	)
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
fun CentralArea() {
	Column(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		BasicText(
			text = "12:32:06",
			style = TextStyle(
				color = MaterialTheme.colorScheme.onBackground,
				fontSize = 48.sp,
				fontWeight = FontWeight.Bold
			)
		)
		Spacer(modifier = Modifier.height(24.dp))
		PowerButton()
	}
}

@Composable
fun PowerButton() {
	Box(
		modifier = Modifier
			.size(100.dp)
			.background(Color.Transparent)
			.border(8.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
			.padding(16.dp),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = "Connect",
			color = MaterialTheme.colorScheme.onPrimary,
			modifier = Modifier
				.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
				.padding(16.dp)
		)
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
				MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
				MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
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
				// Flag Icon
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.background(
							shape = RoundedCornerShape(40),
							color = Color.Transparent
						),
					contentAlignment = Alignment.TopStart
				) {
					Row {
						Text(text = flag, fontSize = 48.sp)
						Icon(
							imageVector = Icons.Default.SignalCellularAlt,
							contentDescription = "Toggle Theme",
							tint = MaterialTheme.colorScheme.onBackground
						)
					}

				}

				// Location Label
				Text(
					text = "Location",
					color = Color.White.copy(alpha = 0.7f),
					style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
					modifier = Modifier.padding(top = 16.dp)
				)

				// Location Name
				Text(
					text = location,
					color = Color.White,
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
				)

				// Connect Button
				Box(
					modifier = Modifier
						.clip(RoundedCornerShape(8.dp))
						.background(Color.White.copy(alpha = 0.3f))
						.padding(vertical = 4.dp, horizontal = 16.dp),
					contentAlignment = Alignment.Center
				) {
					Text(text = "Connect", color = Color.White)
				}
			}
		}
	}
}
@Preview(showBackground = true)
@Composable
fun PreviewVPNApp() {
	VPNApp()
}
