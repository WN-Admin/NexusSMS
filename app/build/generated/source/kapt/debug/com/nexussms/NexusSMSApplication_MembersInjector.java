package com.nexussms;

import androidx.hilt.work.HiltWorkerFactory;
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
public final class NexusSMSApplication_MembersInjector implements MembersInjector<NexusSMSApplication> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  public NexusSMSApplication_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
  }

  public static MembersInjector<NexusSMSApplication> create(
      Provider<HiltWorkerFactory> workerFactoryProvider) {
    return new NexusSMSApplication_MembersInjector(workerFactoryProvider);
  }

  @Override
  public void injectMembers(NexusSMSApplication instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
  }

  @InjectedFieldSignature("com.nexussms.NexusSMSApplication.workerFactory")
  public static void injectWorkerFactory(NexusSMSApplication instance,
      HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }
}
