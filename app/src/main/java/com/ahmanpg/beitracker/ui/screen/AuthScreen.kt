package com.ahmanpg.beitracker.ui.screen

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ahmanpg.beitracker.ui.components.AppLogo
import com.ahmanpg.beitracker.ui.components.AppLogoText
import com.ahmanpg.beitracker.ui.components.PriceDropChartLoading
import com.ahmanpg.beitracker.ui.theme.*
import com.ahmanpg.beitracker.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun AuthScreen(
    isRegister: Boolean = false,
    onAuthSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var isLogin by remember { mutableStateOf(!isRegister) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                account.idToken?.let { viewModel.signInWithGoogle(it) }
            } catch (e: ApiException) {
                // Handle error
            }
        }
    }

    LaunchedEffect(user) {
        if (user != null) {
            keyboardController?.hide()
            focusManager.clearFocus()
            onAuthSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BeiNavyDark)
    ) {
        // Dynamic Background Elements
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(BeiAccentGreen.copy(alpha = 0.15f), Color.Transparent),
                        radius = size.width * 1.5f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.1f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Premium Logo Container
            Surface(
                color = Color.White.copy(alpha = 0.05f),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp)) {
                    AppLogo(size = 80.dp, cornerRadius = 24.dp, fontSize = 48)
                }
            }
            
            AppLogoText(modifier = Modifier.padding(bottom = 8.dp))

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isLogin) "Welcome Back" else "Create Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isLogin) "Sign in to access your price alerts" else "Join the community of smart shoppers",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Glassy Form Container
            Surface(
                color = Color.White.copy(alpha = 0.03f),
                shape = RoundedCornerShape(32.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AnimatedVisibility(visible = !isLogin) {
                        AuthTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = "FULL NAME",
                            leadingIcon = Icons.Default.Person,
                            imeAction = ImeAction.Next,
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                        )
                    }

                    AuthTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "EMAIL ADDRESS",
                        leadingIcon = Icons.Default.Email,
                        imeAction = ImeAction.Next,
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) })
                    )

                    AuthTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = "PASSWORD",
                        leadingIcon = Icons.Default.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onPasswordToggle = { passwordVisible = !passwordVisible },
                        imeAction = if (isLogin) ImeAction.Done else ImeAction.Next,
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (isLogin) viewModel.signIn(email, password)
                            },
                            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                        )
                    )

                    if (!isLogin) {
                        AuthTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = "CONFIRM PASSWORD",
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            imeAction = ImeAction.Done,
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (password == confirmPassword) {
                                    viewModel.signUp(email, password, name)
                                }
                            })
                        )
                    }

                    if (isLogin) {
                        TextButton(
                            onClick = { 
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (email.isNotBlank()) viewModel.resetPassword(email)
                                else viewModel.clearError()
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Forgot Password?", color = BeiAccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (error != null) {
                        Text(
                            text = error!!,
                            color = if (error!!.contains("sent")) BeiAccentGreen else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            if (isLogin) {
                                viewModel.signIn(email, password)
                            } else {
                                if (password == confirmPassword) {
                                    viewModel.signUp(email, password, name)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = BeiNavyDark
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        if (isLoading) {
                            PriceDropChartLoading(size = 28.dp, color = BeiNavyDark)
                        } else {
                            Text(
                                if (isLogin) "SIGN IN" else "GET STARTED", 
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Social Login - Google
            OutlinedButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(com.ahmanpg.beitracker.R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInLauncher.launch(googleSignInClient.signInIntent)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(24.dp).background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("G", color = Color.Black, fontWeight = FontWeight.Black, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Continue with Google", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            TextButton(onClick = { 
                keyboardController?.hide()
                focusManager.clearFocus()
                isLogin = !isLogin 
                viewModel.clearError()
            }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (isLogin) "New to BeiTracker? " else "Already a member? ",
                        color = Color.White.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (isLogin) "Create Account" else "Sign In",
                        color = BeiAccentGreen,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: (() -> Unit)? = null,
    imeAction: ImeAction = ImeAction.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    Column(modifier = Modifier.padding(vertical = 10.dp)) {
        Text(
            label, 
            color = Color.White.copy(alpha = 0.4f), 
            fontSize = 10.sp, 
            fontWeight = FontWeight.Black, 
            letterSpacing = 1.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(leadingIcon, contentDescription = null, tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(20.dp)) },
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = {
                if (isPassword && onPasswordToggle != null) {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.3f)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.05f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = BeiAccentGreen,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = keyboardActions
        )
    }
}
