/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.event

import dev.arbjerg.lavalink.protocol.v4.Cpu
import dev.arbjerg.lavalink.protocol.v4.FrameStats
import dev.arbjerg.lavalink.protocol.v4.Memory
import dev.arbjerg.lavalink.protocol.v4.Message.StatsEvent
import pl.jwizard.jwac.node.AudioNode

/**
 * Event triggered when the statistics of the audio node are updated.
 *
 * This event is emitted to provide updated statistics about the audio node, such as the current number of players, the
 * number of players actively playing, the system uptime, memory usage, and CPU usage. This information can be used for
 * monitoring the performance of the audio node or for diagnosing potential issues.
 *
 * @property audioNode The [AudioNode] whose statistics have been updated.
 * @property frameStats The frame statistics containing data on frame rates, dropped frames, etc.
 * @property players The total number of players currently connected to the audio node.
 * @property playingPlayers The number of players actively playing audio.
 * @property uptime The uptime of the audio node in milliseconds.
 * @property memory The memory usage statistics of the audio node.
 * @property cpu The CPU usage statistics of the audio node.
 * @author Miłosz Gilga
 */
data class KStatsEvent(
	override val audioNode: AudioNode,
	val frameStats: FrameStats?,
	val players: Int,
	val playingPlayers: Int,
	val uptime: Long,
	val memory: Memory,
	val cpu: Cpu,
) : ClientEvent() {

	companion object {
		/**
		 * Creates a [KStatsEvent] instance from a protocol-specific [StatsEvent].
		 *
		 * @param node The [AudioNode] responsible for the statistics.
		 * @param event The [StatsEvent] received from the Lavalink protocol.
		 * @return A new instance of [KStatsEvent] representing the updated statistics.
		 */
		fun fromProtocol(node: AudioNode, event: StatsEvent) =
			KStatsEvent(node, event.frameStats, event.players, event.playingPlayers, event.uptime, event.memory, event.cpu)
	}
}
