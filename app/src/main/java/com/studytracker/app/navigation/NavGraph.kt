package com.studytracker.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.studytracker.BuildConfig
import com.studytracker.core.data.local.prefs.AppPreferences
import com.studytracker.feature.child.ChildHomeScreen
import com.studytracker.feature.child.ChildQuizScreen
import com.studytracker.feature.child.ChildTutorialScreen
import com.studytracker.feature.parent.AIPlanStudioScreen
import com.studytracker.feature.parent.ParentDashboardScreen
import com.studytracker.feature.parent.ParentQuizReviewScreen
import com.studytracker.feature.parent.RoleSelectionScreen
import com.studytracker.feature.parent.SessionReviewScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    val context = LocalContext.current
    val prefs = remember { AppPreferences.getInstance(context) }

    val startDestination = remember {
        when (BuildConfig.APP_ROLE) {
            "CHILD" -> if (prefs.hasCompletedTutorial.value) Screen.ChildHome.route else Screen.ChildTutorial.route
            "PARENT" -> Screen.ParentDashboard.route
            else -> Screen.RoleSelection.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.RoleSelection.route) {
            RoleSelectionScreen(
                onNavigateToChildHome = {
                    navController.navigate(Screen.ChildHome.route)
                },
                onNavigateToChildTutorial = {
                    navController.navigate(Screen.ChildTutorial.route)
                },
                onNavigateToParent = {
                    navController.navigate(Screen.ParentDashboard.route)
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
                },
                onNavigateToQuiz = { quizId ->
                    navController.navigate(Screen.ChildQuiz.createRoute(quizId))
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
                },
                onNavigateToQuizReview = { quizId ->
                    navController.navigate(Screen.ParentQuizReview.createRoute(quizId))
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

        composable(
            route = Screen.ChildQuiz.route,
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            ChildQuizScreen(
                quizId = quizId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.ParentQuizReview.route,
            arguments = listOf(navArgument("quizId") { type = NavType.StringType })
        ) { backStackEntry ->
            val quizId = backStackEntry.arguments?.getString("quizId") ?: ""
            ParentQuizReviewScreen(
                quizId = quizId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
