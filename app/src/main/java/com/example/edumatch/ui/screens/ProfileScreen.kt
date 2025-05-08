package com.example.edumatch.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.zIndex
import com.example.edumatch.R
import androidx.compose.foundation.BorderStroke
import java.time.LocalDate
import java.time.Period
import com.example.edumatch.data.model.UserViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path

@Composable
fun ProfileScreen(
    userViewModel: UserViewModel,
    onSettingsClick: () -> Unit,
    onEditProfile: () -> Unit
) {
    val userName by userViewModel.userName.collectAsState()
    val userBirthDate by userViewModel.userBirthDate.collectAsState()
    val userAge = userBirthDate?.let { birthDate ->
        Period.between(birthDate, LocalDate.now()).years
    } ?: 0
    val photos by userViewModel.photos.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Верхний белый фон с дугой
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val path = Path()
                    val height = size.height
                    val width = size.width
                    
                    path.moveTo(0f, 0f)
                    path.lineTo(width, 0f)
                    path.lineTo(width, height * 0.75f)
                    path.quadraticBezierTo(
                        width / 2f, height * 0.9f,
                        0f, height * 0.75f
                    )
                    path.lineTo(0f, 0f)
                    path.close()

                    // Рисуем тень
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Transparent
                            ),
                            startY = height * 0.75f,
                            endY = height * 0.9f
                        )
                    )

                    // Рисуем белый фон
                    drawPath(
                        path = path,
                        color = Color.White
                    )
                }
            }

            // Основной контент
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(100.dp))
                Spacer(modifier = Modifier.height(48.dp))
                // Аватар с дугообразным прогресс-баром и плашкой
                Box(contentAlignment = Alignment.Center, modifier = Modifier.height(200.dp)) {
                    // Окружность и прогресс-бар вокруг аватара
                    Canvas(modifier = Modifier
                        .size(184.dp)
                        .offset(x = 0.dp, y = 0.dp)
                    ) {
                        val arcStroke = 12.dp.toPx()
                        val arcRect = Rect(arcStroke/2, arcStroke/2, size.width-arcStroke/2, size.height-arcStroke/2)
                        // Серая дуга (260 градусов)
                        drawArc(
                            color = Color(0xFFE0E0E0),
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = arcStroke, cap = StrokeCap.Round),
                            size = Size(size.width-arcStroke, size.height-arcStroke),
                            topLeft = arcRect.topLeft
                        )
                        // Прогресс-бар (пример: 26% от 260 градусов)
                        drawArc(
                            color = Color(0xFF2563EB),
                            startAngle = -220f,
                            sweepAngle = 260f * 0.26f,
                            useCenter = false,
                            style = Stroke(width = arcStroke, cap = StrokeCap.Round),
                            size = Size(size.width-arcStroke, size.height-arcStroke),
                            topLeft = arcRect.topLeft
                        )
                    }
                    // Аватар
                    if (photos.isNotEmpty()) {
                        Image(
                            painter = rememberAsyncImagePainter(photos[0]),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier
                                .size(160.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Плашка процента
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .offset(y = (-4).dp)
                            .zIndex(1f)
                            .background(Color(0xFF2563EB), shape = CircleShape)
                            .padding(horizontal = 42.dp, vertical = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "26% COMPLETE",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
                // Имя и возраст сразу под плашкой
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = when {
                        userName.isNotEmpty() && userAge > 0 -> "$userName, $userAge"
                        userName.isNotEmpty() -> userName
                        else -> "Профиль без имени"
                    },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(32.dp))
                // Кнопки: Settings, Edit Profile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp),
                    horizontalArrangement = Arrangement.spacedBy(80.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Settings
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 12.dp
                        ) {
                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier.size(70.dp)
                            ) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color(0xFFB0B0B0), modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("SETTINGS", fontSize = 15.sp, color = Color(0xFFB0B0B0))
                    }
                    // Edit Profile
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 12.dp
                        ) {
                            IconButton(
                                onClick = onEditProfile,
                                modifier = Modifier.size(70.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit Profile", tint = Color(0xFFB0B0B0), modifier = Modifier.size(32.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("EDIT PROFILE", fontSize = 15.sp, color = Color(0xFFB0B0B0))
                    }
                }
            }
        }
    }
} 