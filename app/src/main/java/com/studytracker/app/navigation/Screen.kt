package com.studytracker.app.navigation

sealed class Screen(val route: String) {
    object RoleSelection : Screen("role_selection")
    object ChildTutorial : Screen("child_tutorial")
    object ChildHome : Screen("child_home")
    object ParentDashboard : Screen("parent_dashboard")
    object AIPlanStudio : Screen("ai_plan_studio")
    object SessionReview : Screen("session_review/{sessionId}") {
        fun createRoute(sessionId: String) = "session_review/$sessionId"
    }
    object ChildQuiz : Screen("child_quiz/{quizId}") {
        fun createRoute(quizId: String) = "child_quiz/$quizId"
    }
    object ParentQuizReview : Screen("parent_quiz_review/{quizId}") {
        fun createRoute(quizId: String) = "parent_quiz_review/$quizId"
    }
    object DeveloperConsole : Screen("developer_console")
}
