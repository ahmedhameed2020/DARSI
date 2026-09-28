package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.DarsiTab
import com.example.ui.theme.DarsiNavy
import com.example.ui.theme.DarsiNavySubtle
import com.example.ui.theme.DarsiRoyalBlue
import com.example.ui.theme.DarsiRoyalBlueSubtle

data class NavigationItem(
    val tab: DarsiTab,
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun DarsiBottomBar(
    currentTab: DarsiTab,
    onTabSelected: (DarsiTab) -> Unit,
    language: String = "en",
    modifier: Modifier = Modifier
) {
    val strings = com.example.ui.localization.darsiStrings(language)
    val items = listOf(
        NavigationItem(DarsiTab.TODAY, strings.today, Icons.Default.Today, "tab_today"),
        NavigationItem(DarsiTab.CALENDAR, strings.calendar, Icons.Default.CalendarMonth, "tab_calendar"),
        NavigationItem(DarsiTab.STUDENTS, strings.students, Icons.Default.People, "tab_students"),
        NavigationItem(DarsiTab.PAYMENTS, strings.payments, Icons.Default.Payments, "tab_payments"),
        NavigationItem(DarsiTab.MORE, strings.more, Icons.Default.MoreHoriz, "tab_more")
    )

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp,
        modifier = modifier.testTag("darsi_bottom_nav")
    ) {
        items.forEach { item ->
            val isSelected = (currentTab == item.tab)
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.tab) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        fontSize = 11.sp
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DarsiRoyalBlue,
                    selectedTextColor = DarsiRoyalBlue,
                    indicatorColor = DarsiRoyalBlueSubtle,
                    unselectedIconColor = DarsiNavySubtle,
                    unselectedTextColor = DarsiNavySubtle
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}
