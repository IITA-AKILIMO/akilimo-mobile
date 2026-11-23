package com.akilimo.mobile.screenshots

import com.akilimo.mobile.database.AppDatabase
import com.akilimo.mobile.entities.AkilimoUser
import com.akilimo.mobile.entities.CassavaMarketPrice
import com.akilimo.mobile.entities.CassavaUnit
import com.akilimo.mobile.entities.Fertilizer
import com.akilimo.mobile.entities.InvestmentAmount
import com.akilimo.mobile.entities.StarchFactory
import com.akilimo.mobile.enums.EnumCountry
import com.akilimo.mobile.enums.EnumUseCase

/**
 * Generic sample data for the store screenshots. The app normally downloads the reference lists
 * (fertilizers, markets, investment amounts) from the API; a fresh test install has none, so
 * the forms would show empty states. Nothing here is real farmer data.
 */
object DemoData {
    const val USER_NAME = "screenshot-demo"
    private const val COUNTRY = "TZ"
    private const val CURRENCY = "TZS"
    private const val SYMBOL = "TSh"

    fun user(language: String) = AkilimoUser(
        userName = USER_NAME,
        firstName = "Jane",
        lastName = "Doe",
        farmName = "Doe Family Farm",
        enumCountry = EnumCountry.TZ,
        farmSize = 2.0,
        // Mwanza region, so the map picker opens on a real farming area instead of the ocean at 0,0
        latitude = -2.5164,
        longitude = 32.9175,
        zoomLevel = 12.0,
        languageCode = language,
        recommendationLanguage = language,
    )

    suspend fun seedReferenceData(db: AppDatabase) {
        val fertilizers = listOf(
            Triple("urea", "Urea", Triple(46, 0, 0)),
            Triple("dap", "DAP", Triple(18, 46, 0)),
            Triple("npk171717", "NPK 17:17:17", Triple(17, 17, 17)),
            Triple("can", "CAN", Triple(27, 0, 0)),
        )
        fertilizers.forEachIndexed { index, (key, name, npk) ->
            db.fertilizerDao().insert(
                Fertilizer(
                    id = index + 1,
                    key = key,
                    name = name,
                    type = key,
                    category = if (npk.first > 0 && npk.second + npk.third > 0) "COMPOUND" else "STRAIGHT",
                    weight = 50.0,
                    sortOrder = index,
                    price = 0.0,
                    useCase = EnumUseCase.FR,
                    countryCode = COUNTRY,
                    nContent = npk.first,
                    pContent = npk.second,
                    kContent = npk.third,
                    available = true,
                    cimAvailable = true,
                    cisAvailable = true,
                ),
            )
        }

        listOf(100_000.0, 200_000.0, 300_000.0, 400_000.0).forEachIndexed { index, amount ->
            db.investmentAmountDao().insert(
                InvestmentAmount(
                    id = index + 1,
                    countryCode = COUNTRY,
                    currencyCode = CURRENCY,
                    currencySymbol = SYMBOL,
                    investmentAmount = amount,
                    exactAmount = false,
                    active = true,
                    areaUnit = "acre",
                    sortOrder = index,
                ),
            )
        }

        listOf("Sample Starch Factory 1", "Sample Starch Factory 2", "Sample Starch Factory 3")
            .forEachIndexed { index, name ->
                db.starchFactoryDao().insert(
                    StarchFactory(
                        id = index + 1,
                        name = name,
                        label = name,
                        countryCode = COUNTRY,
                        isActive = true,
                        sortOrder = index,
                    ),
                )
            }

        // Labels are EnumUnitOfSale names, which the screen turns into a translated "1 tonne" style name
        val units = listOf(
            Triple("ONE_KG", 1.0, 400.0),
            Triple("FIFTY_KG", 50.0, 20_000.0),
            Triple("HUNDRED_KG", 100.0, 40_000.0),
            Triple("THOUSAND_KG", 1000.0, 400_000.0),
        )
        units.forEachIndexed { index, (label, weight, price) ->
            db.cassavaUnitDao().insert(
                CassavaUnit(
                    id = index + 1,
                    unitWeight = weight,
                    sortOrder = index,
                    label = label,
                    description = null,
                    isActive = true,
                ),
            )
            db.cassavaMarketPriceDao().insert(
                CassavaMarketPrice(
                    id = index + 1,
                    countryCode = EnumCountry.TZ,
                    currencyCode = CURRENCY,
                    currencySymbol = SYMBOL,
                    averagePrice = price,
                    exactPrice = false,
                    itemTag = label,
                    active = true,
                    sortOrder = index,
                ),
            )
        }
    }
}
