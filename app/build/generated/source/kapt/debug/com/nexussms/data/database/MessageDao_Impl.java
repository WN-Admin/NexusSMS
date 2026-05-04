package com.nexussms.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.nexussms.data.converters.DateConverter;
import com.nexussms.data.models.Message;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
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
public final class MessageDao_Impl implements MessageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Message> __insertionAdapterOfMessage;

  private final DateConverter __dateConverter = new DateConverter();

  private final EntityDeletionOrUpdateAdapter<Message> __deletionAdapterOfMessage;

  private final EntityDeletionOrUpdateAdapter<Message> __updateAdapterOfMessage;

  private final SharedSQLiteStatement __preparedStmtOfDeleteConversationMessages;

  private final SharedSQLiteStatement __preparedStmtOfMarkConversationAsRead;

  public MessageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMessage = new EntityInsertionAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `messages` (`id`,`conversationId`,`senderId`,`recipientId`,`content`,`timestamp`,`isIncoming`,`isSent`,`isDelivered`,`isRead`,`attachmentUrls`,`messageType`,`socialMediaPlatform`,`encryptionType`,`signature`,`hasReactions`,`reactionData`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final Message entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getConversationId());
        if (entity.getSenderId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSenderId());
        }
        if (entity.getRecipientId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getRecipientId());
        }
        if (entity.getContent() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getContent());
        }
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getTimestamp());
        if (_tmp == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp);
        }
        final int _tmp_1 = entity.isIncoming() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.isSent() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        final int _tmp_3 = entity.isDelivered() ? 1 : 0;
        statement.bindLong(9, _tmp_3);
        final int _tmp_4 = entity.isRead() ? 1 : 0;
        statement.bindLong(10, _tmp_4);
        if (entity.getAttachmentUrls() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getAttachmentUrls());
        }
        if (entity.getMessageType() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getMessageType());
        }
        if (entity.getSocialMediaPlatform() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getSocialMediaPlatform());
        }
        if (entity.getEncryptionType() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getEncryptionType());
        }
        if (entity.getSignature() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getSignature());
        }
        final int _tmp_5 = entity.getHasReactions() ? 1 : 0;
        statement.bindLong(16, _tmp_5);
        if (entity.getReactionData() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getReactionData());
        }
      }
    };
    this.__deletionAdapterOfMessage = new EntityDeletionOrUpdateAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `messages` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final Message entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfMessage = new EntityDeletionOrUpdateAdapter<Message>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `messages` SET `id` = ?,`conversationId` = ?,`senderId` = ?,`recipientId` = ?,`content` = ?,`timestamp` = ?,`isIncoming` = ?,`isSent` = ?,`isDelivered` = ?,`isRead` = ?,`attachmentUrls` = ?,`messageType` = ?,`socialMediaPlatform` = ?,`encryptionType` = ?,`signature` = ?,`hasReactions` = ?,`reactionData` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final Message entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getConversationId());
        if (entity.getSenderId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getSenderId());
        }
        if (entity.getRecipientId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getRecipientId());
        }
        if (entity.getContent() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getContent());
        }
        final Long _tmp = __dateConverter.dateToTimestamp(entity.getTimestamp());
        if (_tmp == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, _tmp);
        }
        final int _tmp_1 = entity.isIncoming() ? 1 : 0;
        statement.bindLong(7, _tmp_1);
        final int _tmp_2 = entity.isSent() ? 1 : 0;
        statement.bindLong(8, _tmp_2);
        final int _tmp_3 = entity.isDelivered() ? 1 : 0;
        statement.bindLong(9, _tmp_3);
        final int _tmp_4 = entity.isRead() ? 1 : 0;
        statement.bindLong(10, _tmp_4);
        if (entity.getAttachmentUrls() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getAttachmentUrls());
        }
        if (entity.getMessageType() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getMessageType());
        }
        if (entity.getSocialMediaPlatform() == null) {
          statement.bindNull(13);
        } else {
          statement.bindString(13, entity.getSocialMediaPlatform());
        }
        if (entity.getEncryptionType() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getEncryptionType());
        }
        if (entity.getSignature() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getSignature());
        }
        final int _tmp_5 = entity.getHasReactions() ? 1 : 0;
        statement.bindLong(16, _tmp_5);
        if (entity.getReactionData() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getReactionData());
        }
        statement.bindLong(18, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteConversationMessages = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM messages WHERE conversationId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkConversationAsRead = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE messages SET isRead = 1 WHERE conversationId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertMessage(final Message message, final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfMessage.insertAndReturnId(message);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteMessage(final Message message, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfMessage.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateMessage(final Message message, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfMessage.handle(message);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteConversationMessages(final long conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteConversationMessages.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, conversationId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteConversationMessages.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markConversationAsRead(final long conversationId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkConversationAsRead.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, conversationId);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkConversationAsRead.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Message> getMessage(final long id) {
    final String _sql = "SELECT * FROM messages WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<Message>() {
      @Override
      @Nullable
      public Message call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfRecipientId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsIncoming = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncoming");
          final int _cursorIndexOfIsSent = CursorUtil.getColumnIndexOrThrow(_cursor, "isSent");
          final int _cursorIndexOfIsDelivered = CursorUtil.getColumnIndexOrThrow(_cursor, "isDelivered");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfAttachmentUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentUrls");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final int _cursorIndexOfEncryptionType = CursorUtil.getColumnIndexOrThrow(_cursor, "encryptionType");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfHasReactions = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReactions");
          final int _cursorIndexOfReactionData = CursorUtil.getColumnIndexOrThrow(_cursor, "reactionData");
          final Message _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            if (_cursor.isNull(_cursorIndexOfSenderId)) {
              _tmpSenderId = null;
            } else {
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            }
            final String _tmpRecipientId;
            if (_cursor.isNull(_cursorIndexOfRecipientId)) {
              _tmpRecipientId = null;
            } else {
              _tmpRecipientId = _cursor.getString(_cursorIndexOfRecipientId);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = __dateConverter.fromTimestamp(_tmp);
            final boolean _tmpIsIncoming;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIncoming);
            _tmpIsIncoming = _tmp_1 != 0;
            final boolean _tmpIsSent;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSent);
            _tmpIsSent = _tmp_2 != 0;
            final boolean _tmpIsDelivered;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDelivered);
            _tmpIsDelivered = _tmp_3 != 0;
            final boolean _tmpIsRead;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_4 != 0;
            final String _tmpAttachmentUrls;
            if (_cursor.isNull(_cursorIndexOfAttachmentUrls)) {
              _tmpAttachmentUrls = null;
            } else {
              _tmpAttachmentUrls = _cursor.getString(_cursorIndexOfAttachmentUrls);
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
            final String _tmpEncryptionType;
            if (_cursor.isNull(_cursorIndexOfEncryptionType)) {
              _tmpEncryptionType = null;
            } else {
              _tmpEncryptionType = _cursor.getString(_cursorIndexOfEncryptionType);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final boolean _tmpHasReactions;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasReactions);
            _tmpHasReactions = _tmp_5 != 0;
            final String _tmpReactionData;
            if (_cursor.isNull(_cursorIndexOfReactionData)) {
              _tmpReactionData = null;
            } else {
              _tmpReactionData = _cursor.getString(_cursorIndexOfReactionData);
            }
            _result = new Message(_tmpId,_tmpConversationId,_tmpSenderId,_tmpRecipientId,_tmpContent,_tmpTimestamp,_tmpIsIncoming,_tmpIsSent,_tmpIsDelivered,_tmpIsRead,_tmpAttachmentUrls,_tmpMessageType,_tmpSocialMediaPlatform,_tmpEncryptionType,_tmpSignature,_tmpHasReactions,_tmpReactionData);
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
  public Flow<List<Message>> getConversationMessages(final long conversationId) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfRecipientId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsIncoming = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncoming");
          final int _cursorIndexOfIsSent = CursorUtil.getColumnIndexOrThrow(_cursor, "isSent");
          final int _cursorIndexOfIsDelivered = CursorUtil.getColumnIndexOrThrow(_cursor, "isDelivered");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfAttachmentUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentUrls");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final int _cursorIndexOfEncryptionType = CursorUtil.getColumnIndexOrThrow(_cursor, "encryptionType");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfHasReactions = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReactions");
          final int _cursorIndexOfReactionData = CursorUtil.getColumnIndexOrThrow(_cursor, "reactionData");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            if (_cursor.isNull(_cursorIndexOfSenderId)) {
              _tmpSenderId = null;
            } else {
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            }
            final String _tmpRecipientId;
            if (_cursor.isNull(_cursorIndexOfRecipientId)) {
              _tmpRecipientId = null;
            } else {
              _tmpRecipientId = _cursor.getString(_cursorIndexOfRecipientId);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = __dateConverter.fromTimestamp(_tmp);
            final boolean _tmpIsIncoming;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIncoming);
            _tmpIsIncoming = _tmp_1 != 0;
            final boolean _tmpIsSent;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSent);
            _tmpIsSent = _tmp_2 != 0;
            final boolean _tmpIsDelivered;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDelivered);
            _tmpIsDelivered = _tmp_3 != 0;
            final boolean _tmpIsRead;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_4 != 0;
            final String _tmpAttachmentUrls;
            if (_cursor.isNull(_cursorIndexOfAttachmentUrls)) {
              _tmpAttachmentUrls = null;
            } else {
              _tmpAttachmentUrls = _cursor.getString(_cursorIndexOfAttachmentUrls);
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
            final String _tmpEncryptionType;
            if (_cursor.isNull(_cursorIndexOfEncryptionType)) {
              _tmpEncryptionType = null;
            } else {
              _tmpEncryptionType = _cursor.getString(_cursorIndexOfEncryptionType);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final boolean _tmpHasReactions;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasReactions);
            _tmpHasReactions = _tmp_5 != 0;
            final String _tmpReactionData;
            if (_cursor.isNull(_cursorIndexOfReactionData)) {
              _tmpReactionData = null;
            } else {
              _tmpReactionData = _cursor.getString(_cursorIndexOfReactionData);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpSenderId,_tmpRecipientId,_tmpContent,_tmpTimestamp,_tmpIsIncoming,_tmpIsSent,_tmpIsDelivered,_tmpIsRead,_tmpAttachmentUrls,_tmpMessageType,_tmpSocialMediaPlatform,_tmpEncryptionType,_tmpSignature,_tmpHasReactions,_tmpReactionData);
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
  public Flow<List<Message>> getUnreadMessages(final long conversationId) {
    final String _sql = "SELECT * FROM messages WHERE conversationId = ? AND isRead = 0 ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfRecipientId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsIncoming = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncoming");
          final int _cursorIndexOfIsSent = CursorUtil.getColumnIndexOrThrow(_cursor, "isSent");
          final int _cursorIndexOfIsDelivered = CursorUtil.getColumnIndexOrThrow(_cursor, "isDelivered");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfAttachmentUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentUrls");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final int _cursorIndexOfEncryptionType = CursorUtil.getColumnIndexOrThrow(_cursor, "encryptionType");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfHasReactions = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReactions");
          final int _cursorIndexOfReactionData = CursorUtil.getColumnIndexOrThrow(_cursor, "reactionData");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            if (_cursor.isNull(_cursorIndexOfSenderId)) {
              _tmpSenderId = null;
            } else {
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            }
            final String _tmpRecipientId;
            if (_cursor.isNull(_cursorIndexOfRecipientId)) {
              _tmpRecipientId = null;
            } else {
              _tmpRecipientId = _cursor.getString(_cursorIndexOfRecipientId);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = __dateConverter.fromTimestamp(_tmp);
            final boolean _tmpIsIncoming;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIncoming);
            _tmpIsIncoming = _tmp_1 != 0;
            final boolean _tmpIsSent;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSent);
            _tmpIsSent = _tmp_2 != 0;
            final boolean _tmpIsDelivered;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDelivered);
            _tmpIsDelivered = _tmp_3 != 0;
            final boolean _tmpIsRead;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_4 != 0;
            final String _tmpAttachmentUrls;
            if (_cursor.isNull(_cursorIndexOfAttachmentUrls)) {
              _tmpAttachmentUrls = null;
            } else {
              _tmpAttachmentUrls = _cursor.getString(_cursorIndexOfAttachmentUrls);
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
            final String _tmpEncryptionType;
            if (_cursor.isNull(_cursorIndexOfEncryptionType)) {
              _tmpEncryptionType = null;
            } else {
              _tmpEncryptionType = _cursor.getString(_cursorIndexOfEncryptionType);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final boolean _tmpHasReactions;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasReactions);
            _tmpHasReactions = _tmp_5 != 0;
            final String _tmpReactionData;
            if (_cursor.isNull(_cursorIndexOfReactionData)) {
              _tmpReactionData = null;
            } else {
              _tmpReactionData = _cursor.getString(_cursorIndexOfReactionData);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpSenderId,_tmpRecipientId,_tmpContent,_tmpTimestamp,_tmpIsIncoming,_tmpIsSent,_tmpIsDelivered,_tmpIsRead,_tmpAttachmentUrls,_tmpMessageType,_tmpSocialMediaPlatform,_tmpEncryptionType,_tmpSignature,_tmpHasReactions,_tmpReactionData);
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
  public Flow<Integer> getUnreadCount(final long conversationId) {
    final String _sql = "SELECT COUNT(*) FROM messages WHERE conversationId = ? AND isRead = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, conversationId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
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
  public Flow<List<Message>> getRecentMessages(final long startTime, final int limit) {
    final String _sql = "SELECT * FROM messages WHERE timestamp >= ? ORDER BY timestamp DESC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfRecipientId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsIncoming = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncoming");
          final int _cursorIndexOfIsSent = CursorUtil.getColumnIndexOrThrow(_cursor, "isSent");
          final int _cursorIndexOfIsDelivered = CursorUtil.getColumnIndexOrThrow(_cursor, "isDelivered");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfAttachmentUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentUrls");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final int _cursorIndexOfEncryptionType = CursorUtil.getColumnIndexOrThrow(_cursor, "encryptionType");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfHasReactions = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReactions");
          final int _cursorIndexOfReactionData = CursorUtil.getColumnIndexOrThrow(_cursor, "reactionData");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            if (_cursor.isNull(_cursorIndexOfSenderId)) {
              _tmpSenderId = null;
            } else {
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            }
            final String _tmpRecipientId;
            if (_cursor.isNull(_cursorIndexOfRecipientId)) {
              _tmpRecipientId = null;
            } else {
              _tmpRecipientId = _cursor.getString(_cursorIndexOfRecipientId);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = __dateConverter.fromTimestamp(_tmp);
            final boolean _tmpIsIncoming;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIncoming);
            _tmpIsIncoming = _tmp_1 != 0;
            final boolean _tmpIsSent;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSent);
            _tmpIsSent = _tmp_2 != 0;
            final boolean _tmpIsDelivered;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDelivered);
            _tmpIsDelivered = _tmp_3 != 0;
            final boolean _tmpIsRead;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_4 != 0;
            final String _tmpAttachmentUrls;
            if (_cursor.isNull(_cursorIndexOfAttachmentUrls)) {
              _tmpAttachmentUrls = null;
            } else {
              _tmpAttachmentUrls = _cursor.getString(_cursorIndexOfAttachmentUrls);
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
            final String _tmpEncryptionType;
            if (_cursor.isNull(_cursorIndexOfEncryptionType)) {
              _tmpEncryptionType = null;
            } else {
              _tmpEncryptionType = _cursor.getString(_cursorIndexOfEncryptionType);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final boolean _tmpHasReactions;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasReactions);
            _tmpHasReactions = _tmp_5 != 0;
            final String _tmpReactionData;
            if (_cursor.isNull(_cursorIndexOfReactionData)) {
              _tmpReactionData = null;
            } else {
              _tmpReactionData = _cursor.getString(_cursorIndexOfReactionData);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpSenderId,_tmpRecipientId,_tmpContent,_tmpTimestamp,_tmpIsIncoming,_tmpIsSent,_tmpIsDelivered,_tmpIsRead,_tmpAttachmentUrls,_tmpMessageType,_tmpSocialMediaPlatform,_tmpEncryptionType,_tmpSignature,_tmpHasReactions,_tmpReactionData);
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
  public Flow<List<Message>> getMessagesByType(final String type) {
    final String _sql = "SELECT * FROM messages WHERE messageType = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (type == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, type);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"messages"}, new Callable<List<Message>>() {
      @Override
      @NonNull
      public List<Message> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfConversationId = CursorUtil.getColumnIndexOrThrow(_cursor, "conversationId");
          final int _cursorIndexOfSenderId = CursorUtil.getColumnIndexOrThrow(_cursor, "senderId");
          final int _cursorIndexOfRecipientId = CursorUtil.getColumnIndexOrThrow(_cursor, "recipientId");
          final int _cursorIndexOfContent = CursorUtil.getColumnIndexOrThrow(_cursor, "content");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfIsIncoming = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncoming");
          final int _cursorIndexOfIsSent = CursorUtil.getColumnIndexOrThrow(_cursor, "isSent");
          final int _cursorIndexOfIsDelivered = CursorUtil.getColumnIndexOrThrow(_cursor, "isDelivered");
          final int _cursorIndexOfIsRead = CursorUtil.getColumnIndexOrThrow(_cursor, "isRead");
          final int _cursorIndexOfAttachmentUrls = CursorUtil.getColumnIndexOrThrow(_cursor, "attachmentUrls");
          final int _cursorIndexOfMessageType = CursorUtil.getColumnIndexOrThrow(_cursor, "messageType");
          final int _cursorIndexOfSocialMediaPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "socialMediaPlatform");
          final int _cursorIndexOfEncryptionType = CursorUtil.getColumnIndexOrThrow(_cursor, "encryptionType");
          final int _cursorIndexOfSignature = CursorUtil.getColumnIndexOrThrow(_cursor, "signature");
          final int _cursorIndexOfHasReactions = CursorUtil.getColumnIndexOrThrow(_cursor, "hasReactions");
          final int _cursorIndexOfReactionData = CursorUtil.getColumnIndexOrThrow(_cursor, "reactionData");
          final List<Message> _result = new ArrayList<Message>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Message _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpConversationId;
            _tmpConversationId = _cursor.getLong(_cursorIndexOfConversationId);
            final String _tmpSenderId;
            if (_cursor.isNull(_cursorIndexOfSenderId)) {
              _tmpSenderId = null;
            } else {
              _tmpSenderId = _cursor.getString(_cursorIndexOfSenderId);
            }
            final String _tmpRecipientId;
            if (_cursor.isNull(_cursorIndexOfRecipientId)) {
              _tmpRecipientId = null;
            } else {
              _tmpRecipientId = _cursor.getString(_cursorIndexOfRecipientId);
            }
            final String _tmpContent;
            if (_cursor.isNull(_cursorIndexOfContent)) {
              _tmpContent = null;
            } else {
              _tmpContent = _cursor.getString(_cursorIndexOfContent);
            }
            final Date _tmpTimestamp;
            final Long _tmp;
            if (_cursor.isNull(_cursorIndexOfTimestamp)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(_cursorIndexOfTimestamp);
            }
            _tmpTimestamp = __dateConverter.fromTimestamp(_tmp);
            final boolean _tmpIsIncoming;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsIncoming);
            _tmpIsIncoming = _tmp_1 != 0;
            final boolean _tmpIsSent;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsSent);
            _tmpIsSent = _tmp_2 != 0;
            final boolean _tmpIsDelivered;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsDelivered);
            _tmpIsDelivered = _tmp_3 != 0;
            final boolean _tmpIsRead;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsRead);
            _tmpIsRead = _tmp_4 != 0;
            final String _tmpAttachmentUrls;
            if (_cursor.isNull(_cursorIndexOfAttachmentUrls)) {
              _tmpAttachmentUrls = null;
            } else {
              _tmpAttachmentUrls = _cursor.getString(_cursorIndexOfAttachmentUrls);
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
            final String _tmpEncryptionType;
            if (_cursor.isNull(_cursorIndexOfEncryptionType)) {
              _tmpEncryptionType = null;
            } else {
              _tmpEncryptionType = _cursor.getString(_cursorIndexOfEncryptionType);
            }
            final String _tmpSignature;
            if (_cursor.isNull(_cursorIndexOfSignature)) {
              _tmpSignature = null;
            } else {
              _tmpSignature = _cursor.getString(_cursorIndexOfSignature);
            }
            final boolean _tmpHasReactions;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfHasReactions);
            _tmpHasReactions = _tmp_5 != 0;
            final String _tmpReactionData;
            if (_cursor.isNull(_cursorIndexOfReactionData)) {
              _tmpReactionData = null;
            } else {
              _tmpReactionData = _cursor.getString(_cursorIndexOfReactionData);
            }
            _item = new Message(_tmpId,_tmpConversationId,_tmpSenderId,_tmpRecipientId,_tmpContent,_tmpTimestamp,_tmpIsIncoming,_tmpIsSent,_tmpIsDelivered,_tmpIsRead,_tmpAttachmentUrls,_tmpMessageType,_tmpSocialMediaPlatform,_tmpEncryptionType,_tmpSignature,_tmpHasReactions,_tmpReactionData);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
