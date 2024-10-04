package com.zaneschepke.wireguardautotunnel.ui.screens.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild


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
					tint = if(!isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface
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
					color = if(!isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface,
					style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
				)

			}
		}
	}
}
