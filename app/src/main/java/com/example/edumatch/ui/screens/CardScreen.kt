package com.example.edumatch.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.edumatch.R
import com.example.edumatch.data.model.User
import kotlin.math.abs
import kotlin.math.roundToInt
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import coil.request.ImageRequest
import coil.size.Size
import coil.ImageLoader
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import android.util.Log

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FirebaseImage(photoRefOrUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var url by remember(photoRefOrUrl) { mutableStateOf<String?>(null) }

    LaunchedEffect(photoRefOrUrl) {
        if (photoRefOrUrl.startsWith("http")) {
            Log.d("FirebaseImage", "Используется downloadUrl: $photoRefOrUrl")
            url = photoRefOrUrl
        } else {
            try {
                val downloadUrl = FirebaseStorage.getInstance().reference.child(photoRefOrUrl).downloadUrl.await().toString()
                Log.d("FirebaseImage", "Путь: $photoRefOrUrl, получен downloadUrl: $downloadUrl")
                url = downloadUrl
            } catch (e: Exception) {
                Log.d("FirebaseImage", "Ошибка получения downloadUrl для пути: $photoRefOrUrl, ошибка: ${e.message}")
                url = null
            }
        }
    }

    if (url != null) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(url)
                .crossfade(true)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)
                .size(Size.ORIGINAL)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(Color.Gray)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TinderCard(
    user: User,
    modifier: Modifier = Modifier,
    offsetX: Float = 0f
) {
    val context = LocalContext.current
    var currentPhotoIndex by remember { mutableStateOf(0) }
    val photos = user.photos
    val photoCount = photos.size

    // Предзагрузка следующей фотки пользователя (если есть)
    if (photoCount > 0 && currentPhotoIndex < photos.lastIndex) {
        val nextPhotoUrl = photos[currentPhotoIndex + 1]
        LaunchedEffect(nextPhotoUrl) {
            val url = if (nextPhotoUrl.startsWith("http")) nextPhotoUrl else try {
                FirebaseStorage.getInstance().reference.child(nextPhotoUrl).downloadUrl.await().toString()
            } catch (e: Exception) { null }
            if (url != null) {
                Log.d("TinderCard", "Предзагрузка следующей фотки: $url")
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .size(Size.ORIGINAL)
                    .build()
                coil.Coil.imageLoader(context).enqueue(request)
            } else {
                Log.d("TinderCard", "Ошибка предзагрузки следующей фотки: $nextPhotoUrl")
            }
        }
    }
    
    // Анимация для подсветки LIKE/NOPE
    val likeAlpha = ((offsetX / 120f).coerceIn(0f, 1f))
    val nopeAlpha = ((-offsetX / 120f).coerceIn(0f, 1f))
    
    Box(
        modifier = modifier
    ) {
        if (photoCount > 0) {
            // Фото пользователя с поддержкой downloadUrl и пути
            FirebaseImage(
                photoRefOrUrl = photos[currentPhotoIndex],
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )

            // Индикаторы фото (поверх изображения)
            if (photoCount > 1) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, start = 24.dp, end = 24.dp)
                        .align(Alignment.TopCenter)
                        .zIndex(2f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    photos.forEachIndexed { idx, _ ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .background(
                                    color = if (idx == currentPhotoIndex) Color.White else Color.White.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                        )
                    }
                }
            }
            // Кликабельные боксы для переключения фото (поверх изображения)
            if (photoCount > 1) {
                Row(Modifier.fillMaxSize().zIndex(2f)) {
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                enabled = currentPhotoIndex > 0,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { currentPhotoIndex-- }
                    )
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                enabled = currentPhotoIndex < photos.lastIndex,
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { currentPhotoIndex++ }
                    )
                }
            }
        } else {
            // Заглушка, если фото нет
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Gray)
            )
        }

        // Информация о пользователе
        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(160.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
        )

        Column(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom)
        ) {
            Spacer(Modifier.weight(0.8f)) // поднять контент выше
            // Имя и возраст всегда отображаются
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    user.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                if (user.age > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${user.age}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                }
            }
            // Описание "Обо мне" только на первой фотке
            if (currentPhotoIndex == 0 && user.description.isNotBlank()) {
                Spacer(Modifier.height(3.dp)) // уменьшенный отступ
                Text(
                    user.description,
                    color = Color.White,
                    fontSize = 16.sp
                )
            }
            // Чипы интересов только на второй фотке
            if (currentPhotoIndex == 1 && user.interests.isNotEmpty()) {
                Spacer(Modifier.height(3.dp)) // уменьшенный отступ
                FlowRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    user.interests.forEach { interest ->
                        AssistChip(
                            onClick = {},
                            label = { Text(interest, color = Color.White) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = Color.White.copy(alpha = 0.18f)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp)) // дополнительный отступ между контентом и кнопками
        }

        // Подсветка свайпа
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    when {
                        offsetX > 0 -> Color(0x8032CD32).copy(alpha = (offsetX / 300f).coerceIn(0f, 0.5f))
                        offsetX < 0 -> Color(0x80FF0000).copy(alpha = (-offsetX / 300f).coerceIn(0f, 0.5f))
                        else -> Color.Transparent
                    }
                )
        )
        // Надписи LIKE/NOPE
        if (likeAlpha > 0.01f) {
            Text(
                text = "LIKE",
                color = Color(0xFF4CAF50),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp)
                    .graphicsLayer(alpha = likeAlpha, rotationZ = -15f)
            )
        }
        if (nopeAlpha > 0.01f) {
            Text(
                text = "NOPE",
                color = Color(0xFFFF1744),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(24.dp)
                    .graphicsLayer(alpha = nopeAlpha, rotationZ = 15f)
            )
        }
    }
}

@Composable
fun CardScreen(
    users: List<User>,
    currentUserId: String,
    likedUserIds: Set<String>,
    dislikedUserIds: Set<String>,
    onLike: (User) -> Unit = {},
    onDislike: (User) -> Unit = {},
    onMessage: (User) -> Unit = {},
    onReturn: () -> Unit = {}
) {
    // Фильтруем только чужие и не лайкнутые/не дизлайкнутые карточки
    val filteredUsers = users.filter { user ->
        user.id != currentUserId &&
        user.id !in likedUserIds &&
        user.id !in dislikedUserIds &&
        user.photos.isNotEmpty()
    }
    var currentIndex by remember { mutableStateOf(0) }
    val visibleUsers = filteredUsers.drop(currentIndex)
    val topUser = visibleUsers.firstOrNull()
    val nextUser = visibleUsers.getOrNull(1)

    // Состояния для смещения верхней карточки
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isSwiped by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val animatedOffsetX by animateFloatAsState(
        targetValue = if (isSwiped) {
            if (offsetX > 0) 1000f else -1000f
        } else offsetX,
        animationSpec = tween(durationMillis = 0),
        finishedListener = {
            if (isSwiped) {
                if (offsetX > 0) onLike(topUser!!)
                else onDislike(topUser!!)
                currentIndex++
                offsetX = 0f
                offsetY = 0f
                isSwiped = false
            }
        }
    )
    val displayOffsetX = if (isSwiped) animatedOffsetX else offsetX

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // Следующая карточка
        if (nextUser != null) {
            TinderCard(
                user = nextUser,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp)),
                offsetX = 0f
            )
        }

        // Верхняя карточка
        if (topUser != null) {
            TinderCard(
                user = topUser,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .shadow(16.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .graphicsLayer(
                        translationX = displayOffsetX,
                        translationY = offsetY,
                        rotationZ = (displayOffsetX / 40).coerceIn(-20f, 20f)
                    )
                    .pointerInput(currentIndex) {
                        detectDragGestures(
                            onDragEnd = {
                                when {
                                    offsetX > 200 -> isSwiped = true
                                    offsetX < -200 -> isSwiped = true
                                    else -> {
                                        offsetX = 0f
                                        offsetY = 0f
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            }
                        )
                    },
                offsetX = displayOffsetX
            )
        }
    }
} 