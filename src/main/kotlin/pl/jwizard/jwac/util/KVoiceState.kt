/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.util

import dev.arbjerg.lavalink.protocol.v4.VoiceState

/**
 * Extension function to check if a [VoiceState] object is empty.
 *
 * This function checks if the [VoiceState] is considered "empty" based on the following conditions:
 * - If the object is `null`, it returns `true`.
 * - If any of the fields (`token`, `endpoint`, or `sessionId`) are blank, it returns `true`.
 * Otherwise, it returns `false`.
 *
 * @return `true` if the [VoiceState] is empty (either null or has blank fields), `false` otherwise.
 * @author Miłosz Gilga
 */
internal fun VoiceState?.isEmpty() = if (this != null) {
	this.token.isBlank() || this.endpoint.isBlank() || this.sessionId.isBlank()
} else {
	true
}

