package com.nexussms.di;

import com.nexussms.data.database.NexusSMSDatabase;
import com.nexussms.data.repository.SocialAccountRepository;
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
public final class AppModule_ProvideSocialAccountRepositoryFactory implements Factory<SocialAccountRepository> {
  private final Provider<NexusSMSDatabase> databaseProvider;

  public AppModule_ProvideSocialAccountRepositoryFactory(
      Provider<NexusSMSDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SocialAccountRepository get() {
    return provideSocialAccountRepository(databaseProvider.get());
  }

  public static AppModule_ProvideSocialAccountRepositoryFactory create(
      Provider<NexusSMSDatabase> databaseProvider) {
    return new AppModule_ProvideSocialAccountRepositoryFactory(databaseProvider);
  }

  public static SocialAccountRepository provideSocialAccountRepository(NexusSMSDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSocialAccountRepository(database));
  }
}
