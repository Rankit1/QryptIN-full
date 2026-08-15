package com.qryptin.auth.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.qryptin.auth.model.AuthUiState
import com.qryptin.auth.model.QryptinIdAvailability
import com.qryptin.auth.ui.components.*
import com.qryptin.auth.viewmodel.AuthViewModel
import com.qryptin.core.designsystem.ui.theme.*
import com.qryptin.auth.AuthModule
// ─────────────────────────────────────────────────────────────
//  RegisterScreen — new user profile creation
//  Full Name, About/Bio, QryptIN ID (live uniqueness check),
//  Email (optional), Profile Picture (optional, from gallery
//  only — never auto-generated). All fields start empty.
// ─────────────────────────────────────────────────────────────
@Composable
fun RegisterScreen(
    viewModel   : AuthViewModel,
    onRegistered: () -> Unit,
    onBack      : () -> Unit,
) {
    val uiState  by viewModel.uiState.collectAsState()
    val keyboard  = LocalSoftwareKeyboardController.current
    val nameFocus = remember { FocusRequester() }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) viewModel.onProfilePhotoSelected(uri) }

    // Auto-focus name field on screen open
    LaunchedEffect(Unit) { nameFocus.requestFocus() }

    // Pass user name to Firebase repository whenever it changes
    LaunchedEffect(viewModel.fullName) {
        AuthModule.setUserNameForAuth(viewModel.fullName)
    }

    AuthScaffold(
        topBar = {
            Spacer(Modifier.height(12.dp))
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint               = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(12.dp))

            AuthHeader(
                title    = "Create Your Profile",
                subtitle = "Let's set up your account",
            )

            Spacer(Modifier.height(28.dp))

            // ── Profile picture picker ──────────────────────────
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                ProfilePicturePicker(
                    photoUri = viewModel.profilePhotoUri,
                    name     = viewModel.fullName,
                    onClick  = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                )
            }

            Spacer(Modifier.height(32.dp))

            // ── Full Name ────────────────────────────────────────
            LabeledField(label = "Full Name") {
                OutlinedTextField(
                    value          = viewModel.fullName,
                    onValueChange  = viewModel::onFullNameChanged,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocus),
                    placeholder    = { Text("Enter your full name", style = TypoBodyMedium) },
                    singleLine     = true,
                    isError        = viewModel.nameError != null,
                    leadingIcon    = { Icon(Icons.Rounded.Person, null, modifier = Modifier.size(20.dp)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction      = ImeAction.Next,
                    ),
                    shape  = RoundedCornerShape(QryptDimens.RadiusSmall),
                    colors = fieldColors(),
                    textStyle = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                )
                FieldError(viewModel.nameError)
            }

            Spacer(Modifier.height(20.dp))

            // ── About / Bio ──────────────────────────────────────
            LabeledField(label = "About / Bio") {
                OutlinedTextField(
                    value          = viewModel.bio,
                    onValueChange  = viewModel::onBioChanged,
                    modifier       = Modifier.fillMaxWidth(),
                    placeholder    = { Text("Building secure connections.", style = TypoBodyMedium) },
                    singleLine     = true,
                    leadingIcon    = { Icon(Icons.Rounded.Info, null, modifier = Modifier.size(20.dp)) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction      = ImeAction.Next,
                    ),
                    shape  = RoundedCornerShape(QryptDimens.RadiusSmall),
                    colors = fieldColors(),
                    textStyle = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── QryptIN ID ───────────────────────────────────────
            LabeledField(label = "QryptIN ID (username)") {
                OutlinedTextField(
                    value          = viewModel.qryptinId,
                    onValueChange  = viewModel::onQryptinIdChanged,
                    modifier       = Modifier.fillMaxWidth(),
                    placeholder    = { Text("e.g. debasmita", style = TypoBodyMedium) },
                    singleLine     = true,
                    isError        = viewModel.qryptinIdAvailability is QryptinIdAvailability.Taken ||
                                      viewModel.qryptinIdAvailability is QryptinIdAvailability.Invalid,
                    leadingIcon    = { Icon(Icons.Rounded.AlternateEmail, null, modifier = Modifier.size(20.dp)) },
                    trailingIcon   = { QryptinIdStatusIcon(viewModel.qryptinIdAvailability) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        imeAction      = ImeAction.Next,
                    ),
                    shape  = RoundedCornerShape(QryptDimens.RadiusSmall),
                    colors = fieldColors(),
                    textStyle = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                )
                QryptinIdStatusText(viewModel.qryptinIdAvailability)
            }

            Spacer(Modifier.height(20.dp))

            // ── Email (optional) ──────────────────────────────────
            LabeledField(label = "Email (optional)") {
                OutlinedTextField(
                    value          = viewModel.email,
                    onValueChange  = viewModel::onEmailChanged,
                    modifier       = Modifier.fillMaxWidth(),
                    placeholder    = { Text("you@example.com", style = TypoBodyMedium) },
                    singleLine     = true,
                    leadingIcon    = { Icon(Icons.Rounded.Email, null, modifier = Modifier.size(20.dp)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction    = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = {
                        keyboard?.hide()
                        viewModel.submitRegistration(onRegistered = {})
                    }),
                    shape  = RoundedCornerShape(QryptDimens.RadiusSmall),
                    colors = fieldColors(),
                    textStyle = TypoBodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                )
            }

            Spacer(Modifier.height(16.dp))

            AnimatedVisibility(
                visible = viewModel.registrationError != null,
                enter   = fadeIn() + slideInVertically { -10 },
                exit    = fadeOut(),
            ) {
                viewModel.registrationError?.let { err ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(err, style = TypoBodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            QryptPrimaryButton(
                text      = "Continue",
                onClick   = {
                    keyboard?.hide()
                    viewModel.submitRegistration(onRegistered = onRegistered)
                },
                enabled   = viewModel.isRegistrationValid,
                isLoading = uiState is AuthUiState.Loading,
            )

            Spacer(Modifier.height(20.dp))
            SecurityBadge(text = "Your profile is stored securely on this device")
            Spacer(Modifier.height(20.dp))
            TermsText()
            Spacer(Modifier.height(40.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  Profile picture picker — placeholder/initials until a real
//  photo is picked from the gallery. Never auto-generated.
// ─────────────────────────────────────────────────────────────
@Composable
private fun ProfilePicturePicker(
    photoUri: android.net.Uri?,
    name    : String,
    onClick : () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(104.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (photoUri != null) {
            AsyncImage(
                model              = photoUri,
                contentDescription = "Profile photo",
                modifier           = Modifier.fillMaxSize().clip(CircleShape),
            )
        } else {
            Icon(
                Icons.Rounded.PersonOutline,
                contentDescription = null,
                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp),
            )
        }

        // Camera badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.PhotoCamera,
                contentDescription = "Add photo",
                tint     = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun QryptinIdStatusIcon(state: QryptinIdAvailability) {
    when (state) {
        is QryptinIdAvailability.Checking ->
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        is QryptinIdAvailability.Available ->
            Icon(Icons.Rounded.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        is QryptinIdAvailability.Taken, is QryptinIdAvailability.Invalid ->
            Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
        QryptinIdAvailability.Idle -> {}
    }
}

@Composable
private fun QryptinIdStatusText(state: QryptinIdAvailability) {
    val (message, color) = when (state) {
        is QryptinIdAvailability.Available -> "Available" to MaterialTheme.colorScheme.primary
        is QryptinIdAvailability.Taken     -> state.message to MaterialTheme.colorScheme.error
        is QryptinIdAvailability.Invalid   -> state.message to MaterialTheme.colorScheme.error
        is QryptinIdAvailability.Checking  -> "Checking availability..." to MaterialTheme.colorScheme.onSurfaceVariant
        QryptinIdAvailability.Idle         -> return
    }
    Spacer(Modifier.height(6.dp))
    Text(message, style = TypoBodySmall, color = color, modifier = Modifier.padding(start = 4.dp))
}

@Composable
private fun LabeledField(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = TypoLabelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun FieldError(error: String?) {
    AnimatedVisibility(
        visible = error != null,
        enter   = fadeIn() + slideInVertically { -10 },
        exit    = fadeOut(),
    ) {
        error?.let { err ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(err, style = TypoBodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor     = MaterialTheme.colorScheme.error,
)
