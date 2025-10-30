package com.zynt.sumviltadconnect.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zynt.sumviltadconnect.ui.components.ApiErrorView
import com.zynt.sumviltadconnect.ui.components.ApiDiagnosticDialog
import com.zynt.sumviltadconnect.viewmodel.ApiDiagnosticViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiDiagnosticScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiDiagnosticViewModel = viewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    var showDiagnosticDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Diagnostic") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDiagnosticDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Advanced Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Current Status Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "API Connection Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Current URL: ${viewModel.getCurrentApiUrl()}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (viewModel.lastTestResult != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Last Test: ${if (viewModel.lastTestResult == true) "✅ Success" else "❌ Failed"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (viewModel.lastTestResult == true)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Actions
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test Connection Button
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.testConnection()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoading
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(Icons.Default.NetworkCheck, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text("Test API Connection")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Try Common Fixes Button
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                viewModel.tryCommonFixes()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoading
                    ) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Try Common Fixes")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Common API URLs to try
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Try Different API URLs",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val commonUrls = listOf(
                        "https://fieldconnect.site/api/",
                        "https://api.fieldconnect.site/",
                        "https://fieldconnect.site/",
                        "https://www.fieldconnect.site/api/"
                    )

                    commonUrls.forEach { url ->
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.testSpecificUrl(url)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            enabled = !viewModel.isLoading
                        ) {
                            Text(url)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Results Section
            if (viewModel.testResults.isNotEmpty() || viewModel.errorMessage != null) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Test Results",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (viewModel.errorMessage != null) {
                            Text(
                                text = viewModel.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (viewModel.testResults.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier.heightIn(max = 200.dp)
                            ) {
                                items(viewModel.testResults) { result ->
                                    Text(
                                        text = "• ${result.endpoint}: ${if (result.isSuccess) "✅" else "❌"} (${result.statusCode})",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDiagnosticDialog) {
        ApiDiagnosticDialog(
            onDismiss = { showDiagnosticDialog = false },
            onFixApplied = {
                coroutineScope.launch {
                    viewModel.testConnection()
                }
            }
        )
    }
}
