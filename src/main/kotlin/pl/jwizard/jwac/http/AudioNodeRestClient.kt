/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.http

import dev.arbjerg.lavalink.protocol.v4.*
import kotlinx.serialization.encodeToString
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pl.jwizard.jwac.node.NodeConfig
import reactor.core.publisher.Mono
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * A client for interacting with the audio node REST API.
 *
 * This class provides methods for sending requests to an audio node, including operations for managing players,
 * loading tracks, and retrieving node information. Each method returns a [Mono] representing the asynchronous
 * operation, following the reactive programming paradigm.
 *
 * @property nodeConfig Configuration for the audio node (ex. URL and authorization).
 * @property httpClient The [OkHttpClient] instance used for sending HTTP requests.
 * @author Miłosz Gilga
 */
internal class AudioNodeRestClient(
	private val nodeConfig: NodeConfig,
	private val httpClient: OkHttpClient,
) {

	/**
	 * Retrieves the player associated with a given session and guild.
	 *
	 * @param sessionId The session ID.
	 * @param guildId The guild ID.
	 * @return A [Mono] containing the [Player] object.
	 */
	internal fun getPlayer(sessionId: String?, guildId: Long): Mono<Player> =
		performRequest("/v4/sessions/$sessionId/players/$guildId").toMono()

	/**
	 * Updates the player state for a given session and guild.
	 *
	 * @param sessionId The session ID.
	 * @param player The [PlayerUpdate] object containing updated player data.
	 * @param guildId The guild ID.
	 * @param noReplace If true, the player state will not be replaced entirely.
	 * @return A [Mono] containing the updated [Player] object.
	 */
	internal fun updatePlayer(sessionId: String?, player: PlayerUpdate, guildId: Long, noReplace: Boolean): Mono<Player> =
		performRequest(
			url = "/v4/sessions/$sessionId/players/$guildId?noReplace=$noReplace",
			httpMethod = HttpMethod.PATCH,
			body = json.encodeToString(player).toRequestBody("application/json".toMediaType()),
		).toMono()

	/**
	 * Destroys the player for the specified session and guild.
	 *
	 * @param sessionId The session ID.
	 * @param guildId The guild ID.
	 * @return A [Mono] representing the completion of the operation.
	 */
	internal fun destroyPlayer(sessionId: String?, guildId: Long): Mono<Unit> = performRequest(
		url = "/v4/sessions/$sessionId/players/$guildId",
		httpMethod = HttpMethod.DELETE,
	).toMono()

	/**
	 * Loads track information based on a given identifier.
	 *
	 * @param decoded The track identifier, URL-encoded.
	 * @return A [Mono] containing the [LoadResult], which includes track data.
	 */
	internal fun loadItem(decoded: String): Mono<LoadResult> =
		performRequest("/v4/loadtracks?identifier=${URLEncoder.encode(decoded, StandardCharsets.UTF_8)}").toMono()

	/**
	 * Retrieves information about the audio node.
	 *
	 * @return A [Mono] containing the [Info] object representing the node's information.
	 */
	internal fun getNodeInfo(): Mono<Info> = performRequest("/v4/info").toMono()

	/**
	 * Performs an HTTP request to the audio node. This method constructs an HTTP request based on the provided URL,
	 * HTTP method, and optional body.
	 *
	 * @param url The relative URL to send the request to.
	 * @param httpMethod The HTTP method (GET, POST, PATCH, DELETE).
	 * @param body The request body, if applicable (optional).
	 * @return The [Call] object representing the HTTP request.
	 */
	private fun performRequest(url: String, httpMethod: HttpMethod = HttpMethod.GET, body: RequestBody? = null): Call {
		val request = Request.Builder()
			.url(URL(nodeConfig.httpUrl + url))
			.addHeader("Authorization", nodeConfig.password)
			.method(httpMethod.name, body)
			.build()
		return httpClient.newCall(request)
	}

	/**
	 * Converts an HTTP [Call] into a [Mono] containing the parsed response.
	 *
	 * This method enqueues the request and asynchronously processes the response, converting it into a [Mono] of the
	 * specified type.
	 *
	 * @param T The type of the response object to return (e.g., [Player], [LoadResult]).
	 * @return A [Mono] containing the parsed response object.
	 */
	private inline fun <reified T : Any> Call.toMono() = Mono.create {
		it.onCancel(this::cancel)
		enqueue(AsyncResponseCallback(it) { rawResponse -> json.decodeFromString<T>(rawResponse) })
	}
}
