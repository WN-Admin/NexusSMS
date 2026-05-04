package com.nexussms.data.repository;

import com.nexussms.data.database.ShortcutDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ShortcutRepository_Factory implements Factory<ShortcutRepository> {
  private final Provider<ShortcutDao> shortcutDaoProvider;

  public ShortcutRepository_Factory(Provider<ShortcutDao> shortcutDaoProvider) {
    this.shortcutDaoProvider = shortcutDaoProvider;
  }

  @Override
  public ShortcutRepository get() {
    return newInstance(shortcutDaoProvider.get());
  }

  public static ShortcutRepository_Factory create(Provider<ShortcutDao> shortcutDaoProvider) {
    return new ShortcutRepository_Factory(shortcutDaoProvider);
  }

  public static ShortcutRepository newInstance(ShortcutDao shortcutDao) {
    return new ShortcutRepository(shortcutDao);
  }
}
