package com.qryptin.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.qryptin.auth.AuthModule
import com.qryptin.core.designsystem.ui.theme.BottomNavDestination
import com.qryptin.core.designsystem.ui.theme.QryptBottomNavBar
import kotlinx.coroutines.launch

/**
 * SettingsPlaceholder — until :feature:settings is built.
 * Updated to use centralized navigation logic.
 */
@Composable
internal fun SettingsPlaceholder(
    navController : NavController,
    currentDest   : BottomNavDestination,
    onNavSelected : (BottomNavDestination) -> Unit,
    onLoggedOut   : () -> Unit = {},
) {
    val context        = LocalContext.current
    val scope          = rememberCoroutineScope()
    val sessionRepository = remember { AuthModule.provideSessionRepository(context) }

    Scaffold(
        bottomBar = {
            QryptBottomNavBar(
                selected = currentDest,
                onSelect = onNavSelected
            )
        },
    ) { innerPadding ->
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text("Settings — coming soon", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(32.dp))
            OutlinedButton(
                onClick = {
                    scope.launch {
                        sessionRepository.endSession()
                        onLoggedOut()
                    }
                },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Rounded.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log Out")
            }
        }
    }
}
