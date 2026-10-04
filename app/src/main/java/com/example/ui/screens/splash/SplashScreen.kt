package com.example.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.i18n.LocaleManager
import com.example.data.repository.AuthRepository
import com.example.ui.components.AmanixLogoEmblem
import com.example.ui.components.glassmorphic
import com.example.ui.theme.AmanixCyanPrimary
import com.example.ui.theme.AmanixTextMuted
import com.example.ui.theme.AmanixTextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authRepository: AuthRepository,
    onNavigateToHome: () -> Unit,
    onNavigateToAuth: () -> Unit
) {
    val scale = remember { Animatable(0.85f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1.0f, animationSpec = tween(700))
        alpha.animateTo(1.0f, animationSpec = tween(600))
        delay(600)

        val activeUser = authRepository.checkSession()
        if (activeUser != null) {
            onNavigateToHome()
        } else {
            onNavigateToAuth()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
                .glassmorphic(
                    shape = RoundedCornerShape(28.dp),
                    backgroundColor = Color(0x350F1D36),
                    borderColor = Color(0x5500D2FF),
                    borderWidth = 1.2.dp
                )
                .padding(horizontal = 36.dp, vertical = 44.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AmanixLogoEmblem(
                    sizeDp = 92
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "AMANIX",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 6.sp,
                    color = AmanixCyanPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = LocaleManager.tr("app_tagline"),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    color = AmanixTextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Autonomous Intelligence Architecture",
                    fontSize = 11.sp,
                    color = AmanixTextMuted
                )
            }
        }
    }
}
