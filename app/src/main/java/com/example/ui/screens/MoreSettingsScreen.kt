package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.DarsiDatabase
import com.example.data.local.entity.TutorSettingsEntity
import com.example.ui.components.DarsiCard
import com.example.ui.theme.DarsiAmber
import com.example.ui.theme.DarsiBorder
import com.example.ui.theme.DarsiCoralRed
import com.example.ui.theme.DarsiCoralRedBg
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSuccessGreen
import com.example.ui.theme.DarsiSuccessGreenBg
import com.example.ui.theme.DarsiSuccessGreenDark
import com.example.ui.theme.DarsiSurfaceVariant
import com.example.util.BackupHelper
import kotlinx.coroutines.launch

@Composable
fun MoreSettingsScreen(
    settings: TutorSettingsEntity?,
    onSaveSettings: (TutorSettingsEntity) -> Unit,
    onLoadDemoData: () -> Unit,
    onClearAllData: () -> Unit,
    language: String = "en",
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentSettings = settings ?: TutorSettingsEntity()

    var tutorName by remember(currentSettings.tutorName) { mutableStateOf(currentSettings.tutorName) }
    var subjects by remember(currentSettings.subjects) { mutableStateOf(currentSettings.subjects) }
    var selectedCurrency by remember(currentSettings.defaultCurrency) { mutableStateOf(currentSettings.defaultCurrency) }
    var defaultDuration by remember(currentSettings.defaultDurationMinutes) { mutableStateOf(currentSettings.defaultDurationMinutes) }
    var travelBufferMinutes by remember(currentSettings.defaultTravelBufferMinutes) { mutableStateOf(currentSettings.defaultTravelBufferMinutes) }
    var calendarSyncEnabled by remember(currentSettings.googleCalendarEnabled) { mutableStateOf(currentSettings.googleCalendarEnabled) }
    var selectedLanguage by remember(currentSettings.appLanguage) { mutableStateOf(currentSettings.appLanguage) }
    val isArabic = selectedLanguage == "ar"

    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showClearDataConfirm by remember { mutableStateOf(false) }
    var savedToast by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBack != null) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isArabic) "رجوع" else "Back",
                            tint = DarsiNavy
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = if (isArabic) "الإعدادات" else "Settings",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarsiNavy
                    )
                    Text(
                        text = if (isArabic) "خصّص مساعدك الشخصي للتدريس" else "Personalize your teaching companion",
                        fontSize = 13.sp,
                        color = DarsiNavySubtle
                    )
                }
            }
        }

        // PRIVACY BANNER (Mandatory signature feature: "Your teaching data stays on this device.")
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarsiSuccessGreenBg),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarsiSuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = DarsiSuccessGreen,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isArabic) "الخصوصية أولًا" else "Privacy First",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiSuccessGreenDark
                        )
                        Text(
                            text = if (isArabic) "بيانات التدريس تبقى على هذا الجهاز." else "Your teaching data stays on this device.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DarsiNavy
                        )
                        Text(
                            text = if (isArabic) "لا تحتاج إلى حساب. بيانات الطلاب والمدفوعات تبقى محلية." else "No account required. All student notes and payments remain 100% offline.",
                            fontSize = 11.sp,
                            color = DarsiNavyMuted
                        )
                    }
                }
            }
        }

        // Section: Profile & Teaching Defaults
        item {
            SettingsCard(title = if (isArabic) "الملف والتدريس" else "PROFILE & TEACHING") {
                OutlinedTextField(
                    value = tutorName,
                    onValueChange = { tutorName = it },
                    label = { Text(if (isArabic) "اسم المدرس" else "Tutor Name") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = subjects,
                    onValueChange = { subjects = it },
                    label = { Text(if (isArabic) "المواد التي تدرّسها" else "Subjects Taught (comma separated)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isArabic) "مدة الحصة الافتراضية" else "Default Lesson Duration",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(45, 60, 90, 120).forEach { mins ->
                        val isSelected = (defaultDuration == mins)
                        FilledTonalButton(
                            onClick = { defaultDuration = mins },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                                contentColor = if (isSelected) Color.White else DarsiNavy
                            )
                        ) {
                            Text("${mins}m", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Section: Currency
        item {
            SettingsCard(title = if (isArabic) "العملة" else "CURRENCY") {
                Text(
                    text = "Active currency for student fees & packages:",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val currencies = listOf("QAR", "SAR", "AED", "KWD", "USD")
                    currencies.forEach { curr ->
                        val isSelected = (selectedCurrency == curr)
                        FilledTonalButton(
                            onClick = { selectedCurrency = curr },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                                contentColor = if (isSelected) Color.White else DarsiNavy
                            )
                        ) {
                            Text(curr, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section: Travel Buffer
        item {
            SettingsCard(title = if (isArabic) "هامش وقت التنقل" else "TRAVEL BUFFER") {
                Text(
                    text = "Default extra preparation buffer added to travel departure calculations:",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val buffers = listOf(0, 10, 15, 20)
                    buffers.forEach { b ->
                        val isSelected = (travelBufferMinutes == b)
                        FilledTonalButton(
                            onClick = { travelBufferMinutes = b },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isSelected) DarsiRoyalBlue else DarsiSurfaceVariant,
                                contentColor = if (isSelected) Color.White else DarsiNavy
                            )
                        ) {
                            Text("${b}m", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Example: For a 25 min trip and ${travelBufferMinutes} min buffer, suggested departure is ${25 + travelBufferMinutes} min before lesson start.",
                    fontSize = 11.sp,
                    color = DarsiNavySubtle
                )
            }
        }

        // Section: Google Calendar
        item {
            SettingsCard(title = if (isArabic) "تقويم Google" else "GOOGLE CALENDAR") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) "مزامنة مع تقويم Google" else "Sync to Google Calendar",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                        Text(
                            text = "One-way outbound sync: Darsi → Google Calendar (Calendar: Darsi). Disconnecting never deletes local data.",
                            fontSize = 11.sp,
                            color = DarsiNavyMuted
                        )
                    }
                    Switch(
                        checked = calendarSyncEnabled,
                        onCheckedChange = { calendarSyncEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = DarsiRoyalBlue)
                    )
                }

                if (calendarSyncEnabled) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = DarsiRoyalBlueSubtle,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = DarsiRoyalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Dedicated Calendar: 'Darsi' · Ready for outbound export",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = DarsiRoyalBlue
                            )
                        }
                    }
                }
            }
        }

        // Section: Language & RTL
        item {
            SettingsCard(title = if (isArabic) "اللغة والتوطين" else "LANGUAGE & LOCALIZATION") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { selectedLanguage = "en" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedLanguage == "en") DarsiRoyalBlue else DarsiSurfaceVariant,
                            contentColor = if (selectedLanguage == "en") Color.White else DarsiNavy
                        )
                    ) {
                        Text("English", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = { selectedLanguage = "ar" },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedLanguage == "ar") DarsiRoyalBlue else DarsiSurfaceVariant,
                            contentColor = if (selectedLanguage == "ar") Color.White else DarsiNavy
                        )
                    ) {
                        Text("العربية (دَرْسي)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section: Save Settings Button
        item {
            Button(
                onClick = {
                    val updated = currentSettings.copy(
                        tutorName = tutorName,
                        subjects = subjects,
                        defaultCurrency = selectedCurrency,
                        defaultDurationMinutes = defaultDuration,
                        defaultTravelBufferMinutes = travelBufferMinutes,
                        googleCalendarEnabled = calendarSyncEnabled,
                        appLanguage = selectedLanguage,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSaveSettings(updated)
                    Toast.makeText(context, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_settings_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isArabic) "حفظ التغييرات" else "Save Changes", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Section: Backup & Restore
        item {
            SettingsCard(title = if (isArabic) "النسخ الاحتياطي والاستعادة" else "BACKUP & RESTORE") {
                Text(
                    text = "Export an encrypted or readable JSON backup of all students, lessons, notes, and payments to safely store or transfer to another device.",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            coroutineScope.launch {
                                val db = DarsiDatabase.getInstance(context)
                                val backupJson = BackupHelper.createBackupJson(db)
                                BackupHelper.shareBackup(context, backupJson)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "تصدير نسخة" else "Export Backup", fontSize = 12.sp)
                    }

                    FilledTonalButton(
                        onClick = { showRestoreDialog = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "استعادة نسخة" else "Restore Backup", fontSize = 12.sp)
                    }
                }
            }
        }

        // Section: Seed / Demo Data
        item {
            SettingsCard(title = if (isArabic) "بيانات تجريبية" else "SAMPLE / DEMO DATA") {
                Text(
                    text = "Easily load realistic Gulf / Qatar sample data (Ahmed Ali, Mohammed Hassan, Grade 12 Group A) to explore all scheduling, attendance, and payment flows.",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            onLoadDemoData()
                            Toast.makeText(context, "Loaded Qatar sample data", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                    ) {
                        Text(if (isArabic) "تحميل بيانات تجريبية" else "Load Sample Data", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { showClearDataConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (isArabic) "مسح كل البيانات" else "Reset All Data", fontSize = 12.sp, color = DarsiCoralRed)
                    }
                }
            }
        }

        // Section: About Darsi
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Darsi | دَرْسي",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Text(
                    text = if (isArabic) "مساعدك الشخصي للدروس الخصوصية." else "Your private teaching companion.",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
                Text(
                    text = "Version 1.0 · Local-First Architecture",
                    fontSize = 11.sp,
                    color = DarsiNavySubtle
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Teaching Backup") },
            text = {
                Column {
                    Text(
                        text = "Paste your Darsi JSON backup string below to restore students, lessons, and payment records:",
                        fontSize = 12.sp,
                        color = DarsiNavyMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("{\n  \"appName\": \"Darsi\", ...\n}") },
                        minLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val db = DarsiDatabase.getInstance(context)
                            val ok = BackupHelper.restoreBackupJson(db, restoreJsonInput)
                            if (ok) {
                                Toast.makeText(context, "Backup restored successfully!", Toast.LENGTH_LONG).show()
                                showRestoreDialog = false
                            } else {
                                Toast.makeText(context, "Invalid backup format", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Data Confirmation Dialog
    if (showClearDataConfirm) {
        AlertDialog(
            onDismissRequest = { showClearDataConfirm = false },
            title = { Text("Clear All Teaching Data?") },
            text = {
                Text("This will delete all students, groups, lessons, notes, and payments on this device. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showClearDataConfirm = false
                        Toast.makeText(context, "All data wiped", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DarsiCoralRed)
                ) {
                    Text("Confirm Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    DarsiCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = DarsiNavySubtle
            )
            Spacer(modifier = Modifier.height(10.dp))
            content()
        }
    }
}
