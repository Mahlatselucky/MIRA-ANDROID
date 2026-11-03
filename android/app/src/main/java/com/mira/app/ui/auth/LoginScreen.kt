package com.mira.app.ui.auth

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

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
    val context = LocalContext.current
    
    val auth = FirebaseAuth.getInstance()
    val credentialManager = CredentialManager.create(context)

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
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.mira_logo),
            contentDescription = "MIRA logo",
            modifier = Modifier
                .size(160.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = stringResource(R.string.login_welcome),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Terracotta,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = stringResource(R.string.login_subtitle),
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 40.dp, top = 8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.login_email)) },
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
            label = { Text(stringResource(R.string.login_password)) },
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
                if (isLoading) stringResource(R.string.login_loading) else stringResource(R.string.login_button),
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                errorMessage = null
                scope.launch {
                    try {
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId("626204447933-gquoanksmdiqjkcf9pvu3s85rp1k38me.apps.googleusercontent.com")
                            .setAutoSelectEnabled(true)
                            .build()

                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(
                            context = context,
                            request = request,
                        )
                        
                        val credential = result.credential
                        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    onLoginSuccess("google_token_${user?.uid}")
                                } else {
                                    errorMessage = "Firebase authentication failed"
                                }
                                isLoading = false
                            }
                        } else {
                            errorMessage = "Google Sign-In failed: Invalid credential"
                            isLoading = false
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("GoogleSignIn", "Error", e)
                        errorMessage = "Google Sign-In cancelled or failed"
                        isLoading = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Continue with Google", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text(stringResource(R.string.login_no_account), color = Terracotta)
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}
