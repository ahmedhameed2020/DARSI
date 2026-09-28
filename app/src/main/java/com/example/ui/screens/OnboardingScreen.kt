package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CalendarMonth
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TutorSettingsEntity
import com.example.ui.components.DarsiCard
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSuccessGreen
import com.example.ui.theme.DarsiSuccessGreenBg
import com.example.ui.theme.DarsiSuccessGreenDark
import com.example.ui.theme.DarsiSurfaceVariant

@Composable
fun OnboardingScreen(
    onComplete: (TutorSettingsEntity, loadDemoData: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableIntStateOf(1) }
    var tutorName by remember { mutableStateOf("") }
    var subjects by remember { mutableStateOf("English, Mathematics") }
    var durationMinutes by remember { mutableIntStateOf(60) }
    var currency by remember { mutableStateOf("QAR") }
    var googleCalendarEnabled by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("en") }
    val isArabic = selectedLanguage == "ar"

    CompositionLocalProvider(
        LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
    ) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("onboarding_screen")
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (step) {
            1 -> {
                // Screen 1: Welcome
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isArabic) "مساعدك الشخصي للدروس الخصوصية." else "Your private teaching companion.",
                    fontSize = 15.sp,
                    color = DarsiNavyMuted,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { selectedLanguage = "en" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (!isArabic) DarsiRoyalBlue else DarsiSurfaceVariant,
                            contentColor = if (!isArabic) Color.White else DarsiNavy
                        )
                    ) { Text("English") }
                    FilledTonalButton(
                        onClick = { selectedLanguage = "ar" },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isArabic) DarsiRoyalBlue else DarsiSurfaceVariant,
                            contentColor = if (isArabic) Color.White else DarsiNavy
                        )
                    ) { Text("العربية") }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = DarsiSuccessGreenBg,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = DarsiSuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "خصوصية كاملة وعمل دون اتصال. لا حسابات ولا سحابة." else "100% offline & private. No accounts, no clouds.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarsiNavy
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { step = 2 },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_continue_1")
                ) {
                    Text(if (isArabic) "متابعة" else "Continue", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            2 -> {
                // Screen 2: Your Name
                Text(
                    text = if (isArabic) "ما اسمك؟" else "What is your name?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "سنستخدم الاسم للترحيب بك داخل التطبيق." else "We will use this to greet you each morning.",
                    fontSize = 13.sp,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = tutorName,
                    onValueChange = { tutorName = it },
                    placeholder = { Text(if (isArabic) "مثال: أحمد" else "e.g. Mr. Tariq or Sara") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_name_input")
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { step = 3 },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_continue_2")
                ) {
                    Text(if (isArabic) "التالي" else "Next", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            3 -> {
                // Screen 3: What do you teach?
                Text(
                    text = if (isArabic) "ماذا تدرّس؟" else "What do you teach?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "أدخل مادة أو أكثر." else "Enter one or more subjects.",
                    fontSize = 13.sp,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(24.dp))
                OutlinedTextField(
                    value = subjects,
                    onValueChange = { subjects = it },
                    placeholder = { Text("e.g. English, Mathematics, Chemistry") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_subjects_input")
                )
                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { step = 4 },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isArabic) "التالي" else "Next", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            4 -> {
                // Screen 4: Lesson Duration & Currency
                Text(
                    text = if (isArabic) "الإعدادات الافتراضية للتدريس" else "Teaching Defaults",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isArabic) "مدة الحصة الافتراضية:" else "Default lesson duration:",
                    fontSize = 13.sp,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(60, 90, 120).forEach { mins ->
                        val isSelected = (durationMinutes == mins)
                        FilledTonalButton(
                            onClick = { durationMinutes = mins },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                                contentColor = if (isSelected) Color.White else DarsiNavy
                            )
                        ) {
                            Text("${mins}m", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (isArabic) "العملة الافتراضية:" else "Default currency:",
                    fontSize = 13.sp,
                    color = DarsiNavyMuted
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
                            modifier = Modifier.weight(1f),
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

                Spacer(modifier = Modifier.height(28.dp))
                Button(
                    onClick = { step = 5 },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isArabic) "التالي" else "Next", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            5 -> {
                // Screen 5: Google Calendar Setup
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = DarsiRoyalBlue,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Google Calendar",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Optionally export your scheduled lessons to Google Calendar under a dedicated 'Darsi' calendar. Local data is always preserved.",
                    fontSize = 13.sp,
                    color = DarsiNavyMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        googleCalendarEnabled = true
                        step = 6
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Enable Google Calendar Export", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = {
                        googleCalendarEnabled = false
                        step = 6
                    }
                ) {
                    Text("Skip for now", color = DarsiNavyMuted, fontSize = 13.sp)
                }
            }

            6 -> {
                // Final: You're ready!
                Surface(
                    color = DarsiSuccessGreenBg,
                    shape = CircleShape,
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = DarsiSuccessGreenDark,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (isArabic) "أنت جاهز." else "You're ready.",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isArabic) "ابدأ بإضافة أول طالب أو جرّب بيانات تجريبية من قطر." else "Start by adding your first student or explore with sample Qatar demo data.",
                    fontSize = 14.sp,
                    color = DarsiNavyMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                val finalSettings = TutorSettingsEntity(
                    tutorName = tutorName.ifEmpty { "Teacher" },
                    subjects = subjects,
                    defaultDurationMinutes = durationMinutes,
                    defaultCurrency = currency,
                    googleCalendarEnabled = googleCalendarEnabled,
                    appLanguage = selectedLanguage,
                    isOnboardingCompleted = true
                )

                Button(
                    onClick = { onComplete(finalSettings, true) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_load_sample_btn")
                ) {
                    Text(if (isArabic) "تجربة بيانات قطرية" else "Explore with Qatar Sample Data", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { onComplete(finalSettings, false) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("onboarding_start_empty_btn")
                ) {
                    Text(if (isArabic) "البدء بجدول فارغ" else "Start with Blank Schedule", fontSize = 14.sp, color = DarsiNavy)
                }
            }
        }
    }
    }
}
