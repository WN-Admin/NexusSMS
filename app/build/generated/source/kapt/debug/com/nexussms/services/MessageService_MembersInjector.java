package com.nexussms.services;

import com.nexussms.data.repository.MessageRepository;
import com.nexussms.security.EncryptionManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MessageService_MembersInjector implements MembersInjector<MessageService> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<EncryptionManager> encryptionManagerProvider;

  public MessageService_MembersInjector(Provider<MessageRepository> messageRepositoryProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.encryptionManagerProvider = encryptionManagerProvider;
  }

  public static MembersInjector<MessageService> create(
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    return new MessageService_MembersInjector(messageRepositoryProvider, encryptionManagerProvider);
  }

  @Override
  public void injectMembers(MessageService instance) {
    injectMessageRepository(instance, messageRepositoryProvider.get());
    injectEncryptionManager(instance, encryptionManagerProvider.get());
  }

  @InjectedFieldSignature("com.nexussms.services.MessageService.messageRepository")
  public static void injectMessageRepository(MessageService instance,
      MessageRepository messageRepository) {
    instance.messageRepository = messageRepository;
  }

  @InjectedFieldSignature("com.nexussms.services.MessageService.encryptionManager")
  public static void injectEncryptionManager(MessageService instance,
      EncryptionManager encryptionManager) {
    instance.encryptionManager = encryptionManager;
  }
}
