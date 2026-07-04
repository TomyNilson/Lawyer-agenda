package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.theme.FadedAppBackground
import com.example.ui.theme.SlateBackground

@Composable
fun LawyerApp(viewModel: LawyerViewModel) {
    val isLocked by viewModel.isLocked.collectAsState()
    val hasPassword by viewModel.hasPassword.collectAsState()

    FadedAppBackground(backgroundColor = SlateBackground) {
        if (isLocked && hasPassword) {
            SecurityScreen(
                viewModel = viewModel,
                onUnlocked = {
                    // ViewModel updates isLocked to false automatically
                }
            )
        } else {
            val navController = rememberNavController()

            NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.fillMaxSize()
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToCases = { isDisposed ->
                        navController.navigate("cases/$isDisposed")
                    },
                    onNavigateToAddCase = {
                        navController.navigate("add_case?caseId=-1")
                    },
                    onNavigateToCourts = {
                        navController.navigate("manage_courts")
                    },
                    onNavigateToCaseTypes = {
                        navController.navigate("manage_case_types")
                    },
                    onNavigateToReminders = {
                        navController.navigate("reminders")
                    },
                    onNavigateToBackup = {
                        navController.navigate("backup_security")
                    },
                    onLockClicked = {
                        viewModel.lockApp()
                    }
                )
            }

            composable(
                route = "cases/{disposed}",
                arguments = listOf(navArgument("disposed") { type = NavType.BoolType })
            ) { backStackEntry ->
                val disposed = backStackEntry.arguments?.getBoolean("disposed") ?: false
                CaseListScreen(
                    viewModel = viewModel,
                    initialDisposedTab = disposed,
                    onBack = { navController.popBackStack() },
                    onCaseClicked = { caseId ->
                        navController.navigate("case_details/$caseId")
                    },
                    onAddCaseClicked = {
                        navController.navigate("add_case?caseId=-1")
                    }
                )
            }

            composable(
                route = "add_case?caseId={caseId}",
                arguments = listOf(navArgument("caseId") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { backStackEntry ->
                val caseIdArg = backStackEntry.arguments?.getInt("caseId") ?: -1
                val caseIdToEdit = if (caseIdArg != -1) caseIdArg else null
                AddCaseScreen(
                    viewModel = viewModel,
                    caseIdToEdit = caseIdToEdit,
                    onBack = { navController.popBackStack() },
                    onSaved = { newCaseId ->
                        navController.popBackStack()
                        if (caseIdToEdit == null) {
                            navController.navigate("case_details/$newCaseId")
                        }
                    }
                )
            }

            composable(
                route = "case_details/{caseId}",
                arguments = listOf(navArgument("caseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val caseId = backStackEntry.arguments?.getInt("caseId") ?: 0
                CaseDetailsScreen(
                    viewModel = viewModel,
                    caseId = caseId,
                    onBack = { navController.popBackStack() },
                    onEditCase = { id ->
                        navController.navigate("add_case?caseId=$id")
                    }
                )
            }

            composable("manage_courts") {
                ManageListsScreen(
                    viewModel = viewModel,
                    isCourtsMode = true,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("manage_case_types") {
                ManageListsScreen(
                    viewModel = viewModel,
                    isCourtsMode = false,
                    onBack = { navController.popBackStack() }
                )
            }

            composable("reminders") {
                ReminderCasesScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onCaseClicked = { caseId ->
                        navController.navigate("case_details/$caseId")
                    }
                )
            }

            composable("backup_security") {
                BackupAndSecurityScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
    }
}
