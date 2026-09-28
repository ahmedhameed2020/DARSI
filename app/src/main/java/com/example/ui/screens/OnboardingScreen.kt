package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TutorSettingsEntity
import com.example.ui.theme.DarsiBackgroundWarm
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSuccessGreen
import com.example.ui.theme.DarsiSuccessGreenBg
import com.example.ui.theme.DarsiSuccessGreenDark
import com.example.ui.theme.DarsiSurfaceVariant

@Composable
fun OnboardingScreen(
    onComplete: (TutorSettingsEntity, openAddStudent: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }
    var tutorName by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableIntStateOf(60) }
    var currency by remember { mutableStateOf("QAR") }
    var selectedLanguage by remember { mutableStateOf("en") }
    val isArabic = selectedLanguage == "ar"

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = DarsiBackgroundWarm
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("onboarding_screen")
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (step) {
                    1 -> {
                        // Screen 1: Welcome + Language
                        Surface(
                            color = DarsiRoyalBlueSubtle,
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = DarsiRoyalBlue,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Darsi | دَرْسي",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic) "مساعدك الشخصي للدروس الخصوصية." else "Your private teaching companion.",
                            fontSize = 15.sp,
                            color = DarsiNavyMuted,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Language Toggle Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { selectedLanguage = "en" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (!isArabic) DarsiRoyalBlue else DarsiSurfaceVariant,
                                    contentColor = if (!isArabic) Color.White else DarsiNavy
                                )
                            ) {
                                Text("English", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                            FilledTonalButton(
                                onClick = { selectedLanguage = "ar" },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = if (isArabic) DarsiRoyalBlue else DarsiSurfaceVariant,
                                    contentColor = if (isArabic) Color.White else DarsiNavy
                                )
                            ) {
                                Text("العربية", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Privacy Reassurance
                        Surface(
                            color = DarsiSuccessGreenBg,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = DarsiSuccessGreen,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = if (isArabic)
                                        "بيانات التدريس تبقى على هذا الجهاز."
                                    else
                                        "Your teaching data stays on this device.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DarsiNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(36.dp))

                        Button(
                            onClick = { step = 2 },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_continue_1")
                        ) {
                            Text(
                                if (isArabic) "متابعة" else "Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    2 -> {
                        // Screen 2: Basic Setup
                        Text(
                            text = if (isArabic) "إعدادات أساسية" else "Basic Setup",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isArabic) "خصّص تجربتك في ثوانٍ معدودة" else "Set up your preferences in seconds",
                            fontSize = 14.sp,
                            color = DarsiNavyMuted
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Tutor Name
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isArabic) "اسم المعلم" else "Tutor name",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarsiNavy
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = tutorName,
                                onValueChange = { tutorName = it },
                                placeholder = {
                                    Text(
                                        if (isArabic) "مثال: أحمد" else "e.g. Ahmed",
                                        color = DarsiNavyMuted
                                    )
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_name_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Default Lesson Duration
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isArabic) "مدة الحصة الافتراضية" else "Default lesson duration",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarsiNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(60, 90, 120).forEach { mins ->
                                    val isSelected = (durationMinutes == mins)
                                    FilledTonalButton(
                                        onClick = { durationMinutes = mins },
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (isSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                                            contentColor = if (isSelected) Color.White else DarsiNavy
                                        )
                                    ) {
                                        Text(
                                            if (isArabic) "$mins د" else "${mins}m",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Currency
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = if (isArabic) "العملة" else "Currency",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarsiNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("QAR", "SAR", "AED", "KWD", "USD").forEach { curr ->
                                    val isSelected = (currency == curr)
                                    FilledTonalButton(
                                        onClick = { currency = curr },
                                        modifier = Modifier.weight(1f).height(44.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = if (isSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                                            contentColor = if (isSelected) Color.White else DarsiNavy
                                        )
                                    ) {
                                        Text(curr, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        Button(
                            onClick = { step = 3 },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_continue_2")
                        ) {
                            Text(
                                if (isArabic) "متابعة" else "Continue",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    3 -> {
                        // Screen 3: Ready
                        Surface(
                            color = DarsiSuccessGreenBg,
                            shape = CircleShape,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = DarsiSuccessGreenDark,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = if (isArabic) "أنت جاهز." else "You're ready.",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isArabic)
                                "ابدأ بتنظيم دروسك ومتابعة طلابك بكل سهولة."
                            else
                                "Start organizing your lessons and students with ease.",
                            fontSize = 14.sp,
                            color = DarsiNavyMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        val finalSettings = TutorSettingsEntity(
                            tutorName = tutorName.ifBlank { if (isArabic) "أحمد" else "Ahmed" },
                            subjects = "Mathematics, English",
                            defaultDurationMinutes = durationMinutes,
                            defaultCurrency = currency,
                            googleCalendarEnabled = false,
                            appLanguage = selectedLanguage,
                            isOnboardingCompleted = true
                        )

                        // Choice 1: Add my first student
                        Button(
                            onClick = { onComplete(finalSettings, true) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_add_first_student_btn")
                        ) {
                            Text(
                                if (isArabic) "إضافة أول طالب" else "Add my first student",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Choice 2: Explore Darsi
                        OutlinedButton(
                            onClick = { onComplete(finalSettings, false) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("onboarding_explore_btn")
                        ) {
                            Text(
                                if (isArabic) "استكشاف دَرْسي" else "Explore Darsi",
                                fontSize = 15.sp,
                                color = DarsiNavy,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
