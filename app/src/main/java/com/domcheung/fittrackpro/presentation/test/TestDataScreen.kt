package com.domcheung.fittrackpro.presentation.test

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.domcheung.fittrackpro.ui.theme.HandDrawnShapes

/**
 * Screen for testing data layer functionality
 * This is a temporary screen for development and testing
 */
@Composable
fun TestDataScreen(
    viewModel: TestDataViewModel = hiltViewModel()
) {
    val testState by viewModel.testState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        TestScreenHeader()

        // Status Card
        TestStatusCard(testState = testState)

        // Test Buttons
        TestButtonsSection(
            onRunTests = { viewModel.runDataLayerTests() },
            onClearData = { viewModel.clearTestData() },
            onTestSession = { viewModel.testWorkoutSession() },
            isLoading = testState.isLoading
        )

        // Instructions Card
        TestInstructionsCard()
    }
}

@Composable
private fun TestScreenHeader() {
    Column {
        Text(
            text = "🧪 Data Layer Test",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Test database operations and API integration",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TestStatusCard(testState: TestDataState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant1),
        colors = CardDefaults.cardColors(
            containerColor = when {
                testState.isError -> MaterialTheme.colorScheme.errorContainer
                testState.isSuccess -> MaterialTheme.colorScheme.tertiaryContainer
                testState.isLoading -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when {
                        testState.isError -> Icons.Default.Error
                        testState.isSuccess -> Icons.Default.CheckCircle
                        testState.isLoading -> Icons.Default.Refresh
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = when {
                        testState.isError -> MaterialTheme.colorScheme.error
                        testState.isSuccess -> MaterialTheme.colorScheme.tertiary
                        testState.isLoading -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Test Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (testState.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = testState.message.ifEmpty { "Ready to run tests" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (testState.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Error: ${testState.error.localizedMessage}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TestButtonsSection(
    onRunTests: () -> Unit,
    onClearData: () -> Unit,
    onTestSession: () -> Unit,
    isLoading: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Test Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Run Full Tests Button
        Button(
            onClick = onRunTests,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .clip(HandDrawnShapes.buttonDefault)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Run All Data Tests")
        }

        // Test Session Button
        OutlinedButton(
            onClick = onTestSession,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .clip(HandDrawnShapes.buttonDefault)
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Workout Session")
        }

        // Clear Data Button
        OutlinedButton(
            onClick = onClearData,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .clip(HandDrawnShapes.buttonDefault),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear All Test Data")
        }
    }
}

@Composable
private fun TestInstructionsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HandDrawnShapes.cardVariant2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Testing Instructions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            val instructions = listOf(
                "1. Run 'Run All Data Tests' to verify database operations",
                "2. Check the status messages to see test progress",
                "3. 'Test Workout Session' tests session creation",
                "4. Use 'Clear All Test Data' to clean up after testing",
                "5. Check Android Studio logs for detailed error messages"
            )

            instructions.forEach { instruction ->
                Text(
                    text = instruction,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Note: This is a development tool. Remove from production builds.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}