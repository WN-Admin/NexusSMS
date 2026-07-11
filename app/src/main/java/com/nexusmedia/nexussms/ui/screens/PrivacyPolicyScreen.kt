package com.nexusmedia.nexussms.ui.screens

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy Policy") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Privacy Policy for NexusSMS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Last updated: July 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            PrivacySection(
                title = "1. Information We Collect",
                body = "NexusSMS processes SMS and MMS messages on your device. All message data is stored locally on your device and is not transmitted to any external servers unless you explicitly enable cloud backup features."
            )
            PrivacySection(
                title = "2. Message Data",
                body = "SMS and MMS messages are stored in an encrypted local database. Messages are encrypted using AES-256-GCM encryption when encryption is enabled. Message content is never shared with third parties."
            )
            PrivacySection(
                title = "3. Contacts",
                body = "NexusSMS accesses your contacts to display contact names alongside phone numbers. Contact information is stored locally and is not transmitted externally."
            )
            PrivacySection(
                title = "4. Location",
                body = "Location sharing is only performed when you explicitly choose to share your location. Location data is shared as a Google Maps link with your intended recipient only."
            )
            PrivacySection(
                title = "5. Cloud Backup",
                body = "If you enable Google Drive backup, your encrypted message data is uploaded to your personal Google Drive. Backup data is encrypted before upload. You can disable cloud backup at any time."
            )
            PrivacySection(
                title = "6. Analytics",
                body = "NexusSMS does not collect analytics data. NexusSMS does not use tracking technologies. NexusSMS does not serve advertisements."
            )
            PrivacySection(
                title = "7. Security",
                body = "App lock protection using PIN, pattern, or biometrics. End-to-end encryption for message content. Encrypted storage for sensitive settings."
            )
            PrivacySection(
                title = "8. Third-Party Services",
                body = "Google Drive API (for backup, only if enabled by you). No other third-party services are used."
            )
            PrivacySection(
                title = "9. Children's Privacy",
                body = "NexusSMS is not directed at children under 13. We do not knowingly collect information from children."
            )
            PrivacySection(
                title = "10. Changes to This Policy",
                body = "We may update this privacy policy from time to time. Changes will be posted in the app and on our website."
            )
            PrivacySection(
                title = "11. Contact",
                body = "For questions about this privacy policy, contact us at:\nadmin@watchnexus.ca\nNexus Media, Canada"
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    Text(
        text = body,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}
