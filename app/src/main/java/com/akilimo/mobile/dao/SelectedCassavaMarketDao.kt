package com.akilimo.mobile.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.akilimo.mobile.entities.SelectedCassavaMarket
import com.akilimo.mobile.entities.relations.SelectedCassavaMarketWithDetails
import kotlinx.coroutines.flow.Flow

@Dao
interface SelectedCassavaMarketDao {

    @Query("SELECT * FROM selected_cassava_markets")
    fun observeAll(): Flow<List<SelectedCassavaMarketWithDetails>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(selected: SelectedCassavaMarket): Long

    @Update
    fun update(selected: SelectedCassavaMarket)

    @Upsert
    fun upsert(selectedMarket: SelectedCassavaMarket)


    @Query("SELECT * FROM selected_cassava_markets WHERE user_id = :userId LIMIT 1")
    fun getSelectedByUser(userId: Int): SelectedCassavaMarketWithDetails?

    @Query("SELECT * FROM selected_cassava_markets WHERE market_price_id = :marketPriceId LIMIT 1")
    fun getSelectedByMarketPriceId(marketPriceId: Int): SelectedCassavaMarket?

    @Query("SELECT * FROM selected_cassava_markets WHERE starch_factory_id = :starchFactoryId LIMIT 1")
    fun getSelectedByStarchFactoryId(starchFactoryId: Int): SelectedCassavaMarket?

    @Query("DELETE FROM selected_cassava_markets WHERE user_id = :userId")
    fun deleteSelected(userId: Int)

    @Query("SELECT * FROM selected_cassava_markets WHERE user_id = :userId")
    fun observeByUser(userId: Int): Flow<SelectedCassavaMarketWithDetails?>

    @Query("SELECT * FROM selected_cassava_markets WHERE user_id = :userId and market_price_id=:marketPriceId")
    fun observeByUserAndMarket(
        userId: Int,
        marketPriceId: Int
    ): Flow<SelectedCassavaMarketWithDetails?>

    @Query("SELECT * FROM selected_cassava_markets WHERE user_id = :userId and starch_factory_id=:factoryId")
    fun observeByUserAndFactory(
        userId: Int,
        factoryId: Int
    ): Flow<SelectedCassavaMarketWithDetails?>

    @Query("SELECT COUNT(*) FROM selected_cassava_markets WHERE user_id = :userId AND market_price_id=:marketPriceId")
    fun isMarketSelected(userId: Int, marketPriceId: Int): Int

    @Query("SELECT COUNT(*) FROM selected_cassava_markets WHERE user_id = :userId AND starch_factory_id=:factoryId")
    fun isFactorySelected(userId: Int, factoryId: Int): Int

}
