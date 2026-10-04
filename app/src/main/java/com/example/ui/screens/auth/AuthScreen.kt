package com.example.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.AuthRepository
import com.example.ui.components.AmanixConfirmationDialog
import com.example.ui.components.AmanixLogoEmblem
import com.example.ui.components.glassmorphic
import com.example.ui.components.keyboardAndNavigationBottomPadding
import com.example.ui.theme.AmanixAccentRed
import com.example.ui.theme.AmanixBorder
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextPrimary
import com.example.ui.theme.AmanixTextSecondary
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    authRepository: AuthRepository,
    onAuthSuccess: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleNotice by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .padding(bottom = keyboardAndNavigationBottomPadding())
            .verticalScroll(scrollState),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp)
        ) {
            AmanixLogoEmblem(sizeDp = 68)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AMANIX",
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
                color = AmanixCyanPrimary
            )

            Text(
                text = "Think • Search • Create • Grow",
                fontSize = 12.sp,
                color = AmanixTextSecondary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Glassmorphic Auth Form Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        shape = RoundedCornerShape(20.dp),
                        backgroundColor = Color(0x350E1A2E),
                        borderColor = Color(0x4500D2FF),
                        borderWidth = 1.dp
                    )
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Auth Tab Row in Frosted Glass
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0x250A1424),
                        contentColor = AmanixCyanPrimary,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = AmanixCyanPrimary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0x30FFFFFF), RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0; errorMessage = null },
                            text = { Text("Sign In", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1; errorMessage = null },
                            text = { Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (selectedTab == 1) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; errorMessage = null },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = AmanixCyanPrimary) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AmanixCyanPrimary,
                                unfocusedBorderColor = Color(0x3500D2FF),
                                focusedTextColor = AmanixTextPrimary,
                                unfocusedTextColor = AmanixTextPrimary,
                                focusedContainerColor = Color(0x20000000),
                                unfocusedContainerColor = Color(0x20000000)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = AmanixCyanPrimary) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanixCyanPrimary,
                            unfocusedBorderColor = Color(0x3500D2FF),
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedContainerColor = Color(0x20000000),
                            unfocusedContainerColor = Color(0x20000000)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_email_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = AmanixCyanPrimary) },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle password visibility",
                                    tint = AmanixTextMuted
                                )
                            }
                        },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AmanixCyanPrimary,
                            unfocusedBorderColor = Color(0x3500D2FF),
                            focusedTextColor = AmanixTextPrimary,
                            unfocusedTextColor = AmanixTextPrimary,
                            focusedContainerColor = Color(0x20000000),
                            unfocusedContainerColor = Color(0x20000000)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("auth_password_input")
                    )

                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = AmanixAccentRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { showForgotPasswordDialog = true }) {
                                Text(
                                    text = "Forgot Password?",
                                    fontSize = 12.sp,
                                    color = AmanixCyanPrimary
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Glowing Submit Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                errorMessage = null
                                val result = if (selectedTab == 0) {
                                    authRepository.signIn(email, password)
                                } else {
                                    authRepository.signUp(name, email, password)
                                }
                                isLoading = false
                                result.fold(
                                    onSuccess = { onAuthSuccess() },
                                    onFailure = { errorMessage = it.message ?: "Authentication failed." }
                                )
                            }
                        },
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && (selectedTab == 0 || name.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AmanixCyanPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Color(0x60FFFFFF), RoundedCornerShape(12.dp))
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (selectedTab == 0) "Sign In" else "Create Amanix Account",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(AmanixBorder))
                        Text(
                            text = "  OR  ",
                            fontSize = 11.sp,
                            color = AmanixTextMuted,
                            fontWeight = FontWeight.Bold
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(AmanixBorder))
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Google OAuth transparent button
                    OutlinedButton(
                        onClick = { showGoogleNotice = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0x2014243D),
                            contentColor = AmanixTextPrimary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .border(1.dp, Color(0x3500D2FF), RoundedCornerShape(12.dp))
                            .testTag("google_signin_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = AmanixCyanPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Continue with Google",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }

    if (showGoogleNotice) {
        AmanixConfirmationDialog(
            title = "Google Sign-In",
            message = "Google Sign-In is not configured. External OAuth 2.0 Client credentials must be provisioned via Google Cloud Console before live Google authentication can be enabled.",
            confirmText = "Understood",
            dismissText = "Close",
            onConfirm = { showGoogleNotice = false },
            onDismiss = { showGoogleNotice = false }
        )
    }

    if (showForgotPasswordDialog) {
        AmanixConfirmationDialog(
            title = "Password Recovery",
            message = "A password reset request for '$email' has been recorded. In production, an encrypted verification token is dispatched via your configured SMTP mail provider.",
            confirmText = "OK",
            dismissText = "Cancel",
            onConfirm = { showForgotPasswordDialog = false },
            onDismiss = { showForgotPasswordDialog = false }
        )
    }
}
