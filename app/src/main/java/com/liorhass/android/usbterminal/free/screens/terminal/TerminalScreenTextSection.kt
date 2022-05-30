// Copyright 2022 Lior Hass
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.liorhass.android.usbterminal.free.screens.terminal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.liorhass.android.usbterminal.free.main.MainViewModel
import com.liorhass.android.usbterminal.free.main.ScreenLine
import com.liorhass.android.usbterminal.free.ui.theme.DefaultTextColorInTextMode
import com.liorhass.android.usbterminal.free.ui.util.isKeyboardOpenAsState
import kotlinx.coroutines.delay

@Composable
fun ColumnScope.TerminalScreenTextSection(
    lines: State<Array<ScreenLine>>,
    shouldMeasureScreenDimensions: Int,
    onScreenDimensionsMeasured: (MainViewModel.ScreenDimensions) -> Unit,
    shouldScrollToBottom: State<Boolean>,
    onScrolledToBottom: () -> Unit,
    fontSize: Int,
    mainFocusRequester: FocusRequester,
    auxFocusRequester: FocusRequester,
    onKeyboardStateChange: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val isKeyboardOpen by isKeyboardOpenAsState()
    var atBottomBeforeKBWasOpened by remember { mutableStateOf(false) }

    val interactionSource = remember { MutableInteractionSource() }
    Box(modifier = Modifier
        .fillMaxWidth()
        .weight(1f, fill = true) // We use this instead of fillMaxSize() so TerminalScreenTextSection won't fill the screen and leave room for the status line at the bottom
    ) {
        if (shouldMeasureScreenDimensions != 0) {
            MeasureScreenDimensions(onScreenDimensionsMeasured, fontSize, shouldMeasureScreenDimensions)
        }
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                // .weight(1f, true)
                .background(Color.Black) //todo: from config in combination with text color
                .clickable(interactionSource, indication = null) {
                    atBottomBeforeKBWasOpened =
                        lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index == lazyListState.layoutInfo.totalItemsCount - 1
                    openSoftKeyboard(
                        coroutineScope = coroutineScope,
                        mainFocusRequester = mainFocusRequester,
                        auxFocusRequester = auxFocusRequester,
                    )
                },
        ) {
            items(
                items = lines.value,
                key = { line -> line.uid },
            ) { line ->
                TerminalScreenLine(line, fontSize)
            }
        }
    }
    LaunchedEffect(key1 = isKeyboardOpen) {
        if (isKeyboardOpen && atBottomBeforeKBWasOpened) {
           lazyListState.scrollToItem(lines.value.lastIndex)
        }
        onKeyboardStateChange()
    }
    if (shouldScrollToBottom.value) {
        LaunchedEffect(lines.value.size) {
            lazyListState.scrollToItem(lines.value.lastIndex)
            delay(100)
            lazyListState.scrollToItem(lines.value.lastIndex)
            onScrolledToBottom()
        }
    }
}

@Composable
fun TerminalScreenLine(
    line : ScreenLine,
    fontSize: Int,
    modifier: Modifier = Modifier,
    onTextLayout: (TextLayoutResult) -> Unit = {},
) {
//    Timber.d("TerminalScreenLine(): line.textLength=${line.textLength}  line.annotation.size=${line.getAnnotatedString().spanStyles.size}")
    Text(
        text = line.getAnnotatedString(),
        color = DefaultTextColorInTextMode, // todo: from config
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize.sp,
        overflow = TextOverflow.Clip,
        maxLines = 1,
        onTextLayout = onTextLayout,
        modifier = modifier,
    )
}

