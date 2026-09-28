package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.GroupEntity
import com.example.domain.model.StudentWithBalance
import com.example.ui.components.DarsiCard
import com.example.ui.components.PaymentStatusBadge
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavyMuted
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle
import com.example.ui.theme.DarsiSurfaceVariant
import com.example.util.DateTimeUtils

@Composable
fun StudentsScreen(
    students: List<StudentWithBalance>,
    groups: List<GroupEntity>,
    currency: String,
    onOpenStudent: (Long) -> Unit,
    onOpenGroup: (Long) -> Unit,
    onAddStudent: () -> Unit,
    onAddGroup: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSection by remember { mutableIntStateOf(0) } // 0=Students, 1=Groups
    var selectedFilter by remember { mutableStateOf("All") } // All, Active, Due, Package

    val filteredStudents = remember(students, searchQuery, selectedFilter) {
        students.filter { s ->
            val matchesQuery = s.student.name.contains(searchQuery, ignoreCase = true) ||
                    s.student.subject.contains(searchQuery, ignoreCase = true) ||
                    s.student.grade.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedFilter) {
                "Due" -> s.balanceDue > 0.0
                "Package" -> s.activePackage != null
                "Active" -> s.student.status == "ACTIVE"
                else -> true
            }

            matchesQuery && matchesFilter
        }
    }

    val filteredGroups = remember(groups, searchQuery) {
        groups.filter { g ->
            g.name.contains(searchQuery, ignoreCase = true) ||
                    g.subject.contains(searchQuery, ignoreCase = true) ||
                    g.grade.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("students_screen")
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by student, group, or subject...", fontSize = 13.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = DarsiNavyMuted,
                    modifier = Modifier.size(20.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = DarsiRoyalBlue,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("student_search_input")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Section Tabs: Students vs Groups
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = Color.Transparent,
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = {
                    Text(
                        "Students (${students.size})",
                        fontWeight = if (selectedSection == 0) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedSection == 0) DarsiRoyalBlue else DarsiNavyMuted
                    )
                }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = {
                    Text(
                        "Groups (${groups.size})",
                        fontWeight = if (selectedSection == 1) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedSection == 1) DarsiRoyalBlue else DarsiNavyMuted
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedSection == 0) {
            // Filter Chips for Students
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val chips = listOf("All", "Active", "Due", "Package")
                items(chips) { chip ->
                    val isSelected = (selectedFilter == chip)
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = chip },
                        label = { Text(chip, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarsiRoyalBlueSubtle,
                            selectedLabelColor = DarsiRoyalBlue
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Students List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredStudents.isEmpty()) {
                    item {
                        DarsiCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No students found",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add your first student to get started.",
                                    fontSize = 13.sp,
                                    color = DarsiNavyMuted
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onAddStudent,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Student", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredStudents, key = { it.student.id }) { item ->
                        StudentCardItem(
                            item = item,
                            currency = currency,
                            onClick = { onOpenStudent(item.student.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        } else {
            // Groups List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredGroups.isEmpty()) {
                    item {
                        DarsiCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No groups yet",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DarsiNavy
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Create student groups for batch lessons.",
                                    fontSize = 13.sp,
                                    color = DarsiNavyMuted
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = onAddGroup,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarsiRoyalBlue)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Add Group", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    items(filteredGroups, key = { it.id }) { group ->
                        GroupCardItem(
                            group = group,
                            currency = currency,
                            onClick = { onOpenGroup(group.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun StudentCardItem(
    item: StudentWithBalance,
    currency: String,
    onClick: () -> Unit
) {
    val student = item.student
    val packageInfo = item.activePackage?.let {
        val remaining = it.totalLessons - it.usedLessons
        "Package: $remaining remaining"
    }

    DarsiCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("student_card_${student.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = DarsiRoyalBlueSubtle,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = DarsiRoyalBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = student.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                        Text(
                            text = "${student.grade} · ${student.subject}",
                            fontSize = 12.sp,
                            color = DarsiNavyMuted
                        )
                    }
                }

                PaymentStatusBadge(
                    balanceDue = item.balanceDue,
                    currency = currency,
                    packageInfo = packageInfo
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Next Lesson info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val nextLessonText = if (item.nextLesson != null) {
                    "Next: ${DateTimeUtils.formatReadableDate(item.nextLesson.startEpochMillis)} at ${DateTimeUtils.formatTime(item.nextLesson.startEpochMillis)}"
                } else {
                    "No upcoming lessons"
                }

                Text(
                    text = nextLessonText,
                    fontSize = 12.sp,
                    color = if (item.nextLesson != null) DarsiNavy else DarsiNavySubtle
                )

                Text(
                    text = "${item.completedLessonsCount} completed",
                    fontSize = 11.sp,
                    color = DarsiNavySubtle
                )
            }
        }
    }
}

@Composable
fun GroupCardItem(
    group: GroupEntity,
    currency: String,
    onClick: () -> Unit
) {
    DarsiCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("group_card_${group.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = group.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = DarsiNavy
                        )
                        Text(
                            text = "${group.grade} · ${group.subject}",
                            fontSize = 12.sp,
                            color = DarsiNavyMuted
                        )
                    }
                }

                Surface(
                    color = DarsiSurfaceVariant,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$currency ${group.defaultPrice.toInt()}/lesson",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarsiNavy,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            if (group.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = group.notes,
                    fontSize = 12.sp,
                    color = DarsiNavyMuted
                )
            }
        }
    }
}
