package com.akilimo.mobile.di

import com.akilimo.mobile.dao.SavedAdviceDao
import com.akilimo.mobile.data.account.KeystoreTokenCipher
import com.akilimo.mobile.data.account.TokenCipher
import com.akilimo.mobile.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Wiring for the optional farmer accounts. */
@Module
@InstallIn(SingletonComponent::class)
object AccountModule {

    @Provides
    @Singleton
    fun provideTokenCipher(): TokenCipher = KeystoreTokenCipher.create()

    @Provides
    @Singleton
    fun provideSavedAdviceDao(db: AppDatabase): SavedAdviceDao = db.savedAdviceDao()
}
