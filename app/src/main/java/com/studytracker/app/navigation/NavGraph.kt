package com.studytracker.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.studytracker.feature.child.ChildHomeScreen
import com.studytracker.feature.child.ChildTutorialScreen
import com.studytracker.feature.parent.AIPlanStudioScreen
import com.studytracker.feature.parent.ParentDashboardScreen
import com.studytracker.feature.parent.RoleSelectionScreen
import com.studytracker.feature.parent.SessionReviewScreen
import com.studytracker.feature.test_mode.DeveloperConsoleScreen

@Composable
fun AppNavGraph(
    navController: NavHostController,
    isFirstChildLaunch: Boolean = true
) {
    NavHost(
        navController = navController,
        startDestination = Screen.RoleSelection.route
    ) {
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onNavigateToChild = {
                    if (isFirstChildLaunch) {
                        navController.navigate(Screen.ChildTutorial.route)
                    } else {
                        navController.navigate(Screen.ChildHome.route)
                    }
                },
                onNavigateToParent = {
                    navController.navigate(Screen.ParentDashboard.route)
                },
                onNavigateToDevMode = {
                    navController.navigate(Screen.DeveloperConsole.route)
                }
            )
        }

        composable(Screen.ChildTutorial.route) {
            ChildTutorialScreen(
                onFinishTutorial = {
                    navController.navigate(Screen.ChildHome.route) {
                        popUpTo(Screen.RoleSelection.route)
                    }
                }
            )
        }

        composable(Screen.ChildHome.route) {
            ChildHomeScreen(
                onNavigateBackToRole = {
                    navController.popBackStack()
                },
                onOpenTutorial = {
                    navController.navigate(Screen.ChildTutorial.route)
                }
            )
        }

        composable(Screen.ParentDashboard.route) {
            ParentDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlanStudio = {
                    navController.navigate(Screen.AIPlanStudio.route)
                },
                onNavigateToSessionReview = { sessionId ->
                    navController.navigate(Screen.SessionReview.createRoute(sessionId))
                }
            )
        }

        composable(Screen.AIPlanStudio.route) {
            AIPlanStudioScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.SessionReview.route,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            SessionReviewScreen(
                sessionId = sessionId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.DeveloperConsole.route) {
            DeveloperConsoleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
