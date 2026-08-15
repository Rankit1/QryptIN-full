package com.qryptin.contacts.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.qryptin.contacts.model.Contact
import com.qryptin.contacts.model.ContactTab
import com.qryptin.contacts.ui.components.*
import com.qryptin.contacts.viewmodel.ContactsViewModel
import com.qryptin.core.designsystem.ui.theme.*
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────
//  ContactsScreen
//  Root composable. Wires ViewModel → layout → child composables.
// ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    viewModel              : ContactsViewModel,
    onContactClick         : (Contact) -> Unit = {},
    onNewContactClick      : () -> Unit        = {},
    onInviteContact        : (Contact) -> Unit = {},
    onChatClick            : ((Contact) -> Unit)? = null,
    currentNavDestination  : BottomNavDestination = BottomNavDestination.CONTACTS,
    onNavDestinationSelected: (BottomNavDestination) -> Unit = {},
) {
    val uiState    by viewModel.uiState.collectAsState()
    val listState  = rememberLazyListState()
    val scope      = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // Grouped data (stable reference across recompositions)
    val grouped  = remember(uiState.contacts) { viewModel.groupedContacts() }
    val letters  = remember(grouped) { viewModel.indexLetters() }

    // Which section header is currently at the top — drives alphabet bar highlight
    val firstVisibleSection by remember {
        derivedStateOf {
            val firstKey = listState.layoutInfo.visibleItemsInfo
                .firstOrNull()?.key as? String
            firstKey?.takeIf { it.startsWith("header_") }
                ?.removePrefix("header_")
                ?.firstOrNull()
        }
    }

    // Flat list mapping from letter → lazy list item index (for direct scroll)
    val letterToIndex: Map<Char, Int> = remember(grouped) {
        buildMap {
            var cursor = 1            // index 0 is reserved for the top spacer
            grouped.keys.forEachIndexed { _, letter ->
                put(letter, cursor)   // header item index
                cursor += 1           // header
                cursor += (grouped[letter]?.size ?: 0)
            }
        }
    }

    // Issue 2 fix: safe-area-aware scaffold for the Contacts tab.
    QryptINSafeScaffold(
        modifier          = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor    = MaterialTheme.colorScheme.background,
        topBar            = {
            ContactsTopBar(
                scrollBehavior = scrollBehavior,
                onSearchToggle = { /* could open full-screen search */ },
            )
        },
        floatingActionButton = {
            NewContactFab(onClick = onNewContactClick)
        },
        bottomBar = {
            QryptBottomNavBar(
                selected = currentNavDestination,
                onSelect = onNavDestinationSelected,
            )
        },
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Search bar ────────────────────────────
                ContactsSearchBar(
                    query         = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChanged,
                    onClear       = viewModel::clearSearch,
                    modifier      = Modifier.padding(
                        horizontal = QryptDimens.PaddingScreenH,
                        vertical   = QryptDimens.SpaceMD,
                    ),
                )

                // ── Tab row ───────────────────────────────
                ContactTabRow(
                    selectedTab  = uiState.selectedTab,
                    qryptinCount = viewModel.qryptinCount,
                    inviteCount  = viewModel.inviteCount,
                    onTabSelected = { tab ->
                        viewModel.onTabSelected(tab)
                        scope.launch { listState.scrollToItem(0) }
                    },
                )

                // ── Main content ──────────────────────────
                Box(modifier = Modifier.weight(1f)) {
                    when {
                        uiState.isLoading -> ContactsLoadingShimmer()
                        uiState.contacts.isEmpty() -> ContactsEmptyState(
                            query       = uiState.searchQuery,
                            selectedTab = uiState.selectedTab,
                        )
                        else -> ContactsListWithScrollBar(
                            grouped          = grouped,
                            letters          = letters,
                            listState        = listState,
                            firstVisibleSection = firstVisibleSection,
                            letterToIndex    = letterToIndex,
                            onContactClick   = onContactClick,
                            onInviteContact  = onInviteContact,
                            onChatClick      = onChatClick,
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  ContactsTopBar
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactsTopBar(
    scrollBehavior : TopAppBarScrollBehavior,
    onSearchToggle : () -> Unit,
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Contacts",
                    style = TypoTitleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchToggle) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search contacts",
                    tint               = MaterialTheme.colorScheme.onBackground,
                )
            }
            IconButton(onClick = { /* Sort / filter menu */ }) {
                Icon(
                    Icons.Rounded.FilterList,
                    contentDescription = "Filter",
                    tint               = MaterialTheme.colorScheme.onBackground,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor        = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surface,
        ),
        scrollBehavior = scrollBehavior,
    )
}

// ─────────────────────────────────────────────────────────────
//  ContactsListWithScrollBar
//  LazyColumn + alphabetical scroll strip side-by-side
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContactsListWithScrollBar(
    grouped             : Map<Char, List<Contact>>,
    letters             : List<Char>,
    listState           : LazyListState,
    firstVisibleSection : Char?,
    letterToIndex       : Map<Char, Int>,
    onContactClick      : (Contact) -> Unit,
    onInviteContact     : (Contact) -> Unit,
    onChatClick         : ((Contact) -> Unit)? = null,
) {
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Contacts list ─────────────────────────────────
        LazyColumn(
            state                 = listState,
            modifier              = Modifier
                .fillMaxSize()
                .padding(end = 24.dp),    // space for scroll bar
            contentPadding        = PaddingValues(bottom = 96.dp),
        ) {
            grouped.forEach { (letter, contacts) ->
                // Section header
                stickyHeader(key = "header_$letter") {
                    ContactSectionHeader(letter = letter)
                }

                // Contact rows
                items(
                    items = contacts,
                    key   = { it.id },
                ) { contact ->
                    AnimatedVisibility(
                        visible      = true,
                        enter        = fadeIn(tween(200)) + slideInVertically(tween(200)) { 12 },
                    ) {
                        ContactRow(
                            contact     = contact,
                            onClick     = { onContactClick(contact) },
                            onInvite    = { onInviteContact(contact) },
                            onChatClick = onChatClick,
                        )
                    }

                    // Subtle divider between rows
                    HorizontalDivider(
                        modifier  = Modifier.padding(
                            start = QryptDimens.PaddingScreenH + 46.dp + QryptDimens.SpaceMD,
                            end   = QryptDimens.PaddingScreenH,
                        ),
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        thickness = 0.5.dp,
                    )
                }
            }
        }

        // ── Alphabet scroll bar ───────────────────────────
        AlphabetScrollBar(
            letters          = letters,
            activeIndex      = firstVisibleSection,
            onLetterSelected = { letter ->
                letterToIndex[letter]?.let { idx ->
                    scope.launch {
                        listState.animateScrollToItem(
                            index            = idx,
                            scrollOffset     = 0,
                        )
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 4.dp),
        )
    }
}

// ─────────────────────────────────────────────────────────────
//  Loading shimmer
// ─────────────────────────────────────────────────────────────

@Composable
private fun ContactsLoadingShimmer() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue   = 0.3f,
        targetValue    = 0.8f,
        animationSpec  = infiniteRepeatable(
            animation  = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "shimmer_alpha",
    )

    LazyColumn(
        contentPadding = PaddingValues(vertical = QryptDimens.SpaceMD),
    ) {
        items(12) { index ->
            ShimmerRow(alpha = alpha, index = index)
        }
    }
}

@Composable
private fun ShimmerRow(alpha: Float, index: Int) {
    Row(
        modifier             = Modifier
            .fillMaxWidth()
            .padding(horizontal = QryptDimens.PaddingScreenH, vertical = 10.dp),
        verticalAlignment    = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QryptDimens.SpaceMD),
    ) {
        // Avatar placeholder
        Surface(
            shape = androidx.compose.foundation.shape.CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
            modifier = Modifier.size(46.dp),
        ) {}

        Column(
            modifier            = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Surface(
                shape  = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha),
                modifier = Modifier
                    .fillMaxWidth(if (index % 2 == 0) 0.65f else 0.5f)
                    .height(14.dp),
            ) {}
            Surface(
                shape  = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                color  = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha * 0.6f),
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(10.dp),
            ) {}
        }
    }
}
