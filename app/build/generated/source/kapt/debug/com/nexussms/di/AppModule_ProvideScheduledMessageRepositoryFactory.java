package com.nexussms.di;

import com.nexussms.data.database.NexusSMSDatabase;
import com.nexussms.data.repository.ScheduledMessageRepository;
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
public final class AppModule_ProvideScheduledMessageRepositoryFactory implements Factory<ScheduledMessageRepository> {
  private final Provider<NexusSMSDatabase> databaseProvider;

  public AppModule_ProvideScheduledMessageRepositoryFactory(
      Provider<NexusSMSDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ScheduledMessageRepository get() {
    return provideScheduledMessageRepository(databaseProvider.get());
  }

  public static AppModule_ProvideScheduledMessageRepositoryFactory create(
      Provider<NexusSMSDatabase> databaseProvider) {
    return new AppModule_ProvideScheduledMessageRepositoryFactory(databaseProvider);
  }

  public static ScheduledMessageRepository provideScheduledMessageRepository(
      NexusSMSDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideScheduledMessageRepository(database));
  }
}
