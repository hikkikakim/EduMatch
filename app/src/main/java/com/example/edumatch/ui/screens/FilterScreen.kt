package com.example.edumatch.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun FilterScreen(
    onApply: (faculty: String?, major: String?, interests: List<String>, ageRange: IntRange) -> Unit
) {
    Scaffold {
        Button(onClick = { onApply(null, null, emptyList(), 18..30) }) {
            Text("Применить фильтры (заглушка)")
        }
    }
} 