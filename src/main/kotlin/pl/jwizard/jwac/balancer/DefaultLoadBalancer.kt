/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.balancer

import pl.jwizard.jwac.balancer.penalty.VoiceRegionPenaltyProvider
import pl.jwizard.jwac.balancer.region.VoiceRegion
import pl.jwizard.jwac.node.AudioNode

/**
 * The default implementation of a load balancer that selects an available audio node based on penalties and a
 * specified region. This implementation uses penalty providers, like the [VoiceRegionPenaltyProvider], to evaluate
 * the "cost" of using each audio node and selects the one with the least penalty.
 *
 * @author Miłosz Gilga
 */
class DefaultLoadBalancer : LoadBalancer() {

	init {
		addPenaltyProvider(VoiceRegionPenaltyProvider())
	}

	/**
	 * Selects an audio node from the list based on the lowest penalties.
	 *
	 * The method evaluates all the available audio nodes and considers penalties based on the provided [region]. The
	 * node with the lowest combined penalty score is selected. If only one node is available, that node is selected
	 * directly.
	 *
	 * @param audioNodes The list of available audio nodes to choose from.
	 * @param region The voice region to consider when applying penalties (can be `null`).
	 * @param guildId The guild ID associated with the selection (can be `null`).
	 * @return The selected audio node with the lowest total penalty.
	 * @throws IllegalStateException If no available audio node can be found.
	 */
	override fun selectNode(audioNodes: List<AudioNode>, region: VoiceRegion?, guildId: Long?): AudioNode {
		if (audioNodes.size == 1) {
			val audioNode = audioNodes.first()
			if (!audioNode.available) {
				throw IllegalStateException("Audio node: $audioNode is unavailable.")
			}
			return audioNode
		}
		return audioNodes
			.filter { it.available }
			.minByOrNull { it.penalties.calculateTotal() + penaltyProviders.sumOf { p -> p.getPenalty(it, region) } }
			?: throw IllegalStateException("Unable to find any available audio node.")
	}
}
