package com.nexussms.ui.screens;

import androidx.compose.foundation.layout.Arrangement;
import androidx.compose.material.icons.Icons;
import androidx.compose.material3.ExperimentalMaterial3Api;
import androidx.compose.material3.TopAppBarDefaults;
import androidx.compose.runtime.Composable;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.nexussms.data.models.Conversation;
import com.nexussms.ui.viewmodels.ConversationListViewModel;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000\u001e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u001a\u0012\u0010\u0000\u001a\u00020\u00012\b\b\u0002\u0010\u0002\u001a\u00020\u0003H\u0007\u001aB\u0010\u0004\u001a\u00020\u00012\u0006\u0010\u0005\u001a\u00020\u00062\f\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\u00010\b2\u0010\b\u0002\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\b2\u0010\b\u0002\u0010\n\u001a\n\u0012\u0004\u0012\u00020\u0001\u0018\u00010\bH\u0007\u00a8\u0006\u000b"}, d2 = {"ConversationListScreen", "", "viewModel", "Lcom/nexussms/ui/viewmodels/ConversationListViewModel;", "ConversationItem", "conversation", "Lcom/nexussms/data/models/Conversation;", "onDeleteClick", "Lkotlin/Function0;", "onPinClick", "onUnpinClick", "app_debug"})
public final class ConversationListScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void ConversationListScreen(@org.jetbrains.annotations.NotNull()
    com.nexussms.ui.viewmodels.ConversationListViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ConversationItem(@org.jetbrains.annotations.NotNull()
    com.nexussms.data.models.Conversation conversation, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteClick, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> onPinClick, @org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function0<kotlin.Unit> onUnpinClick) {
    }
}