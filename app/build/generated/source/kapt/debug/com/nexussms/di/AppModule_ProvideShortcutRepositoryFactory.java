package com.nexussms.di;

import com.nexussms.data.database.NexusSMSDatabase;
import com.nexussms.data.repository.ShortcutRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideShortcutRepositoryFactory implements Factory<ShortcutRepository> {
  private final Provider<NexusSMSDatabase> databaseProvider;

  public AppModule_ProvideShortcutRepositoryFactory(Provider<NexusSMSDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ShortcutRepository get() {
    return provideShortcutRepository(databaseProvider.get());
  }

  public static AppModule_ProvideShortcutRepositoryFactory create(
      Provider<NexusSMSDatabase> databaseProvider) {
    return new AppModule_ProvideShortcutRepositoryFactory(databaseProvider);
  }

  public static ShortcutRepository provideShortcutRepository(NexusSMSDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideShortcutRepository(database));
  }
}
