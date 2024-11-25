/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.node

import dev.arbjerg.lavalink.protocol.v4.PlayerUpdate
import dev.arbjerg.lavalink.protocol.v4.Stats
import okhttp3.OkHttpClient
import pl.jwizard.jwac.AudioClient
import pl.jwizard.jwac.ReactiveEventPublisher
import pl.jwizard.jwac.balancer.penalty.Penalties
import pl.jwizard.jwac.event.ClientEvent
import pl.jwizard.jwac.http.AudioNodeRestClient
import pl.jwizard.jwac.http.RestException
import pl.jwizard.jwac.player.AudioPlayer
import pl.jwizard.jwac.player.AudioPlayerUpdateBuilder
import pl.jwizard.jwac.ws.AudioWsClient
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.Closeable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Represents an audio node in a distributed audio system.
 *
 * This class handles the communication with an audio node, including managing player instances, sending commands to
 * the node, and keeping track of node availability and penalties. It connects to the node using both WebSocket (for
 * real-time updates) and REST APIs (for typical CRUD operations on players).
 *
 * @property nodeConfig Configuration for this audio node.
 * @property audioClient The main audio client that interacts with this node.
 * @property instanceName The name of this instance of the audio node.
 * @constructor Initializes the audio node with the given configuration and client.
 */
class AudioNode(
	private val nodeConfig: NodeConfig,
	private val audioClient: AudioClient,
	private val instanceName: String,
) : Closeable {

	val name = nodeConfig.name
	val pool = nodeConfig.pool
	val config = nodeConfig

	/**
	 * Publisher for events related to this node.
	 */
	internal val publisher = ReactiveEventPublisher<ClientEvent>()

	/**
	 * A map of guild IDs to their corresponding audio players.
	 */
	internal val players = ConcurrentHashMap<Long, AudioPlayer>()

	/**
	 * Manages penalties for this node based on its performance and health.
	 */
	internal val penalties = Penalties(this)

	/**
	 * Indicates whether this node is currently available.
	 */
	internal var available = false

	/**
	 * The session ID for the WebSocket connection with this node.
	 */
	internal var sessionId: String? = null

	/**
	 * Stats related to this node, such as its performance and health.
	 */
	var stats: Stats? = null
		internal set

	/**
	 * HTTP client used for REST API/WS requests to this node.
	 */
	private val httpClient = OkHttpClient.Builder()
		.callTimeout(nodeConfig.httpTimeout, TimeUnit.MILLISECONDS)
		.build()

	/**
	 * REST client used to interact with this node's REST API.
	 */
	private val rest = AudioNodeRestClient(nodeConfig, httpClient)

	/**
	 * WebSocket client used to interact with this node's WebSocket interface.
	 */
	private val ws = AudioWsClient(nodeConfig, httpClient, this, publisher, audioClient)

	/**
	 * Connects to the node with the given bot ID.
	 *
	 * @param botId The bot ID that will be used to establish the WebSocket connection with the node.
	 */
	internal fun connect(botId: Long) {
		ws.connect(instanceName, botId, sessionId)
	}

	/**
	 * Reconnects to the node using the given bot ID.
	 *
	 * @param botId The bot ID that will be used to reconnect to the node.
	 */
	internal fun reconnectNode(botId: Long) {
		ws.reconnect(instanceName, botId, sessionId)
	}

	/**
	 * Retrieves a cached player for the given guild ID.
	 *
	 * @param guildId The guild ID for which the player is being fetched.
	 * @return The cached player for the guild, or null if no player exists.
	 */
	fun getCachedPlayer(guildId: Long) = players[guildId]

	/**
	 * Retrieves or creates a player for the given guild ID.
	 *
	 * @param guildId The guild ID for which the player is being fetched or created.
	 * @return A Mono representing the player for the given guild.
	 */
	fun getPlayer(guildId: Long) = withNodeAvailableCheck {
		if (players.containsKey(guildId)) {
			players[guildId].toMono()
		} else {
			rest.getPlayer(sessionId, guildId).map { AudioPlayer(this, it) }
				.onErrorResume {
					if (it is RestException && it.code == 404) {
						createOrUpdatePlayer(guildId)
					} else {
						it.toMono()
					}
				}
				.doOnSuccess { players[it.guildId] = it }
		}
	}

	/**
	 * Creates or updates a player for the given guild ID.
	 *
	 * @param guildId The guild ID for which the player is being created or updated.
	 * @return An instance of AudioPlayerUpdateBuilder for building player updates.
	 */
	fun createOrUpdatePlayer(guildId: Long) = AudioPlayerUpdateBuilder(guildId, this)

	/**
	 * Updates a player for the given guild ID with the provided player update.
	 *
	 * @param guildId The guild ID for which the player update is being applied.
	 * @param playerUpdate The update to apply to the player.
	 * @param noReplace If true, prevents replacing the player if it already exists.
	 * @return A Mono representing the updated player.
	 */
	fun updatePlayer(guildId: Long, playerUpdate: PlayerUpdate, noReplace: Boolean) = withNodeAvailableCheck {
		rest.updatePlayer(sessionId, playerUpdate, guildId, noReplace)
			.map { AudioPlayer(this, it) }
			.doOnSuccess { players[guildId] = it }
	}

	/**
	 * Destroys the player for the given guild ID.
	 *
	 * @param guildId The guild ID for which the player is being destroyed.
	 * @return A Mono representing the result of the destroy operation.
	 */
	fun destroyPlayer(guildId: Long) = withNodeAvailableCheck {
		rest.destroyPlayer(sessionId, guildId)
			.doOnSuccess { removeCachedPlayer(guildId) }
	}

	/**
	 * Destroys the player and link for the given guild ID.
	 *
	 * @param guildId The guild ID for which the player and link are being destroyed.
	 * @return A Mono representing the result of the destroy operation.
	 */
	fun destroyPlayerAndLink(guildId: Long) = withNodeAvailableCheck {
		rest.destroyPlayer(sessionId, guildId).doOnSuccess {
			removeCachedPlayer(guildId)
			audioClient.removeDestroyedLink(guildId)
		}
	}

	/**
	 * Loads an item (ex. track or playlist) from the node using the provided identifier.
	 *
	 * @param decoded The identifier of the item to load.
	 * @return A Mono representing the loaded item.
	 */
	fun loadItem(decoded: String) = withNodeAvailableCheck { rest.loadItem(decoded) }

	/**
	 * Retrieves information about the node.
	 *
	 * @return A Mono representing the node information.
	 */
	fun getNodeInfo() = withNodeAvailableCheck(rest::getNodeInfo)

	/**
	 * Removes the cached player for the given guild ID.
	 *
	 * @param guildId The guild ID for which the player is being removed.
	 */
	internal fun removeCachedPlayer(guildId: Long) {
		players.remove(guildId)
	}

	/**
	 * Transfers orphaned players to this node.
	 */
	internal fun transferOrphansToSelf() {
		audioClient.transferOrphansTo(this)
	}

	/**
	 * Checks if the node is in the given node pool.
	 *
	 * @param pool The node pool to check if this node belongs to.
	 * @return True if the node is in the specified pool, false otherwise.
	 */
	internal fun inNodePool(pool: NodePool) = this.pool.poolName == pool.poolName

	/**
	 * Helper function that checks if the node is available before performing an action.
	 *
	 * @param onPerformRequest The function that performs the request if the node is available.
	 * @return A Mono representing the result of the action.
	 */
	private fun <T : Any> withNodeAvailableCheck(onPerformRequest: () -> Mono<T>): Mono<T> {
		if (!available) {
			return Mono.error(IllegalStateException("Audio node is not available"))
		}
		return onPerformRequest()
	}

	/**
	 * Closes the audio node, terminating any open connections and cleaning up resources.
	 */
	override fun close() {
		available = false
		ws.close()
		httpClient.dispatcher.executorService.shutdown()
		httpClient.connectionPool.evictAll()
		httpClient.cache?.close()
		publisher.dispose()
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) {
			return true
		}
		if (javaClass != other?.javaClass) {
			return false
		}
		other as AudioNode
		return available == other.available && nodeConfig == other.nodeConfig && sessionId == other.sessionId
	}

	override fun hashCode(): Int {
		var result = nodeConfig.hashCode()
		result = 31 * result + sessionId.hashCode()
		result = 31 * result + available.hashCode()
		return result
	}

	override fun toString() = "${nodeConfig.name} (pool: ${nodeConfig.pool})"
}
