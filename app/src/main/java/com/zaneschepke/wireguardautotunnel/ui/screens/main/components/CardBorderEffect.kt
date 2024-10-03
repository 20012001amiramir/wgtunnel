package com.zaneschepke.wireguardautotunnel.ui.screens.main.components


import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.R
import com.zaneschepke.wireguardautotunnel.ui.screens.main.GradientBackground
import com.zaneschepke.wireguardautotunnel.ui.screens.main.rotate
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun GlassmorphicCard(
	modifier: Modifier = Modifier,
	hazeState: HazeState = remember { HazeState() },
	springScale: Float = 1.05f,
	content: @Composable BoxScope.() -> Unit
) {
	// Анимация границы
	val infiniteTransition = rememberInfiniteTransition(label = "BorderAnimation")
	val angle by infiniteTransition.animateFloat(
		initialValue = 0f,
		targetValue = 360f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 2500, easing = LinearEasing),
			repeatMode = RepeatMode.Restart
		), label = "Border Animation"
	)

	// Градиент для границы карточки
	val density = LocalDensity.current
	val boxSize = 200.dp
	val boxSizePx = with(density) { boxSize.toPx() }
	val centerOffset = Offset(boxSizePx / 2, boxSizePx / 2)
	val gradientBrush = Brush.linearGradient(
		colors = listOf(Color(0xFF7790ff), Color(0xFFff787a), Color(0xFF1acd9a), Color(0xFFff8fc0)),
		start = Offset(0f, 0f).rotate(angle, centerOffset),
		end = Offset(boxSizePx, boxSizePx).rotate(angle, centerOffset)
	)

	Box(
		modifier = modifier
			.haze(hazeState) // Включаем эффект haze
			.clip(RoundedCornerShape(50.dp))
			.border(
				width = 4.dp,
				brush = gradientBrush,
				shape = RoundedCornerShape(16.dp)
			)
			.background(Color.Transparent)
			.scale(springScale)
			.padding(8.dp)
			.size(width = 340.dp, height = 500.dp)
	) {
		Box(
			modifier = Modifier
				.hazeChild(
					state = hazeState,
					shape = RoundedCornerShape(50.dp),
					style = HazeMaterials.ultraThin()
				)
		) {
			content() // Вставляем переданный контент в эффект haze
		}
	}
}
