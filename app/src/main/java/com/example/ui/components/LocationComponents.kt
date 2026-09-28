package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.entity.StudentEntity
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSurfaceVariant
import com.example.util.MapsAndLocationHelper

@Composable
fun StudentLocationCard(
    student: StudentEntity,
    onEditLocation: () -> Unit,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isArabic = language == "ar"
    var isExpanded by remember { mutableStateOf(false) }

    val hasLocation = student.areaName.isNotBlank() || student.addressText.isNotBlank() || student.mapsLink.isNotBlank() || student.latitude != null
    val isOnline = student.defaultLessonLocationType == "ONLINE" || student.locationLabel.equals("Online", ignoreCase = true)

    DarsiCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isOnline) Icons.Default.Public else Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DarsiRoyalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "الموقع" else "LOCATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = DarsiNavySubtle
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditLocation,
                        modifier = Modifier.size(32.dp).testTag("edit_student_location_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = if (isArabic) "تعديل الموقع" else "Edit Location",
                            tint = DarsiNavyMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (hasLocation && !isOnline) {
                        IconButton(
                            onClick = { isExpanded = !isExpanded },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) {
                                    if (isArabic) "إخفاء التفاصيل" else "Hide details"
                                } else {
                                    if (isArabic) "عرض التفاصيل" else "Show details"
                                },
                                tint = DarsiNavyMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (isOnline) {
                Text(
                    text = if (isArabic) "حصص أونلاين" else "Online Lessons",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Text(
                    text = if (isArabic) "تُعقد الحصص عن بُعد عبر مكالمة فيديو." else "Lessons conducted remotely via video call.",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
            } else if (!hasLocation) {
                Text(
                    text = if (isArabic) "لم يتم حفظ موقع" else "No location saved",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarsiNavy
                )
                Text(
                    text = if (isArabic) "اضغط على أيقونة التعديل لإضافة منطقة أو عنوان الطالب." else "Tap the edit icon to add student's home area or address.",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
            } else {
                Text(
                    text = student.locationLabel.ifBlank { if (isArabic) "منزل الطالب" else "Student Home" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                if (student.areaName.isNotBlank()) {
                    Text(
                        text = student.areaName,
                        fontSize = 13.sp,
                        color = DarsiNavyMuted
                    )
                }

                if (student.defaultTravelTimeMinutes != null && student.defaultTravelTimeMinutes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                            tint = DarsiNavySubtle,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isArabic) "~${student.defaultTravelTimeMinutes} دقيقة وقت التنقل" else "~${student.defaultTravelTimeMinutes} min travel time",
                            fontSize = 12.sp,
                            color = DarsiNavyMuted
                        )
                    }
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(modifier = Modifier.padding(top = 10.dp)) {
                        if (student.addressText.isNotBlank()) {
                            DetailRow(if (isArabic) "العنوان" else "Address", student.addressText)
                        }
                        if (student.locationNotes.isNotBlank()) {
                            DetailRow(if (isArabic) "ملاحظات الموقع" else "Location Notes", student.locationNotes)
                        }
                        if (student.latitude != null && student.longitude != null) {
                            DetailRow(if (isArabic) "الإحداثيات" else "Coordinates", "${String.format("%.4f", student.latitude)}, ${String.format("%.4f", student.longitude)}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            MapsAndLocationHelper.openInMaps(
                                context = context,
                                latitude = student.latitude,
                                longitude = student.longitude,
                                addressText = student.addressText,
                                areaName = student.areaName,
                                mapsLink = student.mapsLink,
                                label = "${student.name}'s Location"
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("student_open_in_maps_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "فتح في الخرائط" else "Open in Maps", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            MapsAndLocationHelper.navigate(
                                context = context,
                                latitude = student.latitude,
                                longitude = student.longitude,
                                addressText = student.addressText,
                                areaName = student.areaName,
                                label = "${student.name}'s Home"
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f).testTag("student_navigate_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "الاتجاهات" else "Navigate", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LocationEditorDialog(
    initialLabel: String,
    initialArea: String,
    initialAddress: String,
    initialMapsLink: String,
    initialNotes: String,
    initialTravelTime: Int?,
    initialLocationType: String,
    initialLat: Double?,
    initialLng: Double?,
    title: String = "Edit Location",
    language: String = "en",
    onDismiss: () -> Unit,
    onSave: (
        label: String,
        area: String,
        address: String,
        mapsLink: String,
        notes: String,
        travelTimeMinutes: Int?,
        locationType: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit
) {
    val context = LocalContext.current
    val isArabic = language == "ar"
    var label by remember { mutableStateOf(initialLabel) }
    var area by remember { mutableStateOf(initialArea) }
    var address by remember { mutableStateOf(initialAddress) }
    var mapsLink by remember { mutableStateOf(initialMapsLink) }
    var notes by remember { mutableStateOf(initialNotes) }
    var travelTimeStr by remember { mutableStateOf(initialTravelTime?.toString() ?: "") }
    var locationType by remember { mutableStateOf(initialLocationType) }
    var lat by remember { mutableStateOf<Double?>(initialLat) }
    var lng by remember { mutableStateOf<Double?>(initialLng) }
    var isLocating by remember { mutableStateOf(false) }

    // Foreground Permission Launcher: strictly requested on user click
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isLocating = true
            MapsAndLocationHelper.fetchCurrentLocation(
                context = context,
                onLocationResult = { fetchedLat, fetchedLng ->
                    lat = fetchedLat
                    lng = fetchedLng
                    if (mapsLink.isBlank()) {
                        mapsLink = "https://maps.google.com/?q=$fetchedLat,$fetchedLng"
                    }
                    isLocating = false
                    Toast.makeText(context, if (isArabic) "تم تحديد الموقع بنجاح" else "Location captured successfully", Toast.LENGTH_SHORT).show()
                },
                onError = { err ->
                    isLocating = false
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, if (isArabic) "تم رفض إذن الوصول إلى الموقع." else "Location permission was denied.", Toast.LENGTH_SHORT).show()
        }
    }

    val labelOptions = if (isArabic) {
        listOf("منزل الطالب", "المنزل", "مقر المعلم", "المدرسة", "المكتبة", "أونلاين", "أخرى")
    } else {
        listOf("Student Home", "Home", "Tutor Location", "School", "Library", "Online", "Other")
    }
    val typeOptions = listOf(
        "STUDENT_HOME" to (if (isArabic) "منزل الطالب" else "Student Home"),
        "TUTOR_LOCATION" to (if (isArabic) "مقر المعلم" else "Tutor Location"),
        "ONLINE" to (if (isArabic) "أونلاين" else "Online"),
        "CUSTOM" to (if (isArabic) "مخصص" else "Custom")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(if (isArabic) "نوع موقع الحصة:" else "Lesson Location Type:", fontSize = 12.sp, color = DarsiNavyMuted)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    typeOptions.forEach { (code, lbl) ->
                        FilterChip(
                            selected = (locationType == code),
                            onClick = {
                                locationType = code
                                if (code == "ONLINE") {
                                    label = if (isArabic) "أونلاين" else "Online"
                                    area = if (isArabic) "أونلاين" else "Online"
                                }
                            },
                            label = { Text(lbl, fontSize = 11.sp) }
                        )
                    }
                }

                if (locationType != "ONLINE") {
                    Text(if (isArabic) "تسمية الموقع:" else "Location Label:", fontSize = 12.sp, color = DarsiNavyMuted)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        labelOptions.forEach { opt ->
                            FilterChip(
                                selected = (label == opt),
                                onClick = { label = opt },
                                label = { Text(opt, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = area,
                        onValueChange = { area = it },
                        label = { Text(if (isArabic) "المنطقة / الحي (مثال: الوعب، الدفنة)" else "Area / District (e.g. Al Waab, West Bay)") },
                        modifier = Modifier.fillMaxWidth().testTag("location_area_input")
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text(if (isArabic) "عنوان الشارع (مثال: فيلا 21، شارع 320)" else "Street Address (e.g. Villa 21, Street 320)") },
                        modifier = Modifier.fillMaxWidth().testTag("location_address_input")
                    )

                    OutlinedTextField(
                        value = mapsLink,
                        onValueChange = { mapsLink = it },
                        label = { Text(if (isArabic) "رابط الخرائط (رابط Google Maps / Apple Maps)" else "Maps Link (Google Maps / Apple Maps URL)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = travelTimeStr,
                        onValueChange = { travelTimeStr = it.filter { ch -> ch.isDigit() } },
                        label = { Text(if (isArabic) "وقت التنقل التقديري (بالدقائق)" else "Estimated Travel Time (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isArabic) "ملاحظات الموقع (مثال: المدخل الجانبي، الموقف)" else "Location Notes (e.g. side entrance, parking)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Optional Action: Use Current Location (Foreground GPS on user demand)
                    OutlinedButton(
                        onClick = {
                            val finePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                            if (finePerm == PackageManager.PERMISSION_GRANTED) {
                                isLocating = true
                                MapsAndLocationHelper.fetchCurrentLocation(
                                    context = context,
                                    onLocationResult = { fetchedLat, fetchedLng ->
                                        lat = fetchedLat
                                        lng = fetchedLng
                                        if (mapsLink.isBlank()) {
                                            mapsLink = "https://maps.google.com/?q=$fetchedLat,$fetchedLng"
                                        }
                                        isLocating = false
                                        Toast.makeText(context, if (isArabic) "تم تحديد الموقع بنجاح" else "Location captured successfully", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { err ->
                                        isLocating = false
                                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("use_current_location_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = DarsiRoyalBlue
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isLocating) {
                                if (isArabic) "جاري تحديد الموقع..." else "Locating..."
                            } else if (lat != null) {
                                if (isArabic) "تم تحديد الموقع ✓" else "Location Captured ✓"
                            } else {
                                if (isArabic) "استخدام الموقع الحالي" else "Use Current Location"
                            },
                            fontSize = 12.sp,
                            color = DarsiRoyalBlue
                        )
                    }

                    if (lat != null && lng != null) {
                        Text(
                            text = if (isArabic) "الإحداثيات: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}" else "Coordinates: ${String.format("%.4f", lat)}, ${String.format("%.4f", lng)}",
                            fontSize = 11.sp,
                            color = DarsiNavyMuted
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text(if (isArabic) "رابط الاجتماع أو التعليمات (مثال: رابط Google Meet)" else "Meeting link or instructions (e.g. Google Meet URL)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        label.trim().ifBlank { if (locationType == "ONLINE") (if (isArabic) "أونلاين" else "Online") else (if (isArabic) "منزل الطالب" else "Student Home") },
                        area.trim(),
                        address.trim(),
                        mapsLink.trim(),
                        notes.trim(),
                        travelTimeStr.toIntOrNull(),
                        locationType,
                        lat,
                        lng
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue),
                modifier = Modifier.testTag("save_location_btn")
            ) {
                Text(if (isArabic) "حفظ الموقع" else "Save Location")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
