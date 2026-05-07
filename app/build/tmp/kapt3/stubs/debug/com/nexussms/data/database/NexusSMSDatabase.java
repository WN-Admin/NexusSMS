package com.nexussms.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.nexussms.data.models.Message;
import com.nexussms.data.models.Conversation;
import com.nexussms.data.models.Shortcut;
import com.nexussms.data.models.ScheduledMessage;
import com.nexussms.data.models.UserSignature;
import com.nexussms.data.models.Theme;
import com.nexussms.data.models.SocialAccount;
import com.nexussms.data.models.ContactShortcut;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\'\u0018\u0000 \u00142\u00020\u0001:\u0001\u0014B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\b\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0007H&J\b\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\u000bH&J\b\u0010\f\u001a\u00020\rH&J\b\u0010\u000e\u001a\u00020\u000fH&J\b\u0010\u0010\u001a\u00020\u0011H&J\b\u0010\u0012\u001a\u00020\u0013H&\u00a8\u0006\u0015"}, d2 = {"Lcom/nexussms/data/database/NexusSMSDatabase;", "Landroidx/room/RoomDatabase;", "<init>", "()V", "messageDao", "Lcom/nexussms/data/database/MessageDao;", "conversationDao", "Lcom/nexussms/data/database/ConversationDao;", "shortcutDao", "Lcom/nexussms/data/database/ShortcutDao;", "scheduledMessageDao", "Lcom/nexussms/data/database/ScheduledMessageDao;", "signatureDao", "Lcom/nexussms/data/database/SignatureDao;", "themeDao", "Lcom/nexussms/data/database/ThemeDao;", "socialAccountDao", "Lcom/nexussms/data/database/SocialAccountDao;", "contactShortcutDao", "Lcom/nexussms/data/database/ContactShortcutDao;", "Companion", "app_debug"})
@androidx.room.Database(entities = {com.nexussms.data.models.Message.class, com.nexussms.data.models.Conversation.class, com.nexussms.data.models.Shortcut.class, com.nexussms.data.models.ScheduledMessage.class, com.nexussms.data.models.UserSignature.class, com.nexussms.data.models.Theme.class, com.nexussms.data.models.SocialAccount.class, com.nexussms.data.models.ContactShortcut.class}, version = 1, exportSchema = false)
public abstract class NexusSMSDatabase extends androidx.room.RoomDatabase {
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private static volatile com.nexussms.data.database.NexusSMSDatabase Instance;
    @org.jetbrains.annotations.NotNull()
    public static final com.nexussms.data.database.NexusSMSDatabase.Companion Companion = null;
    
    public NexusSMSDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.nexussms.data.database.MessageDao messageDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.nexussms.data.database.ConversationDao conversationDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.nexussms.data.database.ShortcutDao shortcutDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.nexussms.data.database.ScheduledMessageDao scheduledMessageDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.nexussms.data.database.SignatureDao signatureDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.nexussms.data.database.ThemeDao themeDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.nexussms.data.database.SocialAccountDao socialAccountDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.nexussms.data.database.ContactShortcutDao contactShortcutDao();
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u000e\u0010\u0006\u001a\u00020\u00052\u0006\u0010\u0007\u001a\u00020\bR\u0010\u0010\u0004\u001a\u0004\u0018\u00010\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/nexussms/data/database/NexusSMSDatabase$Companion;", "", "<init>", "()V", "Instance", "Lcom/nexussms/data/database/NexusSMSDatabase;", "getDatabase", "context", "Landroid/content/Context;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.nexussms.data.database.NexusSMSDatabase getDatabase(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
            return null;
        }
    }
}