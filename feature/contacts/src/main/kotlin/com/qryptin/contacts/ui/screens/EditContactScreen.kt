package com.qryptin.contacts.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.contacts.viewmodel.EditContactViewModel
import com.qryptin.core.designsystem.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  EditContactScreen
//  Issue 3 fix: nickname, phone number, and email couldn't be
//  edited after a contact was saved. This screen lets the user
//  change all three, persists the update to Room, then shows a
//  confirmation Snackbar and auto-navigates back.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContactScreen(
    contactId : String,
    onBack    : () -> Unit,
    onSaved   : () -> Unit,
    viewModel : EditContactViewModel = viewModel(),
) {
    LaunchedEffect(contactId) { viewModel.load(contactId) }

    val contact    by viewModel.contact.collectAsState()
    val isLoading  by viewModel.isLoading.collectAsState()
    val isSaving   by viewModel.isSaving.collectAsState()
    val saveError  by viewModel.saveError.collectAsState()
    val saveSuccess by viewModel.saveSuccess.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var nickname by remember { mutableStateOf<String?>(null) }
    var phone by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }

    // Seed the editable fields once the contact loads (only once, so the
    // user's in-progress edits aren't clobbered by later recompositions).
    LaunchedEffect(contact) {
        contact?.let { detail ->
            if (nickname == null) nickname = detail.nickname ?: ""
            if (phone == null) phone = detail.phoneNumber
            if (email == null) email = detail.email ?: ""
        }
    }

    LaunchedEffect(saveSuccess) {
        if (saveSuccess) {
            // Show the confirmation without awaiting its full dismissal
            // (which defaults to several seconds) so "auto navigate back"
            // actually happens promptly, matching the requested flow.
            launch { snackbarHostState.showSnackbar("Contact updated successfully") }
            viewModel.consumeSaveSuccess()
            delay(700)
            onSaved()
        }
    }
    LaunchedEffect(saveError) {
        saveError?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.dismissError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                title = {
                    Text(
                        "Edit contact",
                        style = TypoTitleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    TextButton(
                        enabled = !isSaving && contact != null,
                        onClick = {
                            viewModel.save(
                                nickname = nickname.orEmpty(),
                                phone    = phone.orEmpty(),
                                email    = email.orEmpty(),
                            )
                        },
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier    = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color       = MaterialTheme.colorScheme.primary,
                            )
                        } else {
                            Text(
                                "Save",
                                style      = TypoLabelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color      = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier          = Modifier.fillMaxSize().padding(padding),
            contentAlignment  = Alignment.TopCenter,
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier.padding(top = QryptDimens.SpaceXL),
                    color    = MaterialTheme.colorScheme.primary,
                )
                contact == null -> Text(
                    "Contact not found",
                    style    = TypoBodyLarge,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = QryptDimens.SpaceXL),
                )
                else -> Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = QryptDimens.PaddingScreenH, vertical = QryptDimens.SpaceLG),
                ) {
                    OutlinedTextField(
                        value         = nickname.orEmpty(),
                        onValueChange = { nickname = it },
                        label         = { Text("Nickname") },
                        placeholder   = { Text(contact!!.originalQryptName ?: "Optional") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(QryptDimens.SpaceMD))

                    OutlinedTextField(
                        value         = phone.orEmpty(),
                        onValueChange = { phone = it },
                        label         = { Text("Phone number") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(QryptDimens.SpaceMD))

                    OutlinedTextField(
                        value         = email.orEmpty(),
                        onValueChange = { email = it },
                        label         = { Text("Email") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                    )

                    if (contact!!.isQryptUser && !contact!!.originalQryptName.isNullOrBlank()) {
                        Spacer(Modifier.height(QryptDimens.SpaceMD))
                        Text(
                            "Leave nickname blank to show \"${contact!!.originalQryptName}\" (their registered QryptIN name).",
                            style = TypoBodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
