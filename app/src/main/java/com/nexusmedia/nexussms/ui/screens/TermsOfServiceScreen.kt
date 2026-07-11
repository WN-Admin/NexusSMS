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
fun TermsOfServiceScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms of Service") },
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
                text = "Terms of Service for NexusSMS",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Last updated: July 2026",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            TosSection(
                title = "1. Acceptance of Terms",
                body = "By installing and using NexusSMS, you agree to these Terms of Service."
            )
            TosSection(
                title = "2. Description of Service",
                body = "NexusSMS is a messaging application that provides SMS, MMS, and enhanced messaging capabilities for Android devices."
            )
            TosSection(
                title = "3. User Responsibilities",
                body = "You are responsible for the messages you send through NexusSMS. You must comply with all applicable laws when using messaging services. You are responsible for maintaining the security of your app lock credentials."
            )
            TosSection(
                title = "4. Messaging",
                body = "NexusSMS sends SMS and MMS messages through your mobile carrier. Standard carrier charges apply. Message delivery is not guaranteed and depends on carrier network availability."
            )
            TosSection(
                title = "5. RCS Messaging",
                body = "RCS features depend on carrier support and device compatibility. RCS messages may not be available for all contacts."
            )
            TosSection(
                title = "6. Cloud Backup",
                body = "Cloud backup is optional and stores encrypted data on your Google Drive. You are responsible for your Google Drive account security. We cannot recover your backup if you lose access to your Google Drive account."
            )
            TosSection(
                title = "7. Encryption",
                body = "AES-256 encryption is available for message content. Encryption keys are stored locally on your device. We cannot decrypt your messages if you lose your encryption key."
            )
            TosSection(
                title = "8. Intellectual Property",
                body = "NexusSMS and all associated trademarks are the property of Nexus Media."
            )
            TosSection(
                title = "9. Limitation of Liability",
                body = "NexusSMS is provided \"as is\" without warranties. Nexus Media is not liable for any damages arising from the use of this application."
            )
            TosSection(
                title = "10. Termination",
                body = "You may stop using NexusSMS at any time by uninstalling the application."
            )
            TosSection(
                title = "11. Governing Law",
                body = "These terms are governed by the laws of Canada."
            )
            TosSection(
                title = "12. Contact",
                body = "For questions about these terms, contact:\nadmin@watchnexus.ca\nNexus Media, Canada"
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TosSection(title: String, body: String) {
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
