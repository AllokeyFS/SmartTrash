package com.example.smarttrash.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.smarttrash.ui.screens.barcode.BarcodeScannerScreen
import com.example.smarttrash.ui.screens.detail.WasteDetailScreen
import com.example.smarttrash.ui.screens.home.HomeScreen
import com.example.smarttrash.ui.screens.map.MapScreen
import com.example.smarttrash.ui.screens.search.SearchScreen
import com.example.smarttrash.ui.screens.settings.SettingsScreen
import com.example.smarttrash.ui.viewmodel.MapViewModel

sealed class Screen(val route: String) {
    data object Home     : Screen("home")
    data object Search   : Screen("search")
    data object Barcode  : Screen("barcode")
    data object Settings : Screen("settings")

    data object Map : Screen("map?filter={filter}") {
        fun createRoute(filter: String? = null) =
            if (filter != null) "map?filter=$filter" else "map?filter="
    }

    data object Detail : Screen("detail/{itemId}") {
        fun createRoute(itemId: String) = "detail/$itemId"
    }
}

@Composable
fun SmartTrashNavGraph(navController: NavHostController) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        composable(Screen.Barcode.route) {
            BarcodeScannerScreen(navController = navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }

        composable(
            route     = Screen.Map.route,
            arguments = listOf(
                navArgument("filter") {
                    type         = NavType.StringType
                    defaultValue = ""
                    nullable     = true
                }
            )
        ) { backStackEntry ->
            val filter       = backStackEntry.arguments?.getString("filter")
            val mapViewModel : MapViewModel = hiltViewModel()
            if (!filter.isNullOrBlank()) {
                mapViewModel.onWasteTypeFilterSelected(filter)
            }
            MapScreen(viewModel = mapViewModel)
        }

        composable(
            route     = Screen.Detail.route,
            arguments = listOf(
                navArgument("itemId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val itemId = backStackEntry.arguments
                ?.getString("itemId") ?: return@composable
            WasteDetailScreen(
                itemId        = itemId,
                onBackPressed = { navController.popBackStack() },
                navController = navController
            )
        }
    }
}