package com.nexussms.services;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class ScheduledMessageWorker_AssistedFactory_Impl implements ScheduledMessageWorker_AssistedFactory {
  private final ScheduledMessageWorker_Factory delegateFactory;

  ScheduledMessageWorker_AssistedFactory_Impl(ScheduledMessageWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public ScheduledMessageWorker create(Context arg0, WorkerParameters arg1) {
    return delegateFactory.get(arg0, arg1);
  }

  public static Provider<ScheduledMessageWorker_AssistedFactory> create(
      ScheduledMessageWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ScheduledMessageWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<ScheduledMessageWorker_AssistedFactory> createFactoryProvider(
      ScheduledMessageWorker_Factory delegateFactory) {
    return InstanceFactory.create(new ScheduledMessageWorker_AssistedFactory_Impl(delegateFactory));
  }
}
