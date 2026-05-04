package com.nexussms.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class NexusSMSDatabase_Impl extends NexusSMSDatabase {
  private volatile MessageDao _messageDao;

  private volatile ConversationDao _conversationDao;

  private volatile ShortcutDao _shortcutDao;

  private volatile ScheduledMessageDao _scheduledMessageDao;

  private volatile SignatureDao _signatureDao;

  private volatile ThemeDao _themeDao;

  private volatile SocialAccountDao _socialAccountDao;

  private volatile ContactShortcutDao _contactShortcutDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `conversationId` INTEGER NOT NULL, `senderId` TEXT NOT NULL, `recipientId` TEXT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `isIncoming` INTEGER NOT NULL, `isSent` INTEGER NOT NULL, `isDelivered` INTEGER NOT NULL, `isRead` INTEGER NOT NULL, `attachmentUrls` TEXT NOT NULL, `messageType` TEXT NOT NULL, `socialMediaPlatform` TEXT NOT NULL, `encryptionType` TEXT NOT NULL, `signature` TEXT NOT NULL, `hasReactions` INTEGER NOT NULL, `reactionData` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `conversations` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `participantPhone` TEXT NOT NULL, `participantName` TEXT NOT NULL, `participantAvatar` TEXT NOT NULL, `lastMessage` TEXT NOT NULL, `lastMessageTime` INTEGER NOT NULL, `unreadCount` INTEGER NOT NULL, `isPinned` INTEGER NOT NULL, `isMuted` INTEGER NOT NULL, `theme` TEXT NOT NULL, `messageType` TEXT NOT NULL, `socialMediaPlatform` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `shortcuts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `trigger` TEXT NOT NULL, `expansion` TEXT NOT NULL, `category` TEXT NOT NULL, `usageCount` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `scheduled_messages` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `conversationId` INTEGER NOT NULL, `recipientPhone` TEXT NOT NULL, `content` TEXT NOT NULL, `scheduledTime` INTEGER NOT NULL, `createdTime` INTEGER NOT NULL, `isRCS` INTEGER NOT NULL, `attachmentUrls` TEXT NOT NULL, `status` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_signatures` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `signature` TEXT NOT NULL, `isDefault` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `themes` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `primaryColor` TEXT NOT NULL, `secondaryColor` TEXT NOT NULL, `bubbleColorSent` TEXT NOT NULL, `bubbleColorReceived` TEXT NOT NULL, `textColor` TEXT NOT NULL, `backgroundColor` TEXT NOT NULL, `isDarkMode` INTEGER NOT NULL, `isCustom` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `social_accounts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `platform` TEXT NOT NULL, `accountId` TEXT NOT NULL, `username` TEXT NOT NULL, `accessToken` TEXT NOT NULL, `refreshToken` TEXT NOT NULL, `isActive` INTEGER NOT NULL, `displayName` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `contact_shortcuts` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `contactPhone` TEXT NOT NULL, `shortcutId` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '08634ca9bbd02201a8ed8f9cfdb25938')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `messages`");
        db.execSQL("DROP TABLE IF EXISTS `conversations`");
        db.execSQL("DROP TABLE IF EXISTS `shortcuts`");
        db.execSQL("DROP TABLE IF EXISTS `scheduled_messages`");
        db.execSQL("DROP TABLE IF EXISTS `user_signatures`");
        db.execSQL("DROP TABLE IF EXISTS `themes`");
        db.execSQL("DROP TABLE IF EXISTS `social_accounts`");
        db.execSQL("DROP TABLE IF EXISTS `contact_shortcuts`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsMessages = new HashMap<String, TableInfo.Column>(17);
        _columnsMessages.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("conversationId", new TableInfo.Column("conversationId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("senderId", new TableInfo.Column("senderId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("recipientId", new TableInfo.Column("recipientId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isIncoming", new TableInfo.Column("isIncoming", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isSent", new TableInfo.Column("isSent", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isDelivered", new TableInfo.Column("isDelivered", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("isRead", new TableInfo.Column("isRead", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("attachmentUrls", new TableInfo.Column("attachmentUrls", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("messageType", new TableInfo.Column("messageType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("socialMediaPlatform", new TableInfo.Column("socialMediaPlatform", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("encryptionType", new TableInfo.Column("encryptionType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("signature", new TableInfo.Column("signature", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("hasReactions", new TableInfo.Column("hasReactions", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsMessages.put("reactionData", new TableInfo.Column("reactionData", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoMessages = new TableInfo("messages", _columnsMessages, _foreignKeysMessages, _indicesMessages);
        final TableInfo _existingMessages = TableInfo.read(db, "messages");
        if (!_infoMessages.equals(_existingMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "messages(com.nexussms.data.models.Message).\n"
                  + " Expected:\n" + _infoMessages + "\n"
                  + " Found:\n" + _existingMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsConversations = new HashMap<String, TableInfo.Column>(12);
        _columnsConversations.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("participantPhone", new TableInfo.Column("participantPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("participantName", new TableInfo.Column("participantName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("participantAvatar", new TableInfo.Column("participantAvatar", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("lastMessage", new TableInfo.Column("lastMessage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("lastMessageTime", new TableInfo.Column("lastMessageTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("unreadCount", new TableInfo.Column("unreadCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("isPinned", new TableInfo.Column("isPinned", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("isMuted", new TableInfo.Column("isMuted", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("theme", new TableInfo.Column("theme", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("messageType", new TableInfo.Column("messageType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsConversations.put("socialMediaPlatform", new TableInfo.Column("socialMediaPlatform", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysConversations = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesConversations = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoConversations = new TableInfo("conversations", _columnsConversations, _foreignKeysConversations, _indicesConversations);
        final TableInfo _existingConversations = TableInfo.read(db, "conversations");
        if (!_infoConversations.equals(_existingConversations)) {
          return new RoomOpenHelper.ValidationResult(false, "conversations(com.nexussms.data.models.Conversation).\n"
                  + " Expected:\n" + _infoConversations + "\n"
                  + " Found:\n" + _existingConversations);
        }
        final HashMap<String, TableInfo.Column> _columnsShortcuts = new HashMap<String, TableInfo.Column>(5);
        _columnsShortcuts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShortcuts.put("trigger", new TableInfo.Column("trigger", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShortcuts.put("expansion", new TableInfo.Column("expansion", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShortcuts.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsShortcuts.put("usageCount", new TableInfo.Column("usageCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysShortcuts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesShortcuts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoShortcuts = new TableInfo("shortcuts", _columnsShortcuts, _foreignKeysShortcuts, _indicesShortcuts);
        final TableInfo _existingShortcuts = TableInfo.read(db, "shortcuts");
        if (!_infoShortcuts.equals(_existingShortcuts)) {
          return new RoomOpenHelper.ValidationResult(false, "shortcuts(com.nexussms.data.models.Shortcut).\n"
                  + " Expected:\n" + _infoShortcuts + "\n"
                  + " Found:\n" + _existingShortcuts);
        }
        final HashMap<String, TableInfo.Column> _columnsScheduledMessages = new HashMap<String, TableInfo.Column>(9);
        _columnsScheduledMessages.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("conversationId", new TableInfo.Column("conversationId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("recipientPhone", new TableInfo.Column("recipientPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("content", new TableInfo.Column("content", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("scheduledTime", new TableInfo.Column("scheduledTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("createdTime", new TableInfo.Column("createdTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("isRCS", new TableInfo.Column("isRCS", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("attachmentUrls", new TableInfo.Column("attachmentUrls", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsScheduledMessages.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysScheduledMessages = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesScheduledMessages = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoScheduledMessages = new TableInfo("scheduled_messages", _columnsScheduledMessages, _foreignKeysScheduledMessages, _indicesScheduledMessages);
        final TableInfo _existingScheduledMessages = TableInfo.read(db, "scheduled_messages");
        if (!_infoScheduledMessages.equals(_existingScheduledMessages)) {
          return new RoomOpenHelper.ValidationResult(false, "scheduled_messages(com.nexussms.data.models.ScheduledMessage).\n"
                  + " Expected:\n" + _infoScheduledMessages + "\n"
                  + " Found:\n" + _existingScheduledMessages);
        }
        final HashMap<String, TableInfo.Column> _columnsUserSignatures = new HashMap<String, TableInfo.Column>(4);
        _columnsUserSignatures.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSignatures.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSignatures.put("signature", new TableInfo.Column("signature", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSignatures.put("isDefault", new TableInfo.Column("isDefault", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserSignatures = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserSignatures = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserSignatures = new TableInfo("user_signatures", _columnsUserSignatures, _foreignKeysUserSignatures, _indicesUserSignatures);
        final TableInfo _existingUserSignatures = TableInfo.read(db, "user_signatures");
        if (!_infoUserSignatures.equals(_existingUserSignatures)) {
          return new RoomOpenHelper.ValidationResult(false, "user_signatures(com.nexussms.data.models.UserSignature).\n"
                  + " Expected:\n" + _infoUserSignatures + "\n"
                  + " Found:\n" + _existingUserSignatures);
        }
        final HashMap<String, TableInfo.Column> _columnsThemes = new HashMap<String, TableInfo.Column>(10);
        _columnsThemes.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("primaryColor", new TableInfo.Column("primaryColor", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("secondaryColor", new TableInfo.Column("secondaryColor", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("bubbleColorSent", new TableInfo.Column("bubbleColorSent", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("bubbleColorReceived", new TableInfo.Column("bubbleColorReceived", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("textColor", new TableInfo.Column("textColor", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("backgroundColor", new TableInfo.Column("backgroundColor", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("isDarkMode", new TableInfo.Column("isDarkMode", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsThemes.put("isCustom", new TableInfo.Column("isCustom", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysThemes = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesThemes = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoThemes = new TableInfo("themes", _columnsThemes, _foreignKeysThemes, _indicesThemes);
        final TableInfo _existingThemes = TableInfo.read(db, "themes");
        if (!_infoThemes.equals(_existingThemes)) {
          return new RoomOpenHelper.ValidationResult(false, "themes(com.nexussms.data.models.Theme).\n"
                  + " Expected:\n" + _infoThemes + "\n"
                  + " Found:\n" + _existingThemes);
        }
        final HashMap<String, TableInfo.Column> _columnsSocialAccounts = new HashMap<String, TableInfo.Column>(8);
        _columnsSocialAccounts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocialAccounts.put("platform", new TableInfo.Column("platform", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocialAccounts.put("accountId", new TableInfo.Column("accountId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocialAccounts.put("username", new TableInfo.Column("username", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocialAccounts.put("accessToken", new TableInfo.Column("accessToken", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocialAccounts.put("refreshToken", new TableInfo.Column("refreshToken", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocialAccounts.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSocialAccounts.put("displayName", new TableInfo.Column("displayName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSocialAccounts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSocialAccounts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSocialAccounts = new TableInfo("social_accounts", _columnsSocialAccounts, _foreignKeysSocialAccounts, _indicesSocialAccounts);
        final TableInfo _existingSocialAccounts = TableInfo.read(db, "social_accounts");
        if (!_infoSocialAccounts.equals(_existingSocialAccounts)) {
          return new RoomOpenHelper.ValidationResult(false, "social_accounts(com.nexussms.data.models.SocialAccount).\n"
                  + " Expected:\n" + _infoSocialAccounts + "\n"
                  + " Found:\n" + _existingSocialAccounts);
        }
        final HashMap<String, TableInfo.Column> _columnsContactShortcuts = new HashMap<String, TableInfo.Column>(4);
        _columnsContactShortcuts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContactShortcuts.put("contactPhone", new TableInfo.Column("contactPhone", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContactShortcuts.put("shortcutId", new TableInfo.Column("shortcutId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContactShortcuts.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysContactShortcuts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesContactShortcuts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoContactShortcuts = new TableInfo("contact_shortcuts", _columnsContactShortcuts, _foreignKeysContactShortcuts, _indicesContactShortcuts);
        final TableInfo _existingContactShortcuts = TableInfo.read(db, "contact_shortcuts");
        if (!_infoContactShortcuts.equals(_existingContactShortcuts)) {
          return new RoomOpenHelper.ValidationResult(false, "contact_shortcuts(com.nexussms.data.models.ContactShortcut).\n"
                  + " Expected:\n" + _infoContactShortcuts + "\n"
                  + " Found:\n" + _existingContactShortcuts);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "08634ca9bbd02201a8ed8f9cfdb25938", "d86cc3a0a22a8dcb0d5ad153a3494b55");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "messages","conversations","shortcuts","scheduled_messages","user_signatures","themes","social_accounts","contact_shortcuts");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `messages`");
      _db.execSQL("DELETE FROM `conversations`");
      _db.execSQL("DELETE FROM `shortcuts`");
      _db.execSQL("DELETE FROM `scheduled_messages`");
      _db.execSQL("DELETE FROM `user_signatures`");
      _db.execSQL("DELETE FROM `themes`");
      _db.execSQL("DELETE FROM `social_accounts`");
      _db.execSQL("DELETE FROM `contact_shortcuts`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(MessageDao.class, MessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ConversationDao.class, ConversationDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ShortcutDao.class, ShortcutDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ScheduledMessageDao.class, ScheduledMessageDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SignatureDao.class, SignatureDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ThemeDao.class, ThemeDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SocialAccountDao.class, SocialAccountDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ContactShortcutDao.class, ContactShortcutDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public MessageDao messageDao() {
    if (_messageDao != null) {
      return _messageDao;
    } else {
      synchronized(this) {
        if(_messageDao == null) {
          _messageDao = new MessageDao_Impl(this);
        }
        return _messageDao;
      }
    }
  }

  @Override
  public ConversationDao conversationDao() {
    if (_conversationDao != null) {
      return _conversationDao;
    } else {
      synchronized(this) {
        if(_conversationDao == null) {
          _conversationDao = new ConversationDao_Impl(this);
        }
        return _conversationDao;
      }
    }
  }

  @Override
  public ShortcutDao shortcutDao() {
    if (_shortcutDao != null) {
      return _shortcutDao;
    } else {
      synchronized(this) {
        if(_shortcutDao == null) {
          _shortcutDao = new ShortcutDao_Impl(this);
        }
        return _shortcutDao;
      }
    }
  }

  @Override
  public ScheduledMessageDao scheduledMessageDao() {
    if (_scheduledMessageDao != null) {
      return _scheduledMessageDao;
    } else {
      synchronized(this) {
        if(_scheduledMessageDao == null) {
          _scheduledMessageDao = new ScheduledMessageDao_Impl(this);
        }
        return _scheduledMessageDao;
      }
    }
  }

  @Override
  public SignatureDao signatureDao() {
    if (_signatureDao != null) {
      return _signatureDao;
    } else {
      synchronized(this) {
        if(_signatureDao == null) {
          _signatureDao = new SignatureDao_Impl(this);
        }
        return _signatureDao;
      }
    }
  }

  @Override
  public ThemeDao themeDao() {
    if (_themeDao != null) {
      return _themeDao;
    } else {
      synchronized(this) {
        if(_themeDao == null) {
          _themeDao = new ThemeDao_Impl(this);
        }
        return _themeDao;
      }
    }
  }

  @Override
  public SocialAccountDao socialAccountDao() {
    if (_socialAccountDao != null) {
      return _socialAccountDao;
    } else {
      synchronized(this) {
        if(_socialAccountDao == null) {
          _socialAccountDao = new SocialAccountDao_Impl(this);
        }
        return _socialAccountDao;
      }
    }
  }

  @Override
  public ContactShortcutDao contactShortcutDao() {
    if (_contactShortcutDao != null) {
      return _contactShortcutDao;
    } else {
      synchronized(this) {
        if(_contactShortcutDao == null) {
          _contactShortcutDao = new ContactShortcutDao_Impl(this);
        }
        return _contactShortcutDao;
      }
    }
  }
}
