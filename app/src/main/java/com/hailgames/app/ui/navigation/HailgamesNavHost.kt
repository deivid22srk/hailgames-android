package com.hailgames.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.hailgames.app.data.AuthRepository
import com.hailgames.app.data.ContentRepository
import com.hailgames.app.data.StorageRepository
import com.hailgames.app.ui.screens.AdminFormScreen
import com.hailgames.app.ui.screens.AdminManageScreen
import com.hailgames.app.ui.screens.AdminPanelScreen
import com.hailgames.app.ui.screens.AuthScreen
import com.hailgames.app.ui.screens.ContentDetailScreen
import com.hailgames.app.ui.screens.HomeScreen
import com.hailgames.app.ui.screens.MainScaffold
import com.hailgames.app.ui.screens.SessionViewModel
import com.hailgames.app.ui.screens.SettingsScreen
import com.hailgames.app.ui.screens.rememberSessionViewModel

object Routes {
    const val AUTH = "auth"
    const val MAIN = "main"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val DETAIL = "detail/{itemId}"
    const val ADMIN = "admin"
    const val ADMIN_FORM = "admin/form?itemId={itemId}"
    const val ADMIN_MANAGE = "admin/manage"

    fun detail(itemId: String) = "detail/$itemId"
    fun adminForm(itemId: String? = null) = "admin/form?itemId=${itemId ?: ""}"
}

object MainDestinations {
    const val HOME = "home"
    const val SETTINGS = "settings"
}

@Composable
fun HailgamesNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    sessionViewModel: SessionViewModel = viewModel(factory = rememberSessionViewModel()),
) {
    val sessionState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = if (sessionState.isAuthenticated) Routes.MAIN else Routes.AUTH,
        modifier = modifier
    ) {
        composable(Routes.AUTH) {
            AuthScreen(
                onSuccess = {
                    sessionViewModel.refreshProfile()
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScaffold(
                sessionViewModel = sessionViewModel,
                onNavigateDetail = { itemId -> navController.navigate(Routes.detail(itemId)) },
                onNavigateAdmin = { navController.navigate(Routes.ADMIN) }
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
        ) {
            val itemId = it.arguments?.getString("itemId").orEmpty()
            ContentDetailScreen(
                itemId = itemId,
                isAdmin = sessionState.isAdmin,
                onBack = { navController.popBackStack() },
                onEdit = { navController.navigate(Routes.adminForm(itemId)) },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.ADMIN) {
            AdminPanelScreen(
                isAdmin = sessionState.isAdmin,
                isOwner = sessionState.isOwner,
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(Routes.adminForm(null)) },
                onEdit = { itemId -> navController.navigate(Routes.adminForm(itemId)) },
                onManageAdmins = { navController.navigate(Routes.ADMIN_MANAGE) }
            )
        }

        composable(
            route = Routes.ADMIN_FORM,
            arguments = listOf(navArgument("itemId") { type = NavType.StringType; defaultValue = "" })
        ) {
            val itemId = it.arguments?.getString("itemId").orEmpty()
            AdminFormScreen(
                itemId = itemId.ifBlank { null },
                onDone = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN_MANAGE) {
            AdminManageScreen(
                isOwner = sessionState.isOwner,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
