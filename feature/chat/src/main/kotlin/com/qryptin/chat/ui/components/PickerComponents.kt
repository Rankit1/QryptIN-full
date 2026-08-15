package com.qryptin.chat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items as lazyRowItems
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.qryptin.core.designsystem.ui.theme.TypoLabelMedium
import com.qryptin.core.designsystem.ui.theme.TypoTitleMedium

// ─────────────────────────────────────────────────────────────
//  EmojiPicker
//  Simple categorized grid — no native emoji-keyboard dependency,
//  so it's just as deterministic in previews as on a device.
// ─────────────────────────────────────────────────────────────
private val EmojiCategories = listOf(
    "Smileys" to listOf("😀", "😂", "😍", "😎", "🙂", "😴", "😭", "🤔", "😅", "😇", "🥳", "😡"),
    "Gestures" to listOf("👍", "👎", "👏", "🙌", "🤝", "🙏", "✌️", "🤞", "👌", "💪", "🤙", "👋"),
    "Hearts" to listOf("❤️", "💙", "💚", "💛", "🧡", "💜", "🖤", "🤍", "💖", "💕", "💞", "💯"),
)

@Composable
fun EmojiPicker(
    onEmojiSelected : (String) -> Unit,
    modifier        : Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.height(280.dp)) {
        TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
            EmojiCategories.forEachIndexed { index, (label, _) ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = { selectedTab = index },
                    text     = { Text(label, style = TypoLabelMedium) },
                )
            }
        }

        LazyVerticalGrid(
            columns         = GridCells.Fixed(6),
            contentPadding  = PaddingValues(12.dp),
            modifier        = Modifier.fillMaxWidth().weight(1f),
        ) {
            items(EmojiCategories[selectedTab].second) { emoji ->
                Box(
                    modifier         = Modifier
                        .size(44.dp)
                        .clickable { onEmojiSelected(emoji) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(emoji, style = TypoTitleMedium)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
//  StickerGrid
//  Mock local sticker pack — represented as emoji-style glyphs
//  standing in for real sticker art until a real asset pack
//  ships. "Recently used" is the first row.
// ─────────────────────────────────────────────────────────────
private val MockStickerPack = listOf(
    "🎉", "🔥", "✨", "🎯", "🚀", "💡", "🛡️", "🔐", "🌟", "👑", "🐼", "🦊",
)

@Composable
fun StickerGrid(
    recentlyUsed     : List<String>,
    onStickerSelected: (String) -> Unit,
    modifier         : Modifier = Modifier,
) {
    Column(modifier = modifier.height(280.dp)) {
        if (recentlyUsed.isNotEmpty()) {
            Text(
                "Recently used",
                style    = TypoLabelMedium,
                color    = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
            )
            LazyRow(contentPadding = PaddingValues(horizontal = 12.dp)) {
                lazyRowItems(recentlyUsed) { sticker ->
                    StickerTile(sticker, onStickerSelected, Modifier.padding(4.dp))
                }
            }
        }

        Text(
            "All stickers",
            style    = TypoLabelMedium,
            color    = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
        )
        LazyVerticalGrid(
            columns        = GridCells.Fixed(5),
            contentPadding = PaddingValues(12.dp),
            modifier       = Modifier.fillMaxWidth().weight(1f),
        ) {
            items(MockStickerPack) { sticker ->
                StickerTile(sticker, onStickerSelected, Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
private fun StickerTile(
    sticker          : String,
    onStickerSelected: (String) -> Unit,
    modifier         : Modifier = Modifier,
) {
    Box(
        modifier         = modifier
            .size(56.dp)
            .clickable { onStickerSelected(sticker) },
        contentAlignment = Alignment.Center,
    ) {
        Text(sticker, style = TypoTitleMedium)
    }
}
