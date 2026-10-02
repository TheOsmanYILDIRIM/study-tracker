package com.studytracker.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.studytracker.feature.child.ChildHomeScreen
import com.studytracker.feature.child.ChildQuizScreen
import com.studytracker.feature.child.ChildTutorialScreen
import com.studytracker.feature.parent.AIPlanStudioScreen
import com.studytracker.feature.parent.ParentDashboardScreen
import com.studytracker.feature.parent.ParentQuizReviewScreen
import com.studytracker.feature.parent.RoleSelectionScreen
import com.studytracker.feature.parent.SessionReviewScreen

import com.studytracker.feature.v2.V2CoursesScreen
import com.studytracker.feature.v2.V2LessonsScreen
import com.studytracker.feature.v2.V2LearningFlowScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    // V2 measurement curriculum is the canonical product surface.
    // Legacy child/parent routes remain reachable for migration compatibility,
    // but they no longer define the default app experience.
    val startDestination = Screen.V2Courses.route

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
                },
                onNavigateToV2Courses = {
                    navController.navigate(Screen.V2Courses.route)
                }
            )
        }

        composable(Screen.V2Courses.route) {
            V2CoursesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCourse = { courseId ->
                    navController.navigate(Screen.V2Lessons.createRoute(courseId))
                },
                onResumeLesson = { lessonId ->
                    navController.navigate(Screen.V2LearningFlow.createRoute(lessonId))
                }
            )
        }

        composable(
            route = Screen.V2Lessons.route,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) { backStackEntry ->
            val courseId = backStackEntry.arguments?.getString("courseId") ?: ""
            V2LessonsScreen(
                courseId = courseId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToLearningFlow = { lessonId ->
                    navController.navigate(Screen.V2LearningFlow.createRoute(lessonId))
                }
            )
        }

        composable(
            route = Screen.V2LearningFlow.route,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lessonId = backStackEntry.arguments?.getString("lessonId") ?: ""
            V2LearningFlowScreen(
                lessonId = lessonId,
                onNavigateBack = {
                    navController.popBackStack()
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
