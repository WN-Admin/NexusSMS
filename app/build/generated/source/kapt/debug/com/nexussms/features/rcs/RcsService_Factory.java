package com.nexussms.features.rcs;

import android.content.Context;
import com.nexussms.data.repository.MessageRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class RcsService_Factory implements Factory<RcsService> {
  private final Provider<Context> contextProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  public RcsService_Factory(Provider<Context> contextProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  @Override
  public RcsService get() {
    return newInstance(contextProvider.get(), messageRepositoryProvider.get());
  }

  public static RcsService_Factory create(Provider<Context> contextProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    return new RcsService_Factory(contextProvider, messageRepositoryProvider);
  }

  public static RcsService newInstance(Context context, MessageRepository messageRepository) {
    return new RcsService(context, messageRepository);
  }
}
