/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.ws

import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.util.logger

/**
 * A task responsible for reconnecting audio nodes.
 *
 * This class is used for periodically attempting to reconnect audio nodes that may have become disconnected. It is
 * executed by a scheduled executor and attempts to reconnect each audio node in the list. If an exception occurs
 * during the reconnection, it is logged for further investigation.
 *
 * @property botId The bot's unique identifier used during reconnection attempts.
 * @property audioNodes A list of audio nodes that need to be periodically checked and reconnected.
 * @author Miłosz Gilga
 */
internal class ReconnectAudioNodeTask(
	private val botId: Long,
	private val audioNodes: List<AudioNode>,
) : Runnable {

	companion object {
		private val log = logger<ReconnectAudioNodeTask>()
	}

	/**
	 * Attempts to reconnect each audio node in the list.
	 */
	override fun run() {
		try {
			audioNodes.forEach { it.reconnectNode(botId) }
		} catch (ex: Exception) {
			log.error("Unable to reconnect with node. Cause: {}.", ex.message)
		}
	}
}
