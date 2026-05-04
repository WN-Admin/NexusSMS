package com.nexussms.receivers;

import com.nexussms.data.repository.ConversationRepository;
import com.nexussms.data.repository.MessageRepository;
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
public final class SmsReceiver_MembersInjector implements MembersInjector<SmsReceiver> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<ConversationRepository> conversationRepositoryProvider;

  public SmsReceiver_MembersInjector(Provider<MessageRepository> messageRepositoryProvider,
      Provider<ConversationRepository> conversationRepositoryProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.conversationRepositoryProvider = conversationRepositoryProvider;
  }

  public static MembersInjector<SmsReceiver> create(
      Provider<MessageRepository> messageRepositoryProvider,
      Provider<ConversationRepository> conversationRepositoryProvider) {
    return new SmsReceiver_MembersInjector(messageRepositoryProvider, conversationRepositoryProvider);
  }

  @Override
  public void injectMembers(SmsReceiver instance) {
    injectMessageRepository(instance, messageRepositoryProvider.get());
    injectConversationRepository(instance, conversationRepositoryProvider.get());
  }

  @InjectedFieldSignature("com.nexussms.receivers.SmsReceiver.messageRepository")
  public static void injectMessageRepository(SmsReceiver instance,
      MessageRepository messageRepository) {
    instance.messageRepository = messageRepository;
  }

  @InjectedFieldSignature("com.nexussms.receivers.SmsReceiver.conversationRepository")
  public static void injectConversationRepository(SmsReceiver instance,
      ConversationRepository conversationRepository) {
    instance.conversationRepository = conversationRepository;
  }
}
