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
import com.nexussms.data.models.Shortcut;
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
public final class ShortcutDao_Impl implements ShortcutDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Shortcut> __insertionAdapterOfShortcut;

  private final EntityDeletionOrUpdateAdapter<Shortcut> __deletionAdapterOfShortcut;

  private final EntityDeletionOrUpdateAdapter<Shortcut> __updateAdapterOfShortcut;

  public ShortcutDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfShortcut = new EntityInsertionAdapter<Shortcut>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `shortcuts` (`id`,`trigger`,`expansion`,`category`,`usageCount`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Shortcut entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTrigger() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTrigger());
        }
        if (entity.getExpansion() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getExpansion());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCategory());
        }
        statement.bindLong(5, entity.getUsageCount());
      }
    };
    this.__deletionAdapterOfShortcut = new EntityDeletionOrUpdateAdapter<Shortcut>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `shortcuts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Shortcut entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfShortcut = new EntityDeletionOrUpdateAdapter<Shortcut>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `shortcuts` SET `id` = ?,`trigger` = ?,`expansion` = ?,`category` = ?,`usageCount` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Shortcut entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getTrigger() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getTrigger());
        }
        if (entity.getExpansion() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getExpansion());
        }
        if (entity.getCategory() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getCategory());
        }
        statement.bindLong(5, entity.getUsageCount());
        statement.bindLong(6, entity.getId());
      }
    };
  }

  @Override
  public Object insertShortcut(final Shortcut shortcut,
      final Continuation<? super Long> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object deleteShortcut(final Shortcut shortcut,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object updateShortcut(final Shortcut shortcut,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Flow<List<Shortcut>> getAllShortcuts() {
    final String _sql = "SELECT * FROM shortcuts ORDER BY usageCount DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"shortcuts"}, new Callable<List<Shortcut>>() {
      @Override
      public List<Shortcut> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTrigger = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger");
          final int _cursorIndexOfExpansion = CursorUtil.getColumnIndexOrThrow(_cursor, "expansion");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfUsageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "usageCount");
          final List<Shortcut> _result = new ArrayList<Shortcut>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Shortcut _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTrigger;
            if (_cursor.isNull(_cursorIndexOfTrigger)) {
              _tmpTrigger = null;
            } else {
              _tmpTrigger = _cursor.getString(_cursorIndexOfTrigger);
            }
            final String _tmpExpansion;
            if (_cursor.isNull(_cursorIndexOfExpansion)) {
              _tmpExpansion = null;
            } else {
              _tmpExpansion = _cursor.getString(_cursorIndexOfExpansion);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final int _tmpUsageCount;
            _tmpUsageCount = _cursor.getInt(_cursorIndexOfUsageCount);
            _item = new Shortcut(_tmpId,_tmpTrigger,_tmpExpansion,_tmpCategory,_tmpUsageCount);
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
  public Flow<Shortcut> getShortcut(final String trigger) {
    final String _sql = "SELECT * FROM shortcuts WHERE trigger = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (trigger == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, trigger);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"shortcuts"}, new Callable<Shortcut>() {
      @Override
      public Shortcut call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTrigger = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger");
          final int _cursorIndexOfExpansion = CursorUtil.getColumnIndexOrThrow(_cursor, "expansion");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfUsageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "usageCount");
          final Shortcut _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTrigger;
            if (_cursor.isNull(_cursorIndexOfTrigger)) {
              _tmpTrigger = null;
            } else {
              _tmpTrigger = _cursor.getString(_cursorIndexOfTrigger);
            }
            final String _tmpExpansion;
            if (_cursor.isNull(_cursorIndexOfExpansion)) {
              _tmpExpansion = null;
            } else {
              _tmpExpansion = _cursor.getString(_cursorIndexOfExpansion);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final int _tmpUsageCount;
            _tmpUsageCount = _cursor.getInt(_cursorIndexOfUsageCount);
            _result = new Shortcut(_tmpId,_tmpTrigger,_tmpExpansion,_tmpCategory,_tmpUsageCount);
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
  public Flow<List<Shortcut>> searchShortcuts(final String pattern) {
    final String _sql = "SELECT * FROM shortcuts WHERE trigger LIKE ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (pattern == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, pattern);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"shortcuts"}, new Callable<List<Shortcut>>() {
      @Override
      public List<Shortcut> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTrigger = CursorUtil.getColumnIndexOrThrow(_cursor, "trigger");
          final int _cursorIndexOfExpansion = CursorUtil.getColumnIndexOrThrow(_cursor, "expansion");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfUsageCount = CursorUtil.getColumnIndexOrThrow(_cursor, "usageCount");
          final List<Shortcut> _result = new ArrayList<Shortcut>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Shortcut _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpTrigger;
            if (_cursor.isNull(_cursorIndexOfTrigger)) {
              _tmpTrigger = null;
            } else {
              _tmpTrigger = _cursor.getString(_cursorIndexOfTrigger);
            }
            final String _tmpExpansion;
            if (_cursor.isNull(_cursorIndexOfExpansion)) {
              _tmpExpansion = null;
            } else {
              _tmpExpansion = _cursor.getString(_cursorIndexOfExpansion);
            }
            final String _tmpCategory;
            if (_cursor.isNull(_cursorIndexOfCategory)) {
              _tmpCategory = null;
            } else {
              _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            }
            final int _tmpUsageCount;
            _tmpUsageCount = _cursor.getInt(_cursorIndexOfUsageCount);
            _item = new Shortcut(_tmpId,_tmpTrigger,_tmpExpansion,_tmpCategory,_tmpUsageCount);
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
  public Object incrementUsageCount(final long id, final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
