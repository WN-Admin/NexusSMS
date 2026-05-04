package com.nexussms.di;

import com.nexussms.data.database.NexusSMSDatabase;
import com.nexussms.data.repository.ConversationRepository;
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
public final class AppModule_ProvideConversationRepositoryFactory implements Factory<ConversationRepository> {
  private final Provider<NexusSMSDatabase> databaseProvider;

  public AppModule_ProvideConversationRepositoryFactory(
      Provider<NexusSMSDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ConversationRepository get() {
    return provideConversationRepository(databaseProvider.get());
  }

  public static AppModule_ProvideConversationRepositoryFactory create(
      Provider<NexusSMSDatabase> databaseProvider) {
    return new AppModule_ProvideConversationRepositoryFactory(databaseProvider);
  }

  public static ConversationRepository provideConversationRepository(NexusSMSDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideConversationRepository(database));
  }
}
