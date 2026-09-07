// ABOUTME: About screen showing the app version, maker, and where the legal documents live
// ABOUTME: Watches cannot open links well, so the addresses are shown as text to type elsewhere
package com.buswatch.ui.screens

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.buswatch.LegalLinks

@Composable
fun AboutScreen(
    versionName: String,
    onBack: () -> Unit
) {
    val view = LocalView.current

    ScalingLazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = "BusWatch $versionName",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
        item {
            Text(
                text = "Made by Attention Feed, Inc. Bus data from Transport for London. " +
                    "Your location is sent only to TfL to find nearby stops.",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        item { LegalLink(label = "Privacy policy", url = LegalLinks.PRIVACY_POLICY_URL) }
        item { LegalLink(label = "Terms of service", url = LegalLinks.TERMS_OF_SERVICE_URL) }
        item {
            Text(
                text = "Contact: ${LegalLinks.CONTACT_EMAIL}",
                style = MaterialTheme.typography.caption2,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        item {
            Chip(
                onClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                    onBack()
                },
                label = { Text(text = "Back") },
                colors = ChipDefaults.secondaryChipColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp)
            )
        }
    }
}

@Composable
private fun LegalLink(label: String, url: String) {
    Text(
        text = "$label:\n$url",
        style = MaterialTheme.typography.caption2,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
    )
}
