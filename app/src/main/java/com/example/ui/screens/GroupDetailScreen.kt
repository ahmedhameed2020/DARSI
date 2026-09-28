package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.dao.GroupMemberWithStudent
import com.example.data.local.entity.GroupEntity
import com.example.data.local.entity.StudentEntity
import com.example.domain.model.GroupDetails
import com.example.ui.components.DetailRow
import com.example.ui.components.DarsiCard
import com.example.ui.components.LocationEditorDialog
import com.example.ui.theme.DarsiBorder
import com.example.ui.theme.DarsiCoralRed
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSurfaceVariant
import com.example.util.CurrencyUtils
import com.example.util.DateTimeUtils
import com.example.util.MapsAndLocationHelper

@Composable
fun GroupDetailScreen(
    details: GroupDetails,
    allStudents: List<StudentEntity>,
    currency: String,
    onBack: () -> Unit,
    onAddMember: (Long, Double?) -> Unit,
    onRemoveMember: (Long) -> Unit,
    onBookGroupLesson: (Long) -> Unit,
    onDeleteGroup: (Long) -> Unit,
    onUpdateGroup: (GroupEntity) -> Unit = {},
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isArabic = language == "ar"
    val group = details.group
    var showAddMemberDialog by remember { mutableStateOf(false) }
    var showLocationEditorDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("group_detail_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("group_detail_back_btn")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = if (isArabic) "رجوع" else "Back",
                    tint = DarsiNavy
                )
            }
            Text(
                text = if (isArabic) "تفاصيل المجموعة" else "Group Details",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = DarsiNavy,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onDeleteGroup(group.id) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = if (isArabic) "حذف المجموعة" else "Delete Group",
                    tint = DarsiCoralRed
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Group Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = group.name,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavy
                                )
                                Text(
                                    text = "${group.grade} · ${group.subject}",
                                    fontSize = 13.sp,
                                    color = DarsiNavyMuted
                                )
                            }
                            Surface(
                                color = DarsiRoyalBlueSubtle,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "${details.members.size} طلاب" else "${details.members.size} students",
                                    color = DarsiRoyalBlue,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        DetailRow(if (isArabic) "السعر الافتراضي" else "Default Price", if (isArabic) "${CurrencyUtils.format(group.defaultPrice, currency)} / طالب" else "${CurrencyUtils.format(group.defaultPrice, currency)} / student")
                        DetailRow(if (isArabic) "المدة الافتراضية" else "Default Duration", if (isArabic) "${group.defaultDurationMinutes} دقيقة" else "${group.defaultDurationMinutes} minutes")

                        if (group.notes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = group.notes,
                                fontSize = 12.sp,
                                color = DarsiNavyMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { onBookGroupLesson(group.id) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "جدولة حصة للمجموعة" else "Schedule Group Lesson", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Group Default Location Card
            item {
                val isOnline = group.defaultLocationType == "ONLINE"
                val hasLoc = group.areaName.isNotBlank() || group.addressText.isNotBlank() || group.mapsLink.isNotBlank() || group.latitude != null
                DarsiCard(modifier = Modifier.fillMaxWidth()) {
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
                                    text = if (isArabic) "موقع الحصة الافتراضي" else "DEFAULT LESSON LOCATION",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = DarsiNavySubtle
                                )
                            }
                            IconButton(
                                onClick = { showLocationEditorDialog = true },
                                modifier = Modifier.size(32.dp).testTag("edit_group_location_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Location",
                                    tint = DarsiNavyMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        if (isOnline) {
                            Text(if (isArabic) "حصص أونلاين" else "Online Sessions", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarsiNavy)
                            Text("Default to remote video call sessions.", fontSize = 12.sp, color = DarsiNavyMuted)
                        } else if (!hasLoc) {
                            Text(group.defaultLocationLabel.ifBlank { "Tutor Location" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarsiNavy)
                            Text("Tap the edit icon to add area or address for this group.", fontSize = 12.sp, color = DarsiNavyMuted)
                        } else {
                            Text(group.defaultLocationLabel.ifBlank { "Group Location" }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = DarsiNavy)
                            if (group.areaName.isNotBlank()) {
                                Text(group.areaName, fontSize = 13.sp, color = DarsiNavyMuted)
                            }
                            if (group.addressText.isNotBlank()) {
                                Text(group.addressText, fontSize = 12.sp, color = DarsiNavyMuted)
                            }
                            if (group.defaultTravelTimeMinutes != null && group.defaultTravelTimeMinutes > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Timer, contentDescription = null, tint = DarsiNavySubtle, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("~${group.defaultTravelTimeMinutes} min travel time", fontSize = 12.sp, color = DarsiNavyMuted)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        MapsAndLocationHelper.openInMaps(
                                            context = context,
                                            latitude = group.latitude,
                                            longitude = group.longitude,
                                            addressText = group.addressText,
                                            areaName = group.areaName,
                                            mapsLink = group.mapsLink,
                                            label = "${group.name} Location"
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("group_open_in_maps_btn")
                                ) {
                                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Maps", fontSize = 11.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        MapsAndLocationHelper.navigate(
                                            context = context,
                                            latitude = group.latitude,
                                            longitude = group.longitude,
                                            addressText = group.addressText,
                                            areaName = group.areaName,
                                            label = "${group.name} Location"
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f).testTag("group_navigate_btn")
                                ) {
                                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Navigate", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Next Lesson Card
            item {
                if (details.nextLesson != null) {
                    DarsiCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = DarsiRoyalBlue,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "NEXT GROUP LESSON",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp,
                                    color = DarsiNavySubtle
                                )
                                Text(
                                    text = "${DateTimeUtils.formatReadableDate(details.nextLesson.startEpochMillis)} at ${DateTimeUtils.formatTime(details.nextLesson.startEpochMillis)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavy
                                )
                                Text(
                                    text = DateTimeUtils.getCountdownString(details.nextLesson.startEpochMillis, details.nextLesson.endEpochMillis, isArabic = isArabic),
                                    fontSize = 12.sp,
                                    color = DarsiRoyalBlue
                                )
                            }
                        }
                    }
                }
            }

            // Members Header & Action
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ENROLLED STUDENTS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        color = DarsiNavySubtle
                    )
                    Button(
                        onClick = { showAddMemberDialog = !showAddMemberDialog },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (showAddMemberDialog) "Close" else "Add Student", fontSize = 11.sp)
                    }
                }
            }

            // Add Member Picker
            if (showAddMemberDialog) {
                item {
                    val availableStudents = allStudents.filter { s ->
                        details.members.none { it.studentId == s.id }
                    }
                    DarsiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Select student to add:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarsiNavy
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            if (availableStudents.isEmpty()) {
                                Text("All existing students are already enrolled.", fontSize = 12.sp, color = DarsiNavyMuted)
                            } else {
                                availableStudents.forEach { s ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(s.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DarsiNavy)
                                            Text("${s.grade} · ${s.subject}", fontSize = 11.sp, color = DarsiNavySubtle)
                                        }
                                        Button(
                                            onClick = {
                                                onAddMember(s.id, null)
                                                showAddMemberDialog = false
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                                        ) {
                                            Text("Enroll", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Enrolled Members List
            if (details.members.isEmpty()) {
                item {
                    DarsiCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No students enrolled in this group yet.", fontSize = 13.sp, color = DarsiNavyMuted)
                        }
                    }
                }
            } else {
                items(details.members, key = { it.memberId }) { member ->
                    GroupMemberRowItem(
                        member = member,
                        defaultGroupPrice = group.defaultPrice,
                        currency = currency,
                        onRemove = { onRemoveMember(member.studentId) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showLocationEditorDialog) {
        LocationEditorDialog(
            initialLabel = group.defaultLocationLabel,
            initialArea = group.areaName,
            initialAddress = group.addressText,
            initialMapsLink = group.mapsLink,
            initialNotes = "",
            initialTravelTime = group.defaultTravelTimeMinutes,
            initialLocationType = group.defaultLocationType,
            initialLat = group.latitude,
            initialLng = group.longitude,
            title = "Edit Location for ${group.name}",
            onDismiss = { showLocationEditorDialog = false },
            onSave = { label, area, address, mapsLink, _, travelTime, locType, lat, lng ->
                val updated = group.copy(
                    defaultLocationLabel = label,
                    areaName = area,
                    addressText = address,
                    mapsLink = mapsLink,
                    defaultTravelTimeMinutes = travelTime,
                    defaultLocationType = locType,
                    latitude = lat,
                    longitude = lng,
                    updatedAt = System.currentTimeMillis()
                )
                onUpdateGroup(updated)
            }
        )
    }
}

@Composable
fun GroupMemberRowItem(
    member: GroupMemberWithStudent,
    defaultGroupPrice: Double,
    currency: String,
    onRemove: () -> Unit
) {
    val priceCharged = member.priceOverride ?: defaultGroupPrice

    DarsiCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = member.studentName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarsiNavy
                )
                Text(
                    text = "${member.studentGrade} · Phone: ${member.studentPhone}",
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
                Text(
                    text = "Price: ${CurrencyUtils.format(priceCharged, currency)} / lesson" +
                            (if (member.priceOverride != null) " (Override)" else " (Standard)"),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = DarsiRoyalBlue
                )
            }

            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove student from group",
                    tint = DarsiCoralRed.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
