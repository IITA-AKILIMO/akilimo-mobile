package com.akilimo.mobile.di

import android.content.Context
import com.akilimo.mobile.database.AppDatabase
import com.akilimo.mobile.dao.MaizePerformanceRepo
import com.akilimo.mobile.repos.AkilimoUserRepo
import com.akilimo.mobile.repos.CassavaMarketPriceRepo
import com.akilimo.mobile.repos.CassavaUnitRepo
import com.akilimo.mobile.repos.CassavaYieldRepo
import com.akilimo.mobile.repos.CurrentPracticeRepo
import com.akilimo.mobile.repos.FieldOperationCostsRepo
import com.akilimo.mobile.repos.InvestmentRepo
import com.akilimo.mobile.repos.SelectedCassavaMarketRepo
import com.akilimo.mobile.repos.SelectedInvestmentRepo
import com.akilimo.mobile.repos.StarchFactoryRepo
import com.akilimo.mobile.dao.ProduceMarketRepo
import com.akilimo.mobile.repos.AdviceCompletionRepo
import com.akilimo.mobile.repos.FertilizerPriceRepo
import com.akilimo.mobile.repos.FertilizerRepo
import com.akilimo.mobile.repos.SelectedFertilizerRepo
import com.akilimo.mobile.repos.UserPreferencesRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        AppDatabase.getDatabase(context)

    @Provides
    @Singleton
    fun provideAkilimoUserRepo(db: AppDatabase): AkilimoUserRepo =
        AkilimoUserRepo(db.akilimoUserDao())

    @Provides
    @Singleton
    fun provideUserPreferencesRepo(db: AppDatabase): UserPreferencesRepo =
        UserPreferencesRepo(db.userPreferencesDao())

    @Provides
    @Singleton
    fun provideMaizePerformanceRepo(db: AppDatabase): MaizePerformanceRepo =
        MaizePerformanceRepo(db.maizePerformanceDao())

    @Provides
    @Singleton
    fun provideInvestmentRepo(db: AppDatabase): InvestmentRepo =
        InvestmentRepo(db.investmentAmountDao())

    @Provides
    @Singleton
    fun provideSelectedInvestmentRepo(db: AppDatabase): SelectedInvestmentRepo =
        SelectedInvestmentRepo(db.selectedInvestmentDao())

    @Provides
    @Singleton
    fun provideCassavaYieldRepo(db: AppDatabase): CassavaYieldRepo =
        CassavaYieldRepo(db.cassavaYieldDao())

    @Provides
    @Singleton
    fun provideSelectedCassavaMarketRepo(db: AppDatabase): SelectedCassavaMarketRepo =
        SelectedCassavaMarketRepo(db.selectedCassavaMarketDao())

    @Provides
    @Singleton
    fun provideStarchFactoryRepo(db: AppDatabase): StarchFactoryRepo =
        StarchFactoryRepo(db.starchFactoryDao())

    @Provides
    @Singleton
    fun provideCassavaMarketPriceRepo(db: AppDatabase): CassavaMarketPriceRepo =
        CassavaMarketPriceRepo(db.cassavaMarketPriceDao())

    @Provides
    @Singleton
    fun provideCassavaUnitRepo(db: AppDatabase): CassavaUnitRepo =
        CassavaUnitRepo(db.cassavaUnitDao())

    @Provides
    @Singleton
    fun provideFieldOperationCostsRepo(db: AppDatabase): FieldOperationCostsRepo =
        FieldOperationCostsRepo(db.fieldOperationCostsDao())

    @Provides
    @Singleton
    fun provideCurrentPracticeRepo(db: AppDatabase): CurrentPracticeRepo =
        CurrentPracticeRepo(db.currentPracticeDao())

    @Provides
    @Singleton
    fun provideAdviceCompletionRepo(db: AppDatabase): AdviceCompletionRepo =
        AdviceCompletionRepo(db.adviceCompletionDao())

    @Provides
    @Singleton
    fun provideFertilizerRepo(db: AppDatabase): FertilizerRepo =
        FertilizerRepo(db.fertilizerDao())

    @Provides
    @Singleton
    fun provideSelectedFertilizerRepo(db: AppDatabase): SelectedFertilizerRepo =
        SelectedFertilizerRepo(db.selectedFertilizerDao())

    @Provides
    @Singleton
    fun provideFertilizerPriceRepo(db: AppDatabase): FertilizerPriceRepo =
        FertilizerPriceRepo(db.fertilizerPriceDao())

    @Provides
    @Singleton
    fun provideProduceMarketRepo(db: AppDatabase): ProduceMarketRepo =
        ProduceMarketRepo(db.produceMarketDao())
}
