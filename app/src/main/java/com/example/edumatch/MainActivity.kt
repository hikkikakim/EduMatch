package com.example.edumatch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.edumatch.ui.theme.EduMatchTheme
import com.example.edumatch.ui.screens.AuthScreen
import com.example.edumatch.ui.screens.ProfileEditScreen
import com.example.edumatch.ui.screens.CardScreen
import com.example.edumatch.ui.screens.ProfileScreen
import com.example.edumatch.ui.screens.MatchScreen
import com.example.edumatch.ui.screens.FilterScreen
import com.example.edumatch.ui.screens.SettingsScreen
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.layout.padding
import com.example.edumatch.data.model.User
import com.example.edumatch.ui.screens.ProfileFillScreen
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.edumatch.data.model.UserViewModel
import androidx.activity.viewModels
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.runtime.collectAsState

sealed class BottomNavItem(val route: String, val label: String, val icon: @Composable () -> Unit) {
    object Matches : BottomNavItem("matches", "Мэтчи", { Icon(imageVector = Icons.Filled.Person, contentDescription = null) })
    object Cards : BottomNavItem("cards", "Карточки", { Icon(imageVector = Icons.Filled.Home, contentDescription = null) })
    object Profile : BottomNavItem("profile", "Профиль", { Icon(imageVector = Icons.Filled.Person, contentDescription = null) })
}

@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavHostController, items: List<BottomNavItem>, currentRoute: String?) {
    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = item.icon,
                label = { Text(item.label) }
            )
        }
    }
}

class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel.initialize(this)
        // Добавляем слушатель смены пользователя
        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                userViewModel.initialize(this)
            } else {
                // Если пользователь вышел — сбрасываем данные
                userViewModel.clearAllUserData()
            }
        }
        enableEdgeToEdge()
        setContent {
            EduMatchTheme {
                val navController = rememberNavController()
                val bottomNavItems = listOf(
                    BottomNavItem.Matches,
                    BottomNavItem.Cards,
                    BottomNavItem.Profile
                )
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in bottomNavItems.map { it.route }) {
                            BottomNavigationBar(navController, bottomNavItems, currentRoute)
                        }
                    }
                ) { innerPadding ->
                    EduMatchNavHost(navController, userViewModel, Modifier.fillMaxSize().padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun EduMatchNavHost(navController: androidx.navigation.NavHostController, userViewModel: UserViewModel, modifier: Modifier = Modifier) {
    NavHost(navController = navController, startDestination = "auth", modifier = modifier) {
        composable("auth") {
            AuthScreen(
                onLogin = { navController.navigate("cards") },
                onSignUp = { navController.navigate("profileFill") }
            )
        }
        composable("profile_edit") {
            ProfileEditScreen(
                userViewModel = userViewModel,
                onBack = { navController.navigateUp() }
            )
        }
        composable("cards") {
            val users by userViewModel.allUsers.collectAsState()
            CardScreen(
                users = users,
                onLike = { user ->
                    // TODO: Добавить логику лайка
                    Log.d("CardScreen", "Liked user: ${user.name}")
                },
                onDislike = { user ->
                    // TODO: Добавить логику дизлайка
                    Log.d("CardScreen", "Disliked user: ${user.name}")
                },
                onMessage = { user ->
                    // TODO: Добавить логику отправки сообщения
                    Log.d("CardScreen", "Message to user: ${user.name}")
                },
                onReturn = {
                    // TODO: Добавить логику возврата
                    Log.d("CardScreen", "Return to previous card")
                }
            )
        }
        composable("profile") {
            ProfileScreen(
                userViewModel = userViewModel,
                onSettingsClick = { navController.navigate("settings") },
                onEditProfile = { navController.navigate("profile_edit") }
            )
        }
        composable("matches") {
            MatchScreen(userId = "")
        }
        composable("filters") {
            FilterScreen(onApply = { faculty, major, interests, ageRange ->
                navController.popBackStack()
            })
        }
        composable("profileFill") {
            ProfileFillScreen(
                userViewModel = userViewModel,
                onContinue = { name: String, birthDate: LocalDate, gender: String, showGender: Boolean, habits: Map<String, String?>? ->
                    navController.navigate("cards")
                },
                onBack = {
                    navController.navigate("auth")
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onBack = { navController.navigateUp() },
                onLogout = { 
                    // TODO: Очистить данные пользователя
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeactivate = {
                    // TODO: Отправить запрос на деактивацию
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onDeleteAccount = {
                    // TODO: Отправить запрос на удаление
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}