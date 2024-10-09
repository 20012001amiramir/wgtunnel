package com.zaneschepke.wireguardautotunnel.ui.common.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

@Composable
fun BottomNavBar(hazeState: HazeState, navController: NavController, bottomNavItems: List<BottomNavItem>) {
	var showBottomBar by rememberSaveable { mutableStateOf(true) }
	val navBackStackEntry by navController.currentBackStackEntryAsState()

	showBottomBar = bottomNavItems.firstOrNull {
		navBackStackEntry?.destination?.hierarchy?.any { dest ->
			bottomNavItems.map { dest.hasRoute(route = it.route::class) }.contains(true)
		} == true
	} != null
	// Градиент от серого к белому
	val backgroundGradient = Brush.linearGradient(
		colors = listOf(
			Color.White.copy(alpha = 0.3f),
			Color.Gray.copy(alpha = 0.5f),
		),
	)

	if (showBottomBar) {
		NavigationBar(
			modifier = Modifier
				.background(
					brush = backgroundGradient,
				)
				.hazeChild(
					state = hazeState,
					style = HazeStyle(
						blurRadius = 4.dp,
					),
					shape = RoundedCornerShape(16.dp),
				),
			containerColor = Color.Transparent,
		) {

			bottomNavItems.forEach { item ->
				val selected = navBackStackEntry.isCurrentRoute(item.route)

				NavigationBarItem(
					selected = selected,
					onClick = {
						if (selected) return@NavigationBarItem
						navController.navigate(item.route) {
							// Попап до стартовой точки графа
							popUpTo(navController.graph.findStartDestination().id) {
								saveState = true
							}
							launchSingleTop = true
						}
					},
					label = {
						Text(
							text = item.name,
							fontWeight = FontWeight.SemiBold,
							color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Цвет текста в зависимости от состояния
						)
					},
					icon = {
						Icon(
							imageVector = item.icon,
							contentDescription = "${item.name} Icon",
							tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Цвет иконки в зависимости от состояния
						)
					},
					colors = NavigationBarItemDefaults.colors(
						selectedIconColor = MaterialTheme.colorScheme.primary, // Цвет для активного состояния
						unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Цвет для неактивного состояния
						selectedTextColor = MaterialTheme.colorScheme.primary, // Цвет текста для активного состояния
						unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), // Цвет текста для неактивного состояния
						indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) // Цвет индикатора для выбранного элемента
					)
				)
			}
		}
	}
}

