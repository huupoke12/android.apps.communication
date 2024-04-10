package io.github.huupoke12.android.apps.communication.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import io.github.huupoke12.android.apps.communication.ui.screens.BlockListScreen
import io.github.huupoke12.android.apps.communication.ui.screens.CreateRoomScreen
import io.github.huupoke12.android.apps.communication.ui.screens.FilePreviewScreen
import io.github.huupoke12.android.apps.communication.ui.screens.ResetPasswordScreen
import io.github.huupoke12.android.apps.communication.ui.screens.RoomDetailScreen
import io.github.huupoke12.android.apps.communication.ui.screens.RoomScreen
import io.github.huupoke12.android.apps.communication.ui.screens.SignInScreen
import io.github.huupoke12.android.apps.communication.ui.screens.SignOutScreen
import io.github.huupoke12.android.apps.communication.ui.screens.SignUpScreen
import io.github.huupoke12.android.apps.communication.ui.screens.SplashScreen
import io.github.huupoke12.android.apps.communication.ui.screens.UserScreen
import io.github.huupoke12.android.apps.communication.ui.screens.home.HomeScreen
import java.net.URLEncoder

enum class AppScreen {
    SPLASH, FILE_PREVIEW, SIGN_IN, SIGN_OUT, SIGN_UP, RESET_PASSWORD,
    HOME, USER, ROOM, ROOM_CREATE, BLOCK_LIST
}

@Composable
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = AppScreen.SPLASH.name,
        modifier = modifier,
    ) {
        composable(AppScreen.SPLASH.name) {
            SplashScreen(
                onLoad = {
                    navController.navigate(AppScreen.SIGN_IN.name)
                },
            )
        }
        composable(
            "${AppScreen.FILE_PREVIEW.name}/{filePath}",
            listOf(navArgument("filePath") { type = NavType.StringType })
        ) {
            FilePreviewScreen(
                dismiss = navController::popBackStack,
            )
        }
        composable(AppScreen.SIGN_OUT.name) {
            SignOutScreen(
                navigateToSplash = { navController.popBackStack(AppScreen.SPLASH.name, false) }
            )
        }
        composable(AppScreen.SIGN_IN.name) {
            SignInScreen(
                navigateToSignUp = { navController.navigate(AppScreen.SIGN_UP.name) },
                navigateToResetPassword = { navController.navigate(AppScreen.RESET_PASSWORD.name) },
                onSignedIn = {
                    navController.navigate(AppScreen.HOME.name) {
                        popUpTo(AppScreen.SPLASH.name)
                    }
                },
            )
        }
        composable(AppScreen.SIGN_UP.name) {
            SignUpScreen(
                navigateBackToSplash = { navController.popBackStack(AppScreen.SPLASH.name, false) }
            )
        }
        composable(AppScreen.RESET_PASSWORD.name) {
            ResetPasswordScreen(
                navigateBack = navController::popBackStack,
            )
        }
        composable(AppScreen.HOME.name) {
            HomeScreen(
                navigateToRoomScreen = { roomId ->
                    navController.navigate("${AppScreen.ROOM.name}/$roomId")
                },
                navigateToUserScreen = { userId ->
                    navController.navigate("${AppScreen.USER.name}/$userId")
                },
                navigateToBlockListScreen = {
                    navController.navigate(AppScreen.BLOCK_LIST.name)
                },
                navigateToCreateRoomScreen = {
                    navController.navigate(AppScreen.ROOM_CREATE.name)
                },
                navigateToSignOutScreen = {
                    navController.navigate(AppScreen.SIGN_OUT.name) {
                        popUpTo(AppScreen.SPLASH.name)
                    }
                },
            )
        }
        composable(
            "${AppScreen.USER.name}/{userId}",
            listOf(navArgument("userId") { type = NavType.StringType })
        ) {
            UserScreen(
                navigateBack = navController::popBackStack,
                navigateToRoomScreen = { roomId, roomAction ->
                    navController.navigate("${AppScreen.ROOM.name}/$roomId?action=${roomAction?.name}") {
                        popUpTo(AppScreen.HOME.name)
                    }
                }
            )
        }
        composable(
            "${AppScreen.ROOM.name}/{roomId}?action={roomAction}",
            listOf(
                navArgument("roomId") { type = NavType.StringType },
                navArgument("roomAction") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            )
        ) {
            RoomScreen(
                navigateToPreviewScreen = { file ->
                    val encodedPath = URLEncoder.encode(file.path, Charsets.UTF_8.name())
                    navController.navigate("${AppScreen.FILE_PREVIEW.name}/$encodedPath")
                },
                navigateBack = navController::popBackStack,
                navigateToRoomDetailScreen = { roomId ->
                    navController.navigate("${AppScreen.ROOM.name}/$roomId/detail")
                },
            )
        }
        composable(
            "${AppScreen.ROOM.name}/{roomId}/detail",
            listOf(navArgument("roomId") { type = NavType.StringType })
        ) {
            RoomDetailScreen(
                navigateBack = navController::popBackStack,
                navigateToUserScreen = { userId ->
                    navController.navigate("${AppScreen.USER.name}/$userId")
                },
                navigateToHomeScreen = {
                    navController.popBackStack(AppScreen.HOME.name, false)
                }
            )
        }
        composable(AppScreen.BLOCK_LIST.name) {
            BlockListScreen(
                navigateToUserScreen = { userId ->
                    navController.navigate("${AppScreen.USER.name}/$userId")
                },
                navigateBack = navController::popBackStack,
            )
        }
        composable(AppScreen.ROOM_CREATE.name) {
            CreateRoomScreen(
                navigateBack = navController::popBackStack,
                navigateToUserScreen = { userId ->
                    navController.navigate("${AppScreen.USER.name}/$userId")
                },
                navigateToRoomScreen = { roomId ->
                    navController.navigate("${AppScreen.ROOM.name}/$roomId") {
                        popUpTo(AppScreen.HOME.name)
                    }
                }
            )
        }
    }
}
