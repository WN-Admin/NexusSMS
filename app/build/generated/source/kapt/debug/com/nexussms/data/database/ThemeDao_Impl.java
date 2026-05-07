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
import com.nexussms.data.models.Theme;
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
public final class ThemeDao_Impl implements ThemeDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Theme> __insertionAdapterOfTheme;

  private final EntityDeletionOrUpdateAdapter<Theme> __deletionAdapterOfTheme;

  private final EntityDeletionOrUpdateAdapter<Theme> __updateAdapterOfTheme;

  public ThemeDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTheme = new EntityInsertionAdapter<Theme>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `themes` (`id`,`name`,`primaryColor`,`secondaryColor`,`bubbleColorSent`,`bubbleColorReceived`,`textColor`,`backgroundColor`,`isDarkMode`,`isCustom`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Theme entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getPrimaryColor() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPrimaryColor());
        }
        if (entity.getSecondaryColor() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSecondaryColor());
        }
        if (entity.getBubbleColorSent() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBubbleColorSent());
        }
        if (entity.getBubbleColorReceived() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBubbleColorReceived());
        }
        if (entity.getTextColor() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTextColor());
        }
        if (entity.getBackgroundColor() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getBackgroundColor());
        }
        final int _tmp = entity.isDarkMode() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isCustom() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
      }
    };
    this.__deletionAdapterOfTheme = new EntityDeletionOrUpdateAdapter<Theme>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `themes` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Theme entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfTheme = new EntityDeletionOrUpdateAdapter<Theme>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `themes` SET `id` = ?,`name` = ?,`primaryColor` = ?,`secondaryColor` = ?,`bubbleColorSent` = ?,`bubbleColorReceived` = ?,`textColor` = ?,`backgroundColor` = ?,`isDarkMode` = ?,`isCustom` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement, final Theme entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getName() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getName());
        }
        if (entity.getPrimaryColor() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getPrimaryColor());
        }
        if (entity.getSecondaryColor() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSecondaryColor());
        }
        if (entity.getBubbleColorSent() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getBubbleColorSent());
        }
        if (entity.getBubbleColorReceived() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getBubbleColorReceived());
        }
        if (entity.getTextColor() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTextColor());
        }
        if (entity.getBackgroundColor() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getBackgroundColor());
        }
        final int _tmp = entity.isDarkMode() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isCustom() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindLong(11, entity.getId());
      }
    };
  }

  @Override
  public Object insertTheme(final Theme theme, final Continuation<? super Long> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object deleteTheme(final Theme theme, final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object updateTheme(final Theme theme, final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Flow<List<Theme>> getAllThemes() {
    final String _sql = "SELECT * FROM themes ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"themes"}, new Callable<List<Theme>>() {
      @Override
      public List<Theme> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPrimaryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "primaryColor");
          final int _cursorIndexOfSecondaryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "secondaryColor");
          final int _cursorIndexOfBubbleColorSent = CursorUtil.getColumnIndexOrThrow(_cursor, "bubbleColorSent");
          final int _cursorIndexOfBubbleColorReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "bubbleColorReceived");
          final int _cursorIndexOfTextColor = CursorUtil.getColumnIndexOrThrow(_cursor, "textColor");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final List<Theme> _result = new ArrayList<Theme>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Theme _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpPrimaryColor;
            if (_cursor.isNull(_cursorIndexOfPrimaryColor)) {
              _tmpPrimaryColor = null;
            } else {
              _tmpPrimaryColor = _cursor.getString(_cursorIndexOfPrimaryColor);
            }
            final String _tmpSecondaryColor;
            if (_cursor.isNull(_cursorIndexOfSecondaryColor)) {
              _tmpSecondaryColor = null;
            } else {
              _tmpSecondaryColor = _cursor.getString(_cursorIndexOfSecondaryColor);
            }
            final String _tmpBubbleColorSent;
            if (_cursor.isNull(_cursorIndexOfBubbleColorSent)) {
              _tmpBubbleColorSent = null;
            } else {
              _tmpBubbleColorSent = _cursor.getString(_cursorIndexOfBubbleColorSent);
            }
            final String _tmpBubbleColorReceived;
            if (_cursor.isNull(_cursorIndexOfBubbleColorReceived)) {
              _tmpBubbleColorReceived = null;
            } else {
              _tmpBubbleColorReceived = _cursor.getString(_cursorIndexOfBubbleColorReceived);
            }
            final String _tmpTextColor;
            if (_cursor.isNull(_cursorIndexOfTextColor)) {
              _tmpTextColor = null;
            } else {
              _tmpTextColor = _cursor.getString(_cursorIndexOfTextColor);
            }
            final String _tmpBackgroundColor;
            if (_cursor.isNull(_cursorIndexOfBackgroundColor)) {
              _tmpBackgroundColor = null;
            } else {
              _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            }
            final boolean _tmpIsDarkMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDarkMode);
            _tmpIsDarkMode = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            _item = new Theme(_tmpId,_tmpName,_tmpPrimaryColor,_tmpSecondaryColor,_tmpBubbleColorSent,_tmpBubbleColorReceived,_tmpTextColor,_tmpBackgroundColor,_tmpIsDarkMode,_tmpIsCustom);
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
  public Flow<Theme> getTheme(final long id) {
    final String _sql = "SELECT * FROM themes WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, id);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"themes"}, new Callable<Theme>() {
      @Override
      public Theme call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPrimaryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "primaryColor");
          final int _cursorIndexOfSecondaryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "secondaryColor");
          final int _cursorIndexOfBubbleColorSent = CursorUtil.getColumnIndexOrThrow(_cursor, "bubbleColorSent");
          final int _cursorIndexOfBubbleColorReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "bubbleColorReceived");
          final int _cursorIndexOfTextColor = CursorUtil.getColumnIndexOrThrow(_cursor, "textColor");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final Theme _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpPrimaryColor;
            if (_cursor.isNull(_cursorIndexOfPrimaryColor)) {
              _tmpPrimaryColor = null;
            } else {
              _tmpPrimaryColor = _cursor.getString(_cursorIndexOfPrimaryColor);
            }
            final String _tmpSecondaryColor;
            if (_cursor.isNull(_cursorIndexOfSecondaryColor)) {
              _tmpSecondaryColor = null;
            } else {
              _tmpSecondaryColor = _cursor.getString(_cursorIndexOfSecondaryColor);
            }
            final String _tmpBubbleColorSent;
            if (_cursor.isNull(_cursorIndexOfBubbleColorSent)) {
              _tmpBubbleColorSent = null;
            } else {
              _tmpBubbleColorSent = _cursor.getString(_cursorIndexOfBubbleColorSent);
            }
            final String _tmpBubbleColorReceived;
            if (_cursor.isNull(_cursorIndexOfBubbleColorReceived)) {
              _tmpBubbleColorReceived = null;
            } else {
              _tmpBubbleColorReceived = _cursor.getString(_cursorIndexOfBubbleColorReceived);
            }
            final String _tmpTextColor;
            if (_cursor.isNull(_cursorIndexOfTextColor)) {
              _tmpTextColor = null;
            } else {
              _tmpTextColor = _cursor.getString(_cursorIndexOfTextColor);
            }
            final String _tmpBackgroundColor;
            if (_cursor.isNull(_cursorIndexOfBackgroundColor)) {
              _tmpBackgroundColor = null;
            } else {
              _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            }
            final boolean _tmpIsDarkMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDarkMode);
            _tmpIsDarkMode = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            _result = new Theme(_tmpId,_tmpName,_tmpPrimaryColor,_tmpSecondaryColor,_tmpBubbleColorSent,_tmpBubbleColorReceived,_tmpTextColor,_tmpBackgroundColor,_tmpIsDarkMode,_tmpIsCustom);
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
  public Flow<List<Theme>> getDefaultThemes() {
    final String _sql = "SELECT * FROM themes WHERE isCustom = 0 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"themes"}, new Callable<List<Theme>>() {
      @Override
      public List<Theme> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPrimaryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "primaryColor");
          final int _cursorIndexOfSecondaryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "secondaryColor");
          final int _cursorIndexOfBubbleColorSent = CursorUtil.getColumnIndexOrThrow(_cursor, "bubbleColorSent");
          final int _cursorIndexOfBubbleColorReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "bubbleColorReceived");
          final int _cursorIndexOfTextColor = CursorUtil.getColumnIndexOrThrow(_cursor, "textColor");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final List<Theme> _result = new ArrayList<Theme>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Theme _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpPrimaryColor;
            if (_cursor.isNull(_cursorIndexOfPrimaryColor)) {
              _tmpPrimaryColor = null;
            } else {
              _tmpPrimaryColor = _cursor.getString(_cursorIndexOfPrimaryColor);
            }
            final String _tmpSecondaryColor;
            if (_cursor.isNull(_cursorIndexOfSecondaryColor)) {
              _tmpSecondaryColor = null;
            } else {
              _tmpSecondaryColor = _cursor.getString(_cursorIndexOfSecondaryColor);
            }
            final String _tmpBubbleColorSent;
            if (_cursor.isNull(_cursorIndexOfBubbleColorSent)) {
              _tmpBubbleColorSent = null;
            } else {
              _tmpBubbleColorSent = _cursor.getString(_cursorIndexOfBubbleColorSent);
            }
            final String _tmpBubbleColorReceived;
            if (_cursor.isNull(_cursorIndexOfBubbleColorReceived)) {
              _tmpBubbleColorReceived = null;
            } else {
              _tmpBubbleColorReceived = _cursor.getString(_cursorIndexOfBubbleColorReceived);
            }
            final String _tmpTextColor;
            if (_cursor.isNull(_cursorIndexOfTextColor)) {
              _tmpTextColor = null;
            } else {
              _tmpTextColor = _cursor.getString(_cursorIndexOfTextColor);
            }
            final String _tmpBackgroundColor;
            if (_cursor.isNull(_cursorIndexOfBackgroundColor)) {
              _tmpBackgroundColor = null;
            } else {
              _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            }
            final boolean _tmpIsDarkMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDarkMode);
            _tmpIsDarkMode = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            _item = new Theme(_tmpId,_tmpName,_tmpPrimaryColor,_tmpSecondaryColor,_tmpBubbleColorSent,_tmpBubbleColorReceived,_tmpTextColor,_tmpBackgroundColor,_tmpIsDarkMode,_tmpIsCustom);
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
  public Flow<List<Theme>> getCustomThemes() {
    final String _sql = "SELECT * FROM themes WHERE isCustom = 1 ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"themes"}, new Callable<List<Theme>>() {
      @Override
      public List<Theme> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfPrimaryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "primaryColor");
          final int _cursorIndexOfSecondaryColor = CursorUtil.getColumnIndexOrThrow(_cursor, "secondaryColor");
          final int _cursorIndexOfBubbleColorSent = CursorUtil.getColumnIndexOrThrow(_cursor, "bubbleColorSent");
          final int _cursorIndexOfBubbleColorReceived = CursorUtil.getColumnIndexOrThrow(_cursor, "bubbleColorReceived");
          final int _cursorIndexOfTextColor = CursorUtil.getColumnIndexOrThrow(_cursor, "textColor");
          final int _cursorIndexOfBackgroundColor = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundColor");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfIsCustom = CursorUtil.getColumnIndexOrThrow(_cursor, "isCustom");
          final List<Theme> _result = new ArrayList<Theme>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Theme _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpName;
            if (_cursor.isNull(_cursorIndexOfName)) {
              _tmpName = null;
            } else {
              _tmpName = _cursor.getString(_cursorIndexOfName);
            }
            final String _tmpPrimaryColor;
            if (_cursor.isNull(_cursorIndexOfPrimaryColor)) {
              _tmpPrimaryColor = null;
            } else {
              _tmpPrimaryColor = _cursor.getString(_cursorIndexOfPrimaryColor);
            }
            final String _tmpSecondaryColor;
            if (_cursor.isNull(_cursorIndexOfSecondaryColor)) {
              _tmpSecondaryColor = null;
            } else {
              _tmpSecondaryColor = _cursor.getString(_cursorIndexOfSecondaryColor);
            }
            final String _tmpBubbleColorSent;
            if (_cursor.isNull(_cursorIndexOfBubbleColorSent)) {
              _tmpBubbleColorSent = null;
            } else {
              _tmpBubbleColorSent = _cursor.getString(_cursorIndexOfBubbleColorSent);
            }
            final String _tmpBubbleColorReceived;
            if (_cursor.isNull(_cursorIndexOfBubbleColorReceived)) {
              _tmpBubbleColorReceived = null;
            } else {
              _tmpBubbleColorReceived = _cursor.getString(_cursorIndexOfBubbleColorReceived);
            }
            final String _tmpTextColor;
            if (_cursor.isNull(_cursorIndexOfTextColor)) {
              _tmpTextColor = null;
            } else {
              _tmpTextColor = _cursor.getString(_cursorIndexOfTextColor);
            }
            final String _tmpBackgroundColor;
            if (_cursor.isNull(_cursorIndexOfBackgroundColor)) {
              _tmpBackgroundColor = null;
            } else {
              _tmpBackgroundColor = _cursor.getString(_cursorIndexOfBackgroundColor);
            }
            final boolean _tmpIsDarkMode;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsDarkMode);
            _tmpIsDarkMode = _tmp != 0;
            final boolean _tmpIsCustom;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsCustom);
            _tmpIsCustom = _tmp_1 != 0;
            _item = new Theme(_tmpId,_tmpName,_tmpPrimaryColor,_tmpSecondaryColor,_tmpBubbleColorSent,_tmpBubbleColorReceived,_tmpTextColor,_tmpBackgroundColor,_tmpIsDarkMode,_tmpIsCustom);
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
