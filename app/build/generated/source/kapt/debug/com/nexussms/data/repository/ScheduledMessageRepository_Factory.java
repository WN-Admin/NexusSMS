package com.nexussms.data.repository;

import com.nexussms.data.database.ScheduledMessageDao;
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
public final class ScheduledMessageRepository_Factory implements Factory<ScheduledMessageRepository> {
  private final Provider<ScheduledMessageDao> scheduledMessageDaoProvider;

  public ScheduledMessageRepository_Factory(
      Provider<ScheduledMessageDao> scheduledMessageDaoProvider) {
    this.scheduledMessageDaoProvider = scheduledMessageDaoProvider;
  }

  @Override
  public ScheduledMessageRepository get() {
    return newInstance(scheduledMessageDaoProvider.get());
  }

  public static ScheduledMessageRepository_Factory create(
      Provider<ScheduledMessageDao> scheduledMessageDaoProvider) {
    return new ScheduledMessageRepository_Factory(scheduledMessageDaoProvider);
  }

  public static ScheduledMessageRepository newInstance(ScheduledMessageDao scheduledMessageDao) {
    return new ScheduledMessageRepository(scheduledMessageDao);
  }
}
