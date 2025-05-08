package com.example.edumatch.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.edumatch.data.model.UserViewModel
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.CornerRadius
import com.example.edumatch.data.model.User
import androidx.compose.ui.zIndex
import android.net.Uri
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.res.painterResource
import com.example.edumatch.R
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.style.TextOverflow

val interestCategories = listOf(
    "Вечеринки" to listOf("Квесты", "Бары", "Покупки на барахолках", "Музеи", "Рейвы", "Автокинотеатр", "Мюзиклы", "Походы по кафе", "Аквариум", "Клубы", "Выставки", "Покупки", "Автомобили", "Викторины в пабе", "Фестивали", "Счастливый час", "Стендап-комедия", "Караоке", "Вечеринки дома", "Театр", "Кальян", "Катание на роликах", "Живая музыка", "Тур по барам", "Боулинг", "Мотоциклы", "Вечеринки", "Ночная жизнь", "Галереи искусств", "Фестиваль кино", "Пабы", "Концерты", "Городские праздники"),
    "Глобальные ценности" to listOf("Духовное здоровье", "Права избирателей", "Изменение климата", "Права ЛГБТКИА+"),
    "Досуг дома" to listOf("Чтение", "Просмотр сериалов", "Домашние тренировки", "Викторины")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditScreen(
    userViewModel: UserViewModel,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Изменить", "Предпросмотр")
    val photos by userViewModel.photos.collectAsState()
    val userName by userViewModel.userName.collectAsState()
    val userBirthDate by userViewModel.userBirthDate.collectAsState()
    val userAge = userBirthDate?.let { java.time.Period.between(it, java.time.LocalDate.now()).years } ?: 0
    val savedInterests by userViewModel.interests.collectAsState()
    // TODO: При смене пользователя вызывать userViewModel.loadUserInterests(userId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Изменить профиль") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTab) {
                0 -> EditTab(photos, userViewModel)
                1 -> PreviewTab(photos, userName, userAge, savedInterests, userViewModel.aboutMe.collectAsState().value, userViewModel.faculty.collectAsState().value)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTab(photos: List<Uri>, userViewModel: UserViewModel) {
    val maxPhotos = 9
    var showPhotoDialog by remember { mutableStateOf(false) }
    var photoDialogIndex by remember { mutableStateOf(-1) }
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val aboutMe by userViewModel.aboutMe.collectAsState()
    val scrollState = rememberScrollState()
    val savedInterests by userViewModel.interests.collectAsState()
    var showInterestsDialog by remember { mutableStateOf(false) }

    val uniquePhotos = photos.distinct().take(maxPhotos)
    val paddedPhotos: List<Uri?> = uniquePhotos.map { it as Uri? } + List(maxPhotos - uniquePhotos.size) { null }

    // --- Фото ---
    val createImageUri = remember {
        {
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_${'$'}timeStamp_"
                val storageDir = context.cacheDir
                val file = File.createTempFile(imageFileName, ".jpg", storageDir)
                FileProvider.getUriForFile(
                    context,
                    "${'$'}{context.packageName}.provider",
                    file
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = maxPhotos)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                userViewModel.uploadPhotoToStorageAndSave(uri) { success ->
                    // Можно добавить обработку успеха/ошибки
                }
            }
        }
        showPhotoDialog = false
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success && cameraImageUri != null && photoDialogIndex in 0..8) {
            val newPhotos = photos.toMutableList()
            if (photoDialogIndex < newPhotos.size) {
                newPhotos[photoDialogIndex] = cameraImageUri!!
            } else if (newPhotos.size < maxPhotos) {
                newPhotos.add(cameraImageUri!!)
            }
            userViewModel.setPhotos(newPhotos)
        }
        showPhotoDialog = false
    }
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            createImageUri()?.let { uri ->
                cameraImageUri = uri
                cameraLauncher.launch(uri)
            }
        } else {
            showPermissionDialog = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
            .verticalScroll(scrollState)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Медиафайлы", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Text("Добавь до 9 фото.", color = Color.Gray, fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                for (row in 0 until 3) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (col in 0 until 3) {
                            val index = row * 3 + col
                            Box(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(143.dp)
                                    .padding(8.dp)
                            ) {
                                val uri = paddedPhotos.getOrNull(index)
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFE9EBF8))
                                ) {
                                    if (uri != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(uri),
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(16.dp)),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Canvas(modifier = Modifier.matchParentSize()) {
                                            drawRoundRect(
                                                color = Color(0xFF2563EB),
                                                size = this.size,
                                                cornerRadius = CornerRadius(16.dp.toPx()),
                                                style = Stroke(
                                                    width = 2.dp.toPx(),
                                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                                )
                                            )
                                        }
                                    }
                                }
                                // Кнопка в правом нижнем углу (+ или ×)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 8.dp, y = 8.dp)
                                        .size(32.dp)
                                        .background(
                                            color = if (uri != null) Color.White else Color(0xFF2563EB),
                                            shape = CircleShape
                                        )
                                        .zIndex(1f)
                                        .clickable {
                                            if (uri != null) {
                                                val newList = photos.toMutableList().apply { removeAt(index) }
                                                userViewModel.setPhotos(newList)
                                            } else {
                                                photoDialogIndex = index
                                                showPhotoDialog = true
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uri != null) Icons.Default.Close else Icons.Default.Add,
                                        contentDescription = if (uri != null) "Remove photo" else "Add photo",
                                        tint = if (uri != null) Color(0xFF2563EB) else Color.White
                                    )
                                }
                            }
                        }
                    }
                    if (row < 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            // Диалог выбора фото
            if (showPhotoDialog && photoDialogIndex in 0..8) {
                AlertDialog(
                    onDismissRequest = { showPhotoDialog = false },
                    title = { Text("Добавить фото") },
                    text = {
                        Column {
                            Button(
                                onClick = {
                                    showPhotoDialog = false
                                    galleryLauncher.launch(
                                        PickVisualMediaRequest(
                                            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) { Text("Галерея") }
                            Button(
                                onClick = {
                                    if (hasCameraPermission) {
                                        createImageUri()?.let { uri ->
                                            cameraImageUri = uri
                                            showPhotoDialog = false
                                            cameraLauncher.launch(uri)
                                        }
                                    } else {
                                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) { Text("Камера") }
                        }
                    },
                    confirmButton = {},
                    dismissButton = {}
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        // Секция "Обо мне"
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Обо мне", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
            Spacer(Modifier.weight(1f))
            Text("+30%", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Spacer(Modifier.height(1.dp))
        OutlinedTextField(
            value = aboutMe,
            onValueChange = { if (it.length <= 500) userViewModel.setAboutMe(it) },
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = { Text("Обо мне", color = Color.Gray) },
            minLines = 2,
            maxLines = 8,
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                containerColor = Color.White,
                unfocusedBorderColor = Color.White,
                focusedBorderColor = Color.White,
                cursorColor = Color(0xFF2563EB)
            ),
            singleLine = false,
            trailingIcon = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(end = 4.dp, bottom = 4.dp),
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("${500 - aboutMe.length}", color = Color.Gray)
                }
            }
        )
        Spacer(Modifier.height(24.dp))
        // Секция "Интересы"
        Text("Интересы", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(1.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .clickable { showInterestsDialog = true }
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (savedInterests.isEmpty()) {
                    Text("Добавить интересы", color = Color.Gray, fontSize = 16.sp)
                } else {
                    Text(savedInterests.joinToString(), color = Color.Black, fontSize = 16.sp, maxLines = 1)
                }
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.Gray, modifier = Modifier.graphicsLayer(rotationZ = 180f))
            }
        }
        Spacer(Modifier.height(24.dp))
        if (showInterestsDialog) {
            InterestModalSheet(
                allCategories = interestCategories,
                initialSelected = savedInterests,
                onDone = {
                    userViewModel.setInterests(it)
                    showInterestsDialog = false
                }
            )
        }
        // --- Секции профиля ---
        Spacer(Modifier.height(24.dp))
        // Цели в отношениях
        Text("Цели в отношениях", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { /* TODO: показать выбор целей */ }
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Я ищу", color = Color.Black, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Text("Пусто", color = Color.Gray, fontSize = 15.sp)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // Твой рост
        Text("Твой рост", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("170см", color = Color.Black, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // Языки
        Text("Языки, которые я знаю", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Английский, Русский", color = Color.Black, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // Больше обо мне
        Text("Больше обо мне", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Знак зодиака", color = Color.Black, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Text("Скорпион", color = Color.Black, fontSize = 15.sp)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Образование", color = Color.Black, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Text("Бакалавриат", color = Color.Black, fontSize = 15.sp)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Стиль жизни
        Text("Стиль жизни", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Питомцы", color = Color.Black, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Text("Кошка", color = Color.Black, fontSize = 15.sp)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Спроси меня
        Text("Спроси меня", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Вечеринки", color = Color.Black, fontSize = 15.sp)
                    Spacer(Modifier.weight(1f))
                    Text("общаюсь, идти как о...", color = Color.Black, fontSize = 15.sp)
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        // Telegram
        Text("Telegram", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("@username", color = Color.Black, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        // Instagram
        Text("Instagram", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("@username", color = Color.Black, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp).graphicsLayer(rotationZ = 270f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewTab(photos: List<android.net.Uri>, userName: String, userAge: Int, interests: List<String>, aboutMe: String, faculty: String) {
    val user = User(
        name = userName,
        age = userAge,
        faculty = faculty
    )
    var currentPhotoIndex by remember { mutableStateOf(0) }
    val photoList = if (photos.isNotEmpty()) photos else listOf<android.net.Uri?>(null)
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        ProfilePreviewCardWithGallery(
            user = user,
            photos = photoList,
            currentPhotoIndex = currentPhotoIndex,
            onPhotoTap = { isRight ->
                if (isRight && currentPhotoIndex < photoList.lastIndex) {
                    currentPhotoIndex++
                } else if (!isRight && currentPhotoIndex > 0) {
                    currentPhotoIndex--
                }
            },
            interests = interests,
            aboutMe = aboutMe
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfilePreviewCardWithGallery(
    user: User,
    photos: List<android.net.Uri?>,
    currentPhotoIndex: Int,
    onPhotoTap: (isRight: Boolean) -> Unit,
    interests: List<String> = emptyList(),
    aboutMe: String = ""
) {
    var showGoalsSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
    ) {
        val photoUri = photos.getOrNull(currentPhotoIndex)
        if (photoUri != null) {
            Image(
                painter = rememberAsyncImagePainter(photoUri),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE9EBF8))
            )
        }
        // Индикаторы фото (поверх изображения)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 16.dp, end = 16.dp, bottom = 4.dp)
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
        // Прозрачные кликабельные области для переключения фото
        Row(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        enabled = currentPhotoIndex > 0,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onPhotoTap(false) }
            )
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        enabled = currentPhotoIndex < photos.lastIndex,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onPhotoTap(true) }
            )
        }
        // Подложка и текст
        Box(
            Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .height(120.dp)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    user.name,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                if (user.name.isNotBlank() && user.age > 0) {
                    Spacer(Modifier.width(8.dp))
                    Text(
                        user.age.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White
                    )
                }
            }
            if (currentPhotoIndex == 0 && user.faculty.isNotBlank()) {
                Text(user.faculty, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }
            // Динамическое отображение "Обо мне" и интересов
            if (aboutMe.isNotBlank()) {
                if (currentPhotoIndex == 0) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            painter = painterResource(id = R.drawable.quote),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp).padding(end = 4.dp)
                        )
                        Text(
                            aboutMe,
                            color = Color.White,
                            fontSize = 15.sp,
                            overflow = TextOverflow.Visible
                        )
                    }
                } else if (currentPhotoIndex == 1 && interests.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        interests.forEach { interest ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF444B5A)
                            ) {
                                Text(
                                    interest,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            } else if (aboutMe.isBlank() && interests.isNotEmpty() && currentPhotoIndex == 0) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interests.forEach { interest ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF444B5A)
                        ) {
                            Text(
                                interest,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InterestModalSheet(
    allCategories: List<Pair<String, List<String>>>,
    initialSelected: List<String> = emptyList(),
    onDone: (List<String>) -> Unit
) {
    var selectedInterests by remember { mutableStateOf(initialSelected.toMutableList()) }
    var searchQuery by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { onDone(selectedInterests) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(Modifier.fillMaxSize()) {
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxSize()
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(0.dp)) {
                    // Верхняя панель
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onDone(selectedInterests) }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть")
                        }
                        Spacer(Modifier.weight(1f))
                        Text("${selectedInterests.size} из 10", color = Color.Gray)
                    }
                    Text(
                        "Интересы",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 20.dp, bottom = 8.dp)
                    )
                    // Выбранные интересы
                    if (selectedInterests.isNotEmpty()) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedInterests.forEach { interest ->
                                AssistChip(
                                    onClick = {
                                        selectedInterests = selectedInterests.toMutableList().apply { remove(interest) }
                                    },
                                    label = { Text(interest) },
                                    trailingIcon = {
                                        Icon(Icons.Default.Close, contentDescription = "Удалить")
                                    },
                                    colors = AssistChipDefaults.assistChipColors(containerColor = Color(0xFFE9EBF8)),
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                    }
                    // Поиск
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        placeholder = { Text("Поиск", color = Color.Gray) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        singleLine = true,
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            containerColor = Color(0xFFF5F7FA),
                            unfocusedBorderColor = Color(0xFFF5F7FA),
                            focusedBorderColor = Color(0xFF2563EB)
                        )
                    )
                    Spacer(Modifier.height(16.dp))
                    // Категории и интересы
                    val filteredCategories = allCategories.map { (cat, interests) ->
                        cat to if (searchQuery.isBlank()) interests else interests.filter { it.contains(searchQuery, ignoreCase = true) }
                    }.filter { it.second.isNotEmpty() }
                    Column(
                        Modifier
                            .weight(1f, fill = true)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 20.dp)
                    ) {
                        filteredCategories.forEach { (category, interests) ->
                            Text(category, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                interests.forEach { interest ->
                                    val selected = selectedInterests.contains(interest)
                                    AssistChip(
                                        onClick = {
                                            if (selected) {
                                                selectedInterests = selectedInterests.toMutableList().apply { remove(interest) }
                                            } else if (selectedInterests.size < 10) {
                                                selectedInterests = selectedInterests.toMutableList().apply { add(interest) }
                                            }
                                        },
                                        label = { Text(interest) },
                                        colors = if (selected) AssistChipDefaults.assistChipColors(containerColor = Color(0xFF2563EB), labelColor = Color.White)
                                            else AssistChipDefaults.assistChipColors(containerColor = Color(0xFFF5F7FA)),
                                        shape = RoundedCornerShape(50)
                                    )
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
} 