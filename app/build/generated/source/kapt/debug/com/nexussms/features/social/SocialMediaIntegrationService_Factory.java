package com.nexussms.features.social;

import com.nexussms.data.repository.MessageRepository;
import com.nexussms.data.repository.SocialAccountRepository;
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
public final class SocialMediaIntegrationService_Factory implements Factory<SocialMediaIntegrationService> {
  private final Provider<SocialAccountRepository> socialAccountRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  public SocialMediaIntegrationService_Factory(
      Provider<SocialAccountRepository> socialAccountRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    this.socialAccountRepositoryProvider = socialAccountRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public SocialMediaIntegrationService get() {
    return newInstance(socialAccountRepositoryProvider.get(), messageRepositoryProvider.get());
  }

  public static SocialMediaIntegrationService_Factory create(
      Provider<SocialAccountRepository> socialAccountRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    return new SocialMediaIntegrationService_Factory(socialAccountRepositoryProvider, messageRepositoryProvider);
  }

  public static SocialMediaIntegrationService newInstance(
      SocialAccountRepository socialAccountRepository, MessageRepository messageRepository) {
    return new SocialMediaIntegrationService(socialAccountRepository, messageRepository);
  }
}
