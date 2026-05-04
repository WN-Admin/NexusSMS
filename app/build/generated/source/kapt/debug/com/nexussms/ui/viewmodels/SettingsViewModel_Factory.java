package com.nexussms.ui.viewmodels;

import com.nexussms.data.repository.SignatureRepository;
import com.nexussms.data.repository.SocialAccountRepository;
import com.nexussms.data.repository.ThemeRepository;
import com.nexussms.features.theme.ThemeManager;
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<ThemeRepository> themeRepositoryProvider;

  private final Provider<SignatureRepository> signatureRepositoryProvider;

  private final Provider<SocialAccountRepository> socialAccountRepositoryProvider;

  private final Provider<ThemeManager> themeManagerProvider;

  public SettingsViewModel_Factory(Provider<ThemeRepository> themeRepositoryProvider,
      Provider<SignatureRepository> signatureRepositoryProvider,
      Provider<SocialAccountRepository> socialAccountRepositoryProvider,
      Provider<ThemeManager> themeManagerProvider) {
    this.themeRepositoryProvider = themeRepositoryProvider;
    this.signatureRepositoryProvider = signatureRepositoryProvider;
    this.socialAccountRepositoryProvider = socialAccountRepositoryProvider;
    this.themeManagerProvider = themeManagerProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(themeRepositoryProvider.get(), signatureRepositoryProvider.get(), socialAccountRepositoryProvider.get(), themeManagerProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<ThemeRepository> themeRepositoryProvider,
      Provider<SignatureRepository> signatureRepositoryProvider,
      Provider<SocialAccountRepository> socialAccountRepositoryProvider,
      Provider<ThemeManager> themeManagerProvider) {
    return new SettingsViewModel_Factory(themeRepositoryProvider, signatureRepositoryProvider, socialAccountRepositoryProvider, themeManagerProvider);
  }

  public static SettingsViewModel newInstance(ThemeRepository themeRepository,
      SignatureRepository signatureRepository, SocialAccountRepository socialAccountRepository,
      ThemeManager themeManager) {
    return new SettingsViewModel(themeRepository, signatureRepository, socialAccountRepository, themeManager);
  }
}
