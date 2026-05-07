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
import com.nexussms.data.models.SocialAccount;
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
public final class SocialAccountDao_Impl implements SocialAccountDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SocialAccount> __insertionAdapterOfSocialAccount;

  private final EntityDeletionOrUpdateAdapter<SocialAccount> __deletionAdapterOfSocialAccount;

  private final EntityDeletionOrUpdateAdapter<SocialAccount> __updateAdapterOfSocialAccount;

  public SocialAccountDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSocialAccount = new EntityInsertionAdapter<SocialAccount>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `social_accounts` (`id`,`platform`,`accountId`,`username`,`accessToken`,`refreshToken`,`isActive`,`displayName`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SocialAccount entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getPlatform() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPlatform());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAccountId());
        }
        if (entity.getUsername() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getUsername());
        }
        if (entity.getAccessToken() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAccessToken());
        }
        if (entity.getRefreshToken() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRefreshToken());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getDisplayName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDisplayName());
        }
      }
    };
    this.__deletionAdapterOfSocialAccount = new EntityDeletionOrUpdateAdapter<SocialAccount>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `social_accounts` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SocialAccount entity) {
        statement.bindLong(1, entity.getId());
      }
    };
    this.__updateAdapterOfSocialAccount = new EntityDeletionOrUpdateAdapter<SocialAccount>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `social_accounts` SET `id` = ?,`platform` = ?,`accountId` = ?,`username` = ?,`accessToken` = ?,`refreshToken` = ?,`isActive` = ?,`displayName` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final SocialAccount entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getPlatform() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getPlatform());
        }
        if (entity.getAccountId() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAccountId());
        }
        if (entity.getUsername() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getUsername());
        }
        if (entity.getAccessToken() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAccessToken());
        }
        if (entity.getRefreshToken() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getRefreshToken());
        }
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(7, _tmp);
        if (entity.getDisplayName() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getDisplayName());
        }
        statement.bindLong(9, entity.getId());
      }
    };
  }

  @Override
  public Object insertAccount(final SocialAccount account,
      final Continuation<? super Long> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object deleteAccount(final SocialAccount account,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Object updateAccount(final SocialAccount account,
      final Continuation<? super Unit> $completion) {
    __db.assertNotSuspendingTransaction();
  }

  @Override
  public Flow<List<SocialAccount>> getActiveAccounts() {
    final String _sql = "SELECT * FROM social_accounts WHERE isActive = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"social_accounts"}, new Callable<List<SocialAccount>>() {
      @Override
      public List<SocialAccount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAccessToken = CursorUtil.getColumnIndexOrThrow(_cursor, "accessToken");
          final int _cursorIndexOfRefreshToken = CursorUtil.getColumnIndexOrThrow(_cursor, "refreshToken");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final List<SocialAccount> _result = new ArrayList<SocialAccount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SocialAccount _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlatform;
            if (_cursor.isNull(_cursorIndexOfPlatform)) {
              _tmpPlatform = null;
            } else {
              _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            }
            final String _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getString(_cursorIndexOfAccountId);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpAccessToken;
            if (_cursor.isNull(_cursorIndexOfAccessToken)) {
              _tmpAccessToken = null;
            } else {
              _tmpAccessToken = _cursor.getString(_cursorIndexOfAccessToken);
            }
            final String _tmpRefreshToken;
            if (_cursor.isNull(_cursorIndexOfRefreshToken)) {
              _tmpRefreshToken = null;
            } else {
              _tmpRefreshToken = _cursor.getString(_cursorIndexOfRefreshToken);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            _item = new SocialAccount(_tmpId,_tmpPlatform,_tmpAccountId,_tmpUsername,_tmpAccessToken,_tmpRefreshToken,_tmpIsActive,_tmpDisplayName);
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
  public Flow<List<SocialAccount>> getAccountsByPlatform(final String platform) {
    final String _sql = "SELECT * FROM social_accounts WHERE platform = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (platform == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, platform);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"social_accounts"}, new Callable<List<SocialAccount>>() {
      @Override
      public List<SocialAccount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAccessToken = CursorUtil.getColumnIndexOrThrow(_cursor, "accessToken");
          final int _cursorIndexOfRefreshToken = CursorUtil.getColumnIndexOrThrow(_cursor, "refreshToken");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final List<SocialAccount> _result = new ArrayList<SocialAccount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SocialAccount _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlatform;
            if (_cursor.isNull(_cursorIndexOfPlatform)) {
              _tmpPlatform = null;
            } else {
              _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            }
            final String _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getString(_cursorIndexOfAccountId);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpAccessToken;
            if (_cursor.isNull(_cursorIndexOfAccessToken)) {
              _tmpAccessToken = null;
            } else {
              _tmpAccessToken = _cursor.getString(_cursorIndexOfAccessToken);
            }
            final String _tmpRefreshToken;
            if (_cursor.isNull(_cursorIndexOfRefreshToken)) {
              _tmpRefreshToken = null;
            } else {
              _tmpRefreshToken = _cursor.getString(_cursorIndexOfRefreshToken);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            _item = new SocialAccount(_tmpId,_tmpPlatform,_tmpAccountId,_tmpUsername,_tmpAccessToken,_tmpRefreshToken,_tmpIsActive,_tmpDisplayName);
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
  public Flow<List<SocialAccount>> getAllAccounts() {
    final String _sql = "SELECT * FROM social_accounts";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"social_accounts"}, new Callable<List<SocialAccount>>() {
      @Override
      public List<SocialAccount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfAccountId = CursorUtil.getColumnIndexOrThrow(_cursor, "accountId");
          final int _cursorIndexOfUsername = CursorUtil.getColumnIndexOrThrow(_cursor, "username");
          final int _cursorIndexOfAccessToken = CursorUtil.getColumnIndexOrThrow(_cursor, "accessToken");
          final int _cursorIndexOfRefreshToken = CursorUtil.getColumnIndexOrThrow(_cursor, "refreshToken");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final int _cursorIndexOfDisplayName = CursorUtil.getColumnIndexOrThrow(_cursor, "displayName");
          final List<SocialAccount> _result = new ArrayList<SocialAccount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SocialAccount _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpPlatform;
            if (_cursor.isNull(_cursorIndexOfPlatform)) {
              _tmpPlatform = null;
            } else {
              _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            }
            final String _tmpAccountId;
            if (_cursor.isNull(_cursorIndexOfAccountId)) {
              _tmpAccountId = null;
            } else {
              _tmpAccountId = _cursor.getString(_cursorIndexOfAccountId);
            }
            final String _tmpUsername;
            if (_cursor.isNull(_cursorIndexOfUsername)) {
              _tmpUsername = null;
            } else {
              _tmpUsername = _cursor.getString(_cursorIndexOfUsername);
            }
            final String _tmpAccessToken;
            if (_cursor.isNull(_cursorIndexOfAccessToken)) {
              _tmpAccessToken = null;
            } else {
              _tmpAccessToken = _cursor.getString(_cursorIndexOfAccessToken);
            }
            final String _tmpRefreshToken;
            if (_cursor.isNull(_cursorIndexOfRefreshToken)) {
              _tmpRefreshToken = null;
            } else {
              _tmpRefreshToken = _cursor.getString(_cursorIndexOfRefreshToken);
            }
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            final String _tmpDisplayName;
            if (_cursor.isNull(_cursorIndexOfDisplayName)) {
              _tmpDisplayName = null;
            } else {
              _tmpDisplayName = _cursor.getString(_cursorIndexOfDisplayName);
            }
            _item = new SocialAccount(_tmpId,_tmpPlatform,_tmpAccountId,_tmpUsername,_tmpAccessToken,_tmpRefreshToken,_tmpIsActive,_tmpDisplayName);
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
