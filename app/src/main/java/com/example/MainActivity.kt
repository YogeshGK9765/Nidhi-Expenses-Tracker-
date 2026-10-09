package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ExpenseWithDetails
import com.example.data.model.ThemeMode
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.add.AddEditExpenseScreen
import com.example.ui.expenses.ExpensesScreen
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.NidhiBottomNavigation
import com.example.ui.navigation.Screen
import com.example.ui.reports.ReportsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.splash.SplashScreen
import com.example.ui.theme.NidhiTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as NidhiApplication
        val factory = MainViewModelFactory(app.repository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            var showSplash by remember { mutableStateOf(true) }

            NidhiTheme(darkTheme = isDark) {
                if (showSplash) {
                    SplashScreen(onSplashFinished = { showSplash = false })
                } else {
                    NidhiApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun NidhiApp(viewModel: MainViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var previousScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var editingExpense by remember { mutableStateOf<ExpenseWithDetails?>(null) }

    val onNavigateTo: (Screen) -> Unit = { screen ->
        if (screen != currentScreen) {
            previousScreen = currentScreen
            currentScreen = screen
        }
    }

    // System Back Handler
    BackHandler(enabled = currentScreen != Screen.Home) {
        if (currentScreen == Screen.Add) {
            editingExpense = null
            currentScreen = previousScreen
        } else {
            currentScreen = Screen.Home
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Show bottom navigation bar on all primary tabs
            NidhiBottomNavigation(
                currentRoute = currentScreen.route,
                onNavigate = { targetScreen ->
                    if (targetScreen == Screen.Add) {
                        editingExpense = null
                        onNavigateTo(Screen.Add)
                    } else {
                        onNavigateTo(targetScreen)
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (currentScreen) {
                Screen.Home -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToAdd = {
                            editingExpense = null
                            onNavigateTo(Screen.Add)
                        },
                        onNavigateToExpenses = { onNavigateTo(Screen.Expenses) },
                        onNavigateToSettings = { onNavigateTo(Screen.Settings) },
                        onEditExpense = { exp ->
                            editingExpense = exp
                            onNavigateTo(Screen.Add)
                        }
                    )
                }

                Screen.Expenses -> {
                    ExpensesScreen(
                        viewModel = viewModel,
                        onNavigateToAdd = {
                            editingExpense = null
                            onNavigateTo(Screen.Add)
                        },
                        onEditExpense = { exp ->
                            editingExpense = exp
                            onNavigateTo(Screen.Add)
                        }
                    )
                }

                Screen.Add -> {
                    AddEditExpenseScreen(
                        viewModel = viewModel,
                        existingExpense = editingExpense,
                        onNavigateBack = {
                            editingExpense = null
                            currentScreen = previousScreen
                        }
                    )
                }

                Screen.Reports -> {
                    ReportsScreen(
                        viewModel = viewModel,
                        onNavigateToAdd = {
                            editingExpense = null
                            onNavigateTo(Screen.Add)
                        }
                    )
                }

                Screen.Settings -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
