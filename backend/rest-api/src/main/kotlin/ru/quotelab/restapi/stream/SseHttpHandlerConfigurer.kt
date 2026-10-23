package ru.quotelab.restapi.stream

import io.undertow.server.HttpHandler
import io.undertow.server.handlers.sse.ServerSentEventHandler
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.server.undertow.HttpHandlerConfigurer

@Component
class SseHttpHandlerConfigurer(
    private val broadcaster: SseBroadcaster,
) : HttpHandlerConfigurer {
    override fun configure(handler: HttpHandler): HttpHandler {
        val sse = ServerSentEventHandler(broadcaster)
        return HttpHandler { exchange ->
            if (exchange.requestPath == PATH && exchange.requestMethod.toString() == "GET") {
                sse.handleRequest(exchange)
            } else {
                handler.handleRequest(exchange)
            }
        }
    }

    private companion object {
        const val PATH = "/api/stream"
    }
}
