![](.github/banner.png)

JWizard is an open-source Discord music bot handling audio content from various multimedia sources with innovative web
player. This repository contains a custom client for Lavalink nodes used in the JWizard Core project. It enables
creating separate node pools and applies load balancing based on the Discord gateway audio region (or if it's not
available using a round-robin algorithm). This client is compatible with Lavalink v4 protocol.

## Table of content

* [Architecture concepts](#architecture-concepts)
* [Clone and install](#clone-and-install)
* [Package on local environments](#package-on-local-environments)
* [Documentation](#documentation)
* [Contributing](#contributing)
* [License](#license)

## Architecture concepts

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

## Clone and install

1. Make sure you have at least JDK 17 and Kotlin 2.0.
2. Clone this repository via:

```shell
$ git clone https://github.com/jwizard-bot/jwizard-audio-client
```

## Package on local environments

To package audio client to maven local, type:

- for UNIX based systems:

```bash
$ ./gradlew clean
$ ./gradlew publishToMavenLocal
```

- for Windows systems:

```bash
.\gradlew clean
.\gradlew publishToMavenLocal
```

## Documentation

For detailed documentation, please visit [JWizard documentation](https://jwizard.pl/docs).
<br>
Documentation for latest version (with SHA) you will find [here](https://docs.jwizard.pl/jwac) - in KDoc format.

## Contributing

We welcome contributions from the community! Please read our [CONTRIBUTING](./CONTRIBUTING) file for guidelines on how
to get involved.

## License

This project is licensed under the AGPL-3.0 License - see the LICENSE file for details.
