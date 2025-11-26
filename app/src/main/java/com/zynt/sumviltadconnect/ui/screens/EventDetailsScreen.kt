@file:OptIn(ExperimentalMaterial3Api::class)

package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.BorderStroke
import com.zynt.sumviltadconnect.data.model.Event
import com.zynt.sumviltadconnect.data.repository.FarmerRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.zynt.sumviltadconnect.ui.theme.AppDimensions

import android.widget.Toast

@Stable
class EventDetailsViewModel(private val repo: FarmerRepository = FarmerRepository()): ViewModel() {
    var uiState by mutableStateOf<EventDetailsUiState>(EventDetailsUiState.Loading)
        private set
    
    var participationStatus by mutableStateOf<String?>(null)
        private set
        
    var isParticipatingLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun clearError() {
        errorMessage = null
    }

    fun load(id: Int) {
        uiState = EventDetailsUiState.Loading
        viewModelScope.launch {
            try {
                val response = repo.getEventDetails(id)
                if (response.isSuccessful) {
                    val eventResponse = response.body()
                    if (eventResponse != null && eventResponse.data != null) {
                        val event = eventResponse.data
                        uiState = EventDetailsUiState.Success(event)
                        participationStatus = event.userParticipation
                    } else {
                        uiState = EventDetailsUiState.Error("Event data not found")
                    }
                } else {
                    uiState = EventDetailsUiState.Error("Failed to load event: ${response.message()}")
                }
            } catch (e: Exception) {
                uiState = EventDetailsUiState.Error(e.message ?: "Failed to load event")
            }
        }
    }
    
    fun participate(eventId: Int, status: String) {
        isParticipatingLoading = true
        viewModelScope.launch {
            try {
                repo.participateInEvent(eventId, status)
                participationStatus = status
            } catch (e: Exception) {
                errorMessage = "Failed to update participation: ${e.message}"
            } finally {
                isParticipatingLoading = false
            }
        }
    }
}

sealed class EventDetailsUiState {
    object Loading: EventDetailsUiState()
    data class Success(val event: Event): EventDetailsUiState()
    data class Error(val message: String): EventDetailsUiState()
}

@Composable
fun EventDetailsScreen(
    eventId: Int,
    onNavigateBack: () -> Unit
) {
    val viewModel: EventDetailsViewModel = viewModel()
    val uiState = viewModel.uiState
    val context = LocalContext.current

    LaunchedEffect(eventId) {
        viewModel.load(eventId)
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            when (uiState) {
                is EventDetailsUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is EventDetailsUiState.Success -> {
                    EventDetailsContent(
                        event = uiState.event,
                        participationStatus = viewModel.participationStatus,
                        isParticipatingLoading = viewModel.isParticipatingLoading,
                        onParticipate = { status -> viewModel.participate(eventId, status) },
                        onNavigateBack = onNavigateBack
                    )
                }
                is EventDetailsUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = uiState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.load(eventId) }) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetailsContent(
    event: Event,
    participationStatus: String?,
    isParticipatingLoading: Boolean,
    onParticipate: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val eventStatus = getEventStatus(event)
    val isEventPast = eventStatus == "Past" || eventStatus == "Completed" || eventStatus == "Cancelled"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        EnhancedHeaderSection(
            title = event.title,
            type = event.category ?: "Event",
            status = eventStatus,
            onNavigateBack = onNavigateBack
        )
        
        EnhancedBodySection(
            event = event,
            participationStatus = participationStatus,
            isParticipatingLoading = isParticipatingLoading,
            isEventPast = isEventPast,
            onParticipate = onParticipate
        )
    }
}

@Composable
private fun EnhancedHeaderSection(
    title: String,
    type: String,
    status: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            )
            .padding(AppDimensions.paddingLarge())
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                StatusChip(status = status)
            }
            
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
            
            Badge(
                text = type,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            
            Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
            
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EnhancedBodySection(
    event: Event,
    participationStatus: String?,
    isParticipatingLoading: Boolean,
    isEventPast: Boolean,
    onParticipate: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppDimensions.paddingMedium()),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
    ) {
        // Date and Time Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.paddingMedium())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                    Text(
                        text = "Date & Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                
                Text(
                    text = formatEventDateTime(event.eventDate, event.endDate),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Location Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.paddingMedium())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                
                Text(
                    text = event.location ?: "No location provided",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Description Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(AppDimensions.paddingMedium())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(AppDimensions.paddingSmall()))
                    Text(
                        text = "Description",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(AppDimensions.paddingSmall()))
                
                Text(
                    text = event.description ?: "No description provided.",
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp
                )
            }
        }
        
        // Participation Card
        ParticipationCard(
            status = participationStatus,
            isLoading = isParticipatingLoading,
            isEventPast = isEventPast,
            onParticipate = onParticipate
        )
    }
}

@Composable
private fun ParticipationCard(
    status: String?,
    isLoading: Boolean,
    isEventPast: Boolean,
    onParticipate: (String) -> Unit
) {
    val isAttending = status == "attending" || status == "accepted"
    val isNotAttending = status == "not_attending" || status == "declined"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(AppDimensions.paddingMedium()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isEventPast) "This event has ended" else "Will you attend this event?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(AppDimensions.paddingMedium()))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.paddingMedium())
            ) {
                // Not Going Button
                OutlinedButton(
                    onClick = { onParticipate("not_attending") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && !isEventPast,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isNotAttending) MaterialTheme.colorScheme.errorContainer else Color.Transparent,
                        contentColor = if (isNotAttending) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(
                        1.dp, 
                        if (isNotAttending) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    )
                ) {
                    if (isLoading && isNotAttending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    } else {
                        Text("Not Going")
                    }
                }
                
                // Going Button
                Button(
                    onClick = { onParticipate("attending") },
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading && !isEventPast,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAttending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                ) {
                    if (isLoading && isAttending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        if (isAttending) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Going")
                        } else {
                            Text("Attend")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Badge(
    text: String,
    containerColor: Color,
    contentColor: Color
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun StatusChip(status: String) {
    val (color, text) = when (status.lowercase()) {
        "upcoming" -> MaterialTheme.colorScheme.primaryContainer to "Upcoming"
        "ongoing" -> MaterialTheme.colorScheme.tertiaryContainer to "Ongoing"
        "completed" -> MaterialTheme.colorScheme.secondaryContainer to "Completed"
        "cancelled" -> MaterialTheme.colorScheme.errorContainer to "Cancelled"
        else -> MaterialTheme.colorScheme.surfaceVariant to status.replaceFirstChar { it.uppercase() }
    }
    
    Surface(
        color = color,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getEventStatus(event: Event): String {
    val eventDate = parseEventDate(event.eventDate) ?: return "Upcoming"
    val now = Date()
    
    // If we have an end date, use it to determine if event is past
    val endDate = parseEventDate(event.endDate)
    if (endDate != null) {
        if (endDate.before(now)) return "Past"
        if (eventDate.before(now) && endDate.after(now)) return "Ongoing"
        return "Upcoming"
    }
    
    // If no end date, assume it's past if start date was before today (ignoring time for simplicity if needed, but here using full date time)
    // Or if start date is significantly in the past (e.g. 24 hours ago)
    // For now, strict comparison
    if (eventDate.before(now)) return "Past"
    
    return "Upcoming"
}

private fun parseEventDate(dateString: String?): Date? {
    if (dateString == null) return null
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return try {
        format.parse(dateString)
    } catch (e: Exception) {
        try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

private fun formatEventDateTime(dateStr: String, endDateStr: String?): String {
    return try {
        // Try parsing with time first
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val startDate = try {
            dateTimeFormat.parse(dateStr)
        } catch (e: Exception) {
            dateFormat.parse(dateStr)
        } ?: return dateStr

        // Format date: "Thursday, December 4, 2025"
        val dateOutputFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        val formattedDate = dateOutputFormat.format(startDate)
        
        // Format time: "4:00 PM"
        val timeOutputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedStartTime = timeOutputFormat.format(startDate)
        
        var result = "$formattedDate\n$formattedStartTime"
        
        if (endDateStr != null) {
            val endDate = try {
                dateTimeFormat.parse(endDateStr)
            } catch (e: Exception) {
                dateFormat.parse(endDateStr)
            }
            
            if (endDate != null) {
                val formattedEndTime = timeOutputFormat.format(endDate)
                result += " - $formattedEndTime"
            }
        }
        
        result
    } catch (e: Exception) {
        // Fallback if parsing fails
        dateStr + (if (endDateStr != null) " - $endDateStr" else "")
    }
}
