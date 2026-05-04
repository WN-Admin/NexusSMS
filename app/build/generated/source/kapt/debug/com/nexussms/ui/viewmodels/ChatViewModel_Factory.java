package com.nexussms.ui.viewmodels;

import com.nexussms.data.repository.ConversationRepository;
import com.nexussms.data.repository.MessageRepository;
import com.nexussms.features.rcs.RcsService;
import com.nexussms.features.shortcodes.ShortcodeExpansionService;
import com.nexussms.security.EncryptionManager;
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
public final class ChatViewModel_Factory implements Factory<ChatViewModel> {
  private final Provider<MessageRepository> messageRepositoryProvider;

  private final Provider<ConversationRepository> conversationRepositoryProvider;

  private final Provider<ShortcodeExpansionService> shortcodeExpansionServiceProvider;

  private final Provider<RcsService> rcsServiceProvider;

  private final Provider<EncryptionManager> encryptionManagerProvider;

  public ChatViewModel_Factory(Provider<MessageRepository> messageRepositoryProvider,
      Provider<ConversationRepository> conversationRepositoryProvider,
      Provider<ShortcodeExpansionService> shortcodeExpansionServiceProvider,
      Provider<RcsService> rcsServiceProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    this.messageRepositoryProvider = messageRepositoryProvider;
    this.conversationRepositoryProvider = conversationRepositoryProvider;
    this.shortcodeExpansionServiceProvider = shortcodeExpansionServiceProvider;
    this.rcsServiceProvider = rcsServiceProvider;
    this.encryptionManagerProvider = encryptionManagerProvider;
  }

  @Override
  public ChatViewModel get() {
    return newInstance(messageRepositoryProvider.get(), conversationRepositoryProvider.get(), shortcodeExpansionServiceProvider.get(), rcsServiceProvider.get(), encryptionManagerProvider.get());
  }

  public static ChatViewModel_Factory create(Provider<MessageRepository> messageRepositoryProvider,
      Provider<ConversationRepository> conversationRepositoryProvider,
      Provider<ShortcodeExpansionService> shortcodeExpansionServiceProvider,
      Provider<RcsService> rcsServiceProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    return new ChatViewModel_Factory(messageRepositoryProvider, conversationRepositoryProvider, shortcodeExpansionServiceProvider, rcsServiceProvider, encryptionManagerProvider);
  }

  public static ChatViewModel newInstance(MessageRepository messageRepository,
      ConversationRepository conversationRepository,
      ShortcodeExpansionService shortcodeExpansionService, RcsService rcsService,
      EncryptionManager encryptionManager) {
    return new ChatViewModel(messageRepository, conversationRepository, shortcodeExpansionService, rcsService, encryptionManager);
  }
}
