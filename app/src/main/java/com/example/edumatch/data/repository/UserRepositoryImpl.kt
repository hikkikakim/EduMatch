package com.example.edumatch.data.repository

import android.util.Log
import com.example.edumatch.data.model.User
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class UserRepositoryImpl : UserRepository {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val TAG = "UserRepositoryImpl"

    override fun getCurrentUser(): Flow<User?> = callbackFlow {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(User::class.java)
                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching current user: ${error.message}")
                trySend(null)
            }
        }

        usersRef.child(currentUser.uid).addValueEventListener(listener)
        awaitClose { usersRef.child(currentUser.uid).removeEventListener(listener) }
    }

    override suspend fun createUser(user: User) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                throw IllegalStateException("User must be authenticated to create profile")
            }
            
            val userWithId = user.copy(id = currentUser.uid)
            usersRef.child(currentUser.uid).setValue(userWithId).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user: ${e.message}")
            throw e
        }
    }

    override suspend fun updateUser(user: User) {
        try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                throw IllegalStateException("User must be authenticated to update profile")
            }
            
            if (user.id != currentUser.uid) {
                throw IllegalStateException("User can only update their own profile")
            }
            
            usersRef.child(user.id).setValue(user).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user: ${e.message}")
            throw e
        }
    }

    override suspend fun getUserById(id: String): User? {
        return try {
            val snapshot = usersRef.child(id).get().await()
            snapshot.getValue(User::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user by id: ${e.message}")
            null
        }
    }

    override fun getUsersFiltered(faculty: String?, major: String?, interests: List<String>?): Flow<List<User>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = snapshot.children.mapNotNull { it.getValue(User::class.java) }
                    .filter { user ->
                        val matchesFaculty = faculty == null || user.faculty == faculty
                        val matchesMajor = major == null || user.major == major
                        val matchesInterests = interests == null || 
                            interests.isEmpty() || 
                            user.interests.any { it in interests }
                        
                        matchesFaculty && matchesMajor && matchesInterests
                    }
                trySend(users)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error fetching filtered users: ${error.message}")
                trySend(emptyList())
            }
        }

        usersRef.addValueEventListener(listener)
        awaitClose { usersRef.removeEventListener(listener) }
    }

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    Log.d(TAG, "Получены данные пользователей: ${snapshot.childrenCount} записей")
                    val users = snapshot.children.mapNotNull { 
                        val user = it.getValue(User::class.java)
                        if (user == null) {
                            Log.e(TAG, "Не удалось преобразовать данные пользователя: ${it.key}")
                        }
                        user
                    }
                    Log.d(TAG, "Успешно преобразовано пользователей: ${users.size}")
                    trySend(users)
                } catch (e: Exception) {
                    Log.e(TAG, "Ошибка при парсинге пользователей: ${e.message}")
                    trySend(emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Ошибка при получении пользователей: ${error.message}")
                trySend(emptyList())
            }
        }

        usersRef.addValueEventListener(listener)
        awaitClose { usersRef.removeEventListener(listener) }
    }
} 
 