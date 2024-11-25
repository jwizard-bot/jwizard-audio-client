/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.http

import dev.arbjerg.lavalink.protocol.v4.Error
import dev.arbjerg.lavalink.protocol.v4.json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import reactor.core.publisher.MonoSink
import java.io.IOException

/**
 * An asynchronous HTTP callback handler that processes responses and emits results to a MonoSink.
 *
 * This class handles HTTP responses asynchronously, parsing the response body and emitting the result. It processes
 * errors by converting them into exceptions and passing them to the MonoSink.
 *
 * @param T The type of the parsed response.
 * @property emitter The [MonoSink] instance used to emit the result or error.
 * @property onParse A function that takes the raw response string and returns a parsed result of type [T].
 * @author Miłosz Gilga
 */
internal class AsyncResponseCallback<T : Any>(
	private val emitter: MonoSink<T>,
	private val onParse: (String) -> T,
) : Callback {

	/**
	 * Called when the HTTP request fails.
	 *
	 * This method is invoked when the HTTP request cannot be completed due to an [IOException] (ex. network issues,
	 * timeouts).
	 *
	 * @param call The HTTP call that was executed.
	 * @param e The exception encountered during the request.
	 */
	override fun onFailure(call: Call, e: IOException) {
		emitter.error(e)
	}

	/**
	 * Called when the HTTP request succeeds with a response.
	 *
	 * This method is invoked when the HTTP request is successful. It processes the response, parses the body if
	 * applicable, and emits the result to the MonoSink. If the response contains an error (status code > 299), it
	 * converts the response into a [RestException]. For a `204 No Content` response, it signals success with no value.
	 *
	 * @param call The HTTP call that was executed.
	 * @param response The HTTP response received.
	 */
	override fun onResponse(call: Call, response: Response) {
		response.body?.use {
			val responseStr = it.string()
			if (response.code > 299) {
				val error = json.decodeFromString<Error>(responseStr)
				emitter.error(RestException(error))
				return
			}
			if (response.code == 204) {
				emitter.success()
				return
			}
			val parsed = onParse(responseStr)
			emitter.success(parsed)
		}
	}
}
