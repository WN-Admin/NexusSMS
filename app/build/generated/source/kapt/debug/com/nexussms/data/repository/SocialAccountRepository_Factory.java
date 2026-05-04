package com.nexussms.data.repository;

import com.nexussms.data.database.SocialAccountDao;
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
public final class SocialAccountRepository_Factory implements Factory<SocialAccountRepository> {
  private final Provider<SocialAccountDao> socialAccountDaoProvider;

  public SocialAccountRepository_Factory(Provider<SocialAccountDao> socialAccountDaoProvider) {
    this.socialAccountDaoProvider = socialAccountDaoProvider;
  }

  @Override
  public SocialAccountRepository get() {
    return newInstance(socialAccountDaoProvider.get());
  }

  public static SocialAccountRepository_Factory create(
      Provider<SocialAccountDao> socialAccountDaoProvider) {
    return new SocialAccountRepository_Factory(socialAccountDaoProvider);
  }

  public static SocialAccountRepository newInstance(SocialAccountDao socialAccountDao) {
    return new SocialAccountRepository(socialAccountDao);
  }
}
