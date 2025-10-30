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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zynt.sumviltadconnect.data.model.Event
import com.zynt.sumviltadconnect.data.repository.FarmerRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.shape.RoundedCornerShape

@Stable
private class EventDetailsViewModel(private val repo: FarmerRepository = FarmerRepository()): ViewModel() {
    var uiState by mutableStateOf<EventDetailsUiState>(EventDetailsUiState.Loading)
        private set

    fun load(id: Int) {
        uiState = EventDetailsUiState.Loading
        viewModelScope.launch {
            try {
                val response = repo.getEventDetails(id)
                if (response.isSuccessful) {
                    val eventResponse = response.body()
                    if (eventResponse != null && eventResponse.data != null) {  // Changed from eventResponse.event to eventResponse.data
                        uiState = EventDetailsUiState.Success(eventResponse.data)  // Changed from eventResponse.event to eventResponse.data
                    } else {
                        uiState = EventDetailsUiState.Error("Event not found")
                    }
                } else {
                    uiState = EventDetailsUiState.Error("Failed to load event: ${response.message()}")
                }
            } catch (e: Exception) {
                uiState = EventDetailsUiState.Error(e.message ?: "Failed to load event")
            }
        }
    }
}

private sealed class EventDetailsUiState {
    object Loading: EventDetailsUiState()
    data class Success(val event: Event): EventDetailsUiState()
    data class Error(val message: String): EventDetailsUiState()
}

@Composable
fun EventDetailsScreen(eventId: Int, onBack: () -> Unit) {
    val vm = remember { EventDetailsViewModel() }
    val state = vm.uiState
    LaunchedEffect(eventId) { vm.load(eventId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when(state) {
                EventDetailsUiState.Loading -> DetailsLoadingSkeleton()
                is EventDetailsUiState.Error -> DetailsError(state.message) { vm.load(eventId) }
                is EventDetailsUiState.Success -> DetailsContent(state.event)
            }
        }
    }
}

@Composable
private fun DetailsLoadingSkeleton() {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        repeat(5) {
            Box(Modifier.fillMaxWidth().height(24.dp).shimmerBar())
            Spacer(Modifier.height(12.dp))
        }
        Spacer(Modifier.height(24.dp))
        repeat(6) {
            Box(Modifier.fillMaxWidth().height(14.dp).shimmerBar())
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DetailsError(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
private fun DetailsContent(event: Event) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scroll)
    ) {
        HeaderSection(event)
        BodySection(event)
    }
}

@Composable
private fun HeaderSection(event: Event) {
    val date = parseDate(event.eventDate)
    val end = parseDate(event.endDate)
    val dfDate = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val tf = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            Modifier.fillMaxWidth().background(
                Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer))
            ).padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Event, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
                Spacer(Modifier.width(12.dp))
                Text(event.title, style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, color = Color.White), maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Spacer(Modifier.height(12.dp))
            date?.let {
                Text(dfDate.format(it), color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.Medium)
                Text(
                    buildString {
                        append(tf.format(it))
                        end?.let { e -> append(" – ").append(tf.format(e)) }
                    },
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun BodySection(event: Event) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        InfoRow(icon = Icons.Default.Schedule, label = "Starts", value = event.eventDate ?: "—")
        event.endDate?.let { InfoRow(icon = Icons.Default.Schedule, label = "Ends", value = it) }
        event.location?.let { InfoRow(icon = Icons.Default.LocationOn, label = "Location", value = it) }
        event.organizer?.let { InfoRow(icon = Icons.Default.Person, label = "Organizer", value = it) }
        event.category?.let { InfoRow(icon = Icons.Default.Category, label = "Category", value = it) }

        Spacer(Modifier.height(16.dp))
        Text("Description", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(event.description ?: "No description provided.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        }
    }
}

private fun parseDate(s: String?): Date? = try { if (s==null) null else SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(s) } catch (_:Exception){ null }

@Composable
private fun Modifier.shimmerBar(): Modifier {
    val transition = rememberInfiniteTransition()
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(animation = tween(900), repeatMode = RepeatMode.Reverse)
    )
    return this.background(Color.Gray.copy(alpha = alpha), shape = RoundedCornerShape(6.dp))
}
