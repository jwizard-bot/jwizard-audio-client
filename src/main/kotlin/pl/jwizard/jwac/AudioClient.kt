/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac

import pl.jwizard.jwac.balancer.DefaultLoadBalancer
import pl.jwizard.jwac.balancer.LoadBalancer
import pl.jwizard.jwac.balancer.region.VoiceRegion
import pl.jwizard.jwac.event.ClientEvent
import pl.jwizard.jwac.event.player.*
import pl.jwizard.jwac.link.Link
import pl.jwizard.jwac.link.LinkState
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.node.NodeConfig
import pl.jwizard.jwac.node.NodePool
import pl.jwizard.jwac.util.isEmpty
import pl.jwizard.jwac.util.logger
import pl.jwizard.jwac.ws.ReconnectAudioNodeTask
import reactor.core.Disposable
import java.io.Closeable
import java.util.*
import java.util.concurrent.*

/**
 * Represents the audio client responsible for managing connections, nodes, and guild audio playback state.
 *
 * This class allows managing multiple audio nodes, balancing the load across them, handling events, and managing the
 * connection state between the bot and the distributed audio server.
 *
 * @property secretToken The secret token used for bot authentication.
 * @property instanceName The name of the instance for identification purposes.
 * @property audioNodeListener Listener for events emitted by audio nodes.
 * @author Miłosz Gilga
 */
class AudioClient(
	private val secretToken: String,
	private val instanceName: String,
	private val audioNodeListener: AudioNodeListener,
) : Closeable {

	companion object {
		private val log = logger<AudioClient>()
	}

	/**
	 * Extracted bot ID from the token.
	 */
	private val botId = getUserIdFromToken(secretToken)

	/**
	 * List of all available audio nodes.
	 */
	private val internalNodes = CopyOnWriteArrayList<AudioNode>()

	/**
	 * Cached links for guilds.
	 */
	private val internalLinks = ConcurrentHashMap<Long, Link>()

	/**
	 * Maps guilds to node pools.
	 */
	private val guildCurrentNodePool = ConcurrentHashMap<Long, NodePool>()

	/**
	 * List of event handlers to dispose of.
	 */
	private val disposableEvents = mutableListOf<Disposable>()

	/**
	 * Publisher for client events.
	 */
	private val publisher = ReactiveEventPublisher<ClientEvent>()

	/**
	 * Load balancer for node selection.
	 */
	private val loadBalancer: LoadBalancer = DefaultLoadBalancer()

	/**
	 * Trigger for voice gateway updates.
	 */
	internal var voiceGatewayUpdateTrigger: CompletableFuture<Void>? = null

	/**
	 * Flag indicating whether the client is open.
	 */
	private var isOpen = true

	/**
	 * Single thread scheduled executor for audio-reconnect Websocket client.
	 */
	private val reconnectService = Executors.newSingleThreadScheduledExecutor {
		Thread(it, "audio-client-reconnect-thread").apply { isDaemon = true }
	}

	init {
		val task = ReconnectAudioNodeTask(botId, internalNodes)
		reconnectService.scheduleWithFixedDelay(task, 0, 500, TimeUnit.MILLISECONDS)
	}

	/**
	 * Initializes audio event listeners that subscribe to various audio-related events.
	 */
	fun initAudioEventListeners() {
		disposableEvents += publisher.ofType<KTrackStartEvent>().subscribe(audioNodeListener::onTrackStart)
		disposableEvents += publisher.ofType<KTrackEndEvent>().subscribe(audioNodeListener::onTrackEnd)
		disposableEvents += publisher.ofType<KTrackStuckEvent>().subscribe(audioNodeListener::onTrackStuck)
		disposableEvents += publisher.ofType<KTrackExceptionEvent>().subscribe(audioNodeListener::onTrackException)
		disposableEvents += publisher.ofType<KWsClosedEvent>().subscribe(audioNodeListener::onCloseWsConnection)
		log.info("Init: {} audio client nodes: {}.", internalNodes.size, internalNodes)
	}

	/**
	 * Adds a new audio node to the client.
	 *
	 * @param nodeConfig The configuration for the audio node to add.
	 * @throws IllegalStateException if a node with the same name already exists.
	 */
	fun addNode(nodeConfig: NodeConfig) {
		if (internalNodes.any { it.config.name == nodeConfig.name }) {
			throw IllegalStateException("Node with name \"${nodeConfig.name}\" already exists.")
		}
		val node = AudioNode(nodeConfig, this, instanceName)
		node.connect(botId)
		disposableEvents += node.publisher.ofType<ClientEvent>().subscribe(publisher::publishWithException)
		internalNodes.add(node)
	}

	/**
	 * Retrieves a cached link for the specified guild ID.
	 *
	 * @param guildId The ID of the guild.
	 * @return The cached link if available, otherwise null.
	 */
	fun getLinkIfCached(guildId: Long) = internalLinks[guildId]

	/**
	 * Retrieves or creates a link for the specified guild, using the provided voice region.
	 *
	 * @param guildId The ID of the guild.
	 * @param region The optional voice region to use for node selection.
	 * @return The created or cached link for the guild.
	 * @throws IllegalStateException if the guild's node pool cannot be found.
	 */
	fun getOrCreateLink(guildId: Long, region: VoiceRegion? = null) = internalLinks.getOrPut(guildId) {
		val pool = guildCurrentNodePool[guildId]
			?: throw IllegalStateException("Could not find guild: $guildId with linked node pool.")
		val nodesFromSelectedPool = internalNodes.filter { it.inNodePool(pool) }
		Link(guildId, loadBalancer.selectNode(nodesFromSelectedPool, region, guildId))
	}

	/**
	 * Retrieves all available audio nodes.
	 *
	 * @param onlyAvailable If true, only available nodes are returned. Otherwise, all nodes are returned.
	 * @return The list of nodes.
	 */
	fun getNodes(onlyAvailable: Boolean = true) = if (onlyAvailable) {
		internalNodes.filter(AudioNode::available)
	} else {
		internalNodes
	}

	/**
	 * Handles when an audio node gets disconnected. Updates the state of the links if necessary and reassigns nodes as
	 * needed.
	 *
	 * @param audioNode The node that was disconnected.
	 */
	internal fun onNodeDisconnected(audioNode: AudioNode) {
		if (!isOpen) {
			return
		}
		val nodesFromPool = internalNodes.filter { it.inNodePool(audioNode.pool) }
		if (nodesFromPool.size == 1 && nodesFromPool.first() == audioNode) {
			internalLinks.forEach { (_, link) -> link.updateState(LinkState.DISCONNECTED) }
			return
		}
		if (nodesFromPool.all { !it.available }) {
			internalLinks
				.filter { (_, link) -> link.selectedNode == audioNode }
				.forEach { (_, link) -> link.updateState(LinkState.DISCONNECTED) }
			return
		}
		internalLinks.forEach { (_, link) ->
			if (link.selectedNode == audioNode) {
				link.transferNode(loadBalancer.selectNode(nodesFromPool, link.cachedPlayer?.voiceRegion, link.guildId))
			}
		}
	}

	/**
	 * Removes a link that has been destroyed for the specified guild.
	 *
	 * @param guildId The ID of the guild.
	 */
	internal fun removeDestroyedLink(guildId: Long) = internalLinks.remove(guildId)

	/**
	 * Updates the node pool for a specific guild.
	 *
	 * @param guildId The ID of the guild.
	 * @param pool The new node pool for the guild.
	 */
	internal fun updateGuildNodePool(guildId: Long, pool: NodePool) {
		guildCurrentNodePool[guildId] = pool
	}

	/**
	 * Transfers the audio node to a new pool.
	 *
	 * @param guildId The ID of the guild.
	 * @param newPool The new node pool for the guild.
	 * @param afterSetNode A callback that is invoked after the node has been transferred.
	 */
	internal fun transferNodeFromNewPool(guildId: Long, newPool: NodePool, afterSetNode: (AudioNode) -> Unit) {
		val link = getLinkIfCached(guildId) ?: throw IllegalStateException("Link for guild: $guildId does not exist.")
		if (link.selectedNode.inNodePool(newPool)) {
			log.info("Node is already in pool: {}. Skipping transfer.", newPool)
			afterSetNode(link.selectedNode)
			return
		}
		val nodeFromPool = internalNodes
			.find { it.pool == newPool }
			?: throw IllegalStateException("Could not find any node in pool: $newPool.")

		link.transferToPool(nodeFromPool, newPool, afterSetNode)
	}

	/**
	 * Transfers any orphaned players to an available node.
	 *
	 * @param audioNode The node to transfer orphaned players to.
	 */
	internal fun transferOrphansTo(audioNode: AudioNode) {
		if (!audioNode.available) {
			return
		}
		val unavailableNodes = internalNodes.filter { !it.available }
		val orphans = unavailableNodes.flatMap { it.players.values }
		orphans.mapNotNull { internalLinks[it.guildId] }
			.filter { !it.cachedPlayer?.voiceState.isEmpty() && audioNode.inNodePool(it.selectedNode.pool) }
			.forEach { it.transferNode(audioNode) }
	}

	/**
	 * Extracts the bot's user ID from the token.
	 *
	 * @param token The secret token.
	 * @return The extracted bot user ID.
	 * @throws IllegalArgumentException if the token is invalid.
	 */
	private fun getUserIdFromToken(token: String) = try {
		val parts = token.split(".")
		if (parts.size != 3) {
			throw IllegalArgumentException("Token is not a valid bot token.")
		}
		String(Base64.getDecoder().decode(parts[0])).toLong()
	} catch (e: Exception) {
		throw IllegalArgumentException("Decoding failed: ${e.message}", e)
	}

	/**
	 * Closes the audio client, disposing of all event listeners and audio nodes.
	 */
	override fun close() {
		disposableEvents.forEach { it.dispose() }
		log.info("Disposed: {} event handlers.", disposableEvents.size)

		internalNodes.forEach { it.close() }
		log.info("Disposed: {} audio nodes.", internalNodes)

		reconnectService.shutdownNow()
		publisher.dispose()
		isOpen = false
		log.info("Closing audio client...")
	}
}
