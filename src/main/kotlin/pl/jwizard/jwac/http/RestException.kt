/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.http

import dev.arbjerg.lavalink.protocol.v4.Error

/**
 * Exception that represents errors from a REST API request to the distributed audio server.
 *
 * @property error The error details returned from the distributed audio server.
 * @author Miłosz Gilga
 */
internal class RestException(private val error: Error) : Exception(error.message) {
	val code = error.status
}
