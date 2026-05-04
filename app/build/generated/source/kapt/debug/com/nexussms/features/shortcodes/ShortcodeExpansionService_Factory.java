package com.nexussms.features.shortcodes;

import com.nexussms.data.repository.ShortcutRepository;
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
public final class ShortcodeExpansionService_Factory implements Factory<ShortcodeExpansionService> {
  private final Provider<ShortcutRepository> shortcutRepositoryProvider;

  public ShortcodeExpansionService_Factory(
      Provider<ShortcutRepository> shortcutRepositoryProvider) {
    this.shortcutRepositoryProvider = shortcutRepositoryProvider;
  }

  @Override
  public ShortcodeExpansionService get() {
    return newInstance(shortcutRepositoryProvider.get());
  }

  public static ShortcodeExpansionService_Factory create(
      Provider<ShortcutRepository> shortcutRepositoryProvider) {
    return new ShortcodeExpansionService_Factory(shortcutRepositoryProvider);
  }

  public static ShortcodeExpansionService newInstance(ShortcutRepository shortcutRepository) {
    return new ShortcodeExpansionService(shortcutRepository);
  }
}
