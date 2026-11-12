package com.akilimo.mobile.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A recommendation saved to the farmer's account, kept on the phone so the list also opens offline.
 * [requestId] is the API's id of the saved row (the same on the server and here). [recommendation] is null until the text has
 * been fetched (the history list carries no text). Rows are removed when the farmer signs out or deletes the account.
 */
@Entity(tableName = "saved_advice", indices = [Index("account_id")])
data class SavedAdvice(
    @PrimaryKey
    @ColumnInfo(name = "request_id") val requestId: String,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "country_code") val countryCode: String?,
    @ColumnInfo(name = "use_case") val useCase: String?,
    @ColumnInfo(name = "rec_type") val recType: String?,
    @ColumnInfo(name = "recommendation") val recommendation: String?,
    /** Epoch millis the advice was computed. */
    @ColumnInfo(name = "created_at") val createdAt: Long,
)
