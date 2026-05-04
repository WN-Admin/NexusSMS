package com.nexussms.data.repository;

import com.nexussms.data.database.ThemeDao;
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
public final class ThemeRepository_Factory implements Factory<ThemeRepository> {
  private final Provider<ThemeDao> themeDaoProvider;

  public ThemeRepository_Factory(Provider<ThemeDao> themeDaoProvider) {
    this.themeDaoProvider = themeDaoProvider;
  }

  @Override
  public ThemeRepository get() {
    return newInstance(themeDaoProvider.get());
  }

  public static ThemeRepository_Factory create(Provider<ThemeDao> themeDaoProvider) {
    return new ThemeRepository_Factory(themeDaoProvider);
  }

  public static ThemeRepository newInstance(ThemeDao themeDao) {
    return new ThemeRepository(themeDao);
  }
}
