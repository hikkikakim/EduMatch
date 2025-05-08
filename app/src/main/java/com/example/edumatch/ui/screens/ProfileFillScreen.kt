package com.example.edumatch.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Checkbox
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import com.example.edumatch.data.model.UserViewModel
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.zIndex
import androidx.compose.ui.layout.ContentScale
import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.compose.runtime.remember
import java.text.SimpleDateFormat
import java.util.*
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileFillScreen(
    userViewModel: UserViewModel,
    onContinue: (String, LocalDate, String, Boolean, Map<String, String?>?) -> Unit = { _, _, _, _, _ -> },
    onBack: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(1) }
    var fullName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }
    var dateTextFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedGender by remember { mutableStateOf<String?>(null) }
    var showGenderInProfile by remember { mutableStateOf(false) }
    
    // Состояния для выбора образования
    var selectedUniversity by remember { mutableStateOf<String?>(null) }
    var selectedSchool by remember { mutableStateOf<String?>(null) }
    var selectedFaculty by remember { mutableStateOf<String?>(null) }
    var expandedUniversity by remember { mutableStateOf(false) }
    var expandedSchool by remember { mutableStateOf(false) }
    var expandedFaculty by remember { mutableStateOf(false) }

    // Данные для выпадающих списков
    val universities = listOf("Central Asian University")
    val schools = mapOf(
        "Central Asian University" to listOf(
            "Medical School",
            "Engineering School",
            "Dental School",
            "Business School",
            "Hospitality Management & Tourism"
        )
    )
    val faculties = mapOf(
        "Medical School" to listOf("Medicine"),
        "Engineering School" to listOf(
            "Architecture & Design",
            "Computer Science",
            "Biomedical Engineering",
            "Industrial Engineering"
        ),
        "Dental School" to listOf("Doctor of Dental Surgery"),
        "Business School" to listOf(
            "Accounting and Finance",
            "Economics",
            "International Business",
            "Marketing",
            "English Language Teaching and Educational Management (ELTEM)",
            "Public Relations and Media Management (PRMM)",
            "Management"
        ),
        "Hospitality Management & Tourism" to listOf(
            "International Hospitality Management & Tourism(IHM&T)"
        )
    )

    val photos by userViewModel.photos.collectAsState()
    var showPhotoDialog by remember { mutableStateOf(false) }
    var photoDialogIndex by remember { mutableStateOf(-1) }
    val context = LocalContext.current
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Создаем функцию для генерации URI для камеры
    val createImageUri = remember {
        {
            try {
                val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val imageFileName = "JPEG_${timeStamp}_"
                val storageDir = context.cacheDir
                val file = File.createTempFile(imageFileName, ".jpg", storageDir)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Лаунчеры для выбора фото
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newPhotos = (photos + uris).distinct().take(9)
            userViewModel.setPhotos(newPhotos)
        }
        showPhotoDialog = false
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        val localUri = cameraImageUri
        if (success && localUri != null && photoDialogIndex in 0..8) {
            val newPhotos = photos.toMutableList().also { it.add(localUri) }
            userViewModel.setPhotos(newPhotos)
        }
        showPhotoDialog = false
    }
    
    // Проверяем разрешение на камеру
    val hasCameraPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }
    
    // Лаунчер для запроса разрешения на камеру
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = RequestPermission()
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

    val progress = when (currentStep) {
        1 -> 0.2f
        2 -> 0.4f
        3 -> 0.6f
        4 -> 0.8f
        else -> 1f
    }

    val primaryColor = Color(0xFF3A5BA0)
    val gradientColors = listOf(Color(0xFFFF4B6B), Color(0xFFFF8754))

    // Получаем только первое слово из имени
    val firstName = fullName.split(" ").firstOrNull() ?: ""

    val maxPhotos = 9
    val paddedPhotos: MutableList<Uri?> = photos.map { it as Uri? }.toMutableList().apply {
        while (size < maxPhotos) add(null)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val newPhotos = (photos + uris).take(maxPhotos)
            uris.forEach { uri ->
                userViewModel.uploadPhotoToStorageAndSave(uri) { success ->
                    // Можно обработать успех/ошибку
                }
            }
            userViewModel.setPhotos(newPhotos)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Фиксированная часть с заголовком
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                Box(Modifier.fillMaxWidth()) {
                    LinearProgressIndicator(
                        progress = progress,
                        color = primaryColor,
                        trackColor = Color(0xFFE0E0E0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { 
                            currentStep = (currentStep - 1).coerceAtLeast(1)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = primaryColor
                        )
                    }
                }

                when (currentStep) {
                    1 -> {
                        Text(
                            text = "What is your name?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        TextField(
                            value = fullName,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty()) {
                                    fullName = ""
                                } else {
                                    // Каждое слово с заглавной буквы
                                    fullName = newValue.split(" ").joinToString(" ") { part ->
                                        part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                    }
                                }
                                error = null
                            },
                            placeholder = { Text("Enter your name", color = Color(0xFFB0B0B0)) },
                            isError = error != null,
                            supportingText = error?.let { { Text(it) } },
                            colors = TextFieldDefaults.colors(
                                unfocusedIndicatorColor = Color.Gray,
                                focusedIndicatorColor = primaryColor,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 24.dp, end = 24.dp)
                                .background(Color.Transparent)
                        )

                        Text(
                            text = "This is how your name will appear in your profile. You can't change it.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
                        )
                    }
                    2 -> {
                        Text(
                            text = "When is your birthday?",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            TextField(
                                value = dateTextFieldValue,
                                onValueChange = { newValue ->
                                    // Получаем только цифры из введенного текста
                                    val newDigits = newValue.text.filter { it.isDigit() }
                                    if (newDigits.length <= 8) {
                                        // Форматируем текст со слешами
                                        val formattedText = buildString {
                                            newDigits.forEachIndexed { index, char ->
                                                if (index == 2 || index == 4) append('/')
                                                append(char)
                                            }
                                        }

                                        // Вычисляем новую позицию курсора
                                        val oldDigitsBeforeCursor = dateTextFieldValue.text
                                            .take(dateTextFieldValue.selection.start)
                                            .count { it.isDigit() }
                                        val newCursorDigitPosition = minOf(
                                            oldDigitsBeforeCursor + 
                                            (newValue.text.length - dateTextFieldValue.text.length),
                                            newDigits.length
                                        )
                                        
                                        // Преобразуем позицию в цифрах в позицию в отформатированном тексте
                                        val newCursorPosition = newCursorDigitPosition + 
                                            (if (newCursorDigitPosition > 4) 2 else if (newCursorDigitPosition > 2) 1 else 0)

                                        dateTextFieldValue = TextFieldValue(
                                            text = formattedText,
                                            selection = TextRange(newCursorPosition)
                                        )

                                        // Проверяем валидность даты
                                        if (newDigits.length == 8) {
                                            try {
                                                val day = newDigits.substring(0, 2).toInt()
                                                val month = newDigits.substring(2, 4).toInt()
                                                val year = newDigits.substring(4, 8).toInt()
                                                
                                                selectedDate = LocalDate.of(year, month, day)
                                                dateError = null
                                            } catch (e: Exception) {
                                                selectedDate = null
                                                dateError = "Invalid date"
                                            }
                                        } else {
                                            selectedDate = null
                                            dateError = null
                                        }
                                    }
                                },
                                placeholder = { 
                                    Text(
                                        "DD/MM/YYYY",
                                        color = Color(0xFFB0B0B0),
                                        fontSize = 20.sp,
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 20.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number
                                ),
                                isError = dateError != null,
                                supportingText = dateError?.let { { Text(it) } },
                                colors = TextFieldDefaults.colors(
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .background(Color.Transparent)
                            )
                        }

                        Text(
                            text = "Your profile shows your age, not your birthday.",
                            color = Color(0xFF6B6B6B),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 8.dp)
                        )
                    }
                    3 -> {
                        Text(
                            text = "Select gender",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        val genderOptions = listOf("Woman", "Man")
                        genderOptions.forEach { gender ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 8.dp)
                                    .selectable(
                                        selected = selectedGender == gender,
                                        onClick = { selectedGender = gender }
                                    ),
                                shape = RoundedCornerShape(100.dp),
                                color = Color.Transparent,
                                border = BorderStroke(2.dp, if (selectedGender == gender) primaryColor else Color.Gray)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = gender,
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = showGenderInProfile,
                                onCheckedChange = { showGenderInProfile = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Show my gender in the profile",
                                fontSize = 14.sp
                            )
                        }
                    }
                    4 -> {
                        Text(
                            text = "Select your education",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // University dropdown
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                        ) {
                            Text(
                                text = "University",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expandedUniversity = true }
                            ) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color.Gray)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = selectedUniversity ?: "Select university",
                                            color = if (selectedUniversity == null) Color.Gray else Color.Black
                                        )
                                        Icon(
                                            imageVector = if (expandedUniversity) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null
                                        )
                                    }
                                }
                                
                                DropdownMenu(
                                    expanded = expandedUniversity,
                                    onDismissRequest = { expandedUniversity = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(Color.White)
                                        .padding(vertical = 8.dp)
                                ) {
                                    universities.forEach { university ->
                                        DropdownMenuItem(
                                            text = { 
                                                Text(
                                                    university,
                                                    fontSize = 16.sp,
                                                    color = if (selectedUniversity == university) primaryColor else Color.Black
                                                ) 
                                            },
                                            onClick = {
                                                selectedUniversity = university
                                                selectedSchool = null
                                                selectedFaculty = null
                                                expandedUniversity = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // School dropdown
                        if (selectedUniversity != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            ) {
                                Text(
                                    text = "School",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedSchool = true }
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color.Gray)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedSchool ?: "Select school",
                                                color = if (selectedSchool == null) Color.Gray else Color.Black
                                            )
                                            Icon(
                                                imageVector = if (expandedSchool) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    
                                    DropdownMenu(
                                        expanded = expandedSchool,
                                        onDismissRequest = { expandedSchool = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .background(Color.White)
                                            .padding(vertical = 8.dp)
                                    ) {
                                        schools[selectedUniversity]?.forEach { school ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        school,
                                                        fontSize = 16.sp,
                                                        color = if (selectedSchool == school) primaryColor else Color.Black
                                                    ) 
                                                },
                                                onClick = {
                                                    selectedSchool = school
                                                    selectedFaculty = null
                                                    expandedSchool = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Faculty dropdown
                        if (selectedSchool != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                            ) {
                                Text(
                                    text = "Faculty",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedFaculty = true }
                                ) {
                                    Surface(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(8.dp),
                                        border = BorderStroke(1.dp, Color.Gray)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedFaculty ?: "Select faculty",
                                                color = if (selectedFaculty == null) Color.Gray else Color.Black
                                            )
                                            Icon(
                                                imageVector = if (expandedFaculty) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                contentDescription = null
                                            )
                                        }
                                    }
                                    
                                    DropdownMenu(
                                        expanded = expandedFaculty,
                                        onDismissRequest = { expandedFaculty = false },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .background(Color.White)
                                            .padding(vertical = 8.dp)
                                    ) {
                                        faculties[selectedSchool]?.forEach { faculty ->
                                            DropdownMenuItem(
                                                text = { 
                                                    Text(
                                                        faculty,
                                                        fontSize = 16.sp,
                                                        color = if (selectedFaculty == faculty) primaryColor else Color.Black
                                                    ) 
                                                },
                                                onClick = {
                                                    selectedFaculty = faculty
                                                    expandedFaculty = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    5 -> {
                        Text(
                            text = "Add photos",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Text(
                            text = "Add at least 2 photos to continue",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // Сетка 3x3
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            for (row in 0 until 3) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    for (col in 0 until 3) {
                                        val index = row * 3 + col
                                        val uri = paddedPhotos.getOrNull(index)
                                        Box(
                                            modifier = Modifier
                                                .width(110.dp)
                                                .height(143.dp)
                                                .padding(8.dp)
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
                                                Canvas(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .clip(RoundedCornerShape(16.dp))
                                                ) {
                                                    drawRoundRect(
                                                        color = Color(0xFF2563EB),
                                                        size = size,
                                                        cornerRadius = CornerRadius(16.dp.toPx()),
                                                        style = Stroke(
                                                            width = 2.dp.toPx(),
                                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                                                        )
                                                    )
                                                }
                                            }
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
                                                            val newPhotos = photos.toMutableList().apply { removeAt(index) }
                                                            // Сдвиг фото влево
                                                            val shifted: MutableList<Uri?> = newPhotos.filterNotNull().toMutableList()
                                                            while (shifted.size < maxPhotos) shifted.add(null)
                                                            userViewModel.setPhotos(shifted.filterNotNull())
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
                        // Обновляем диалог выбора фото
                        if (showPhotoDialog && photoDialogIndex in 0..8) {
                            AlertDialog(
                                onDismissRequest = { showPhotoDialog = false },
                                title = { Text("Add photo") },
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
                                        ) { 
                                            Text("Gallery") 
                                        }
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
                                        ) { 
                                            Text("Camera") 
                                        }
                                    }
                                },
                                confirmButton = {},
                                dismissButton = {}
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }

        // Button at the bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .windowInsetsPadding(WindowInsets.ime)
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            val photoCount = photos.count { it != null }
            Button(
                onClick = {
                    when (currentStep) {
                        1 -> {
                            if (fullName.isBlank()) {
                                error = "Name is required"
                            } else {
                                currentStep = 2
                                error = null
                            }
                        }
                        2 -> {
                            if (selectedDate == null) {
                                dateError = "Please enter a valid date"
                            } else {
                                currentStep = 3
                                dateError = null
                            }
                        }
                        3 -> {
                            if (selectedGender == null) {
                                return@Button
                            }
                            currentStep = 4
                        }
                        4 -> {
                            if (selectedUniversity == null || selectedSchool == null || selectedFaculty == null) {
                                return@Button
                            }
                            // После выбора образования переходим к загрузке фото
                            currentStep = 5
                        }
                        5 -> {
                            if (photoCount >= 2) {
                                val uris = photos.filterNotNull()
                                uris.forEach { uri ->
                                    userViewModel.uploadPhotoToStorageAndSave(uri) { success ->
                                        // Можно обработать успех/ошибку
                                    }
                                }
                                userViewModel.setUserName(fullName)
                                userViewModel.setUserBirthDate(selectedDate!!)
                                onContinue(
                                    fullName,
                                    selectedDate!!,
                                    selectedGender!!,
                                    showGenderInProfile,
                                    mapOf(
                                        "university" to selectedUniversity,
                                        "school" to selectedSchool,
                                        "faculty" to selectedFaculty
                                    )
                                )
                            }
                        }
                    }
                },
                enabled = currentStep != 5 || photoCount >= 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp)),
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
            ) {
                Text(
                    text = if (currentStep == 5) "CONTINUE" else "Continue",
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }

    // Диалог запроса разрешения на камеру
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Camera Permission Required") },
            text = { Text("To take photos, please grant access to the camera.") },
            confirmButton = {
                Button(onClick = {
                    showPermissionDialog = false
                    requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                }) {
                    Text("Grant Access")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
} 