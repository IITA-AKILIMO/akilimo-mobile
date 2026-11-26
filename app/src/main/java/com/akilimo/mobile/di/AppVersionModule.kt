package com.akilimo.mobile.di

import com.akilimo.mobile.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

/** The `versionCode` of the installed build. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class InstalledVersionCode

@Module
@InstallIn(SingletonComponent::class)
object AppVersionModule {

    @Provides
    @InstalledVersionCode
    fun provideInstalledVersionCode(): Int = BuildConfig.VERSION_CODE
}
