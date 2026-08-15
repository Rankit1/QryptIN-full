package com.qryptin.contacts.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.model.ContactSaveOption
import com.qryptin.contacts.model.InviteMethod
import com.qryptin.contacts.repository.ContactDetail
import com.qryptin.contacts.viewmodel.ContactDetailViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  ContactDetailScreen
//  Read-only view of a saved contact: registered/nickname,
//  phone, email, QryptIN status, save mode, and when it was
//  added. Reached by tapping a row on the Contacts list.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactDetailScreen(
    contactId          : String,
    onBack              : () -> Unit,
    onEdit              : (String) -> Unit = {},
    // Issue 4 fix: contact page had no way to jump into a chat with
    // that contact — this reuses the exact same open/create-conversation
    // flow already wired for the Contacts list row (getOrCreateDirectChat).
    onStartConversation : (Contact) -> Unit = {},
) {
    val viewModel: ContactDetailViewModel = viewModel()
    LaunchedEffect(contactId) { viewModel.load(contactId) }

    val contact   by viewModel.contact.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

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
                        "Contact Info",
                        style = TypoTitleMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                actions = {
                    if (contact != null) {
                        IconButton(onClick = { onEdit(contactId) }) {
                            Icon(
                                Icons.Rounded.Edit,
                                contentDescription = "Edit contact",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            when {
                isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                contact == null -> Text(
                    "Contact not found",
                    style = TypoBodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                else -> ContactDetailContent(contact!!, onStartConversation = onStartConversation)
            }
        }
    }
}

@Composable
private fun ContactDetailContent(
    detail               : ContactDetail,
    onStartConversation  : (Contact) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = QryptDimens.PaddingScreenH),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(QryptDimens.SpaceXL))

        // ── Avatar ─────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text  = initialsOf(detail.displayName),
                style = TypoDisplaySmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        Spacer(Modifier.height(QryptDimens.SpaceMD))

        Text(
            detail.displayName,
            style = TypoTitleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )

        if (detail.isQryptUser) {
            Spacer(Modifier.height(QryptDimens.SpaceXS))
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = null,
                        tint     = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(12.dp),
                    )
                    Text(
                        "On QryptIN",
                        style = TypoLabelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        Spacer(Modifier.height(QryptDimens.SpaceLG))

        // ── Start Conversation ─────────────────────────────
        // Issue 4 fix: this is the missing "message this contact"
        // entry point on the contact page itself, matching the
        // WhatsApp/Telegram/Signal pattern of a quick-action button
        // directly under the avatar/name.
        QryptPrimaryButton(
            text     = "Start Conversation",
            onClick  = {
                onStartConversation(
                    Contact(
                        id          = detail.id.toString(),
                        displayName = detail.displayName,
                        phone       = detail.phoneNumber,
                        isOnQryptIN = detail.isQryptUser,
                        createdAt   = detail.createdAt,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(0.7f),
        )

        Spacer(Modifier.height(QryptDimens.SpaceXL))

        // ── Info card ──────────────────────────────────────
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(QryptDimens.RadiusMedium),
            colors   = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(QryptDimens.SpaceMD)) {

                DetailRow(icon = Icons.Rounded.Phone, label = "Phone", value = detail.phoneNumber)

                if (!detail.email.isNullOrBlank()) {
                    DetailDivider()
                    DetailRow(icon = Icons.Rounded.Email, label = "Email", value = detail.email)
                }

                if (detail.isQryptUser && !detail.originalQryptName.isNullOrBlank() &&
                    detail.nickname != null
                ) {
                    DetailDivider()
                    DetailRow(
                        icon  = Icons.Rounded.Badge,
                        label = "Registered as",
                        value = detail.originalQryptName,
                    )
                }

                DetailDivider()
                DetailRow(
                    icon  = Icons.Rounded.SimCard,
                    label = "Saved in",
                    value = when (detail.saveMode) {
                        ContactSaveOption.SYNCED_WITH_SIM -> "Synced with SIM"
                        ContactSaveOption.QRYPTIN_ONLY    -> "QryptIN Contacts only"
                    },
                )

                if (detail.inviteMethod != null) {
                    DetailDivider()
                    DetailRow(
                        icon  = if (detail.inviteMethod == InviteMethod.SMS) Icons.Rounded.Sms else Icons.Rounded.Email,
                        label = "Invited via",
                        value = detail.inviteMethod.label,
                    )
                }
            }
        }

        Spacer(Modifier.height(QryptDimens.SpaceLG))
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint     = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Column {
            Text(label, style = TypoLabelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = TypoBodyLarge, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun DetailDivider() {
    HorizontalDivider(
        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 0.5.dp,
    )
}

private fun initialsOf(name: String): String =
    name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercaseChar().toString() }
        .ifEmpty { "?" }
