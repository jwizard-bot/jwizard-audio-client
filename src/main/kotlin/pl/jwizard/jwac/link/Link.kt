/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.link

import dev.arbjerg.lavalink.protocol.v4.VoiceState
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.node.NodePool
import pl.jwizard.jwac.util.logger
import java.time.Duration

/**
 * Represents a link between a guild and an audio node.
 *
 * The [Link] class manages the connection between a guild and an audio node, including the management of the player's
 * state, voice state, and the node the player is connected to. It can also handle transferring the player to a
 * different node or node pool, updating player states, and destroying the player and link when necessary.
 *
 * @property guildId The unique identifier for the guild (server) that the link is associated with.
 * @param audioNode The initial audio node that the link will connect to.
 * @author Miłosz Gilga
 */
class Link(
	val guildId: Long,
	audioNode: AudioNode,
) {

	companion object {
		private val log = logger<Link>()
	}

	/**
	 * The currently selected audio node for this link.
	 */
	internal var selectedNode = audioNode
		private set

	/**
	 * The current state of the link.
	 * @see LinkState
	 */
	private var state = LinkState.DISCONNECTED

	/**
	 * Retrieves the cached player for the guild associated with this link.
	 */
	val cachedPlayer
		get() = selectedNode.getCachedPlayer(guildId)

	/**
	 * Retrieves the player via GET statement for the guild associated with this link, creating it if necessary.
	 */
	val player
		get() = selectedNode.getPlayer(guildId)

	/**
	 * Destroys the player and link associated with this guild on the selected audio node.
	 *
	 * @return A result indicating the destruction process has been initiated.
	 */
	fun destroy() = selectedNode.destroyPlayerAndLink(guildId)

	/**
	 * Creates or updates the player for the guild associated with this link.
	 *
	 * @return A result indicating the creation or update process has been initiated.
	 */
	fun createOrUpdatePlayer() = selectedNode.createOrUpdatePlayer(guildId)

	/**
	 * Loads an item (such as a track or playlist) using the provided identifier.
	 *
	 * @param identifier The identifier for the item to load (ex. track or playlist URL).
	 * @return A result indicating the loading process has been initiated.
	 */
	fun loadItem(identifier: String) = selectedNode.loadItem(identifier)

	/**
	 * Updates the state of the link.
	 *
	 * @param state The new state to set for the link.
	 * @see LinkState
	 */
	internal fun updateState(state: LinkState) {
		this.state = state
	}

	/**
	 * Transfers the link to a new audio node. If a player exists, it will be moved to the new node with a delay to
	 * ensure smooth transition.
	 *
	 * @param newNode The new audio node to transfer the link to.
	 */
	internal fun transferNode(newNode: AudioNode) {
		state = LinkState.CONNECTING
		val player = selectedNode.getCachedPlayer(guildId)
		if (player != null) {
			newNode.createOrUpdatePlayer(guildId)
				.applyBuilder(player.stateToBuilder())
				.delaySubscription(Duration.ofMillis(1000))
				.subscribe(
					{ selectedNode.removeCachedPlayer(guildId) },
					{
						state = LinkState.DISCONNECTED
						log.error("(link: {}) Failed to transfer player to new node: {}. Cause: {}.", this, newNode, it.message)
					}
				)
		}
		selectedNode = newNode
	}

	/**
	 * Transfers the link to a new audio node and node pool. The player's state is transferred along with the link to
	 * the new node. After the transfer, the provided callback is invoked.
	 *
	 * @param newNode The new audio node to transfer the link to.
	 * @param newPool The new node pool the node belongs to.
	 * @param afterSetNode A callback to invoke after the node has been set.
	 */
	internal fun transferToPool(newNode: AudioNode, newPool: NodePool, afterSetNode: (AudioNode) -> Unit) {
		val player = selectedNode.getCachedPlayer(guildId)
		val playerBuilder = newNode.createOrUpdatePlayer(guildId)
		player?.let {
			playerBuilder.setVolume(it.volume)
			playerBuilder.setVoiceState(it.voiceState)
			playerBuilder.setFilters(it.filters)
		}
		selectedNode.destroyPlayer(guildId).subscribe()
		playerBuilder
			.delaySubscription(Duration.ofMillis(1000))
			.subscribe(
				{ afterSetNode(newNode) },
				{
					state = LinkState.DISCONNECTED
					log.error("(link: {}) Failed to transfer player to new node pool: {}.", this, newPool)
				},
			)
		selectedNode = newNode
	}

	/**
	 * Updates the voice state of the player on the current node.
	 *
	 * @param newVoiceState The new voice state to set for the player.
	 */
	internal fun updateNodeVoiceState(newVoiceState: VoiceState) {
		if (!selectedNode.available) {
			return
		}
		state = LinkState.CONNECTING
		selectedNode.createOrUpdatePlayer(guildId)
			.setVoiceState(newVoiceState)
			.subscribe(
				{ log.debug("(link: {}) Updated voice state: {}.", this, newVoiceState) },
				{
					state = LinkState.DISCONNECTED
					log.error("(link: {}) Failed update voice state to: {}. Cause: {}.", this, newVoiceState, it.message)
				}
			)
	}

	override fun equals(other: Any?): Boolean {
		if (this === other) {
			return true
		}
		if (javaClass != other?.javaClass) {
			return false
		}
		return guildId == (other as Link).guildId
	}

	override fun hashCode() = guildId.hashCode()

	override fun toString() = "$selectedNode (guildId: $guildId)"
}
