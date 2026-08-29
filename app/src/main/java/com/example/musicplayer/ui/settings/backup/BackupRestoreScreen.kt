package com.example.musicplayer.ui.settings.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayer.ui.components.AppCard
import com.example.musicplayer.ui.components.ScreenHeader
import com.example.musicplayer.ui.theme.AppColors

@Composable
fun BackupRestoreScreen(
    onBack: () -> Unit,
    viewModel: BackupRestoreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let(viewModel::exportTo) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let(viewModel::readAndValidate) }

    LaunchedEffect(uiState) {
        statusMessage = when (val state = uiState) {
            BackupRestoreUiState.ExportSuccess -> "Backup exported successfully."
            BackupRestoreUiState.ExportFailed -> "Couldn't export the backup. Please try again."
            BackupRestoreUiState.ImportInvalid -> "That file isn't a valid backup."
            is BackupRestoreUiState.ImportUnsupportedVersion ->
                "This backup was created by a newer app version (format v${state.foundVersion}) and can't be restored here."
            BackupRestoreUiState.ImportSuccess -> "Backup restored successfully."
            else -> null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
    ) {
        ScreenHeader(title = "Backup & Restore", onBack = onBack)

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AppCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        "What's included",
                        color = AppColors.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Favorite tracks, equalizer settings, appearance and language preferences, and your sleep timer default. Your music files are never included.",
                        color = AppColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Button(
                onClick = {
                    val fileName = "musicplayer_backup_${System.currentTimeMillis()}.json"
                    exportLauncher.launch(fileName)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Purple),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Export Backup", color = Color.White, fontWeight = FontWeight.SemiBold)
            }

            Button(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.SurfaceAlt),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text("Import Backup", color = AppColors.TextPrimary, fontWeight = FontWeight.SemiBold)
            }

            statusMessage?.let { message ->
                Text(message, color = AppColors.TextSecondary, fontSize = 13.sp)
            }
        }
    }

    val currentState = uiState
    if (currentState is BackupRestoreUiState.AwaitingImportConfirmation) {
        ImportConfirmationDialog(
            favoriteCount = currentState.favoriteCount,
            onConfirm = { replace -> viewModel.confirmImport(currentState.data, replace) },
            onDismiss = viewModel::dismissMessage
        )
    }
}

@Composable
private fun ImportConfirmationDialog(
    favoriteCount: Int,
    onConfirm: (replaceFavorites: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var replaceFavorites by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColors.SurfaceElevated,
        title = { Text("Restore backup?", color = AppColors.TextPrimary) },
        text = {
            Column {
                Text(
                    "This backup contains $favoriteCount favorite track${if (favoriteCount == 1) "" else "s"} plus your equalizer and app preferences. Nothing will change until you confirm.",
                    color = AppColors.TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(16.dp))
                FavoritesMergeOption(
                    label = "Merge with current favorites",
                    selected = !replaceFavorites,
                    onClick = { replaceFavorites = false }
                )
                FavoritesMergeOption(
                    label = "Replace current favorites",
                    selected = replaceFavorites,
                    onClick = { replaceFavorites = true }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(replaceFavorites) }) {
                Text("Restore", color = AppColors.Purple, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = AppColors.TextSecondary)
            }
        }
    )
}

@Composable
private fun FavoritesMergeOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = AppColors.Purple, unselectedColor = AppColors.TextTertiary)
        )
        Text(label, color = AppColors.TextPrimary, fontSize = 13.sp)
    }
}
