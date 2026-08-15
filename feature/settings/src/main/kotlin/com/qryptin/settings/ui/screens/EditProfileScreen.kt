package com.qryptin.settings.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.qryptin.settings.viewmodel.ProfileViewModel
import com.qryptin.core.designsystem.ui.theme.QryptINSafeScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  EditProfileScreen
//
//  Issue 3 fix: previously there was no way to edit name, bio,
//  username, email, or avatar at all. This screen lets the user
//  change all of them; ProfileRepository persists the fields via
//  DataStore and copies a picked avatar into app-private storage
//  so it's available everywhere the app shows a profile photo.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack    : () -> Unit,
    viewModel : ProfileViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var displayName by remember(uiState.profile.displayName) { mutableStateOf(uiState.profile.displayName) }
    var username by remember(uiState.profile.username) { mutableStateOf(uiState.profile.username) }
    var bio by remember(uiState.profile.bio) { mutableStateOf(uiState.profile.bio) }
    var email by remember(uiState.profile.email) { mutableStateOf(uiState.profile.email) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(viewModel::updateAvatar) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            // Fire the snackbar without awaiting its full dismissal (which
            // defaults to several seconds) — show it briefly, then move on,
            // so "auto navigate back" doesn't feel stuck on this screen.
            launch { snackbarHostState.showSnackbar("Profile updated successfully") }
            viewModel.consumeSaveSuccess()
            delay(700)
            onBack()
        }
    }
    LaunchedEffect(uiState.saveError) {
        uiState.saveError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }

    // Issue 2 fix: safe-area-aware scaffold for the Profile screen.
    QryptINSafeScaffold(
        topBar = {
            TopAppBar(
                title          = { Text("Edit profile", fontWeight = FontWeight.SemiBold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Rounded.ArrowBackIosNew, null) } },
                actions = {
                    TextButton(
                        enabled = !uiState.isSaving,
                        onClick = { viewModel.saveProfile(displayName, username, bio, email) },
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Save")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { avatarPicker.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    if (uiState.profile.avatarUri != null) {
                        AsyncImage(
                            model = uiState.profile.avatarUri,
                            contentDescription = "Profile photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                        )
                    } else {
                        Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(48.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable { avatarPicker.launch("image/*") },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = "Change photo", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.height(28.dp))

            OutlinedTextField(
                value = displayName, onValueChange = { displayName = it },
                label = { Text("Display name") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = username, onValueChange = { username = it },
                label = { Text("QryptIN ID") }, singleLine = true,
                prefix = { Text("@") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = bio, onValueChange = { if (it.length <= 140) bio = it },
                label = { Text("Bio") }, minLines = 2, maxLines = 4,
                supportingText = { Text("${bio.length}/140") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text("Email") }, singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
