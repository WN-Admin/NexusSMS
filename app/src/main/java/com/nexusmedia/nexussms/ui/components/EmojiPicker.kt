package com.nexusmedia.nexussms.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val emojiCategories = listOf(
    "Smileys" to listOf("\uD83D\uDE00", "\uD83D\uDE01", "\uD83D\uDE02", "\uD83D\uDE03", "\uD83D\uDE04", "\uD83D\uDE05", "\uD83D\uDE06", "\uD83D\uDE07", "\uD83D\uDE08", "\uD83D\uDE09", "\uD83D\uDE0A", "\uD83D\uDE0B", "\uD83D\uDE0C", "\uD83D\uDE0D", "\uD83D\uDE0E", "\uD83D\uDE0F", "\uD83D\uDE10", "\uD83D\uDE11", "\uD83D\uDE12", "\uD83D\uDE13", "\uD83D\uDE14", "\uD83D\uDE15", "\uD83D\uDE16", "\uD83D\uDE17", "\uD83D\uDE18", "\uD83D\uDE19", "\uD83D\uDE1A", "\uD83D\uDE1B", "\uD83D\uDE1C", "\uD83D\uDE1D", "\uD83D\uDE1E", "\uD83D\uDE1F", "\uD83D\uDE20", "\uD83D\uDE21", "\uD83D\uDE22", "\uD83D\uDE23", "\uD83D\uDE24", "\uD83D\uDE25", "\uD83D\uDE26", "\uD83D\uDE27", "\uD83D\uDE28", "\uD83D\uDE29", "\uD83D\uDE2A", "\uD83D\uDE2B", "\uD83D\uDE2C", "\uD83D\uDE2D", "\uD83D\uDE2E", "\uD83D\uDE2F", "\uD83D\uDE30", "\uD83D\uDE31", "\uD83D\uDE32", "\uD83D\uDE33", "\uD83D\uDE34", "\uD83D\uDE35", "\uD83D\uDE36", "\uD83D\uDE37", "\uD83D\uDE38", "\uD83D\uDE39", "\uD83D\uDE3A", "\uD83D\uDE3B", "\uD83D\uDE3C", "\uD83D\uDE3D", "\uD83D\uDE3E", "\uD83D\uDE3F", "\uD83D\uDE40", "\uD83D\uDE41", "\uD83D\uDE42", "\uD83D\uDE43", "\uD83D\uDE44", "\uD83D\uDE45", "\uD83D\uDE46", "\uD83D\uDE47", "\uD83D\uDE48", "\uD83D\uDE49", "\uD83D\uDE4A", "\uD83D\uDE4B", "\uD83D\uDE4C", "\uD83D\uDE4D", "\uD83D\uDE4E", "\uD83D\uDE4F"),
    "People" to listOf("\uD83D\uDC4B", "\uD83D\uDC4C", "\uD83D\uDC4D", "\uD83D\uDC4E", "\uD83D\uDC4F", "\uD83D\uDC50", "\uD83D\uDC64", "\uD83D\uDC65", "\uD83D\uDC66", "\uD83D\uDC67", "\uD83D\uDC68", "\uD83D\uDC69", "\uD83D\uDC6A", "\uD83D\uDC6B", "\uD83D\uDC6C", "\uD83D\uDC6D", "\uD83D\uDC6E", "\uD83D\uDC6F", "\uD83D\uDC70", "\uD83D\uDC71", "\uD83D\uDC72", "\uD83D\uDC73", "\uD83D\uDC74", "\uD83D\uDC75", "\uD83D\uDC76", "\uD83D\uDC77", "\uD83D\uDC78", "\uD83D\uDC79", "\uD83D\uDC7A", "\uD83D\uDC7B", "\uD83D\uDC7C", "\uD83D\uDC7D", "\uD83D\uDC7E", "\uD83D\uDC7F", "\uD83D\uDC80", "\uD83D\uDC81", "\uD83D\uDC82", "\uD83D\uDC83", "\uD83D\uDC84", "\uD83D\uDC85", "\uD83D\uDC86", "\uD83D\uDC87", "\uD83D\uDC88", "\uD83D\uDC89", "\uD83D\uDC8A", "\uD83D\uDC8B", "\uD83D\uDC8C", "\uD83D\uDC8D", "\uD83D\uDC8E", "\uD83D\uDC8F", "\uD83D\uDC90", "\uD83D\uDC91", "\uD83D\uDC92", "\uD83D\uDC93", "\uD83D\uDC94", "\uD83D\uDC95", "\uD83D\uDC96", "\uD83D\uDC97", "\uD83D\uDC98", "\uD83D\uDC99", "\uD83D\uDC9A", "\uD83D\uDC9B", "\uD83D\uDC9C", "\uD83D\uDC9D", "\uD83D\uDC9E", "\uD83D\uDC9F"),
    "Animals" to listOf("\uD83D\uDC00", "\uD83D\uDC01", "\uD83D\uDC02", "\uD83D\uDC03", "\uD83D\uDC04", "\uD83D\uDC05", "\uD83D\uDC06", "\uD83D\uDC07", "\uD83D\uDC08", "\uD83D\uDC09", "\uD83D\uDC0A", "\uD83D\uDC0B", "\uD83D\uDC0C", "\uD83D\uDC0D", "\uD83D\uDC0E", "\uD83D\uDC0F", "\uD83D\uDC10", "\uD83D\uDC11", "\uD83D\uDC12", "\uD83D\uDC13", "\uD83D\uDC14", "\uD83D\uDC15", "\uD83D\uDC16", "\uD83D\uDC17", "\uD83D\uDC18", "\uD83D\uDC19", "\uD83D\uDC1A", "\uD83D\uDC1B", "\uD83D\uDC1C", "\uD83D\uDC1D", "\uD83D\uDC1E", "\uD83D\uDC1F", "\uD83D\uDC20", "\uD83D\uDC21", "\uD83D\uDC22", "\uD83D\uDC23", "\uD83D\uDC24", "\uD83D\uDC25", "\uD83D\uDC26", "\uD83D\uDC27", "\uD83D\uDC28", "\uD83D\uDC29", "\uD83D\uDC2A", "\uD83D\uDC2B", "\uD83D\uDC2C", "\uD83D\uDC2D", "\uD83D\uDC2E", "\uD83D\uDC2F", "\uD83D\uDC30", "\uD83D\uDC31", "\uD83D\uDC32", "\uD83D\uDC33", "\uD83D\uDC34", "\uD83D\uDC35", "\uD83D\uDC36", "\uD83D\uDC37", "\uD83D\uDC38", "\uD83D\uDC39", "\uD83D\uDC3A", "\uD83D\uDC3B", "\uD83D\uDC3C", "\uD83D\uDC3D", "\uD83D\uDC3E", "\uD83D\uDC3F", "\uD83D\uDC40", "\uD83D\uDC41", "\uD83D\uDC42", "\uD83D\uDC43", "\uD83D\uDC44", "\uD83D\uDC45", "\uD83D\uDC46", "\uD83D\uDC47", "\uD83D\uDC48", "\uD83D\uDC49", "\uD83D\uDC4A"),
    "Food" to listOf("\uD83C\uDF4E", "\uD83C\uDF4F", "\uD83C\uDF50", "\uD83C\uDF51", "\uD83C\uDF52", "\uD83C\uDF53", "\uD83C\uDF54", "\uD83C\uDF55", "\uD83C\uDF56", "\uD83C\uDF57", "\uD83C\uDF58", "\uD83C\uDF59", "\uD83C\uDF5A", "\uD83C\uDF5B", "\uD83C\uDF5C", "\uD83C\uDF5D", "\uD83C\uDF5E", "\uD83C\uDF5F", "\uD83C\uDF60", "\uD83C\uDF61", "\uD83C\uDF62", "\uD83C\uDF63", "\uD83C\uDF64", "\uD83C\uDF65", "\uD83C\uDF66", "\uD83C\uDF67", "\uD83C\uDF68", "\uD83C\uDF69", "\uD83C\uDF6A", "\uD83C\uDF6B", "\uD83C\uDF6C", "\uD83C\uDF6D", "\uD83C\uDF6E", "\uD83C\uDF6F", "\uD83C\uDF70", "\uD83C\uDF71", "\uD83C\uDF72", "\uD83C\uDF73", "\uD83C\uDF74", "\uD83C\uDF75", "\uD83C\uDF76", "\uD83C\uDF77", "\uD83C\uDF78", "\uD83C\uDF79", "\uD83C\uDF7A", "\uD83C\uDF7B", "\uD83C\uDF7C", "\uD83C\uDF7D", "\uD83C\uDF7E", "\uD83C\uDF7F", "\uD83C\uDF80", "\uD83C\uDF81", "\uD83C\uDF82", "\uD83C\uDF83", "\uD83C\uDF84", "\uD83C\uDF85", "\uD83C\uDF86", "\uD83C\uDF87", "\uD83C\uDF88", "\uD83C\uDF89", "\uD83C\uDF8A", "\uD83C\uDF8B", "\uD83C\uDF8C", "\uD83C\uDF8D", "\uD83C\uDF8E", "\uD83C\uDF8F", "\uD83C\uDF90", "\uD83C\uDF91", "\uD83C\uDF92", "\uD83C\uDF93"),
    "Travel" to listOf("\uD83C\uDF0D", "\uD83C\uDF0E", "\uD83C\uDF0F", "\uD83C\uDF10", "\uD83C\uDF11", "\uD83C\uDF12", "\uD83C\uDF13", "\uD83C\uDF14", "\uD83C\uDF15", "\uD83C\uDF16", "\uD83C\uDF17", "\uD83C\uDF18", "\uD83C\uDF19", "\uD83C\uDF1A", "\uD83C\uDF1B", "\uD83C\uDF1C", "\uD83C\uDF1D", "\uD83C\uDF1E", "\uD83C\uDF1F", "\uD83C\uDF20", "\uD83C\uDF21", "\uD83C\uDF22", "\uD83C\uDF23", "\uD83C\uDF24", "\uD83C\uDF25", "\uD83C\uDF26", "\uD83C\uDF27", "\uD83C\uDF28", "\uD83C\uDF29", "\uD83C\uDF2A", "\uD83C\uDF2B", "\uD83C\uDF2C", "\uD83C\uDF2D", "\uD83C\uDF2E", "\uD83C\uDF2F", "\uD83C\uDF30", "\uD83C\uDF31", "\uD83C\uDF32", "\uD83C\uDF33", "\uD83C\uDF34", "\uD83C\uDF35", "\uD83C\uDF36", "\uD83C\uDF37", "\uD83C\uDF38", "\uD83C\uDF39", "\uD83C\uDF3A", "\uD83C\uDF3B", "\uD83C\uDF3C", "\uD83C\uDF3D", "\uD83C\uDF3E", "\uD83C\uDF3F", "\uD83C\uDF40", "\uD83C\uDF41", "\uD83C\uDF42", "\uD83C\uDF43", "\uD83C\uDF44", "\uD83C\uDF45", "\uD83C\uDF46", "\uD83C\uDF47", "\uD83C\uDF48", "\uD83C\uDF49", "\uD83C\uDF4A", "\uD83C\uDF4B", "\uD83C\uDF4C", "\uD83C\uDF4D"),
    "Activities" to listOf("\u26BD", "\u26BE", "\u26F3", "\uD83C\uDFBE", "\uD83C\uDFBF", "\uD83C\uDFC0", "\uD83C\uDFC1", "\uD83C\uDFC2", "\uD83C\uDFC3", "\uD83C\uDFC4", "\uD83C\uDFC5", "\uD83C\uDFC6", "\uD83C\uDFC7", "\uD83C\uDFC8", "\uD83C\uDFC9", "\uD83C\uDFCA", "\uD83C\uDFCB", "\uD83C\uDFCC", "\uD83C\uDFCD", "\uD83C\uDFCE", "\uD83C\uDFCF", "\uD83C\uDFD0", "\uD83C\uDFD1", "\uD83C\uDFD2", "\uD83C\uDFD3", "\uD83C\uDFD4", "\uD83C\uDFD5", "\uD83C\uDFD6", "\uD83C\uDFD7", "\uD83C\uDFD8", "\uD83C\uDFD9", "\uD83C\uDFDA", "\uD83C\uDFDB", "\uD83C\uDFDC", "\uD83C\uDFDD", "\uD83C\uDFDE", "\uD83C\uDFDF", "\uD83C\uDFE0", "\uD83C\uDFE1", "\uD83C\uDFE2", "\uD83C\uDFE3", "\uD83C\uDFE4", "\uD83C\uDFE5", "\uD83C\uDFE6", "\uD83C\uDFE7", "\uD83C\uDFE8", "\uD83C\uDFE9", "\uD83C\uDFEA", "\uD83C\uDFEB", "\uD83C\uDFEC", "\uD83C\uDFED", "\uD83C\uDFEE", "\uD83C\uDFEF", "\uD83C\uDFF0"),
    "Objects" to listOf("\uD83D\uDD0E", "\uD83D\uDD11", "\uD83D\uDD12", "\uD83D\uDD13", "\uD83D\uDD14", "\uD83D\uDD15", "\uD83D\uDD16", "\uD83D\uDD17", "\uD83D\uDD18", "\uD83D\uDD19", "\uD83D\uDD1A", "\uD83D\uDD1B", "\uD83D\uDD1C", "\uD83D\uDD1D", "\uD83D\uDD1E", "\uD83D\uDD1F", "\uD83D\uDD20", "\uD83D\uDD21", "\uD83D\uDD22", "\uD83D\uDD23", "\uD83D\uDD24", "\uD83D\uDD25", "\uD83D\uDD26", "\uD83D\uDD27", "\uD83D\uDD28", "\uD83D\uDD29", "\uD83D\uDD2A", "\uD83D\uDD2B", "\uD83D\uDD2C", "\uD83D\uDD2D", "\uD83D\uDD2E", "\uD83D\uDD2F", "\uD83D\uDD30", "\uD83D\uDD31", "\uD83D\uDD32", "\uD83D\uDD33", "\uD83D\uDD34", "\uD83D\uDD35", "\uD83D\uDD36", "\uD83D\uDD37", "\uD83D\uDD38", "\uD83D\uDD39", "\uD83D\uDD3A", "\uD83D\uDD3B", "\uD83D\uDD3C", "\uD83D\uDD3D", "\uD83D\uDD3E", "\uD83D\uDD3F", "\uD83D\uDD40", "\uD83D\uDD41", "\uD83D\uDD42", "\uD83D\uDD43", "\uD83D\uDD44", "\uD83D\uDD45", "\uD83D\uDD46", "\uD83D\uDD47", "\uD83D\uDD48", "\uD83D\uDD49", "\uD83D\uDD4A", "\uD83D\uDD4B", "\uD83D\uDD4C", "\uD83D\uDD4D", "\uD83D\uDD4E", "\uD83D\uDD4F", "\uD83D\uDD50", "\uD83D\uDD51", "\uD83D\uDD52"),
    "Symbols" to listOf("\u2764\uFE0F", "\uD83E\uDD0D", "\uD83E\uDD0E", "\uD83E\uDD0F", "\uD83E\uDD10", "\uD83E\uDD11", "\uD83E\uDD12", "\uD83E\uDD13", "\uD83E\uDD14", "\uD83E\uDD15", "\uD83E\uDD16", "\uD83E\uDD17", "\uD83E\uDD18", "\uD83E\uDD19", "\uD83E\uDD1A", "\uD83E\uDD1B", "\uD83E\uDD1C", "\uD83E\uDD1D", "\uD83E\uDD1E", "\uD83E\uDD1F", "\uD83E\uDD20", "\uD83E\uDD21", "\uD83E\uDD22", "\uD83E\uDD23", "\uD83E\uDD24", "\uD83E\uDD25", "\uD83E\uDD26", "\uD83E\uDD27", "\uD83E\uDD28", "\uD83E\uDD29", "\uD83E\uDD2A", "\uD83E\uDD2B", "\uD83E\uDD2C", "\uD83E\uDD2D", "\uD83E\uDD2E", "\uD83E\uDD2F", "\uD83E\uDD30", "\uD83E\uDD31", "\uD83E\uDD32", "\uD83E\uDD33", "\uD83E\uDD34", "\uD83E\uDD35", "\uD83E\uDD36", "\uD83E\uDD37", "\uD83E\uDD38", "\uD83E\uDD39", "\uD83E\uDD3A", "\uD83E\uDD3B", "\uD83E\uDD3C", "\uD83E\uDD3D", "\uD83E\uDD3E", "\uD83E\uDD3F", "\uD83E\uDD40", "\uD83E\uDD41", "\uD83E\uDD42", "\uD83E\uDD43", "\uD83E\uDD44", "\uD83E\uDD45", "\uD83E\uDD46", "\uD83E\uDD47", "\uD83E\uDD48", "\uD83E\uDD49", "\uD83E\uDD4A", "\uD83E\uDD4B", "\uD83E\uDD4C", "\uD83E\uDD4D", "\uD83E\uDD4E", "\uD83E\uDD4F")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recentEmojis = remember { mutableStateListOf<String>() }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredCategories = remember(selectedTabIndex, searchQuery) {
        if (searchQuery.isNotBlank()) {
            emojiCategories.map { (name, emojis) ->
                name to emojis.filter { it.contains(searchQuery, ignoreCase = true) }
            }.filter { it.second.isNotEmpty() }
        } else {
            listOf(emojiCategories[selectedTabIndex])
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Emoji Picker") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Search, contentDescription = "Close")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                placeholder = { Text("Search emojis...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
            )

            if (searchQuery.isBlank()) {
                TabRow(selectedTabIndex = selectedTabIndex) {
                    emojiCategories.forEachIndexed { index, (name, _) ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = when (index) {
                                        0 -> "\uD83D\uDE00"
                                        1 -> "\uD83D\uDC4B"
                                        2 -> "\uD83D\uDC31"
                                        3 -> "\uD83C\uDF54"
                                        4 -> "\u2708\uFE0F"
                                        5 -> "\u26BD"
                                        6 -> "\uD83D\uDD0E"
                                        7 -> "\u2764\uFE0F"
                                        else -> name
                                    },
                                    fontSize = 20.sp
                                )
                            }
                        )
                    }
                }

                if (recentEmojis.isNotEmpty()) {
                    Text(
                        text = "Recent",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        recentEmojis.take(20).forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        onEmojiSelected(emoji)
                                    }
                                    .padding(6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 24.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(filteredCategories.flatMap { it.second }) { emoji ->
                    Box(
                        modifier = Modifier
                            .clickable {
                                if (emoji !in recentEmojis) recentEmojis.add(emoji)
                                onEmojiSelected(emoji)
                            }
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
