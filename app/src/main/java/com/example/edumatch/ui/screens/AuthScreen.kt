package com.example.edumatch.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import com.example.edumatch.R
import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLogin: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    val auth = remember { FirebaseAuth.getInstance() }
    var showLogin by remember { mutableStateOf(false) }
    var showSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPassword by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    // Тестовые данные
    val validEmail = "test@centralasian.uz"
    val validPassword = "password123"
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3A5BA0), Color(0xFF0A1D4E))
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(120.dp))
            Text(
                text = "EduMatch",
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // Кнопки внизу
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp, start = 32.dp, end = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!showLogin && !showSignUp) {
                Button(
                    onClick = { showLogin = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("LOG IN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = { showSignUp = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("SIGN UP", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email", color = Color.White) },
                    singleLine = true,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = Color.White) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        containerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = if (showSignUp) 16.dp else 32.dp)
                )
                if (showSignUp) {
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password", color = Color.White) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            containerColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 32.dp)
                    )
                }
                if (error.isNotEmpty()) {
                    Text(error, color = Color.Red, modifier = Modifier.padding(bottom = 8.dp))
                }
                Button(
                    onClick = {
                        if (showLogin) {
                            if (email.isBlank() || password.isBlank()) {
                                error = "Please enter email and password"
                            } else if (!email.endsWith("@centralasian.uz")) {
                                error = "Email must be in domain @centralasian.uz"
                            } else {
                                loading = true
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        loading = false
                                        if (task.isSuccessful) {
                                            error = ""
                                            onLogin()
                                        } else {
                                            val msg = task.exception?.localizedMessage ?: "Login failed"
                                            error = when {
                                                msg.contains("The supplied auth credential is incorrect, malformed or has expired", ignoreCase = true) ->
                                                    "There is no user with this email, please sign up."
                                                else -> msg
                                            }
                                        }
                                    }
                            }
                        } else if (showSignUp) {
                            if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                                error = "Please fill all fields"
                            } else if (!email.endsWith("@centralasian.uz")) {
                                error = "Email must be in domain @centralasian.uz"
                            } else if (password != confirmPassword) {
                                error = "Passwords do not match"
                            } else {
                                loading = true
                                auth.fetchSignInMethodsForEmail(email)
                                    .addOnCompleteListener { checkTask ->
                                        if (checkTask.isSuccessful) {
                                            val methods = checkTask.result?.signInMethods
                                            if (!methods.isNullOrEmpty()) {
                                                loading = false
                                                val msg = "The email address is already in use by another account."
                                                error = "The email address is already in use."
                                            } else {
                                                auth.createUserWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener { regTask ->
                                                        loading = false
                                                        if (regTask.isSuccessful) {
                                                            error = ""
                                                            onSignUp()
                                                        } else {
                                                            val msg = regTask.exception?.localizedMessage ?: "Registration failed"
                                                            error = when {
                                                                msg.contains("The email address is already in use by another account.", ignoreCase = true) ->
                                                                    "The email address is already in use."
                                                                else -> msg
                                                            }
                                                        }
                                                    }
                                            }
                                        } else {
                                            loading = false
                                            error = checkTask.exception?.localizedMessage ?: "Registration failed"
                                        }
                                    }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .border(1.dp, Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(32.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    if (loading) {
                        Spacer(modifier = Modifier.height(8.dp))
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    } else {
                        Text(if (showLogin) "LOG IN" else "SIGN UP", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = {
                    showLogin = false
                    showSignUp = false
                    email = ""
                    password = ""
                    confirmPassword = ""
                    error = ""
                }) {
                    Text("Back", color = Color.White)
                }
            }
        }
    }
} 