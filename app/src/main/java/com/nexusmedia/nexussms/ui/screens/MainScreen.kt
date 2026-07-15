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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.nexusmedia.nexussms.security.KeyExchangeManager
import com.nexusmedia.nexussms.security.SafetyNumberManager
import com.nexusmedia.nexussms.features.security.SpamDetector
import com.nexusmedia.nexussms.features.security.SpamBlocklistManager
import com.nexusmedia.nexussms.features.security.VaultManager
import com.nexusmedia.nexussms.features.automation.RuleEngine
import com.nexusmedia.nexussms.features.backup.WebDavBackupService
import com.nexusmedia.nexussms.features.backup.WebDavClient
import com.nexusmedia.nexussms.ui.viewmodels.ChannelRoutingViewModel
import com.nexusmedia.nexussms.ui.viewmodels.UnifiedContactViewModel
import com.nexusmedia.nexussms.ui.viewmodels.UnifiedContactsListViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

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
                val context = LocalContext.current
                val app = context.applicationContext
                val vaultEntryPoint = EntryPointAccessors.fromApplication(app, VaultEntryPoint::class.java)
                val vaultManager = vaultEntryPoint.vaultManager()
                ConversationListScreen(
                    onConversationClick = { conversationId ->
                        navController.navigate("chat/$conversationId")
                    },
                    onNewConversationClick = {
                        navController.navigate("new_conversation")
                    },
                    onSearchClick = {
                        navController.navigate("search")
                    },
                    onBlocklistClick = {
                        navController.navigate("blocklist")
                    },
                    onArchiveClick = {
                        navController.navigate("archive")
                    },
                    onContactsClick = {
                        navController.navigate("contacts")
                    },
                    onHideInVault = { conversation ->
                        vaultManager.hideConversation(
                            conversationId = conversation.id,
                            displayName = conversation.displayName,
                            lastMessage = conversation.lastMessage,
                            lastMessageTime = conversation.lastMessageTime
                        )
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
                ChatDetailScreen(
                    conversationId = conversationId,
                    onNavigateToDetails = { messageId ->
                        navController.navigate("message_details/$messageId")
                    },
                    onNavigateToSafetyNumber = { convId ->
                        navController.navigate("safety_number/$convId")
                    },
                    onNavigateToKeyVerification = { convId ->
                        navController.navigate("key_verification/$convId")
                    }
                )
            }
            composable("search") {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onConversationClick = { conversationId ->
                        navController.navigate("chat/$conversationId")
                    }
                )
            }
            composable("settings") {
                SettingsScreen(navController = navController)
            }
            composable("shortcuts") {
                ShortcutsScreen()
            }
            composable("templates") {
                TemplatesScreen()
            }
            composable("signatures") {
                SignaturesScreen()
            }
            composable("themes") {
                ThemesScreen(navController = navController)
            }
            composable("theme_creator") {
                ThemeCreatorScreen(navController = navController)
            }
            composable("scheduled") {
                ScheduledMessagesScreen()
            }
            composable("social") {
                SocialAccountsScreen(navController = navController)
            }
            composable("privacy_policy") {
                PrivacyPolicyScreen(navController = navController)
            }
            composable("terms_of_service") {
                TermsOfServiceScreen(navController = navController)
            }
            composable("backup") {
                BackupScreen(
                    onWebDavClick = { navController.navigate("webdav_backup") }
                )
            }
            composable("webdav_backup") {
                val context = LocalContext.current
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, WebDavBackupEntryPoint::class.java)
                WebDavBackupScreen(
                    webDavClient = entryPoint.webDavClient(),
                    webDavBackupService = entryPoint.webDavBackupService(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable("applock") {
                AppLockScreen(navController = navController)
            }
            composable("security") {
                SecuritySettingsScreen(navController = navController)
            }
            composable("spam_detection") {
                val context = LocalContext.current
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, SpamDetectionEntryPoint::class.java)
                SpamDetectionScreen(
                    spamDetector = entryPoint.spamDetector(),
                    spamBlocklistManager = entryPoint.spamBlocklistManager(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable("blocklist") {
                BlocklistScreen(navController = navController)
            }
            composable("archive") {
                ArchiveScreen(navController = navController)
            }
            composable("messaging_settings") {
                MessagingSettingsScreen(navController = navController)
            }
            composable("channel_routing") {
                val vm: ChannelRoutingViewModel = hiltViewModel()
                ChannelRoutingSettingsScreen(
                    routingManager = vm.routingManager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "contact_routing/{contactId}/{contactName}",
                arguments = listOf(
                    navArgument("contactId") { type = NavType.StringType },
                    navArgument("contactName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                val contactName = backStackEntry.arguments?.getString("contactName") ?: "Contact"
                val vm: ChannelRoutingViewModel = hiltViewModel()
                ContactRoutingScreen(
                    contactId = contactId,
                    contactName = java.net.URLDecoder.decode(contactName, "UTF-8"),
                    routingManager = vm.routingManager,
                    onBack = { navController.popBackStack() }
                )
            }
            composable("contacts") {
                val viewModel: UnifiedContactsListViewModel = hiltViewModel()
                UnifiedContactsListScreen(
                    viewModel = viewModel,
                    onContactClick = { contactId ->
                        navController.navigate("contact/$contactId")
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "contact/{contactId}",
                arguments = listOf(navArgument("contactId") { type = NavType.StringType })
            ) { backStackEntry ->
                val contactId = backStackEntry.arguments?.getString("contactId") ?: ""
                val viewModel: UnifiedContactViewModel = hiltViewModel()
                UnifiedContactScreen(
                    contactId = contactId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onConversationClick = { conversationId ->
                        navController.navigate("chat/$conversationId")
                    }
                )
            }
            composable(
                route = "message_details/{messageId}",
                arguments = listOf(navArgument("messageId") { type = NavType.StringType })
            ) { backStackEntry ->
                val messageId = backStackEntry.arguments?.getString("messageId") ?: ""
                MessageDetailsScreen(
                    messageId = messageId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "safety_number/{conversationId}",
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                val context = LocalContext.current
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, KeyVerificationEntryPoint::class.java)
                SafetyNumberScreen(
                    contactId = conversationId,
                    contactName = "Contact",
                    safetyNumberManager = entryPoint.safetyNumberManager(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable("automation") {
                val context = LocalContext.current
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, AutomationEntryPoint::class.java)
                AutomationScreen(
                    ruleEngine = entryPoint.ruleEngine(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable("vault") {
                val context = LocalContext.current
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, VaultEntryPoint::class.java)
                VaultScreen(
                    vaultManager = entryPoint.vaultManager(),
                    onBack = { navController.popBackStack() },
                    onConversationClick = { conversationId ->
                        navController.navigate("chat/$conversationId")
                    }
                )
            }
            composable("vault_setup") {
                val context = LocalContext.current
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, VaultEntryPoint::class.java)
                VaultSetupScreen(
                    vaultManager = entryPoint.vaultManager(),
                    onBack = { navController.popBackStack() },
                    onSetupComplete = {
                        navController.popBackStack()
                        navController.navigate("vault")
                    }
                )
            }
            composable("vault_settings") {
                val context = LocalContext.current
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, VaultEntryPoint::class.java)
                VaultSettingsScreen(
                    vaultManager = entryPoint.vaultManager(),
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = "key_verification/{conversationId}",
                arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
            ) { backStackEntry ->
                val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
                val context = LocalContext.current
                val app = context.applicationContext
                val entryPoint = EntryPointAccessors.fromApplication(app, KeyVerificationEntryPoint::class.java)
                KeyVerificationScreen(
                    contactId = conversationId,
                    contactName = "Contact",
                    keyExchangeManager = entryPoint.keyExchangeManager(),
                    safetyNumberManager = entryPoint.safetyNumberManager(),
                    onBack = { navController.popBackStack() },
                    onNavigateToSafetyNumber = {
                        navController.navigate("safety_number/$conversationId")
                    }
                )
            }
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface KeyVerificationEntryPoint {
    fun safetyNumberManager(): SafetyNumberManager
    fun keyExchangeManager(): KeyExchangeManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SpamDetectionEntryPoint {
    fun spamDetector(): SpamDetector
    fun spamBlocklistManager(): SpamBlocklistManager
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AutomationEntryPoint {
    fun ruleEngine(): RuleEngine
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WebDavBackupEntryPoint {
    fun webDavClient(): WebDavClient
    fun webDavBackupService(): WebDavBackupService
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VaultEntryPoint {
    fun vaultManager(): VaultManager
}
