package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.zynt.sumviltadconnect.ui.components.BrandProgressIndicator
import com.zynt.sumviltadconnect.ui.viewmodel.FarmerViewModel
import com.zynt.sumviltadconnect.utils.TokenManager
import java.text.SimpleDateFormat
import java.util.*
import com.zynt.sumviltadconnect.ui.theme.AppDimensions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalInformationScreen(
    navController: NavController,
    farmerViewModel: FarmerViewModel = viewModel()
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val userProfile by farmerViewModel.userProfile.collectAsState()
    val isLoading by farmerViewModel.isLoading.collectAsState()
    val error by farmerViewModel.error.collectAsState()

    // Edit mode state
    var isEditMode by remember { mutableStateOf(false) }

    // Form state
    var firstName by remember { mutableStateOf("") }
    var middleInitial by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var completeAddress by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var riceFieldArea by remember { mutableStateOf("") }

    // Farm area management state
    var showFarmAreaDialog by remember { mutableStateOf(false) }
    var editFarmAreaId by remember { mutableStateOf<Int?>(null) }
    var farmAreaLocation by remember { mutableStateOf("") }
    var farmAreaSize by remember { mutableStateOf("") }

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var farmAreaToDelete by remember { mutableStateOf<Int?>(null) }

    // Load user profile when screen opens
    LaunchedEffect(Unit) {
        farmerViewModel.fetchUserProfile()
    }

    // Update form fields when profile loads
    LaunchedEffect(userProfile) {
        userProfile?.let { profile ->
            firstName = profile.farmerProfile?.firstName ?: ""
            middleInitial = profile.farmerProfile?.middleInitial ?: ""
            lastName = profile.farmerProfile?.lastName ?: ""
            email = profile.email
            contactNumber = profile.farmerProfile?.contactNumber ?: ""
            completeAddress = profile.farmerProfile?.completeAddress ?: ""
            birthday = profile.farmerProfile?.birthday ?: ""
            riceFieldArea = profile.farmerProfile?.riceFieldArea?.toString() ?: ""
        }
    }

    val paddingMedium = AppDimensions.paddingMedium()
    val paddingSmall = AppDimensions.paddingSmall()
    val paddingLarge = AppDimensions.paddingLarge()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Gradient Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Custom Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = paddingMedium, vertical = paddingSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = Color.White,
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                
                Spacer(modifier = Modifier.width(paddingMedium))
                
                Text(
                    text = "My Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(paddingSmall))

            // Content Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = paddingMedium)
            ) {
                when {
                    isLoading && userProfile == null -> {
                        EnhancedLoadingState()
                    }
                    error != null && userProfile == null -> {
                        EnhancedErrorState(
                            message = error ?: "Unknown error",
                            onRetry = { farmerViewModel.fetchUserProfile() }
                        )
                    }
                    userProfile != null -> {
                        // Enhanced Profile Header
                        EnhancedProfileHeader(
                            firstName = firstName,
                            middleInitial = middleInitial,
                            lastName = lastName,
                            displayName = userProfile?.name ?: "Unknown User",
                            email = email,
                            role = userProfile?.role ?: "user"
                        )

                        Spacer(modifier = Modifier.height(paddingLarge))

                        // Edit Profile Button (Top)
                        AnimatedVisibility(
                            visible = !isEditMode,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Column {
                                Button(
                                    onClick = { isEditMode = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Edit Profile")
                                }
                                Spacer(modifier = Modifier.height(paddingLarge))
                            }
                        }

                        // Personal Information Section
                        SectionHeader(
                            title = "Personal Information",
                            subtitle = if (isEditMode) "Edit your personal details" else "View your personal details"
                        )

                        Spacer(modifier = Modifier.height(paddingMedium))

                        AnimatedContent(
                            targetState = isEditMode,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "profile_edit_animation"
                        ) { editMode ->
                            if (editMode) {
                                EditProfileCard(
                                    firstName = firstName,
                                    onFirstNameChange = { firstName = it },
                                    middleInitial = middleInitial,
                                    onMiddleInitialChange = { if (it.length <= 1) middleInitial = it.uppercase() },
                                    lastName = lastName,
                                    onLastNameChange = { lastName = it },
                                    email = email,
                                    contactNumber = contactNumber,
                                    onContactNumberChange = { contactNumber = it },
                                    birthday = birthday,
                                    onBirthdayChange = { birthday = it },
                                    completeAddress = completeAddress,
                                    onCompleteAddressChange = { completeAddress = it },
                                    riceFieldArea = riceFieldArea,
                                    onRiceFieldAreaChange = { riceFieldArea = it },
                                    isLoading = isLoading
                                )
                            } else {
                                ViewProfileCard(
                                    firstName = firstName,
                                    middleInitial = middleInitial,
                                    lastName = lastName,
                                    fullName = userProfile?.name,
                                    email = email,
                                    userId = userProfile?.id,
                                    role = userProfile?.role,
                                    birthday = birthday,
                                    contactNumber = contactNumber,
                                    completeAddress = completeAddress,
                                    riceFieldArea = riceFieldArea,
                                    emailVerified = userProfile?.email_verified_at != null
                                )
                            }
                        }

                        // Farm Areas Section (only show in view mode)
                        if (!isEditMode) {
                            userProfile?.farmAreas?.let { areas ->
                                Spacer(modifier = Modifier.height(paddingLarge))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    SectionHeader(
                                        title = "Farm Areas",
                                        subtitle = "${areas.size} location${if (areas.size != 1) "s" else ""}"
                                    )

                                    FilledTonalButton(
                                        onClick = {
                                            editFarmAreaId = null
                                            farmAreaLocation = ""
                                            farmAreaSize = ""
                                            showFarmAreaDialog = true
                                        },
                                        enabled = !isLoading,
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add")
                                    }
                                }

                                Spacer(modifier = Modifier.height(paddingMedium))

                                if (areas.isNotEmpty()) {
                                    areas.forEach { farmArea ->
                                        FarmAreaCard(
                                            location = farmArea.farmLocation,
                                            area = farmArea.riceFieldArea,
                                            onEdit = {
                                                editFarmAreaId = farmArea.id
                                                farmAreaLocation = farmArea.farmLocation
                                                farmAreaSize = farmArea.riceFieldArea.toString()
                                                showFarmAreaDialog = true
                                            },
                                            onDelete = {
                                                farmAreaToDelete = farmArea.id
                                                showDeleteDialog = true
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                } else {
                                    EmptyFarmAreasCard(
                                        onAddClick = {
                                            editFarmAreaId = null
                                            farmAreaLocation = ""
                                            farmAreaSize = ""
                                            showFarmAreaDialog = true
                                        }
                                    )
                                }
                            }

                            // Account Activity Section
                            Spacer(modifier = Modifier.height(paddingLarge))

                            SectionHeader(
                                title = "Account Activity",
                                subtitle = "Your account timeline"
                            )

                            Spacer(modifier = Modifier.height(paddingMedium))

                            AccountActivityCard(
                                createdAt = userProfile?.created_at,
                                updatedAt = userProfile?.updated_at
                            )
                        }

                        Spacer(modifier = Modifier.height(paddingLarge))

                        // Action Buttons
                        AnimatedContent(
                            targetState = isEditMode,
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) togetherWith
                                        fadeOut(animationSpec = tween(300))
                            },
                            label = "button_animation"
                        ) { editMode ->
                            if (editMode) {
                                Column {
                                    Button(
                                        onClick = {
                                            if (firstName.isBlank() || lastName.isBlank()) {
                                                errorMessage = "First name and last name are required"
                                                showErrorDialog = true
                                                return@Button
                                            }

                                            val riceFieldAreaValue = riceFieldArea.toDoubleOrNull()

                                            farmerViewModel.updateProfile(
                                                firstName = firstName.ifBlank { null },
                                                middleInitial = middleInitial.ifBlank { null },
                                                lastName = lastName.ifBlank { null },
                                                email = email.ifBlank { null },
                                                contactNumber = contactNumber.ifBlank { null },
                                                completeAddress = completeAddress.ifBlank { null },
                                                birthday = birthday.ifBlank { null },
                                                riceFieldArea = riceFieldAreaValue,
                                                onSuccess = {
                                                    isEditMode = false
                                                    successMessage = "Profile updated successfully!"
                                                    showSuccessDialog = true
                                                },
                                                onError = { error ->
                                                    errorMessage = error
                                                    showErrorDialog = true
                                                }
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isLoading,
                                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = Color.White
                                        )
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Icon(Icons.Default.Save, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Save Changes")
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedButton(
                                        onClick = {
                                            userProfile?.let { profile ->
                                                firstName = profile.farmerProfile?.firstName ?: ""
                                                middleInitial = profile.farmerProfile?.middleInitial ?: ""
                                                lastName = profile.farmerProfile?.lastName ?: ""
                                                email = profile.email
                                                contactNumber = profile.farmerProfile?.contactNumber ?: ""
                                                completeAddress = profile.farmerProfile?.completeAddress ?: ""
                                                birthday = profile.farmerProfile?.birthday ?: ""
                                                riceFieldArea = profile.farmerProfile?.riceFieldArea?.toString() ?: ""
                                            }
                                            isEditMode = false
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !isLoading
                                    ) {
                                        Icon(Icons.Default.Cancel, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Cancel")
                                    }
                                }
                            } else {
                                // Edit button moved to top
                                Spacer(modifier = Modifier.height(1.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                    else -> {
                        // Fallback
                        FallbackProfileView(
                            context = context,
                            onLoadProfile = { farmerViewModel.fetchUserProfile() }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (showFarmAreaDialog) {
            FarmAreaDialog(
                isEdit = editFarmAreaId != null,
                location = farmAreaLocation,
                onLocationChange = { farmAreaLocation = it },
                size = farmAreaSize,
                onSizeChange = { farmAreaSize = it },
                isLoading = isLoading,
                onSave = {
                    if (farmAreaLocation.isBlank()) {
                        errorMessage = "Farm location is required"
                        showErrorDialog = true
                        return@FarmAreaDialog
                    }

                    val sizeValue = farmAreaSize.toDoubleOrNull()
                    if (sizeValue == null || sizeValue <= 0) {
                        errorMessage = "Please enter a valid farm size"
                        showErrorDialog = true
                        return@FarmAreaDialog
                    }

                    val currentEditId = editFarmAreaId
                    if (currentEditId != null) {
                        farmerViewModel.updateFarmArea(
                            farmAreaId = currentEditId,
                            farmLocation = farmAreaLocation,
                            riceFieldArea = sizeValue,
                            onSuccess = {
                                showFarmAreaDialog = false
                                editFarmAreaId = null
                                farmAreaLocation = ""
                                farmAreaSize = ""
                                successMessage = "Farm area updated successfully!"
                                showSuccessDialog = true
                            },
                            onError = { error ->
                                errorMessage = error
                                showErrorDialog = true
                            }
                        )
                    } else {
                        farmerViewModel.addFarmArea(
                            farmLocation = farmAreaLocation,
                            riceFieldArea = sizeValue,
                            onSuccess = {
                                showFarmAreaDialog = false
                                farmAreaLocation = ""
                                farmAreaSize = ""
                                successMessage = "Farm area added successfully!"
                                showSuccessDialog = true
                            },
                            onError = { error ->
                                errorMessage = error
                                showErrorDialog = true
                            }
                        )
                    }
                },
                onDismiss = {
                    showFarmAreaDialog = false
                    editFarmAreaId = null
                    farmAreaLocation = ""
                    farmAreaSize = ""
                }
            )
        }

        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                icon = {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("Success") },
                text = { Text(successMessage.ifEmpty { "Operation completed successfully!" }) },
                confirmButton = {
                    Button(
                        onClick = { showSuccessDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("Error") },
                text = { Text(errorMessage) },
                confirmButton = {
                    Button(
                        onClick = { showErrorDialog = false }
                    ) {
                        Text("OK")
                    }
                }
            )
        }

        if (showDeleteDialog && farmAreaToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                icon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this farm area? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            farmerViewModel.deleteFarmArea(
                                farmAreaId = farmAreaToDelete!!,
                                onSuccess = {
                                    showDeleteDialog = false
                                    farmAreaToDelete = null
                                    successMessage = "Farm area deleted successfully!"
                                    showSuccessDialog = true
                                },
                                onError = { error ->
                                    errorMessage = error
                                    showErrorDialog = true
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = false
                            farmAreaToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

// Enhanced Components

@Composable
private fun EnhancedLoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            BrandProgressIndicator(size = 64.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading profile...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnhancedErrorState(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = "Error",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun EnhancedProfileHeader(
    firstName: String,
    middleInitial: String,
    lastName: String,
    displayName: String,
    email: String,
    role: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar
                Surface(
                    modifier = Modifier.size(100.dp),
                    shape = RoundedCornerShape(50.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    shadowElevation = 8.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(50.dp),
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display full name
                val fullName = if (firstName.isNotBlank() || lastName.isNotBlank()) {
                    listOfNotNull(
                        firstName.takeIf { it.isNotBlank() },
                        middleInitial.takeIf { it.isNotBlank() }?.let { "$it." },
                        lastName.takeIf { it.isNotBlank() }
                    ).joinToString(" ").ifEmpty { displayName }
                } else {
                    displayName
                }

                Text(
                    text = fullName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = email,
                    fontSize = 16.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Role Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = role.uppercase(),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null
) {
    Column {
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun EditProfileCard(
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    middleInitial: String,
    onMiddleInitialChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    email: String,
    contactNumber: String,
    onContactNumberChange: (String) -> Unit,
    birthday: String,
    onBirthdayChange: (String) -> Unit,
    completeAddress: String,
    onCompleteAddressChange: (String) -> Unit,
    riceFieldArea: String,
    onRiceFieldAreaChange: (String) -> Unit,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // First Name
            OutlinedTextField(
                value = firstName,
                onValueChange = onFirstNameChange,
                label = { Text("First Name *") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Middle Initial
            OutlinedTextField(
                value = middleInitial,
                onValueChange = onMiddleInitialChange,
                label = { Text("Middle Initial") },
                leadingIcon = {
                    Icon(Icons.Default.Badge, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Last Name
            OutlinedTextField(
                value = lastName,
                onValueChange = onLastNameChange,
                label = { Text("Last Name *") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Email (disabled)
            OutlinedTextField(
                value = email,
                onValueChange = { },
                label = { Text("Email Address") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Contact Number
            OutlinedTextField(
                value = contactNumber,
                onValueChange = onContactNumberChange,
                label = { Text("Contact Number") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Birthday
            OutlinedTextField(
                value = birthday,
                onValueChange = onBirthdayChange,
                label = { Text("Birthday (YYYY-MM-DD)") },
                leadingIcon = {
                    Icon(Icons.Default.Cake, contentDescription = null)
                },
                placeholder = { Text("1990-01-01") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Complete Address
            OutlinedTextField(
                value = completeAddress,
                onValueChange = onCompleteAddressChange,
                label = { Text("Complete Address") },
                leadingIcon = {
                    Icon(Icons.Default.Home, contentDescription = null)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Rice Field Area
            OutlinedTextField(
                value = riceFieldArea,
                onValueChange = onRiceFieldAreaChange,
                label = { Text("Rice Field Area (hectares)") },
                leadingIcon = {
                    Icon(Icons.Default.Agriculture, contentDescription = null)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
private fun ViewProfileCard(
    firstName: String,
    middleInitial: String,
    lastName: String,
    fullName: String?,
    email: String,
    userId: Int?,
    role: String?,
    birthday: String,
    contactNumber: String,
    completeAddress: String,
    riceFieldArea: String,
    emailVerified: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            if (firstName.isNotBlank()) {
                InfoField(
                    icon = Icons.Default.Person,
                    label = "First Name",
                    value = firstName
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (middleInitial.isNotBlank()) {
                InfoField(
                    icon = Icons.Default.Badge,
                    label = "Middle Initial",
                    value = middleInitial
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (lastName.isNotBlank()) {
                InfoField(
                    icon = Icons.Default.Person,
                    label = "Last Name",
                    value = lastName
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (firstName.isBlank() && lastName.isBlank() && fullName != null) {
                InfoField(
                    icon = Icons.Default.Person,
                    label = "Full Name",
                    value = fullName
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            InfoField(
                icon = Icons.Default.Email,
                label = "Email Address",
                value = email
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoField(
                icon = Icons.Default.Badge,
                label = "User ID",
                value = "#${userId ?: "N/A"}"
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoField(
                icon = Icons.Default.Shield,
                label = "Role",
                value = role?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
                } ?: "N/A"
            )

            if (birthday.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                InfoField(
                    icon = Icons.Default.Cake,
                    label = "Birthday",
                    value = formatDate(birthday)
                )
            }

            if (contactNumber.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                InfoField(
                    icon = Icons.Default.Phone,
                    label = "Contact Number",
                    value = contactNumber
                )
            }

            if (completeAddress.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                InfoField(
                    icon = Icons.Default.Home,
                    label = "Complete Address",
                    value = completeAddress
                )
            }

            if (riceFieldArea.isNotBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                InfoField(
                    icon = Icons.Default.Agriculture,
                    label = "Primary Rice Field Area",
                    value = "$riceFieldArea hectares"
                )
            }

            if (emailVerified) {
                Spacer(modifier = Modifier.height(16.dp))
                InfoField(
                    icon = Icons.Default.Verified,
                    label = "Email Status",
                    value = "Verified"
                )
            }
        }
    }
}

@Composable
private fun FarmAreaCard(
    location: String,
    area: Double,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = location,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$area hectares",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Row {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFarmAreasCard(
    onAddClick: () -> Unit
) {
    Card(
        onClick = onAddClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Agriculture,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "No additional farm areas yet",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap 'Add' to create your first farm area",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun AccountActivityCard(
    createdAt: String?,
    updatedAt: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            InfoField(
                icon = Icons.Default.DateRange,
                label = "Member Since",
                value = formatDate(createdAt)
            )

            Spacer(modifier = Modifier.height(16.dp))

            InfoField(
                icon = Icons.Default.Update,
                label = "Last Updated",
                value = formatDate(updatedAt)
            )
        }
    }
}

@Composable
private fun FarmAreaDialog(
    isEdit: Boolean,
    location: String,
    onLocationChange: (String) -> Unit,
    size: String,
    onSizeChange: (String) -> Unit,
    isLoading: Boolean,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEdit) "Edit Farm Area" else "Add Farm Area",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEdit) "Update farm details" else "Add a new farm location",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Farm Location
                OutlinedTextField(
                    value = location,
                    onValueChange = onLocationChange,
                    label = { Text("Farm Location *") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                    },
                    placeholder = { Text("e.g., North Field, Barangay XYZ") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Farm Size
                OutlinedTextField(
                    value = size,
                    onValueChange = onSizeChange,
                    label = { Text("Farm Size (hectares) *") },
                    leadingIcon = {
                        Icon(Icons.Default.Agriculture, contentDescription = null)
                    },
                    placeholder = { Text("e.g., 2.5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                if (isEdit) Icons.Default.Save else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEdit) "Update" else "Add")
                    }
                }
            }
        }
    }
}

@Composable
private fun FallbackProfileView(
    context: android.content.Context,
    onLoadProfile: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = TokenManager.getUserName(context) ?: "Unknown User",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = TokenManager.getUserEmail(context) ?: "",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLoadProfile,
            modifier = Modifier.fillMaxWidth(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Load Profile Details")
        }
    }
}

@Composable
fun InfoField(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            modifier = Modifier.size(40.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

fun formatDate(dateString: String?): String {
    if (dateString == null) return "N/A"

    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)

        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        date?.let { outputFormat.format(it) } ?: "N/A"
    } catch (e: Exception) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)

            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            date?.let { outputFormat.format(it) } ?: "N/A"
        } catch (e: Exception) {
            dateString
        }
    }
}
