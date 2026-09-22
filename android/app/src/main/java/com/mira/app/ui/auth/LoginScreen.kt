package com.mira.app.ui.auth

/**
 * Login screen. Sends the user's email and password to the API, saves the
 * returned token with TokenManager, and passes control to the rooms screen.
 */

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mira.app.R
import com.mira.app.model.LoginRequest
import com.mira.app.network.RetrofitClient
import com.mira.app.ui.theme.Cream
import com.mira.app.ui.theme.Terracotta
import com.mira.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: (token: String) -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Terracotta,
        unfocusedBorderColor = Terracotta.copy(alpha = 0.4f),
        focusedLabelColor = Terracotta,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.mira_logo),
            contentDescription = "MIRA logo",
            modifier = Modifier
                .size(180.dp)
                .padding(bottom = 16.dp)
        )

        Text(
            text = "Welcome",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Terracotta
        )
        Text(
            text = "You're not the only one.",
            fontSize = 17.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = fieldColors,
            modifier = Modifier.fillMaxWidth()
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                errorMessage = null
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in both fields"
                    return@Button
                }
                isLoading = true
                scope.launch {
                    try {
                        val response = RetrofitClient.apiService.login(
                            LoginRequest(email.trim(), password)
                        )
                        if (response.isSuccessful && response.body() != null) {
                            val token = response.body()!!.token
                            com.mira.app.data.TokenManager.getInstance(context).saveToken(token)
                            onLoginSuccess(token)
                        } else {
                            errorMessage = "Invalid email or password"
                        }
                    } catch (e: Exception) {
            android.util.Log.e("MIRA", "Request failed in LoginScreen", e)
                        errorMessage = "Could not reach the server. Check your connection."
                    } finally {
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = Terracotta,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                if (isLoading) "Logging in..." else "Log in",
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register", color = Terracotta)
        }
    }
}
