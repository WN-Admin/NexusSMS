package com.nexussms.features.theme;

import com.nexussms.data.repository.ThemeRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class ThemeManager_Factory implements Factory<ThemeManager> {
  private final Provider<ThemeRepository> themeRepositoryProvider;

  public ThemeManager_Factory(Provider<ThemeRepository> themeRepositoryProvider) {
    this.themeRepositoryProvider = themeRepositoryProvider;
  }

  @Override
  public ThemeManager get() {
    return newInstance(themeRepositoryProvider.get());
  }

  public static ThemeManager_Factory create(Provider<ThemeRepository> themeRepositoryProvider) {
    return new ThemeManager_Factory(themeRepositoryProvider);
  }

  public static ThemeManager newInstance(ThemeRepository themeRepository) {
    return new ThemeManager(themeRepository);
  }
}
