package com.nexussms;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.nexussms.data.database.NexusSMSDatabase;
import com.nexussms.data.repository.ConversationRepository;
import com.nexussms.data.repository.MessageRepository;
import com.nexussms.data.repository.ScheduledMessageRepository;
import com.nexussms.data.repository.ShortcutRepository;
import com.nexussms.data.repository.SignatureRepository;
import com.nexussms.data.repository.SocialAccountRepository;
import com.nexussms.data.repository.ThemeRepository;
import com.nexussms.di.AppModule_ProvideConversationRepositoryFactory;
import com.nexussms.di.AppModule_ProvideDatabaseFactory;
import com.nexussms.di.AppModule_ProvideMessageRepositoryFactory;
import com.nexussms.di.AppModule_ProvideScheduledMessageRepositoryFactory;
import com.nexussms.di.AppModule_ProvideShortcutRepositoryFactory;
import com.nexussms.di.AppModule_ProvideSignatureRepositoryFactory;
import com.nexussms.di.AppModule_ProvideSocialAccountRepositoryFactory;
import com.nexussms.di.AppModule_ProvideThemeRepositoryFactory;
import com.nexussms.features.rcs.RcsService;
import com.nexussms.features.shortcodes.ShortcodeExpansionService;
import com.nexussms.features.theme.ThemeManager;
import com.nexussms.receivers.SmsReceiver;
import com.nexussms.receivers.SmsReceiver_MembersInjector;
import com.nexussms.security.EncryptionManager;
import com.nexussms.services.MessageService;
import com.nexussms.services.MessageService_MembersInjector;
import com.nexussms.services.ScheduledMessageWorker;
import com.nexussms.services.ScheduledMessageWorker_AssistedFactory;
import com.nexussms.ui.viewmodels.ChatViewModel;
import com.nexussms.ui.viewmodels.ChatViewModel_HiltModules;
import com.nexussms.ui.viewmodels.ConversationListViewModel;
import com.nexussms.ui.viewmodels.ConversationListViewModel_HiltModules;
import com.nexussms.ui.viewmodels.SettingsViewModel;
import com.nexussms.ui.viewmodels.SettingsViewModel_HiltModules;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.IdentifierNameString;
import dagger.internal.KeepFieldType;
import dagger.internal.LazyClassKeyMap;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

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
public final class DaggerNexusSMSApplication_HiltComponents_SingletonC {
  private DaggerNexusSMSApplication_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public NexusSMSApplication_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements NexusSMSApplication_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public NexusSMSApplication_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements NexusSMSApplication_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public NexusSMSApplication_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements NexusSMSApplication_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public NexusSMSApplication_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements NexusSMSApplication_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public NexusSMSApplication_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements NexusSMSApplication_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public NexusSMSApplication_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements NexusSMSApplication_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public NexusSMSApplication_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements NexusSMSApplication_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public NexusSMSApplication_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends NexusSMSApplication_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends NexusSMSApplication_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends NexusSMSApplication_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends NexusSMSApplication_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Map<Class<?>, Boolean> getViewModelKeys() {
      return LazyClassKeyMap.<Boolean>of(MapBuilder.<String, Boolean>newMapBuilder(3).put(LazyClassKeyProvider.com_nexussms_ui_viewmodels_ChatViewModel, ChatViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nexussms_ui_viewmodels_ConversationListViewModel, ConversationListViewModel_HiltModules.KeyModule.provide()).put(LazyClassKeyProvider.com_nexussms_ui_viewmodels_SettingsViewModel, SettingsViewModel_HiltModules.KeyModule.provide()).build());
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_nexussms_ui_viewmodels_ChatViewModel = "com.nexussms.ui.viewmodels.ChatViewModel";

      static String com_nexussms_ui_viewmodels_ConversationListViewModel = "com.nexussms.ui.viewmodels.ConversationListViewModel";

      static String com_nexussms_ui_viewmodels_SettingsViewModel = "com.nexussms.ui.viewmodels.SettingsViewModel";

      @KeepFieldType
      ChatViewModel com_nexussms_ui_viewmodels_ChatViewModel2;

      @KeepFieldType
      ConversationListViewModel com_nexussms_ui_viewmodels_ConversationListViewModel2;

      @KeepFieldType
      SettingsViewModel com_nexussms_ui_viewmodels_SettingsViewModel2;
    }
  }

  private static final class ViewModelCImpl extends NexusSMSApplication_HiltComponents.ViewModelC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<ChatViewModel> chatViewModelProvider;

    private Provider<ConversationListViewModel> conversationListViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;

      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.chatViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.conversationListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
    }

    @Override
    public Map<Class<?>, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return LazyClassKeyMap.<javax.inject.Provider<ViewModel>>of(MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(3).put(LazyClassKeyProvider.com_nexussms_ui_viewmodels_ChatViewModel, ((Provider) chatViewModelProvider)).put(LazyClassKeyProvider.com_nexussms_ui_viewmodels_ConversationListViewModel, ((Provider) conversationListViewModelProvider)).put(LazyClassKeyProvider.com_nexussms_ui_viewmodels_SettingsViewModel, ((Provider) settingsViewModelProvider)).build());
    }

    @Override
    public Map<Class<?>, Object> getHiltViewModelAssistedMap() {
      return Collections.<Class<?>, Object>emptyMap();
    }

    @IdentifierNameString
    private static final class LazyClassKeyProvider {
      static String com_nexussms_ui_viewmodels_ChatViewModel = "com.nexussms.ui.viewmodels.ChatViewModel";

      static String com_nexussms_ui_viewmodels_ConversationListViewModel = "com.nexussms.ui.viewmodels.ConversationListViewModel";

      static String com_nexussms_ui_viewmodels_SettingsViewModel = "com.nexussms.ui.viewmodels.SettingsViewModel";

      @KeepFieldType
      ChatViewModel com_nexussms_ui_viewmodels_ChatViewModel2;

      @KeepFieldType
      ConversationListViewModel com_nexussms_ui_viewmodels_ConversationListViewModel2;

      @KeepFieldType
      SettingsViewModel com_nexussms_ui_viewmodels_SettingsViewModel2;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.nexussms.ui.viewmodels.ChatViewModel 
          return (T) new ChatViewModel(singletonCImpl.provideMessageRepositoryProvider.get(), singletonCImpl.provideConversationRepositoryProvider.get(), singletonCImpl.shortcodeExpansionServiceProvider.get(), singletonCImpl.rcsServiceProvider.get(), singletonCImpl.encryptionManagerProvider.get());

          case 1: // com.nexussms.ui.viewmodels.ConversationListViewModel 
          return (T) new ConversationListViewModel(singletonCImpl.provideConversationRepositoryProvider.get());

          case 2: // com.nexussms.ui.viewmodels.SettingsViewModel 
          return (T) new SettingsViewModel(singletonCImpl.provideThemeRepositoryProvider.get(), singletonCImpl.provideSignatureRepositoryProvider.get(), singletonCImpl.provideSocialAccountRepositoryProvider.get(), singletonCImpl.themeManagerProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends NexusSMSApplication_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends NexusSMSApplication_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }

    @Override
    public void injectMessageService(MessageService messageService) {
      injectMessageService2(messageService);
    }

    private MessageService injectMessageService2(MessageService instance) {
      MessageService_MembersInjector.injectMessageRepository(instance, singletonCImpl.provideMessageRepositoryProvider.get());
      MessageService_MembersInjector.injectEncryptionManager(instance, singletonCImpl.encryptionManagerProvider.get());
      return instance;
    }
  }

  private static final class SingletonCImpl extends NexusSMSApplication_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<NexusSMSDatabase> provideDatabaseProvider;

    private Provider<ScheduledMessageRepository> provideScheduledMessageRepositoryProvider;

    private Provider<MessageRepository> provideMessageRepositoryProvider;

    private Provider<ScheduledMessageWorker_AssistedFactory> scheduledMessageWorker_AssistedFactoryProvider;

    private Provider<ConversationRepository> provideConversationRepositoryProvider;

    private Provider<ShortcutRepository> provideShortcutRepositoryProvider;

    private Provider<ShortcodeExpansionService> shortcodeExpansionServiceProvider;

    private Provider<RcsService> rcsServiceProvider;

    private Provider<EncryptionManager> encryptionManagerProvider;

    private Provider<ThemeRepository> provideThemeRepositoryProvider;

    private Provider<SignatureRepository> provideSignatureRepositoryProvider;

    private Provider<SocialAccountRepository> provideSocialAccountRepositoryProvider;

    private Provider<ThemeManager> themeManagerProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return Collections.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>singletonMap("com.nexussms.services.ScheduledMessageWorker", ((Provider) scheduledMessageWorker_AssistedFactoryProvider));
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<NexusSMSDatabase>(singletonCImpl, 2));
      this.provideScheduledMessageRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ScheduledMessageRepository>(singletonCImpl, 1));
      this.provideMessageRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<MessageRepository>(singletonCImpl, 3));
      this.scheduledMessageWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<ScheduledMessageWorker_AssistedFactory>(singletonCImpl, 0));
      this.provideConversationRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ConversationRepository>(singletonCImpl, 4));
      this.provideShortcutRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ShortcutRepository>(singletonCImpl, 6));
      this.shortcodeExpansionServiceProvider = DoubleCheck.provider(new SwitchingProvider<ShortcodeExpansionService>(singletonCImpl, 5));
      this.rcsServiceProvider = DoubleCheck.provider(new SwitchingProvider<RcsService>(singletonCImpl, 7));
      this.encryptionManagerProvider = DoubleCheck.provider(new SwitchingProvider<EncryptionManager>(singletonCImpl, 8));
      this.provideThemeRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<ThemeRepository>(singletonCImpl, 9));
      this.provideSignatureRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SignatureRepository>(singletonCImpl, 10));
      this.provideSocialAccountRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SocialAccountRepository>(singletonCImpl, 11));
      this.themeManagerProvider = DoubleCheck.provider(new SwitchingProvider<ThemeManager>(singletonCImpl, 12));
    }

    @Override
    public void injectNexusSMSApplication(NexusSMSApplication nexusSMSApplication) {
      injectNexusSMSApplication2(nexusSMSApplication);
    }

    @Override
    public void injectSmsReceiver(SmsReceiver smsReceiver) {
      injectSmsReceiver2(smsReceiver);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private NexusSMSApplication injectNexusSMSApplication2(NexusSMSApplication instance) {
      NexusSMSApplication_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      return instance;
    }

    private SmsReceiver injectSmsReceiver2(SmsReceiver instance) {
      SmsReceiver_MembersInjector.injectMessageRepository(instance, provideMessageRepositoryProvider.get());
      SmsReceiver_MembersInjector.injectConversationRepository(instance, provideConversationRepositoryProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.nexussms.services.ScheduledMessageWorker_AssistedFactory 
          return (T) new ScheduledMessageWorker_AssistedFactory() {
            @Override
            public ScheduledMessageWorker create(Context context, WorkerParameters params) {
              return new ScheduledMessageWorker(context, params, singletonCImpl.provideScheduledMessageRepositoryProvider.get(), singletonCImpl.provideMessageRepositoryProvider.get());
            }
          };

          case 1: // com.nexussms.data.repository.ScheduledMessageRepository 
          return (T) AppModule_ProvideScheduledMessageRepositoryFactory.provideScheduledMessageRepository(singletonCImpl.provideDatabaseProvider.get());

          case 2: // com.nexussms.data.database.NexusSMSDatabase 
          return (T) AppModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 3: // com.nexussms.data.repository.MessageRepository 
          return (T) AppModule_ProvideMessageRepositoryFactory.provideMessageRepository(singletonCImpl.provideDatabaseProvider.get());

          case 4: // com.nexussms.data.repository.ConversationRepository 
          return (T) AppModule_ProvideConversationRepositoryFactory.provideConversationRepository(singletonCImpl.provideDatabaseProvider.get());

          case 5: // com.nexussms.features.shortcodes.ShortcodeExpansionService 
          return (T) new ShortcodeExpansionService(singletonCImpl.provideShortcutRepositoryProvider.get());

          case 6: // com.nexussms.data.repository.ShortcutRepository 
          return (T) AppModule_ProvideShortcutRepositoryFactory.provideShortcutRepository(singletonCImpl.provideDatabaseProvider.get());

          case 7: // com.nexussms.features.rcs.RcsService 
          return (T) new RcsService(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideMessageRepositoryProvider.get());

          case 8: // com.nexussms.security.EncryptionManager 
          return (T) new EncryptionManager(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 9: // com.nexussms.data.repository.ThemeRepository 
          return (T) AppModule_ProvideThemeRepositoryFactory.provideThemeRepository(singletonCImpl.provideDatabaseProvider.get());

          case 10: // com.nexussms.data.repository.SignatureRepository 
          return (T) AppModule_ProvideSignatureRepositoryFactory.provideSignatureRepository(singletonCImpl.provideDatabaseProvider.get());

          case 11: // com.nexussms.data.repository.SocialAccountRepository 
          return (T) AppModule_ProvideSocialAccountRepositoryFactory.provideSocialAccountRepository(singletonCImpl.provideDatabaseProvider.get());

          case 12: // com.nexussms.features.theme.ThemeManager 
          return (T) new ThemeManager(singletonCImpl.provideThemeRepositoryProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}
