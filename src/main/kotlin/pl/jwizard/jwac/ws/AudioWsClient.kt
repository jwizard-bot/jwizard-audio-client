/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.ws

import dev.arbjerg.lavalink.protocol.v4.Message
import dev.arbjerg.lavalink.protocol.v4.Message.*
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent
import dev.arbjerg.lavalink.protocol.v4.Message.EmittedEvent.*
import dev.arbjerg.lavalink.protocol.v4.json
import okhttp3.*
import pl.jwizard.jwac.AudioClient
import pl.jwizard.jwac.ReactiveEventPublisher
import pl.jwizard.jwac.event.ClientEvent
import pl.jwizard.jwac.event.KPlayerUpdateEvent
import pl.jwizard.jwac.event.KReadyEvent
import pl.jwizard.jwac.event.KStatsEvent
import pl.jwizard.jwac.event.player.*
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.node.NodeConfig
import pl.jwizard.jwac.util.logger
import java.io.Closeable
import java.io.EOFException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

/**
 * A WebSocket client for handling audio events between the bot and an audio node.
 *
 * This client listens for WebSocket events such as track changes, player updates, and node statistics from the audio
 * node. It processes these events and updates the bot's state or triggers appropriate actions. It also includes logic
 * to reconnect to the audio node if the connection is lost.
 *
 * @property nodeConfig The configuration for the audio node.
 * @property httpClient The OkHttp client used to manage the WebSocket connection.
 * @property audioNode The audio node the client is communicating with.
 * @property eventPublisher The event publisher to dispatch events to the bot system.
 * @property audioClient The audio client managing the bot's audio state.
 * @author Miłosz Gilga
 */
class AudioWsClient(
	private val nodeConfig: NodeConfig,
	private val httpClient: OkHttpClient,
	private val audioNode: AudioNode,
	private val eventPublisher: ReactiveEventPublisher<ClientEvent>,
	private val audioClient: AudioClient,
) : WebSocketListener(), Closeable {

	companion object {
		private val log = logger<AudioWsClient>()
	}

	/**
	 * Indicates if the WebSocket connection is open.
	 */
	private var open = false

	/**
	 * Indicates if the client is allowed to reconnect.
	 */
	private var mayReconnect = true

	/**
	 * Time of the last reconnection attempt (in milliseconds).
	 */
	private var lastReconnectAttempt = 0L

	/**
	 * The number of reconnection attempts.
	 */
	private var reconnectAttempts = 0

	/**
	 * The WebSocket connection instance.
	 */
	private var webSocket: WebSocket? = null

	/**
	 * The handler for audio WebSocket events.
	 */
	private val audioWsEvent = AudioWsEventImpl(audioNode, audioClient)

	/**
	 * Connects to the audio node WebSocket server.
	 *
	 * @param instanceName The name of the client instance.
	 * @param botId The bot's unique ID.
	 * @param sessionId The session ID if reconnecting (optional).
	 */
	fun connect(instanceName: String, botId: Long, sessionId: String?) {
		webSocket?.let {
			it.close(WsCode.NORMAL, "New WS connection requested.")
			it.cancel()
		}
		val request = Request.Builder()
			.url("${nodeConfig.wsUrl}/v4/websocket")
			.addHeader("Authorization", nodeConfig.password)
			.addHeader("Client-Name", "jwc/$instanceName")
			.addHeader("User-Id", botId.toString())
			.apply { sessionId?.let { addHeader("Session-Id", it) } }
			.build()

		webSocket = httpClient.newWebSocket(request, this)
	}

	/**
	 * Reconnects to the audio node WebSocket server if necessary.
	 *
	 * @param instanceName The name of the client instance.
	 * @param botId The bot's unique ID.
	 * @param sessionId The session ID (optional).
	 */
	fun reconnect(instanceName: String, botId: Long, sessionId: String?) {
		val elapsedTime = System.currentTimeMillis() - lastReconnectAttempt
		if (webSocket != null && !open && elapsedTime > calcReconnectInterval(false) && mayReconnect) {
			lastReconnectAttempt = System.currentTimeMillis()
			reconnectAttempts += 1
			connect(instanceName, botId, sessionId)
		}
	}

	/**
	 * Handles the successful opening of the WebSocket connection.
	 *
	 * @param webSocket The WebSocket instance.
	 * @param response The response from the WebSocket server.
	 */
	override fun onOpen(webSocket: WebSocket, response: Response) {
		log.info("Audio node: {} has been connected.", nodeConfig)
		open = true
		reconnectAttempts = 0
	}

	/**
	 * Handles receiving a message from the WebSocket.
	 *
	 * @param webSocket The WebSocket instance.
	 * @param text The message text received from the WebSocket.
	 */
	override fun onMessage(webSocket: WebSocket, text: String) {
		val event = json.decodeFromString<Message>(text)
		log.debug("-> {}", text)

		when (event.op) {
			Op.Ready -> audioWsEvent.onReady(event as ReadyEvent)
			Op.Stats -> audioWsEvent.onStats(event as StatsEvent)
			Op.PlayerUpdate -> audioWsEvent.onPlayerUpdate(event as PlayerUpdateEvent)
			Op.Event -> {
				event as EmittedEvent
				when (event) {
					is TrackStartEvent -> audioWsEvent.onTrackStart(event)
					is TrackEndEvent -> audioWsEvent.onTrackEnd(event)
					is WebSocketClosedEvent -> audioWsEvent.onWsClosed(event)
					else -> Unit
				}
				audioNode.penalties.handleTrackEvent(event)
			}
			else -> log.error("Unknown event: {} on node: {}.", nodeConfig, text)
		}
		val clientEvent = when (event) {
			is ReadyEvent -> KReadyEvent.fromProtocol(audioNode, event)
			is TrackStartEvent -> KTrackStartEvent.fromProtocol(audioNode, event)
			is TrackEndEvent -> KTrackEndEvent.fromProtocol(audioNode, event)
			is TrackExceptionEvent -> KTrackExceptionEvent.fromProtocol(audioNode, event)
			is TrackStuckEvent -> KTrackStuckEvent.fromProtocol(audioNode, event)
			is WebSocketClosedEvent -> KWsClosedEvent.fromProtocol(audioNode, event)
			is PlayerUpdateEvent -> KPlayerUpdateEvent.fromProtocol(audioNode, event)
			is StatsEvent -> KStatsEvent.fromProtocol(audioNode, event)
		}
		eventPublisher.publishWithException(clientEvent)
	}

	/**
	 * Handles failure in the WebSocket connection.
	 *
	 * @param webSocket The WebSocket instance.
	 * @param t The throwable that caused the failure.
	 * @param response The response from the WebSocket server (if available).
	 */
	override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
		val reconnectSec = calcReconnectInterval()
		when (t) {
			is EOFException -> log.warn("Disconnected from: {}, trying to reconnect.", nodeConfig)
			is SocketTimeoutException -> log.warn("Disconnect from: {} (timeout), trying to reconnect.", nodeConfig)
			is ConnectException -> log.warn(
				"Failed to connect with: {}. Retrying connection attempt in {}s.",
				nodeConfig,
				reconnectSec,
			)
			is SocketException -> {
				if (open) {
					log.warn("Socket error on: {}, Retrying connection attempt in {}s", nodeConfig, reconnectSec)
				} else {
					log.warn("Socket error on: {}. Socket closed.", nodeConfig)
				}
			}
			else -> log.warn("Unknown error on WS connection with node: {}.", nodeConfig)
		}
		log.warn("Error cause: {}.", t.message)

		audioNode.available = false
		open = false

		audioClient.onNodeDisconnected(audioNode)
	}

	/**
	 * Handles the WebSocket connection closing.
	 *
	 * @param webSocket The WebSocket instance.
	 * @param code The closing code.
	 * @param reason The reason for the closure.
	 */
	override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
		audioNode.available = false
		audioClient.onNodeDisconnected(audioNode)
		val disconnectType = if (WsCode.NORMAL.isEqual(code)) {
			mayReconnect = false
			"normally"
		} else {
			"abnormally"
		}
		log.info(
			"Connection with node: {} was closed {} with reason: {} and code: {}.",
			nodeConfig,
			disconnectType,
			reason,
			code
		)
	}

	/**
	 * Handles the WebSocket connection being closed.
	 *
	 * @param webSocket The WebSocket instance.
	 * @param code The closing code.
	 * @param reason The reason for the closure.
	 */
	override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
		if (mayReconnect) {
			log.info("Attempt to re-connect with: {} in time: {}s.", nodeConfig, calcReconnectInterval())
			audioNode.available = false
			open = false
		}
		log.debug("Stop sending ping to node: {}.", nodeConfig)
	}

	/**
	 * Closes the WebSocket connection and stops reconnection attempts.
	 */
	override fun close() {
		mayReconnect = false
		open = false
		webSocket?.close(WsCode.NORMAL, "WS client shutdown.")
		webSocket?.cancel()
	}

	/**
	 * Calculates the reconnection interval based on the number of failed attempts.
	 *
	 * @param toMillis Whether to convert the interval to milliseconds.
	 * @return The calculated reconnection interval in seconds (or milliseconds if `toMillis` is true).
	 */
	private fun calcReconnectInterval(toMillis: Boolean = true): Int {
		var thresholdMillis = reconnectAttempts * 2000 - 200
		if (toMillis) {
			thresholdMillis /= 1000
		}
		return thresholdMillis
	}

	/**
	 * Closes the WebSocket connection with a specific error code and message.
	 *
	 * @param wsErrorCode The WebSocket error code.
	 * @param message The message explaining the closure.
	 */
	private fun WebSocket.close(wsErrorCode: WsCode, message: String) {
		close(wsErrorCode.code, message)
	}
}
