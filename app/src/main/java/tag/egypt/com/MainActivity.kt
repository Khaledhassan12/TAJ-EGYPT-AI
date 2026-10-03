package tag.egypt.com

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import tag.egypt.com.ui.screens.ChatScreen
import tag.egypt.com.ui.screens.ConversationsDrawer
import tag.egypt.com.ui.screens.McpScreen
import tag.egypt.com.ui.screens.PromptsScreen
import tag.egypt.com.ui.screens.ProvidersScreen
import tag.egypt.com.ui.screens.SettingsScreen
import tag.egypt.com.ui.screens.SkillsScreen
import tag.egypt.com.ui.theme.Slate950
import tag.egypt.com.ui.theme.TajEgyTheme
import tag.egypt.com.ui.viewmodel.ChatViewModel
import tag.egypt.com.ui.viewmodel.SettingsViewModel

enum class AppScreen {
    CHAT,
    PROVIDERS,
    MCP,
    SKILLS,
    PROMPTS,
    SETTINGS
}

/**
 * Main Activity for TAJ EGY.
 */
class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TajEgyTheme {
                MainAppContent(
                    chatViewModel = chatViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@Composable
fun MainAppContent(
    chatViewModel: ChatViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentScreen by remember { mutableStateOf(AppScreen.CHAT) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // Handle back press gracefully
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.CHAT) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (currentScreen != AppScreen.CHAT) {
            currentScreen = AppScreen.CHAT
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationsDrawer(
                chatViewModel = chatViewModel,
                onNavigateToChat = {
                    currentScreen = AppScreen.CHAT
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigateToProviders = {
                    currentScreen = AppScreen.PROVIDERS
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigateToMcp = {
                    currentScreen = AppScreen.MCP
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigateToSkills = {
                    currentScreen = AppScreen.SKILLS
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigateToPrompts = {
                    currentScreen = AppScreen.PROMPTS
                    coroutineScope.launch { drawerState.close() }
                },
                onNavigateToSettings = {
                    currentScreen = AppScreen.SETTINGS
                    coroutineScope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Slate950
        ) {
            when (currentScreen) {
                AppScreen.CHAT -> {
                    ChatScreen(
                        chatViewModel = chatViewModel,
                        settingsViewModel = settingsViewModel,
                        onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                        onOpenProviders = { currentScreen = AppScreen.PROVIDERS },
                        onOpenMcp = { currentScreen = AppScreen.MCP },
                        onOpenSkills = { currentScreen = AppScreen.SKILLS },
                        onOpenPrompts = { currentScreen = AppScreen.PROMPTS },
                        onOpenSettings = { currentScreen = AppScreen.SETTINGS }
                    )
                }
                AppScreen.PROVIDERS -> {
                    ProvidersScreen(
                        viewModel = settingsViewModel,
                        onBack = { currentScreen = AppScreen.CHAT }
                    )
                }
                AppScreen.MCP -> {
                    McpScreen(
                        viewModel = settingsViewModel,
                        onBack = { currentScreen = AppScreen.CHAT }
                    )
                }
                AppScreen.SKILLS -> {
                    SkillsScreen(
                        viewModel = settingsViewModel,
                        onBack = { currentScreen = AppScreen.CHAT }
                    )
                }
                AppScreen.PROMPTS -> {
                    PromptsScreen(
                        viewModel = settingsViewModel,
                        onBack = { currentScreen = AppScreen.CHAT },
                        onUsePrompt = { promptText ->
                            chatViewModel.sendMessage(promptText)
                            currentScreen = AppScreen.CHAT
                        }
                    )
                }
                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        settingsViewModel = settingsViewModel,
                        chatViewModel = chatViewModel,
                        onBack = { currentScreen = AppScreen.CHAT }
                    )
                }
            }
        }
    }
}
