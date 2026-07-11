package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavRoutes = listOf("conversations", "settings")
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Chat, contentDescription = "Messages") },
                        label = { Text("Messages") },
                        selected = currentRoute == "conversations",
                        onClick = {
                            if (currentRoute != "conversations") {
                                navController.navigate("conversations") {
                                    popUpTo("conversations") { inclusive = true }
                                }
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                        selected = currentRoute == "settings",
                        onClick = {
                            if (currentRoute != "settings") {
                                navController.navigate("settings") {
                                    popUpTo("settings") { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "conversations",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("conversations") {
                ConversationListScreen(
                    onConversationClick = { conversationId ->
                        navController.navigate("chat/$conversationId")
                    },
                    onNewConversationClick = {
                        navController.navigate("new_conversation")
                    }
                )
            }
            composable("new_conversation") {
                NewConversationScreen(
                    onConversationCreated = { conversationId ->
                        navController.navigate("chat/$conversationId") {
                            popUpTo("conversations")
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "chat/{conversationId}",
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                ChatDetailScreen(conversationId = conversationId)
            }
            composable("settings") {
                SettingsScreen(navController = navController)
            }
            composable("shortcuts") {
                ShortcutsScreen()
            }
            composable("signatures") {
                SignaturesScreen()
            }
            composable("themes") {
                ThemesScreen()
            }
            composable("scheduled") {
                ScheduledMessagesScreen()
            }
            composable("social") {
                SocialAccountsScreen()
            }
            composable("backup") {
                BackupScreen()
            }
            composable("applock") {
                AppLockScreen(navController = navController)
            }
            composable("security") {
                SecuritySettingsScreen(navController = navController)
            }
        }
    }
}
