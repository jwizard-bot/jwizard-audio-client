# Module JWizard Audio Client

![](images/banner.png)

JWizard is an open-source Discord music bot handling audio content from various multimedia sources with innovative web
player. This repository contains a custom client for Lavalink nodes used in the JWizard Core project. It enables
creating separate node pools and applies load balancing based on the Discord gateway audio region (or if it's not
available using a round-robin algorithm). This client is compatible with Lavalink v4 protocol.

### Architecture concepts

* Modified original Lavalink client for Java/Kotlin supporting version 4 of the protocol, tightly integrated with the
  JWizard core.
* Enables fragmentation of nodes into *node pools*, allowing them to be categorized, excluded, or authorized to handle
  playback requests for audio tracks (useful for distributing traffic between nodes handling different audio plugins).
* Each node is represented by a separate instance of a Lavalink server.
* Load balancing between nodes in a selected pool is achieved by choosing a node in the same location as the Discord
  voice server for the audio channel.
* Additionally, the node with the least load is selected from the chosen pool (penalty system).
* A link (guild representation) can have a dynamically assigned node (if a node in the pool fails, the next one is
  selected according to the load balance's algorithm).
* Connections to the Lavalink server are made via HTTP (REST) protocol and WebSocket.
