package com.tensormind.nudge.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensormind.nudge.R
import com.tensormind.nudge.ui.components.PageIndicator
import com.tensormind.nudge.ui.components.SpeechBubble
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
fun SplashScreen3(
    onStartUsingNudge: () -> Unit,
    onFamilyMemberClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("Grandpa Mode") }
    var subtitle by remember { mutableStateOf("Simple. Voice-first. No jargon.") }
    var bubbleText by remember { mutableStateOf("Just\ntell me\nwhat's\nwrong!") }
    var startBtnText by remember { mutableStateOf("Start Using Nudge") }
    var familyMemberText by remember { mutableStateOf("I'm a family member") }

    LaunchedEffect(LanguageManager.currentLanguageCode) {
        if (LanguageManager.currentLanguageCode == "en") {
            title = "Grandpa Mode"
            subtitle = "Simple. Voice-first. No jargon."
            bubbleText = "Just\ntell me\nwhat's\nwrong!"
            startBtnText = "Start Using Nudge"
            familyMemberText = "I'm a family member"
        } else {
            title = LanguageManager.translate("Grandpa Mode")
            subtitle = LanguageManager.translate("Simple. Voice-first. No jargon.")
            bubbleText = LanguageManager.translate("Just tell me what's wrong!").replace(" ", "\n")
            startBtnText = LanguageManager.translate("Start Using Nudge")
            familyMemberText = LanguageManager.translate("I'm a family member")
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
            Spacer(modifier = Modifier.height(20.dp))

            // Grandpa Illustration Container with Speech Bubble
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
            ) {
                // Grandpa Character
                Image(
                    painter = painterResource(id = R.drawable.img_grandpa),
                    contentDescription = "Grandpa Character",
                    modifier = Modifier
                        .size(290.dp)
                        .align(Alignment.BottomCenter)
                )

                // Speech Bubble ("Just tell me what's wrong!")
                SpeechBubble(
                    text = bubbleText,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 28.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Title
            Text(
                text = title,
                color = NavyText,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = subtitle,
                color = SlateSubtitle,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Page Indicator (Dot 3 active)
            PageIndicator(pageCount = 3, currentPage = 2)

            Spacer(modifier = Modifier.height(16.dp))

            // Start Using Nudge Button
            Button(
                onClick = onStartUsingNudge,
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
                        text = startBtnText,
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

            Spacer(modifier = Modifier.height(18.dp))

            // Secondary Action Link: "I'm a family member"
            Text(
                text = familyMemberText,
                color = NudgeBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onFamilyMemberClick() }
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun SplashScreen3Preview() {
    NudgeTheme {
        SplashScreen3(onStartUsingNudge = {}, onFamilyMemberClick = {})
    }
}
