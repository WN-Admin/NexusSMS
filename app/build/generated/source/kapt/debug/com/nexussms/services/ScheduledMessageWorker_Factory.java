package com.nexussms.services;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.nexussms.data.repository.MessageRepository;
import com.nexussms.data.repository.ScheduledMessageRepository;
import dagger.internal.DaggerGenerated;
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
public final class ScheduledMessageWorker_Factory {
  private final Provider<ScheduledMessageRepository> scheduledMessageRepositoryProvider;

  private final Provider<MessageRepository> messageRepositoryProvider;

  public ScheduledMessageWorker_Factory(
      Provider<ScheduledMessageRepository> scheduledMessageRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    this.scheduledMessageRepositoryProvider = scheduledMessageRepositoryProvider;
    this.messageRepositoryProvider = messageRepositoryProvider;
  }

  public ScheduledMessageWorker get(Context context, WorkerParameters params) {
    return newInstance(context, params, scheduledMessageRepositoryProvider.get(), messageRepositoryProvider.get());
  }

  public static ScheduledMessageWorker_Factory create(
      Provider<ScheduledMessageRepository> scheduledMessageRepositoryProvider,
      Provider<MessageRepository> messageRepositoryProvider) {
    return new ScheduledMessageWorker_Factory(scheduledMessageRepositoryProvider, messageRepositoryProvider);
  }

  public static ScheduledMessageWorker newInstance(Context context, WorkerParameters params,
      ScheduledMessageRepository scheduledMessageRepository, MessageRepository messageRepository) {
    return new ScheduledMessageWorker(context, params, scheduledMessageRepository, messageRepository);
  }
}
