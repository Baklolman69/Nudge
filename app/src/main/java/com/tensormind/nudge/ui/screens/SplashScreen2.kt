package com.tensormind.nudge.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.R
import com.tensormind.nudge.ui.components.FeatureCard
import com.tensormind.nudge.ui.components.PageIndicator
import com.tensormind.nudge.ui.theme.BgGradientEnd
import com.tensormind.nudge.ui.theme.BgGradientMid
import com.tensormind.nudge.ui.theme.BgGradientStart
import com.tensormind.nudge.ui.theme.NavyText
import com.tensormind.nudge.ui.theme.NudgeBlue
import com.tensormind.nudge.ui.theme.NudgeTheme
import com.tensormind.nudge.ui.theme.SlateSubtitle

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tensormind.nudge.ai.LanguageManager

@Composable
fun SplashScreen2(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("For a simpler\ndigital life") }
    var subtitle by remember { mutableStateOf("We help you fix problems, spot scams, and keep your phone running smoothly.") }
    var f1Title by remember { mutableStateOf("Solve phone issues") }
    var f1Desc by remember { mutableStateOf("Sound, internet, settings & more") }
    var f2Title by remember { mutableStateOf("Stay safe from scams") }
    var f2Desc by remember { mutableStateOf("Check messages and links") }
    var f3Title by remember { mutableStateOf("Keep your family close") }
    var f3Desc by remember { mutableStateOf("Get help when you need it") }
    var getStartedText by remember { mutableStateOf("Get Started") }

    LaunchedEffect(LanguageManager.currentLanguageCode) {
        if (LanguageManager.currentLanguageCode == "en") {
            title = "For a simpler\ndigital life"
            subtitle = "We help you fix problems, spot scams, and keep your phone running smoothly."
            f1Title = "Solve phone issues"
            f1Desc = "Sound, internet, settings & more"
            f2Title = "Stay safe from scams"
            f2Desc = "Check messages and links"
            f3Title = "Keep your family close"
            f3Desc = "Get help when you need it"
            getStartedText = "Get Started"
        } else {
            title = LanguageManager.translate("For a simpler digital life")
            subtitle = LanguageManager.translate("We help you fix problems, spot scams, and keep your phone running smoothly.")
            f1Title = LanguageManager.translate("Solve phone issues")
            f1Desc = LanguageManager.translate("Sound, internet, settings & more")
            f2Title = LanguageManager.translate("Stay safe from scams")
            f2Desc = LanguageManager.translate("Check messages and links")
            f3Title = LanguageManager.translate("Keep your family close")
            f3Desc = LanguageManager.translate("Get help when you need it")
            getStartedText = LanguageManager.translate("Get Started")
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(BgGradientStart, BgGradientMid, BgGradientEnd)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Main Title
            Text(
                text = title,
                color = NavyText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 38.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle
            Text(
                text = subtitle,
                color = SlateSubtitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 22.sp,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mascot with Heart Bubble
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_mascot_nudge),
                    contentDescription = "Mascot",
                    modifier = Modifier.size(140.dp)
                )

                // Heart speech bubble floating at top right
                Image(
                    painter = painterResource(id = R.drawable.ic_heart_bubble),
                    contentDescription = "Heart",
                    modifier = Modifier
                        .size(38.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = (-10).dp, y = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Feature Cards
            FeatureCard(
                iconRes = R.drawable.ic_shield_green,
                title = f1Title,
                description = f1Desc
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureCard(
                iconRes = R.drawable.ic_shield_purple,
                title = f2Title,
                description = f2Desc
            )

            Spacer(modifier = Modifier.height(10.dp))

            FeatureCard(
                iconRes = R.drawable.ic_family_orange,
                title = f3Title,
                description = f3Desc
            )

            Spacer(modifier = Modifier.weight(1f))

            // Page Indicator (Dot 2 active)
            PageIndicator(pageCount = 3, currentPage = 1)

            Spacer(modifier = Modifier.height(8.dp))

            // Get Started Button
            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NudgeBlue)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = getStartedText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_right),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun SplashScreen2Preview() {
    NudgeTheme {
        SplashScreen2(onGetStarted = {})
    }
}
