package com.nexussms.ui.screens;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.foundation.text.KeyboardOptions;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.TopAppBarDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.input.ImeAction;
import com.nexussms.data.models.Message;
import com.nexussms.ui.viewmodels.ChatViewModel;
import java.text.SimpleDateFormat;
import java.util.Locale;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a\u001a\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0005H\u0007\u001a\u0010\u0010\u0006\u001a\u00020\u00012\u0006\u0010\u0007\u001a\u00020\bH\u0007\u001a&\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00010\u000fH\u0007\u00a8\u0006\u0010"}, d2 = {"ChatDetailScreen", "", "conversationId", "", "viewModel", "Lcom/nexussms/ui/viewmodels/ChatViewModel;", "MessageBubble", "message", "Lcom/nexussms/data/models/Message;", "MessageTypeButton", "label", "", "isSelected", "", "onClick", "Lkotlin/Function0;", "app_debug"})
public final class ChatDetailScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable
    public static final void ChatDetailScreen(long conversationId, @org.jetbrains.annotations.NotNull
    com.nexussms.ui.viewmodels.ChatViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void MessageBubble(@org.jetbrains.annotations.NotNull
    com.nexussms.data.models.Message message) {
    }
    
    @androidx.compose.runtime.Composable
    public static final void MessageTypeButton(@org.jetbrains.annotations.NotNull
    java.lang.String label, boolean isSelected, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}