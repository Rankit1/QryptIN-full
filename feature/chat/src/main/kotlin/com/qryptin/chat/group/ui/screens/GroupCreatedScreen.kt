package com.qryptin.chat.group.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.qryptin.chat.group.viewmodel.GroupCreationViewModel
import com.qryptin.core.designsystem.ui.theme.*

// ─────────────────────────────────────────────────────────────
//  GroupCreatedScreen  — Step 4 / success confirmation
// ─────────────────────────────────────────────────────────────
@Composable
fun GroupCreatedScreen(
    viewModel      : GroupCreationViewModel,
    onOpenGroup    : () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val groupName = uiState.createdGroupName.ifBlank { "Group" }

    // Scale-in animation for the check icon
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val scale by animateFloatAsState(
        targetValue    = if (visible) 1f else 0f,
        animationSpec  = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow,
        ),
        label = "success_scale",
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.spacedBy(16.dp),
                modifier              = Modifier.padding(QryptDimens.PaddingScreenH),
            ) {
                // Success circle
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = "Success",
                            tint     = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(44.dp),
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "Group Created!",
                    style     = TypoDisplaySmall,
                    color     = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )

                Text(
                    "\"$groupName\" has been created successfully.",
                    style     = TypoBodyLarge,
                    color     = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(16.dp))

                QryptPrimaryButton(
                    text    = "Open Group",
                    onClick = onOpenGroup,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
