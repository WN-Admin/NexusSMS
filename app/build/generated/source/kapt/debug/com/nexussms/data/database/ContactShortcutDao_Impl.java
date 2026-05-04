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
import com.nexussms.data.models.ContactShortcut;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ContactShortcutDao_Impl implements ContactShortcutDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ContactShortcut> __insertionAdapterOfContactShortcut;

  private final EntityDeletionOrUpdateAdapter<ContactShortcut> __deletionAdapterOfContactShortcut;

  private final EntityDeletionOrUpdateAdapter<ContactShortcut> __updateAdapterOfContactShortcut;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAllShortcutsForContact;

  public ContactShortcutDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfContactShortcut = new EntityInsertionAdapter<ContactShortcut>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `contact_shortcuts` (`id`,`contactPhone`,`shortcutId`,`isEnabled`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final ContactShortcut entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getContactPhone() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getContactPhone());
        }
        statement.bindLong(3, entity.getShortcutId());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
      }
    };
    this.__deletionAdapterOfContactShortcut = new EntityDeletionOrUpdateAdapter<ContactShortcut>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `contact_shortcuts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final ContactShortcut entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfContactShortcut = new EntityDeletionOrUpdateAdapter<ContactShortcut>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `contact_shortcuts` SET `id` = ?,`contactPhone` = ?,`shortcutId` = ?,`isEnabled` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @Nullable final ContactShortcut entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getContactPhone() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getContactPhone());
        }
        statement.bindLong(3, entity.getShortcutId());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
        statement.bindLong(5, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteAllShortcutsForContact = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM contact_shortcuts WHERE contactPhone = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertContactShortcut(final ContactShortcut contactShortcut,
      final Continuation<? super Long> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Long>() {
      @Override
      @NonNull
      public Long call() throws Exception {
        __db.beginTransaction();
        try {
          final Long _result = __insertionAdapterOfContactShortcut.insertAndReturnId(contactShortcut);
          __db.setTransactionSuccessful();
          return _result;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteContactShortcut(final ContactShortcut contactShortcut,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfContactShortcut.handle(contactShortcut);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateContactShortcut(final ContactShortcut contactShortcut,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfContactShortcut.handle(contactShortcut);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAllShortcutsForContact(final String phone,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAllShortcutsForContact.acquire();
        int _argIndex = 1;
        if (phone == null) {
          _stmt.bindNull(_argIndex);
        } else {
          _stmt.bindString(_argIndex, phone);
        }
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
          __preparedStmtOfDeleteAllShortcutsForContact.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<ContactShortcut>> getEnabledShortcutsForContact(final String phone) {
    final String _sql = "SELECT * FROM contact_shortcuts WHERE contactPhone = ? AND isEnabled = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (phone == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, phone);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"contact_shortcuts"}, new Callable<List<ContactShortcut>>() {
      @Override
      @NonNull
      public List<ContactShortcut> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfContactPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "contactPhone");
          final int _cursorIndexOfShortcutId = CursorUtil.getColumnIndexOrThrow(_cursor, "shortcutId");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<ContactShortcut> _result = new ArrayList<ContactShortcut>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ContactShortcut _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpContactPhone;
            if (_cursor.isNull(_cursorIndexOfContactPhone)) {
              _tmpContactPhone = null;
            } else {
              _tmpContactPhone = _cursor.getString(_cursorIndexOfContactPhone);
            }
            final long _tmpShortcutId;
            _tmpShortcutId = _cursor.getLong(_cursorIndexOfShortcutId);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _item = new ContactShortcut(_tmpId,_tmpContactPhone,_tmpShortcutId,_tmpIsEnabled);
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
