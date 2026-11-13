package com.akilimo.mobile.repos.account

import com.akilimo.mobile.data.account.AccountSessionStore
import com.akilimo.mobile.network.AccountApi
import kotlinx.coroutines.CancellationException
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs account API calls and turns every outcome into an [AccountResult]: no exception leaves it except cancellation.
 * An answer that says the session ended signs the farmer out here, so every caller handles it the same way.
 */
@Singleton
class AccountCalls @Inject constructor(
    private val apiFactory: AccountApiFactory,
    private val sessionStore: AccountSessionStore,
) {
    /** [authenticated]: the call carries the farmer's token, so a 401 means the session ended. */
    suspend fun <T : Any> execute(
        authenticated: Boolean,
        block: suspend (AccountApi) -> Response<T>,
    ): AccountResult<T> = try {
        val response = block(apiFactory.create())
        val body = response.body()
        when {
            response.isSuccessful && body != null -> AccountResult.Success(body)
            response.isSuccessful -> AccountResult.Failure(AccountError.SERVER)
            else -> AccountErrors.failure(response, authenticated).also { failure ->
                if (authenticated && failure.error == AccountError.SESSION_EXPIRED) sessionStore.clear()
            }
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: IOException) {
        AccountResult.Failure(AccountError.NETWORK)
    } catch (_: Exception) {
        AccountResult.Failure(AccountError.SERVER)
    }
}
