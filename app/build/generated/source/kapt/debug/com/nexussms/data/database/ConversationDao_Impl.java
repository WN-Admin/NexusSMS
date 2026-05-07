package com.nexussms.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.nexussms.data.converters.DateConverter;
import com.nexussms.data.models.Conversation;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ConversationDao_Impl implements ConversationDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Conversation> __insertionAdapterOfConversation;

  private final DateConverter __dateConverter = new DateConverter();

  private final EntityDeletionOrUpdateAdapter<Conversation> __deletionAdapterOfConversation;

  private final EntityDeletionOrUpdateAdapter<Conversation> __updateAdapterOfConversation;

  public ConversationDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfConversation = new EntityInsertionAdapter<Conversation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `conversations` (`id`,`participantPhone`,`participantName`,`participantAvatar`,`lastMessage`,`lastMessageTime`,`unreadCount`,`isPinned`,`isMuted`,`theme`,`messageType`,`socialMediaPlatform`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final Conversation entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getParticipantPhone() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getParticipantPhone());
        }
        if (entity.getParticipantName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getParticipantName());
        }
        if (entity.getParticipantAvatar() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getParticipantAvatar());
        }
        if (entity.getLastMessage() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLastMessage());
        }
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getLastMessageTime());
        if (_tmp == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp);
        }
        statement.bindLong(7, entity.getUnreadCount());
        final int _tmp_1 = entity.isPinned() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.isMuted() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        if (entity.getTheme() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getTheme());
        }
        if (entity.getMessageType() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getMessageType());
        }
        if (entity.getSocialMediaPlatform() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getSocialMediaPlatform());
        }
      }
    };
    this.__deletionAdapterOfConversation = new EntityDeletionOrUpdateAdapter<Conversation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `conversations` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final Conversation entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfConversation = new EntityDeletionOrUpdateAdapter<Conversation>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `conversations` SET `id` = ?,`participantPhone` = ?,`participantName` = ?,`participantAvatar` = ?,`lastMessage` = ?,`lastMessageTime` = ?,`unreadCount` = ?,`isPinned` = ?,`isMuted` = ?,`theme` = ?,`messageType` = ?,`socialMediaPlatform` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final Conversation entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getParticipantPhone() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getParticipantPhone());
        }
        if (entity.getParticipantName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getParticipantName());
        }
        if (entity.getParticipantAvatar() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getParticipantAvatar());
        }
        if (entity.getLastMessage() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLastMessage());
        }
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getLastMessageTime());
        if (_tmp == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp);
        }
        statement.bindLong(7, entity.getUnreadCount());
        final int _tmp_1 = entity.isPinned() ? 1 : 0;
        statement.bindLong(8, _tmp_1);
        final int _tmp_2 = entity.isMuted() ? 1 : 0;
        statement.bindLong(9, _tmp_2);
        if (entity.getTheme() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getTheme());
        }
        if (entity.getMessageType() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getMessageType());
        }
        if (entity.getSocialMediaPlatform() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getSocialMediaPlatform());
        }
        statement.bindLong(13, entity.getId());
      }
    };
  }

  @Override
  public Object insertConversation(final Conversation conversation,
      final Continuation<? super Long> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object deleteConversation(final Conversation conversation,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object updateConversation(final Conversation conversation,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Flow<List<Conversation>> getAllConversations() {
    final String _sql = "SELECT * FROM conversations ORDER BY lastMessageTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"conversations"}, new Callable<List<Conversation>>() {
      @Override
      public List<Conversation> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfParticipantPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "participantPhone");
          final int _cursorIndexOfParticipantName = CursorUtil.getColumnIndexOrThrow(_cursor, "participantName");
          final int _cursorIndexOfParticipantAvatar = CursorUtil.getColumnIndexOrThrow(_cursor, "participantAvatar");
          final int _cursorIndexOfLastMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessage");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfTheme = CursorUtil.getColumnIndexOrThrow(_cursor, "theme");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final List<Conversation> _result = new ArrayList<Conversation>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Conversation _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpParticipantPhone;
            if (_cursor.isNull(_cursorIndexOfParticipantPhone)) {
              _tmpParticipantPhone = null;
            } else {
              _tmpParticipantPhone = _cursor.getString(_cursorIndexOfParticipantPhone);
            }
            final String _tmpParticipantName;
            if (_cursor.isNull(_cursorIndexOfParticipantName)) {
              _tmpParticipantName = null;
            } else {
              _tmpParticipantName = _cursor.getString(_cursorIndexOfParticipantName);
            }
            final String _tmpParticipantAvatar;
            if (_cursor.isNull(_cursorIndexOfParticipantAvatar)) {
              _tmpParticipantAvatar = null;
            } else {
              _tmpParticipantAvatar = _cursor.getString(_cursorIndexOfParticipantAvatar);
            }
            final String _tmpLastMessage;
            if (_cursor.isNull(_cursorIndexOfLastMessage)) {
              _tmpLastMessage = null;
            } else {
              _tmpLastMessage = _cursor.getString(_cursorIndexOfLastMessage);
            }
            final Date _tmpLastMessageTime;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _tmpLastMessageTime = __dateConverter.fromTimestamp(_tmp);
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final boolean _tmpIsPinned;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_1 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_2 != 0;
            final String _tmpTheme;
            if (_cursor.isNull(_cursorIndexOfTheme)) {
              _tmpTheme = null;
            } else {
              _tmpTheme = _cursor.getString(_cursorIndexOfTheme);
            }
            final String _tmpMessageType;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmpMessageType = null;
            } else {
              _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            }
            final String _tmpSocialMediaPlatform;
            if (_cursor.isNull(_cursorIndexOfSocialMediaPlatform)) {
              _tmpSocialMediaPlatform = null;
            } else {
              _tmpSocialMediaPlatform = _cursor.getString(_cursorIndexOfSocialMediaPlatform);
            }
            _item = new Conversation(_tmpId,_tmpParticipantPhone,_tmpParticipantName,_tmpParticipantAvatar,_tmpLastMessage,_tmpLastMessageTime,_tmpUnreadCount,_tmpIsPinned,_tmpIsMuted,_tmpTheme,_tmpMessageType,_tmpSocialMediaPlatform);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Conversation> getConversation(final long id) {
    final String _sql = "SELECT * FROM conversations WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"conversations"}, new Callable<Conversation>() {
      @Override
      public Conversation call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfParticipantPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "participantPhone");
          final int _cursorIndexOfParticipantName = CursorUtil.getColumnIndexOrThrow(_cursor, "participantName");
          final int _cursorIndexOfParticipantAvatar = CursorUtil.getColumnIndexOrThrow(_cursor, "participantAvatar");
          final int _cursorIndexOfLastMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessage");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfTheme = CursorUtil.getColumnIndexOrThrow(_cursor, "theme");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final Conversation _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpParticipantPhone;
            if (_cursor.isNull(_cursorIndexOfParticipantPhone)) {
              _tmpParticipantPhone = null;
            } else {
              _tmpParticipantPhone = _cursor.getString(_cursorIndexOfParticipantPhone);
            }
            final String _tmpParticipantName;
            if (_cursor.isNull(_cursorIndexOfParticipantName)) {
              _tmpParticipantName = null;
            } else {
              _tmpParticipantName = _cursor.getString(_cursorIndexOfParticipantName);
            }
            final String _tmpParticipantAvatar;
            if (_cursor.isNull(_cursorIndexOfParticipantAvatar)) {
              _tmpParticipantAvatar = null;
            } else {
              _tmpParticipantAvatar = _cursor.getString(_cursorIndexOfParticipantAvatar);
            }
            final String _tmpLastMessage;
            if (_cursor.isNull(_cursorIndexOfLastMessage)) {
              _tmpLastMessage = null;
            } else {
              _tmpLastMessage = _cursor.getString(_cursorIndexOfLastMessage);
            }
            final Date _tmpLastMessageTime;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _tmpLastMessageTime = __dateConverter.fromTimestamp(_tmp);
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final boolean _tmpIsPinned;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_1 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_2 != 0;
            final String _tmpTheme;
            if (_cursor.isNull(_cursorIndexOfTheme)) {
              _tmpTheme = null;
            } else {
              _tmpTheme = _cursor.getString(_cursorIndexOfTheme);
            }
            final String _tmpMessageType;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmpMessageType = null;
            } else {
              _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            }
            final String _tmpSocialMediaPlatform;
            if (_cursor.isNull(_cursorIndexOfSocialMediaPlatform)) {
              _tmpSocialMediaPlatform = null;
            } else {
              _tmpSocialMediaPlatform = _cursor.getString(_cursorIndexOfSocialMediaPlatform);
            }
            _result = new Conversation(_tmpId,_tmpParticipantPhone,_tmpParticipantName,_tmpParticipantAvatar,_tmpLastMessage,_tmpLastMessageTime,_tmpUnreadCount,_tmpIsPinned,_tmpIsMuted,_tmpTheme,_tmpMessageType,_tmpSocialMediaPlatform);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Conversation> getConversationByPhone(final String phone) {
    final String _sql = "SELECT * FROM conversations WHERE participantPhone = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (phone == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, phone);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"conversations"}, new Callable<Conversation>() {
      @Override
      public Conversation call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfParticipantPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "participantPhone");
          final int _cursorIndexOfParticipantName = CursorUtil.getColumnIndexOrThrow(_cursor, "participantName");
          final int _cursorIndexOfParticipantAvatar = CursorUtil.getColumnIndexOrThrow(_cursor, "participantAvatar");
          final int _cursorIndexOfLastMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessage");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfTheme = CursorUtil.getColumnIndexOrThrow(_cursor, "theme");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final Conversation _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpParticipantPhone;
            if (_cursor.isNull(_cursorIndexOfParticipantPhone)) {
              _tmpParticipantPhone = null;
            } else {
              _tmpParticipantPhone = _cursor.getString(_cursorIndexOfParticipantPhone);
            }
            final String _tmpParticipantName;
            if (_cursor.isNull(_cursorIndexOfParticipantName)) {
              _tmpParticipantName = null;
            } else {
              _tmpParticipantName = _cursor.getString(_cursorIndexOfParticipantName);
            }
            final String _tmpParticipantAvatar;
            if (_cursor.isNull(_cursorIndexOfParticipantAvatar)) {
              _tmpParticipantAvatar = null;
            } else {
              _tmpParticipantAvatar = _cursor.getString(_cursorIndexOfParticipantAvatar);
            }
            final String _tmpLastMessage;
            if (_cursor.isNull(_cursorIndexOfLastMessage)) {
              _tmpLastMessage = null;
            } else {
              _tmpLastMessage = _cursor.getString(_cursorIndexOfLastMessage);
            }
            final Date _tmpLastMessageTime;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _tmpLastMessageTime = __dateConverter.fromTimestamp(_tmp);
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final boolean _tmpIsPinned;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_1 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_2 != 0;
            final String _tmpTheme;
            if (_cursor.isNull(_cursorIndexOfTheme)) {
              _tmpTheme = null;
            } else {
              _tmpTheme = _cursor.getString(_cursorIndexOfTheme);
            }
            final String _tmpMessageType;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmpMessageType = null;
            } else {
              _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            }
            final String _tmpSocialMediaPlatform;
            if (_cursor.isNull(_cursorIndexOfSocialMediaPlatform)) {
              _tmpSocialMediaPlatform = null;
            } else {
              _tmpSocialMediaPlatform = _cursor.getString(_cursorIndexOfSocialMediaPlatform);
            }
            _result = new Conversation(_tmpId,_tmpParticipantPhone,_tmpParticipantName,_tmpParticipantAvatar,_tmpLastMessage,_tmpLastMessageTime,_tmpUnreadCount,_tmpIsPinned,_tmpIsMuted,_tmpTheme,_tmpMessageType,_tmpSocialMediaPlatform);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<List<Conversation>> getPinnedConversations() {
    final String _sql = "SELECT * FROM conversations WHERE isPinned = 1 ORDER BY lastMessageTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"conversations"}, new Callable<List<Conversation>>() {
      @Override
      public List<Conversation> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfParticipantPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "participantPhone");
          final int _cursorIndexOfParticipantName = CursorUtil.getColumnIndexOrThrow(_cursor, "participantName");
          final int _cursorIndexOfParticipantAvatar = CursorUtil.getColumnIndexOrThrow(_cursor, "participantAvatar");
          final int _cursorIndexOfLastMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessage");
          final int _cursorIndexOfLastMessageTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastMessageTime");
          final int _cursorIndexOfUnreadCount = CursorUtil.getColumnIndexOrThrow(_cursor, "unreadCount");
          final int _cursorIndexOfIsPinned = CursorUtil.getColumnIndexOrThrow(_cursor, "isPinned");
          final int _cursorIndexOfIsMuted = CursorUtil.getColumnIndexOrThrow(_cursor, "isMuted");
          final int _cursorIndexOfTheme = CursorUtil.getColumnIndexOrThrow(_cursor, "theme");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final List<Conversation> _result = new ArrayList<Conversation>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Conversation _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpParticipantPhone;
            if (_cursor.isNull(_cursorIndexOfParticipantPhone)) {
              _tmpParticipantPhone = null;
            } else {
              _tmpParticipantPhone = _cursor.getString(_cursorIndexOfParticipantPhone);
            }
            final String _tmpParticipantName;
            if (_cursor.isNull(_cursorIndexOfParticipantName)) {
              _tmpParticipantName = null;
            } else {
              _tmpParticipantName = _cursor.getString(_cursorIndexOfParticipantName);
            }
            final String _tmpParticipantAvatar;
            if (_cursor.isNull(_cursorIndexOfParticipantAvatar)) {
              _tmpParticipantAvatar = null;
            } else {
              _tmpParticipantAvatar = _cursor.getString(_cursorIndexOfParticipantAvatar);
            }
            final String _tmpLastMessage;
            if (_cursor.isNull(_cursorIndexOfLastMessage)) {
              _tmpLastMessage = null;
            } else {
              _tmpLastMessage = _cursor.getString(_cursorIndexOfLastMessage);
            }
            final Date _tmpLastMessageTime;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfLastMessageTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfLastMessageTime);
            }
            _tmpLastMessageTime = __dateConverter.fromTimestamp(_tmp);
            final int _tmpUnreadCount;
            _tmpUnreadCount = _cursor.getInt(_cursorIndexOfUnreadCount);
            final boolean _tmpIsPinned;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsPinned);
            _tmpIsPinned = _tmp_1 != 0;
            final boolean _tmpIsMuted;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsMuted);
            _tmpIsMuted = _tmp_2 != 0;
            final String _tmpTheme;
            if (_cursor.isNull(_cursorIndexOfTheme)) {
              _tmpTheme = null;
            } else {
              _tmpTheme = _cursor.getString(_cursorIndexOfTheme);
            }
            final String _tmpMessageType;
            if (_cursor.isNull(_cursorIndexOfMessageType)) {
              _tmpMessageType = null;
            } else {
              _tmpMessageType = _cursor.getString(_cursorIndexOfMessageType);
            }
            final String _tmpSocialMediaPlatform;
            if (_cursor.isNull(_cursorIndexOfSocialMediaPlatform)) {
              _tmpSocialMediaPlatform = null;
            } else {
              _tmpSocialMediaPlatform = _cursor.getString(_cursorIndexOfSocialMediaPlatform);
            }
            _item = new Conversation(_tmpId,_tmpParticipantPhone,_tmpParticipantName,_tmpParticipantAvatar,_tmpLastMessage,_tmpLastMessageTime,_tmpUnreadCount,_tmpIsPinned,_tmpIsMuted,_tmpTheme,_tmpMessageType,_tmpSocialMediaPlatform);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object incrementUnreadCount(final long id, final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object clearUnreadCount(final long id, final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object deleteConversationById(final long id,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
