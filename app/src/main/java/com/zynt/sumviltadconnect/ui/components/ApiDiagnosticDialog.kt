package com.zynt.sumviltadconnect.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zynt.sumviltadconnect.data.network.ApiClient
import com.zynt.sumviltadconnect.utils.ApiEndpointTester
import kotlinx.coroutines.launch

@Composable
fun ApiDiagnosticDialog(
    onDismiss: () -> Unit,
    onFixApplied: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(false) }
    var testResults by remember { mutableStateOf<List<ApiEndpointTester.EndpointTestResult>>(emptyList()) }
    var suggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var autoFixAttempted by remember { mutableStateOf(false) }
    var autoFixSuccessful by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Text(
                    text = "API Diagnostic Tool",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This tool will diagnose and attempt to fix API connection issues.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                testResults = ApiEndpointTester.testAllEndpoints()
                                suggestions = ApiEndpointTester.diagnoseApiIssues()
                                isLoading = false
                            }
                        },
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Endpoints")
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                autoFixAttempted = true
                                autoFixSuccessful = ApiEndpointTester.attemptAutoFix()
                                if (autoFixSuccessful) {
                                    ApiClient.recreateApiService()
                                    onFixApplied()
                                }
                                // Refresh test results after attempt
                                testResults = ApiEndpointTester.testAllEndpoints()
                                suggestions = ApiEndpointTester.diagnoseApiIssues()
                                isLoading = false
                            }
                        },
                        enabled = !isLoading && testResults.isNotEmpty() && !autoFixSuccessful
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Auto Fix")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Loading indicator
                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Auto-fix result message
                if (autoFixAttempted) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (autoFixSuccessful)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (autoFixSuccessful) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (autoFixSuccessful)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (autoFixSuccessful)
                                    "Auto-fix successful! API connection should now work."
                                else
                                    "Auto-fix unsuccessful. Please follow the suggestions below.",
                                color = if (autoFixSuccessful)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Display current API URL
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Current API URL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ApiClient.getBaseUrl(),
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium
                            )

                            IconButton(onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("API URL", ApiClient.getBaseUrl())
                                clipboard.setPrimaryClip(clip)
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results Section
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    if (testResults.isNotEmpty()) {
                        item {
                            Text(
                                text = "Endpoint Test Results",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(testResults) { result ->
                            EndpointResultCard(result)
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (suggestions.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Suggestions",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }

                            items(suggestions) { suggestion ->
                                Text(
                                    text = suggestion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                // Custom URL input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    var customUrl by remember { mutableStateOf(ApiClient.getBaseUrl()) }

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Set Custom API URL",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        TextField(
                            value = customUrl,
                            onValueChange = { customUrl = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            placeholder = { Text("Enter API URL (e.g., https://fieldconnect.site/api/)") }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            OutlinedButton(
                                onClick = {
                                    ApiClient.setCustomBaseUrl(null)
                                    customUrl = ApiClient.getBaseUrl()
                                    ApiClient.recreateApiService()
                                }
                            ) {
                                Text("Reset")
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    ApiClient.setCustomBaseUrl(customUrl)
                                    ApiClient.recreateApiService()
                                    onFixApplied()
                                    coroutineScope.launch {
                                        isLoading = true
                                        testResults = ApiEndpointTester.testAllEndpoints()
                                        suggestions = ApiEndpointTester.diagnoseApiIssues()
                                        isLoading = false
                                    }
                                }
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Close button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun EndpointResultCard(result: ApiEndpointTester.EndpointTestResult) {
    val backgroundColor = when {
        result.isSuccess -> MaterialTheme.colorScheme.primaryContainer
        result.responseType == ApiEndpointTester.ResponseType.HTML -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val iconTint = when {
        result.isSuccess -> MaterialTheme.colorScheme.primary
        result.responseType == ApiEndpointTester.ResponseType.HTML -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val icon = when {
        result.isSuccess -> Icons.Default.CheckCircle
        result.responseType == ApiEndpointTester.ResponseType.HTML -> Icons.Default.Error
        else -> Icons.Default.Warning
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = result.endpoint,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Status: ${result.statusCode}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Response Type: ${result.responseType}",
                style = MaterialTheme.typography.bodySmall
            )

            if (result.errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Error: ${result.errorMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (result.responseType == ApiEndpointTester.ResponseType.HTML) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "HTML detected instead of JSON. The server may be redirecting to a web page.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
