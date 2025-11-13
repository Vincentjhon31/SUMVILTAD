package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zynt.sumviltadconnect.ui.viewmodel.SettingsViewModel
import com.zynt.sumviltadconnect.ui.theme.AppDimensions

@Composable
fun SettingsScreen(vm: SettingsViewModel = viewModel()) {
    val theme by vm.theme.collectAsState()
    val notificationsEnabled by vm.notificationsEnabled.collectAsState()
    val currentTheme = theme ?: "light" // Default to light if null

    Column(
        Modifier
            .fillMaxSize()
            .padding(AppDimensions.paddingMedium())
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            "Customize your app experience",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = AppDimensions.paddingLarge())
        )

        // Theme selection with enhanced UI
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingExtraSmall()),
            shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
        ) {
            Column(Modifier.padding(AppDimensions.paddingLarge())) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = AppDimensions.paddingMedium())
                ) {
                    Icon(
                        Icons.Default.Brightness6,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(AppDimensions.iconSizeMedium())
                    )
                    Spacer(Modifier.width(AppDimensions.paddingMedium()))
                    Column {
                        Text(
                            "Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Choose your preferred theme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Theme options with better visual design
                ThemeOption(
                    title = "Light (Gray Green)",
                    subtitle = "Clean and bright interface",
                    icon = Icons.Default.LightMode,
                    isSelected = currentTheme == "light",
                    onClick = { vm.setTheme("light") }
                )

                Spacer(Modifier.height(AppDimensions.paddingSmall()))

                ThemeOption(
                    title = "Dark Mode",
                    subtitle = "Easy on the eyes",
                    icon = Icons.Default.DarkMode,
                    isSelected = currentTheme == "dark",
                    onClick = { vm.setTheme("dark") }
                )

                Spacer(Modifier.height(AppDimensions.paddingSmall()))

                ThemeOption(
                    title = "System Default",
                    subtitle = "Follow device settings",
                    icon = Icons.Default.SettingsBrightness,
                    isSelected = currentTheme == "system",
                    onClick = { vm.setTheme("system") }
                )
            }
        }

        Spacer(Modifier.height(AppDimensions.paddingMedium()))

        // Notifications toggle with enhanced UI
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = AppDimensions.paddingExtraSmall()),
            shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium())
        ) {
            Row(
                Modifier.padding(AppDimensions.paddingLarge()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(AppDimensions.buttonHeight())
                        .clip(RoundedCornerShape(AppDimensions.cornerRadiusMedium()))
                        .background(
                            if (notificationsEnabled)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surfaceVariant
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (notificationsEnabled)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppDimensions.iconSizeMedium())
                    )
                }
                Spacer(Modifier.width(AppDimensions.paddingMedium()))
                Column(Modifier.weight(1f)) {
                    Text(
                        "Notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (notificationsEnabled) "Enabled" else "Disabled",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { vm.setNotificationsEnabled(it) },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(AppDimensions.cornerRadiusMedium()),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(AppDimensions.paddingExtraSmall(), MaterialTheme.colorScheme.primary)
        else null
    ) {
        Row(
            modifier = Modifier.padding(AppDimensions.paddingMedium()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AppDimensions.iconSizeMedium())
            )
            Spacer(Modifier.width(AppDimensions.paddingMedium()))
            Column(Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppDimensions.iconSizeMedium())
                )
            } else {
                RadioButton(
                    selected = false,
                    onClick = null,
                    colors = RadioButtonDefaults.colors(
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
