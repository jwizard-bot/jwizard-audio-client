/*
 * Copyright (c) 2024 by JWizard
 * Originally developed by Miłosz Gilga <https://miloszgilga.pl>
 */
package pl.jwizard.jwac.node

import pl.jwizard.jwac.balancer.region.RegionGroup

/**
 * Represents the configuration for a node in a distributed audio system.
 *
 * This class stores the configuration details for connecting to an audio node, including connection parameters
 * (hostname, port, etc.), the pool the node belongs to, and its HTTP timeout settings.
 *
 * @property hostName The hostname of the node.
 * @property port The port number of the node.
 * @property secure Whether the connection to the node is secure (HTTPS/WSS).
 * @property name The name of the node.
 * @property password The password used for authenticating the connection.
 * @property pool The node pool this node belongs to.
 * @property regionGroup The region group this node belongs to. Default is [RegionGroup.UNKNOWN].
 * @property httpTimeout The HTTP timeout for communication with the node, in milliseconds.
 * @constructor Creates a new [NodeConfig] instance with the provided parameters.
 */
class NodeConfig private constructor(
	private val hostName: String,
	private val port: Int,
	private val secure: Boolean,
	val name: String,
	val password: String,
	val pool: NodePool,
	val regionGroup: RegionGroup = RegionGroup.UNKNOWN,
	val httpTimeout: Long,
) {
	val wsUrl = getUrlWithProtocol("ws")
	val httpUrl = getUrlWithProtocol("http")

	/**
	 * Constructs a URL for the given protocol (ws or http) using the hostname, port, and security settings.
	 *
	 * @param protocol The protocol to use for the URL (either "ws" or "http").
	 * @return The constructed URL as a string.
	 */
	private fun getUrlWithProtocol(protocol: String) = "$protocol${if (secure) "s" else ""}://${hostName}:${port}"

	/**
	 * Builder class for constructing [NodeConfig] instances.
	 */
	class Builder {
		private var hostName: String? = null
		private var port: Int? = null
		private var secure: Boolean? = null
		private var name: String? = null
		private var password: String? = null
		private var pool: NodePool? = null
		private var regionGroup: RegionGroup = RegionGroup.UNKNOWN
		private var httpTimeout: Long? = null

		/**
		 * Sets the address (hostname, port, and security settings) for the node.
		 *
		 * @param hostName The hostname of the node.
		 * @param port The port number of the node.
		 * @param secure Whether the connection to the node is secure (HTTPS/WSS).
		 * @return The builder instance, for chaining.
		 */
		fun setAddress(hostName: String, port: Int, secure: Boolean) = apply {
			this.hostName = hostName
			this.port = port
			this.secure = secure
		}

		/**
		 * Sets the descriptor (name and password) for the node.
		 *
		 * @param name The name of the node.
		 * @param password The password used for authenticating the connection.
		 * @return The builder instance, for chaining.
		 */
		fun setHostDescriptor(name: String, password: String) = apply {
			this.name = name
			this.password = password
		}

		/**
		 * Sets the HTTP timeout for communication with the node.
		 *
		 * @param httpTimeout The HTTP timeout in milliseconds.
		 * @return The builder instance, for chaining.
		 */
		fun setHttpTimeout(httpTimeout: Long) = apply { this.httpTimeout = httpTimeout }

		/**
		 * Sets the balancer setup (node pool and region group) for the node.
		 *
		 * @param pool The node pool the node belongs to.
		 * @param regionGroup The region group the node belongs to, represented as a raw string.
		 * @return The builder instance, for chaining.
		 */
		fun setBalancerSetup(pool: NodePool, regionGroup: String) = apply {
			this.pool = pool
			this.regionGroup = RegionGroup.fromRawValue(regionGroup)
		}

		/**
		 * Builds and returns a [NodeConfig] instance with the values provided to the builder.
		 *
		 * @return The constructed [NodeConfig] instance.
		 * @throws IllegalArgumentException if any required fields are missing.
		 */
		fun build(): NodeConfig {
			requireNotNull(hostName) { "Hostname must be set" }
			requireNotNull(port) { "Port must be set" }
			requireNotNull(name) { "Name must be set" }
			requireNotNull(password) { "Password must be set" }
			requireNotNull(pool) { "Pool must be set" }
			requireNotNull(httpTimeout) { "Http timeout must be set" }
			return NodeConfig(hostName!!, port!!, secure!!, name!!, password!!, pool!!, regionGroup, httpTimeout!!)
		}
	}

	override fun toString() = name
}
