/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac

import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import pl.jwizard.jwac.gateway.GatewayVoiceStateInterceptor
import pl.jwizard.jwac.node.AudioNode
import pl.jwizard.jwac.node.NodePool
import pl.jwizard.jwac.util.logger
import java.util.concurrent.CompletableFuture

/**
 * This class is responsible for managing audio sessions for a Discord guild. It interacts with the voice gateway and
 * manages the transitions between audio nodes.
 *
 * @property audioClient The audio client instance responsible for managing the audio connections.
 * @property gatewayVoiceStateInterceptor The interceptor that handles updates related to voice state changes in Discord.
 * @author Miłosz Gilga
 */
class AudioSessionController(
	private val audioClient: AudioClient,
	private val gatewayVoiceStateInterceptor: GatewayVoiceStateInterceptor,
) {

	companion object {
		private val log = logger<AudioSessionController>()
	}

	/**
	 * Loads and transfers the audio session to a new audio node pool. This method ensures that the guild is connected to
	 * the appropriate audio node and manages the voice gateway connection.
	 *
	 * @param guild The guild whose audio session needs to be managed.
	 * @param pool The node pool to which the audio session will be transferred.
	 * @param authorMember The member who initiated the audio session transfer.
	 * @param selfMember The bot’s member in the guild.
	 * @param onTransferNode A callback function that is called when the node transfer is completed.
	 * @return `true`, if any active node in selected [pool] exist, otherwise `false`.
	 */
	fun loadAndTransferToNode(
		guild: Guild,
		pool: NodePool,
		authorMember: Member,
		selfMember: Member,
		onTransferNode: (AudioNode) -> Unit,
	): Boolean {
		val guildId = guild.idLong
		audioClient.updateGuildNodePool(guildId, pool)
		log.debug("Switch to: {} pool in guild: {}.", guildId, pool)

		val availablePoolNodes = audioClient
			.getNodes(onlyAvailable = true)
			.filter { it.pool == pool }

		if (availablePoolNodes.isEmpty()) {
			return false
		}
		audioClient.voiceGatewayUpdateTrigger = CompletableFuture()

		if (gatewayVoiceStateInterceptor.inAudioChannel(selfMember) == false) {
			authorMember.voiceState?.channel?.let {
				gatewayVoiceStateInterceptor.makeConnect(guild, it)
				log.debug("Connect with audio channel: {} in guild: {}.", guildId, it)
			}
		} else {
			log.debug("Already connected in audio channel. Skipping updating voice gateway server.")
			audioClient.voiceGatewayUpdateTrigger?.complete(null)
		}
		audioClient.voiceGatewayUpdateTrigger?.thenRun {
			audioClient.transferNodeFromNewPool(guildId, pool, onTransferNode)
			audioClient.voiceGatewayUpdateTrigger = null
		}
		return true
	}

	/**
	 * Disconnects the bot from the audio channel in the specified guild.
	 *
	 * @param guild The guild where the bot should disconnect from the audio channel.
	 */
	fun disconnectWithAudioChannel(guild: Guild) {
		gatewayVoiceStateInterceptor.disconnect(guild)
		log.debug("Disconnect with audio channel in guild: {}.", guild.idLong)
	}
}
