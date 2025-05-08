package com.example.edumatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit = {},
    onDeactivate: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    var showGender by remember { mutableStateOf(ShowGender.ALL) }
    var searchPreference by remember { mutableStateOf(SearchPreference.WEIGHTED) }
    var activityVisibility by remember { mutableStateOf(ActivityVisibility.STANDARD) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        TopAppBar(
            title = { Text("Настройки", fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Назад")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White
            )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection(
                title = "Показывать",
                content = {
                    RadioButtonGroup(
                        selected = showGender,
                        onSelected = { showGender = it },
                        options = ShowGender.values().toList(),
                        getLabel = { it.label }
                    )
                }
            )

            SettingsSection(
                title = "Искать...",
                content = {
                    RadioButtonGroup(
                        selected = searchPreference,
                        onSelected = { searchPreference = it },
                        options = SearchPreference.values().toList(),
                        getLabel = { it.label }
                    )
                }
            )

            SettingsSection(
                title = "Управление видимости активности",
                content = {
                    RadioButtonGroup(
                        selected = activityVisibility,
                        onSelected = { activityVisibility = it },
                        options = ActivityVisibility.values().toList(),
                        getLabel = { it.label }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Секция управления аккаунтом
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Управление аккаунтом",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка деактивации
                Button(
                    onClick = onDeactivate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF757575)
                    )
                ) {
                    Text("Уйти в инактив")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка выхода
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2563EB)
                    )
                ) {
                    Text("Выйти")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Кнопка удаления аккаунта
                Button(
                    onClick = onDeleteAccount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDC2626)
                    )
                ) {
                    Text("Удалить аккаунт")
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun <T> RadioButtonGroup(
    selected: T,
    onSelected: (T) -> Unit,
    options: List<T>,
    getLabel: (T) -> String
) {
    Column {
        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == option,
                    onClick = { onSelected(option) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = getLabel(option),
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }
        }
    }
}

enum class ShowGender(val label: String) {
    MEN("Мужчин"),
    WOMEN("Женщин"),
    ALL("Всех")
}

enum class SearchPreference(val label: String) {
    WEIGHTED("Взвешенные рекомендации"),
    RECENTLY_ACTIVE("Недавно активные")
}

enum class ActivityVisibility(val label: String) {
    STANDARD("Стандартно"),
    HIDDEN("Скрыто")
} 