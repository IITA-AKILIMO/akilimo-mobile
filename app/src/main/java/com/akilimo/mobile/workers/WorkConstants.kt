package com.akilimo.mobile.workers

object WorkConstants {
    const val FERTILIZER_WORK_NAME = "Fertilizer"
    const val FERTILIZER_PRICE_WORK_NAME = "FertilizerPrices"
    /** Unique name for the Fertilizer -> FertilizerPrices chain. */
    const val FERTILIZER_CHAIN_WORK_NAME = "FertilizerSyncChain"
    const val INVESTMENT_AMOUNT_WORK_NAME = "InvestmentAmount"
    const val STARCH_FACTORY_WORK_NAME = "StarchFactories"
    const val CASSAVA_MARKET_PRICES_WORK_NAME = "CassavaMarketPrices"
    const val CASSAVA_UNITS_WORK_NAME = "CassavaUnit"
    const val MAIZE_UNITS_WORK_NAME = "MaizeUnits"
    /** Start-up download of the privacy policy and terms (retried until it works). */
    const val LEGAL_PAGES_WORK_NAME = "LegalPages"
    /** The refresh of the saved legal pages, about every 30 days. */
    const val LEGAL_PAGES_PERIODIC_WORK_NAME = "LegalPagesRefresh"
}
