package com.nexussms.ui.viewmodels;

import com.nexussms.data.repository.ConversationRepository;
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
public final class ConversationListViewModel_Factory implements Factory<ConversationListViewModel> {
  private final Provider<ConversationRepository> conversationRepositoryProvider;

  public ConversationListViewModel_Factory(
      Provider<ConversationRepository> conversationRepositoryProvider) {
    this.conversationRepositoryProvider = conversationRepositoryProvider;
  }

  @Override
  public ConversationListViewModel get() {
    return newInstance(conversationRepositoryProvider.get());
  }

  public static ConversationListViewModel_Factory create(
      Provider<ConversationRepository> conversationRepositoryProvider) {
    return new ConversationListViewModel_Factory(conversationRepositoryProvider);
  }

  public static ConversationListViewModel newInstance(
      ConversationRepository conversationRepository) {
    return new ConversationListViewModel(conversationRepository);
  }
}
