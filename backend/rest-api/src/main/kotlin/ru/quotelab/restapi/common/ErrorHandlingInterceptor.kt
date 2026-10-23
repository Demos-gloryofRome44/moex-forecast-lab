package ru.quotelab.restapi.common

import com.fasterxml.jackson.core.JsonProcessingException
import org.slf4j.LoggerFactory
import ru.quotelab.domain.problems.InvalidRequestException
import ru.quotelab.domain.problems.MarketDataUnavailableException
import ru.quotelab.domain.problems.NotFoundException
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor
import ru.tinkoff.kora.http.server.common.HttpServerModule
import ru.tinkoff.kora.http.server.common.HttpServerRequest
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.validation.common.ViolationException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException
import java.util.concurrent.CompletionStage
import java.util.concurrent.ExecutionException

@Tag(HttpServerModule::class)
@Component
class ErrorHandlingInterceptor(
    private val writer: JsonWriter<ErrorResponse>,
) : HttpServerInterceptor {
    private val log = LoggerFactory.getLogger(javaClass)

    override fun intercept(
        context: Context,
        request: HttpServerRequest,
        chain: HttpServerInterceptor.InterceptChain,
    ): CompletionStage<HttpServerResponse> =
        try {
            chain.process(context, request).exceptionally { toResponse(unwrap(it), request) }
        } catch (e: Exception) {
            CompletableFuture.completedFuture(toResponse(e, request))
        }

    private fun toResponse(error: Throwable, request: HttpServerRequest): HttpServerResponse = when (error) {
        is NotFoundException -> respond(NOT_FOUND, error.message ?: "Not found")
        is InvalidRequestException -> respond(BAD_REQUEST, error.message ?: "Bad request", error.details)
        is ViolationException -> respond(BAD_REQUEST, "Validation failed", error.violations.map { "${it.path().full()}: ${it.message()}" })
        is IllegalArgumentException -> respond(BAD_REQUEST, error.message ?: "Bad request")
        is MarketDataUnavailableException -> respond(SERVICE_UNAVAILABLE, error.message ?: "Market data is unavailable")
        is BusyException -> respond(CONFLICT, error.message ?: "Busy")
        is HttpServerResponseException ->
            if (error.isMalformedJson()) {
                respond(BAD_REQUEST, "Malformed JSON request body")
            } else {
                respond(error.code(), error.message ?: "Request failed")
            }
        else -> {
            log.error("Unhandled exception on {} {}", request.method(), request.path(), error)
            respond(INTERNAL_ERROR, "Internal server error")
        }
    }

    private fun respond(code: Int, message: String, details: List<String> = emptyList()): HttpServerResponse =
        HttpServerResponse.of(code, HttpBody.of("application/json", writer.toByteArray(ErrorResponse(message, details))))

    private fun Throwable.isMalformedJson(): Boolean =
        generateSequence(cause) { it.cause }.any { it is JsonProcessingException }

    private fun unwrap(e: Throwable): Throwable =
        if ((e is CompletionException || e is ExecutionException) && e.cause != null) unwrap(e.cause!!) else e

    private companion object {
        const val BAD_REQUEST = 400
        const val NOT_FOUND = 404
        const val CONFLICT = 409
        const val INTERNAL_ERROR = 500
        const val SERVICE_UNAVAILABLE = 503
    }
}

class BusyException(message: String) : RuntimeException(message)
