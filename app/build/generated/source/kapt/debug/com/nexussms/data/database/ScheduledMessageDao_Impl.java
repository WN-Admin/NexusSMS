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
import com.nexussms.data.models.ScheduledMessage;
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
public final class ScheduledMessageDao_Impl implements ScheduledMessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ScheduledMessage> __insertionAdapterOfScheduledMessage;

  private final DateConverter __dateConverter = new DateConverter();

  private final EntityDeletionOrUpdateAdapter<ScheduledMessage> __deletionAdapterOfScheduledMessage;

  private final EntityDeletionOrUpdateAdapter<ScheduledMessage> __updateAdapterOfScheduledMessage;

  public ScheduledMessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfScheduledMessage = new EntityInsertionAdapter<ScheduledMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `scheduled_messages` (`id`,`conversationId`,`recipientPhone`,`content`,`scheduledTime`,`createdTime`,`isRCS`,`attachmentUrls`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ScheduledMessage entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getConversationId());
        if (entity.getRecipientPhone() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRecipientPhone());
        }
        if (entity.getContent() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getContent());
        }
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getScheduledTime());
        if (_tmp == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, _tmp);
        }
        final Long _tmp_1 = __dateConverter.dateToTimestamp(entity.getCreatedTime());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp_1);
        }
        final int _tmp_2 = entity.isRCS() ? 1 : 0;
        statement.bindLong(7, _tmp_2);
        if (entity.getAttachmentUrls() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getAttachmentUrls());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getStatus());
        }
      }
    };
    this.__deletionAdapterOfScheduledMessage = new EntityDeletionOrUpdateAdapter<ScheduledMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `scheduled_messages` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ScheduledMessage entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfScheduledMessage = new EntityDeletionOrUpdateAdapter<ScheduledMessage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `scheduled_messages` SET `id` = ?,`conversationId` = ?,`recipientPhone` = ?,`content` = ?,`scheduledTime` = ?,`createdTime` = ?,`isRCS` = ?,`attachmentUrls` = ?,`status` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final ScheduledMessage entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getConversationId());
        if (entity.getRecipientPhone() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getRecipientPhone());
        }
        if (entity.getContent() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getContent());
        }
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getScheduledTime());
        if (_tmp == null) {
          statement.bindNull(5);
        } else {
          statement.bindLong(5, _tmp);
        }
        final Long _tmp_1 = __dateConverter.dateToTimestamp(entity.getCreatedTime());
        if (_tmp_1 == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp_1);
        }
        final int _tmp_2 = entity.isRCS() ? 1 : 0;
        statement.bindLong(7, _tmp_2);
        if (entity.getAttachmentUrls() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getAttachmentUrls());
        }
        if (entity.getStatus() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getStatus());
        }
        statement.bindLong(10, entity.getId());
      }
    };
  }

  @Override
  public Object insertScheduledMessage(final ScheduledMessage message,
      final Continuation<? super Long> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object deleteScheduledMessage(final ScheduledMessage message,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object updateScheduledMessage(final ScheduledMessage message,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Flow<List<ScheduledMessage>> getAllScheduledMessages() {
    final String _sql = "SELECT * FROM scheduled_messages ORDER BY scheduledTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scheduled_messages"}, new Callable<List<ScheduledMessage>>() {
      @Override
      public List<ScheduledMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfCreatedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "createdTime");
          final int _cursorIndexOfIsRCS = CursorUtil.getColumnIndexOrThrow(_cursor, "isRCS");
          final int _cursorIndexOfAttachmentUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentUrls");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<ScheduledMessage> _result = new ArrayList<ScheduledMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final Date _tmpScheduledTime;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfScheduledTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfScheduledTime);
            }
            _tmpScheduledTime = __dateConverter.fromTimestamp(_tmp);
            final Date _tmpCreatedTime;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedTime)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedTime);
            }
            _tmpCreatedTime = __dateConverter.fromTimestamp(_tmp_1);
            final boolean _tmpIsRCS;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsRCS);
            _tmpIsRCS = _tmp_2 != 0;
            final String _tmpAttachmentUrls;
            if (_cursor.isNull(_cursorIndexOfAttachmentUrls)) {
              _tmpAttachmentUrls = null;
            } else {
              _tmpAttachmentUrls = _cursor.getString(_cursorIndexOfAttachmentUrls);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            _item = new ScheduledMessage(_tmpId,_tmpConversationId,_tmpRecipientPhone,_tmpContent,_tmpScheduledTime,_tmpCreatedTime,_tmpIsRCS,_tmpAttachmentUrls,_tmpStatus);
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
  public Flow<List<ScheduledMessage>> getPendingScheduledMessages() {
    final String _sql = "SELECT * FROM scheduled_messages WHERE status = 'SCHEDULED' ORDER BY scheduledTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scheduled_messages"}, new Callable<List<ScheduledMessage>>() {
      @Override
      public List<ScheduledMessage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfCreatedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "createdTime");
          final int _cursorIndexOfIsRCS = CursorUtil.getColumnIndexOrThrow(_cursor, "isRCS");
          final int _cursorIndexOfAttachmentUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentUrls");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<ScheduledMessage> _result = new ArrayList<ScheduledMessage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ScheduledMessage _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final Date _tmpScheduledTime;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfScheduledTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfScheduledTime);
            }
            _tmpScheduledTime = __dateConverter.fromTimestamp(_tmp);
            final Date _tmpCreatedTime;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedTime)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedTime);
            }
            _tmpCreatedTime = __dateConverter.fromTimestamp(_tmp_1);
            final boolean _tmpIsRCS;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsRCS);
            _tmpIsRCS = _tmp_2 != 0;
            final String _tmpAttachmentUrls;
            if (_cursor.isNull(_cursorIndexOfAttachmentUrls)) {
              _tmpAttachmentUrls = null;
            } else {
              _tmpAttachmentUrls = _cursor.getString(_cursorIndexOfAttachmentUrls);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            _item = new ScheduledMessage(_tmpId,_tmpConversationId,_tmpRecipientPhone,_tmpContent,_tmpScheduledTime,_tmpCreatedTime,_tmpIsRCS,_tmpAttachmentUrls,_tmpStatus);
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
  public Flow<ScheduledMessage> getScheduledMessage(final long id) {
    final String _sql = "SELECT * FROM scheduled_messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"scheduled_messages"}, new Callable<ScheduledMessage>() {
      @Override
      public ScheduledMessage call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfRecipientPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientPhone");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfScheduledTime = CursorUtil.getColumnIndexOrThrow(_cursor, "scheduledTime");
          final int _cursorIndexOfCreatedTime = CursorUtil.getColumnIndexOrThrow(_cursor, "createdTime");
          final int _cursorIndexOfIsRCS = CursorUtil.getColumnIndexOrThrow(_cursor, "isRCS");
          final int _cursorIndexOfAttachmentUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentUrls");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final ScheduledMessage _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpRecipientPhone;
            if (_cursor.isNull(_cursorIndexOfRecipientPhone)) {
              _tmpRecipientPhone = null;
            } else {
              _tmpRecipientPhone = _cursor.getString(_cursorIndexOfRecipientPhone);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final Date _tmpScheduledTime;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfScheduledTime)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfScheduledTime);
            }
            _tmpScheduledTime = __dateConverter.fromTimestamp(_tmp);
            final Date _tmpCreatedTime;
            final Long _tmp_1;
            if (_cursor.isNull(_cursorIndexOfCreatedTime)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getLong(_cursorIndexOfCreatedTime);
            }
            _tmpCreatedTime = __dateConverter.fromTimestamp(_tmp_1);
            final boolean _tmpIsRCS;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsRCS);
            _tmpIsRCS = _tmp_2 != 0;
            final String _tmpAttachmentUrls;
            if (_cursor.isNull(_cursorIndexOfAttachmentUrls)) {
              _tmpAttachmentUrls = null;
            } else {
              _tmpAttachmentUrls = _cursor.getString(_cursorIndexOfAttachmentUrls);
            }
            final String _tmpStatus;
            if (_cursor.isNull(_cursorIndexOfStatus)) {
              _tmpStatus = null;
            } else {
              _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            }
            _result = new ScheduledMessage(_tmpId,_tmpConversationId,_tmpRecipientPhone,_tmpContent,_tmpScheduledTime,_tmpCreatedTime,_tmpIsRCS,_tmpAttachmentUrls,_tmpStatus);
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
  public Object deleteScheduledMessageById(final long id,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
