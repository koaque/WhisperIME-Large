package com.whispertflite.ui.privacy

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.whispertflite.ui.theme.*
import com.whispertflite.ui.components.*

@Composable
fun PrivacyConsentScreen(
    onPrivacyAccepted: () -> Unit,
    onPrivacyRejected: () -> Unit,
    viewModel: PrivacyConsentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(WhisperDesignTokens.SpacingM),
        verticalArrangement = Arrangement.spacedBy(WhisperDesignTokens.SpacingM)
    ) {
        // Header
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingM))
                
                Text(
                    text = "Privacy & Terms",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "Your privacy is our priority. Please review and customize your preferences.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = WhisperDesignTokens.SpacingS)
                )
            }
        }
        
        // Privacy Highlights
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(WhisperDesignTokens.SpacingM)
                ) {
                    Text(
                        text = "🔒 Privacy-First Design",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingS))
                    
                    PrivacyHighlight(
                        icon = "✅",
                        title = "Voice stays on your device",
                        subtitle = "No voice data is ever transmitted to external servers"
                    )
                    
                    PrivacyHighlight(
                        icon = "✅", 
                        title = "No data collection by default",
                        subtitle = "All analytics, telemetry, and logging are opt-in only"
                    )
                    
                    PrivacyHighlight(
                        icon = "✅",
                        title = "Complete control",
                        subtitle = "You can disable all data collection and still use the app fully"
                    )
                    
                    PrivacyHighlight(
                        icon = "✅",
                        title = "Battery optimized",
                        subtitle = "Minimal logging by default to preserve battery life"
                    )
                }
            }
        }
        
        // Optional Data Collection
        item {
            Text(
                text = "Optional Features (Your Choice)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(WhisperDesignTokens.SpacingM)
                ) {
                    OptionalFeatureToggle(
                        icon = Icons.Default.Analytics,
                        title = "Anonymous Usage Analytics",
                        subtitle = "Help improve the app with anonymous usage statistics (no voice data)",
                        checked = uiState.analyticsEnabled,
                        onCheckedChange = { viewModel.setAnalyticsEnabled(it) }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = WhisperDesignTokens.SpacingS))
                    
                    OptionalFeatureToggle(
                        icon = Icons.Default.BugReport,
                        title = "Crash Reporting",
                        subtitle = "Send anonymous crash reports to help fix bugs",
                        checked = uiState.crashReportingEnabled,
                        onCheckedChange = { viewModel.setCrashReportingEnabled(it) }
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = WhisperDesignTokens.SpacingS))
                    
                    Text(
                        text = "Logging Level (Battery Impact)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingS))
                    
                    LoggingLevelSelector(
                        selectedLevel = uiState.loggingLevel,
                        onLevelSelected = { viewModel.setLoggingLevel(it) }
                    )
                }
            }
        }
        
        // Legal Documents
        item {
            Text(
                text = "Legal Documents",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Card {
                Column(
                    modifier = Modifier.padding(WhisperDesignTokens.SpacingM)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Privacy Policy",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Row {
                            TextButton(onClick = { viewModel.showPrivacyPolicy() }) {
                                Text("Read")
                            }
                            Checkbox(
                                checked = uiState.privacyPolicyAccepted,
                                onCheckedChange = { viewModel.setPrivacyPolicyAccepted(it) }
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.padding(vertical = WhisperDesignTokens.SpacingS))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Terms of Use",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Row {
                            TextButton(onClick = { viewModel.showTermsOfUse() }) {
                                Text("Read")
                            }
                            Checkbox(
                                checked = uiState.termsAccepted,
                                onCheckedChange = { viewModel.setTermsAccepted(it) }
                            )
                        }
                    }
                }
            }
        }
        
        // Quick Setup Options
        item {
            Text(
                text = "Quick Setup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(WhisperDesignTokens.SpacingS)
            ) {
                OutlinedButton(
                    onClick = { viewModel.enableMaximumPrivacy() },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null
                        )
                        Text("Maximum Privacy")
                        Text(
                            "Best battery",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                OutlinedButton(
                    onClick = { viewModel.enableRecommendedSettings() },
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Balance,
                            contentDescription = null
                        )
                        Text("Recommended")
                        Text(
                            "Balanced",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        // Action Buttons
        item {
            Column {
                Button(
                    onClick = {
                        if (viewModel.canProceed()) {
                            viewModel.acceptTerms()
                            onPrivacyAccepted()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = viewModel.canProceed()
                ) {
                    Text("Continue with These Settings")
                }
                
                Spacer(modifier = Modifier.height(WhisperDesignTokens.SpacingS))
                
                OutlinedButton(
                    onClick = onPrivacyRejected,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Exit App")
                }
                
                Text(
                    text = "You must accept the Privacy Policy and Terms of Use to continue.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = WhisperDesignTokens.SpacingS)
                )
            }
        }
    }
}

@Composable
private fun PrivacyHighlight(
    icon: String,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            modifier = Modifier.padding(end = WhisperDesignTokens.SpacingS)
        )
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OptionalFeatureToggle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(WhisperDesignTokens.SpacingM))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun LoggingLevelSelector(
    selectedLevel: String,
    onLevelSelected: (String) -> Unit
) {
    Column {
        val levels = listOf(
            "DISABLED" to "🔋 Disabled (Best battery)",
            "ERROR_ONLY" to "🔋 Errors only (Excellent battery)", 
            "MINIMAL" to "🔋 Minimal (Good battery)"
        )
        
        levels.forEach { (level, description) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLevelSelected(level) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedLevel == level,
                    onClick = { onLevelSelected(level) }
                )
                
                Text(
                    text = description,
                    modifier = Modifier.padding(start = WhisperDesignTokens.SpacingS)
                )
            }
        }
    }
}