package com.nexussms.di;

import com.nexussms.data.database.NexusSMSDatabase;
import com.nexussms.data.repository.SignatureRepository;
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
public final class AppModule_ProvideSignatureRepositoryFactory implements Factory<SignatureRepository> {
  private final Provider<NexusSMSDatabase> databaseProvider;

  public AppModule_ProvideSignatureRepositoryFactory(Provider<NexusSMSDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SignatureRepository get() {
    return provideSignatureRepository(databaseProvider.get());
  }

  public static AppModule_ProvideSignatureRepositoryFactory create(
      Provider<NexusSMSDatabase> databaseProvider) {
    return new AppModule_ProvideSignatureRepositoryFactory(databaseProvider);
  }

  public static SignatureRepository provideSignatureRepository(NexusSMSDatabase database) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideSignatureRepository(database));
  }
}
