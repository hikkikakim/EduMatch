package com.example.edumatch.data.model

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream
import java.util.UUID
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.example.edumatch.data.repository.UserRepositoryImpl
import com.example.edumatch.data.model.User
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import coil.Coil
import coil.request.ImageRequest
import coil.size.Size

class UserViewModel : ViewModel() {
    private lateinit var context: Context
    private val TAG = "UserViewModel"
    private val storage = Firebase.storage
    
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName

    private val _userBirthDate = MutableStateFlow<LocalDate?>(null)
    val userBirthDate: StateFlow<LocalDate?> = _userBirthDate

    private val _photos = MutableStateFlow<List<Uri>>(emptyList())
    val photos: StateFlow<List<Uri>> = _photos

    private val _interests = MutableStateFlow<List<String>>(emptyList())
    val interests: StateFlow<List<String>> = _interests

    private val _aboutMe = MutableStateFlow("")
    val aboutMe: StateFlow<String> = _aboutMe

    private val _faculty = MutableStateFlow("")
    val faculty: StateFlow<String> = _faculty

    private val _allUsers = MutableStateFlow<List<User>>(emptyList())
    val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val userRepository = UserRepositoryImpl()

    fun initialize(context: Context) {
        this.context = context
        loadUserDataFromFirebase()
        loadAllUsers()
    }

    // --- Firebase ---
    fun saveUserDataToFirebase() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference
        val userMap = mapOf(
            "id" to userId,
            "name" to _userName.value,
            "age" to (_userBirthDate.value?.let { calculateAge(it) } ?: 18),
            "faculty" to _faculty.value,
            "major" to "",
            "interests" to _interests.value,
            "description" to _aboutMe.value,
            "photos" to _photos.value.map { it.toString() }
        )
        db.child("users").child(userId).setValue(userMap)
    }

    fun loadUserDataFromFirebase() {
        viewModelScope.launch {
            try {
                userRepository.getCurrentUser().collect { user ->
                    user?.let {
                        _userName.value = it.name
                        _aboutMe.value = it.description
                        _interests.value = it.interests
                        _faculty.value = it.faculty
                        
                        // Загружаем фотографии с предварительной загрузкой
                        val photoUris = it.photos.mapNotNull { photoUrl ->
                            try {
                                val uri = Uri.parse(photoUrl)
                                // Предварительная загрузка фото
                                preloadPhoto(uri)
                                uri
                            } catch (e: Exception) {
                                Log.e(TAG, "Ошибка создания URI для фото: $photoUrl")
                                null
                            }
                        }
                        _photos.value = photoUris
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading user data: ${e.message}")
                _error.value = "Ошибка загрузки данных пользователя"
            }
        }
    }

    private fun preloadPhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val imageLoader = Coil.imageLoader(context)
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .size(Size.ORIGINAL)
                    .build()
                imageLoader.execute(request)
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка предварительной загрузки фото: ${e.message}")
            }
        }
    }

    fun setUserName(name: String) {
        _userName.value = name
        saveUserDataToFirebase()
    }

    fun setUserBirthDate(date: LocalDate) {
        _userBirthDate.value = date
        saveUserDataToFirebase()
    }

    fun setPhotos(photos: List<Uri>) {
        _photos.value = photos
        saveUserDataToFirebase()
    }

    fun setInterests(interests: List<String>) {
        _interests.value = interests
        saveUserDataToFirebase()
    }

    fun setAboutMe(text: String) {
        _aboutMe.value = text
        saveUserDataToFirebase()
    }

    fun setFaculty(faculty: String) {
        _faculty.value = faculty
        saveUserDataToFirebase()
    }

    suspend fun addPhoto(uri: Uri): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext false
                val photoRef = storage.reference.child("user_photos/$userId/${System.currentTimeMillis()}.jpg")
                
                // Загружаем фото в Firebase Storage
                photoRef.putFile(uri).await()
                
                // Получаем URL загруженного фото
                val downloadUrl = photoRef.downloadUrl.await()
                
                // Обновляем список фотографий
                val currentPhotos = _photos.value.toMutableList()
                currentPhotos.add(downloadUrl)
                _photos.value = currentPhotos

                // Сохраняем в базу данных
                saveUserDataToFirebase()

                true
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка загрузки фото: ${e.message}")
                false
            }
        }
    }

    suspend fun deletePhoto(photoUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@withContext
                
                // Получаем путь к файлу в Storage
                val photoPath = photoUri.lastPathSegment
                if (photoPath != null) {
                    val photoRef = storage.reference.child("user_photos/$userId/$photoPath")
                    // Удаляем файл из Storage
                    photoRef.delete().await()
                }

                // Удаляем URI из списка
                val currentPhotos = _photos.value.toMutableList()
                currentPhotos.remove(photoUri)
                _photos.value = currentPhotos

                // Обновляем базу данных
                saveUserDataToFirebase()
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка удаления фото: ${e.message}")
            }
        }
    }

    fun clearPhotos() {
        // Удаляем все файлы
        val photoDir = File(context.filesDir, "photos")
        if (photoDir.exists()) {
            photoDir.listFiles()?.forEach { it.delete() }
        }
        
        // Очищаем список
        _photos.value = emptyList()
        
        // Очищаем SharedPreferences
        saveUserDataToFirebase()
    }

    fun clearAllUserData() {
        _userName.value = ""
        _userBirthDate.value = null
        _photos.value = emptyList()
        _interests.value = emptyList()
        _aboutMe.value = ""
        _faculty.value = ""
        _allUsers.value = emptyList()
        _error.value = null
        // Сброс всех пользовательских данных при выходе или смене аккаунта
    }

    fun loadAllUsers() {
        viewModelScope.launch {
            try {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                userRepository.getAllUsers().collect { users ->
                    _allUsers.value = users.filter { it.id != currentUserId }.shuffled()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading all users: ${e.message}")
                _error.value = "Ошибка загрузки пользователей"
            }
        }
    }

    private suspend fun getCurrentUser(): User? {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                _error.value = "Пользователь не авторизован"
                null
            } else {
                userRepository.getUserById(currentUser.uid)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current user: ${e.message}")
            _error.value = "Ошибка получения данных пользователя"
            null
        }
    }

    private fun calculateAge(birthDate: LocalDate): Int {
        return LocalDate.now().year - birthDate.year
    }

    fun clearError() {
        _error.value = null
    }

    fun uploadPhotoToStorageAndSave(uri: Uri, onResult: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val storageRef = FirebaseStorage.getInstance().reference
        val photoRef = storageRef.child("user_photos/$userId/${System.currentTimeMillis()}.jpg")
        photoRef.putFile(uri)
            .addOnSuccessListener {
                photoRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    // Добавляем ссылку в профиль пользователя
                    val currentPhotos = _photos.value.map { it.toString() }.toMutableList()
                    currentPhotos.add(downloadUrl.toString())
                    // Сохраняем в базу
                    val db = FirebaseDatabase.getInstance().reference
                    db.child("users").child(userId).child("photos").setValue(currentPhotos)
                    // Обновляем локально
                    _photos.value = currentPhotos.map { Uri.parse(it) }
                    onResult(true)
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    private suspend fun cachePhoto(uri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                val photoDir = File(context.cacheDir, "photos")
                if (!photoDir.exists()) {
                    photoDir.mkdirs()
                }

                val fileName = uri.lastPathSegment ?: UUID.randomUUID().toString()
                val cacheFile = File(photoDir, fileName)

                if (!cacheFile.exists()) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val outputStream = FileOutputStream(cacheFile)
                    inputStream?.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Ошибка кэширования фото: ${e.message}")
            }
        }
    }
}