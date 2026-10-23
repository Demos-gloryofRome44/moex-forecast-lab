package ru.quotelab.restapi.stream

import io.undertow.server.handlers.sse.ServerSentEventConnection
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import java.util.concurrent.CopyOnWriteArraySet

@Component
class SseBroadcaster : ServerSentEventConnectionCallback {
    private val log = LoggerFactory.getLogger(javaClass)
    private val connections = CopyOnWriteArraySet<ServerSentEventConnection>()

    val subscribers: Int get() = connections.size

    override fun connected(connection: ServerSentEventConnection, lastEventId: String?) {
        connection.setKeepAliveTime(KEEP_ALIVE_MILLIS)
        connection.addCloseTask { connections.remove(it) }
        connections.add(connection)
        connection.send("""{"subscribers":${connections.size}}""", "hello", null, null)
        log.debug("SSE subscriber connected, total {}", connections.size)
    }

    fun broadcast(event: String, json: String) {
        connections.forEach { connection ->
            if (connection.isOpen) connection.send(json, event, null, null) else connections.remove(connection)
        }
    }

    private companion object {
        const val KEEP_ALIVE_MILLIS = 15_000L
    }
}
