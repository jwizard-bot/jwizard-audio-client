/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.balancer

import pl.jwizard.jwac.balancer.penalty.PenaltyProvider
import pl.jwizard.jwac.balancer.region.VoiceRegion
import pl.jwizard.jwac.node.AudioNode

/**
 * Abstract class representing a load balancer for selecting the best audio node from a list of available audio nodes.
 * This class supports the concept of applying penalties to nodes using penalty providers to influence the selection
 * process.
 *
 * @author Miłosz Gilga
 */
abstract class LoadBalancer {

	/**
	 * A list of penalty providers that can be used to calculate penalties for the audio nodes. Penalties influence the
	 * selection of the best audio node.
	 */
	protected val penaltyProviders = mutableListOf<PenaltyProvider>()

	/**
	 * Adds a penalty provider to the load balancer.
	 *
	 * @param penaltyProvider The penalty provider to be added to the list.
	 */
	fun addPenaltyProvider(penaltyProvider: PenaltyProvider) {
		penaltyProviders.add(penaltyProvider)
	}

	/**
	 * Removes a penalty provider from the load balancer.
	 *
	 * @param penaltyProvider The penalty provider to be removed from the list.
	 */
	fun removePenaltyProvider(penaltyProvider: PenaltyProvider) {
		penaltyProviders.remove(penaltyProvider)
	}

	/**
	 * Selects an audio node from the given list based on the provided region and guild ID. The selection process
	 * considers penalties from all registered penalty providers.
	 *
	 * @param audioNodes The list of available audio nodes to choose from.
	 * @param region The voice region to consider during the selection (can be `null`).
	 * @param guildId The ID of the guild associated with the audio node selection (can be `null`).
	 * @return The selected audio node that best fits the criteria, considering penalties.
	 * @throws IllegalStateException If no suitable audio node can be found.
	 */
	abstract fun selectNode(audioNodes: List<AudioNode>, region: VoiceRegion?, guildId: Long?): AudioNode
}
