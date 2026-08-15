package com.qryptin.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.qryptin.core.designsystem.ui.theme.*

@Composable
fun HomeScreen(
    currentNavDestination: BottomNavDestination,
    onNavDestinationSelected: (BottomNavDestination) -> Unit,
) {
    Scaffold(
        bottomBar = {
            QryptBottomNavBar(
                selected = currentNavDestination,
                onSelect = onNavDestinationSelected
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text("Home Screen (Chat List Placeholder)", style = TypoTitleMedium)
        }
    }
}
