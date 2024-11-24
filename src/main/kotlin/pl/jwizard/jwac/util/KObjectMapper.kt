/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.arbjerg.lavalink.protocol.v4.json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * A global instance of [ObjectMapper] used for handling JSON serialization and deserialization.
 *
 * @author Miłosz Gilga
 */
val objectMapper = ObjectMapper()

/**
 * Converts a [JsonElement] into an object of type [T].
 *
 * This function takes a [JsonElement], serializes it to a string, and then deserializes it into an object of the
 * specified type [T]. It uses the globally available [objectMapper] for the actual conversion process.
 *
 * @param T The target type to which the JSON element should be converted.
 * @param jsonElement The [JsonElement] that should be deserialized into an object of type [T].
 * @return The deserialized object of type [T].
 * @author Miłosz Gilga
 */
inline fun <reified T> fromJsonElement(jsonElement: JsonElement): T {
	val stringValue = jsonElement.toString()
	return objectMapper.readValue<T>(stringValue)
}

/**
 * Converts an object to a [JsonObject].
 *
 * This function converts any object to a [JsonObject] by first serializing it into a string and then parsing it into
 * a [JsonObject] using the [objectMapper]. This is useful when you need to work with a JSON object representation of
 * a given data.
 *
 * @param data The object to be converted to a [JsonObject]. Can be any object or null.
 * @return A [JsonObject] representation of the input data.
 * @author Miłosz Gilga
 */
fun toJsonObject(data: Any?): JsonObject {
	val jsonNode = objectMapper.readTree(data.toString())
	return toJsonElement(jsonNode) as JsonObject
}

/**
 * Converts an object to a [JsonElement].
 *
 * This function serializes an object into a JSON string and then parses it into a [JsonElement]. The resulting
 * [JsonElement] can be a [JsonObject], [JsonArray], or any other valid JSON element. If the input data is null, it
 * returns an empty [JsonObject].
 *
 * @param data The object to be converted to a [JsonElement]. Can be any object or null.
 * @return A [JsonElement] representation of the input data.
 * @author Miłosz Gilga
 */
fun toJsonElement(data: Any?) = if (data == null) {
	JsonObject(mapOf())
} else {
	val jsonString = objectMapper.writeValueAsString(data)
	json.parseToJsonElement(jsonString)
}
