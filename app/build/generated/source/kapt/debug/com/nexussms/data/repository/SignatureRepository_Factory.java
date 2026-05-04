package com.nexussms.data.repository;

import com.nexussms.data.database.SignatureDao;
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
public final class SignatureRepository_Factory implements Factory<SignatureRepository> {
  private final Provider<SignatureDao> signatureDaoProvider;

  public SignatureRepository_Factory(Provider<SignatureDao> signatureDaoProvider) {
    this.signatureDaoProvider = signatureDaoProvider;
  }

  @Override
  public SignatureRepository get() {
    return newInstance(signatureDaoProvider.get());
  }

  public static SignatureRepository_Factory create(Provider<SignatureDao> signatureDaoProvider) {
    return new SignatureRepository_Factory(signatureDaoProvider);
  }

  public static SignatureRepository newInstance(SignatureDao signatureDao) {
    return new SignatureRepository(signatureDao);
  }
}
