# core-ext-game [![Build Status](https://travis-ci.org/codingchili/chili-game-ext.svg?branch=master)](https://travis-ci.org/codingchili/chili-game-ext)

A collection of game-oriented microservices that builds upon chili-core. One day it will be complete game.

![realm-list.png](https://raw.githubusercontent.com/codingchili/chili-game-ext/master/realm-list.png)
Sample image of realm servers connected to the realm registry.

## Building

- Requires Polymer-cli.
- Requires Bower.

Builds the project and runs all tests.
```
./gradlew build
```

Create the distribution zip and run polymer build with:
```
./gradlew archivePrototype
```

To run the project without having to build a zip and perform polymer builds:
```
./gradlew prototype
```

## Background
The purpose of the service part of the project is to provide implementations for use with game servers. Each service may be distributed on different hosts. Communication channels is provided by the core, with support for various transports and storage plugins. Breaking down the system into microservices improves maintainability, testability and ultimately, productivity.

##### Audience :fire:
* Game developers seeking to implement a 2D RPG multiplayer game.
* Aspiring game developers with an interest in backend development.
* Players who are into simplistic 2D MMORPG's.

##### Great software :blue_heart:
To make this project a reality we use only great software.

- [CQEngine](https://github.com/npgall/cqengine) primary persistence store.
- [Hazelcast](https://hazelcast.com/) for cluster discovery.
- [Vert.x](https://vertx.io/) for the backend (with chili-core)
- [Polymer](https://www.polymer-project.org/) for the website and game UI.
- [MagicaVoxel](https://ephtracy.github.io/) for artwork!
- [PIXI.js](http://www.pixijs.com/) for client side rendering.

##### Contributing :purple_heart:
Do you have an idea for a spell? a new player class? an NPC? gameplay mechanics? story? anything really, we are open 24/7 for contributions. Just submit an issue or a PR.

---

## Configuration
Services following the official guidelines should place their configuration files in;
- 'conf/services/' 
where conf is a directory in the same folder as the server jar.
```
├── conf/
│   ├── system/
│   │   ├── security.yaml
│   │   ├── launcher.yaml
│   │   ├── storage.yaml
│   │   ├── system.yaml
│   ├── services/
│   │   ├── authserver.yaml
│   │   ├── logserver.yaml
│   │   ├── realmserver.yaml
│   │   ├── realmregistry.yaml
│   │   ├── webserver.yaml
│   │   ├── routingserver.yaml
│   ├── realms/
│   │   ├── realmName.yaml
│   ├── game/
│   │   ├── classes/
│   │   ├── afflictions/
│   │   ├── spells/
│   │   ├── instances/
```
Some services comes with additional resources, these can be bundled within the jar if moved to **src/main/resources**. This also applies to configuration files, it is however recommended that these are easily edited. 

###### Services
* Authentication: Account creation and available realms.
* Routing: Routes client requests in/out of the cluster.
* Realms: Handles incoming connections, instance travel.
 * Instances: Handles game logic.
* Realm registry: keeps track of active realms. 
* Website: Provides an interface for account/character/realmlist.
* Logging: Receives logging data from the other components.
* Social: Achievements, chat, guilds. (TBD)
* Auction house: Handles asynchronous trading with orders/offers. (TBD)
* Serverstatus: Provides a quick overview of system uptime. (TBD)

Patching is no longer a service - the webseed standard was not very well supported so support was dropped. We are aiming to create a browser based game primarily and will rely on a CDN to provide game resources. We want to avoid serving files over the cluster or machines that handles the website or the game state.

Communication between services is done over the cluster, other available transports such as websock, tcp, udp and rest is available but not recommended unless a service is not able to join the cluster. Services that needs to authenticate another service should use the AuthenticationGenerator to generate pre-shared keys, secrets and generate tokens. This may be invoked from the standard launcher in chili-core, using the --generate commandline argument.

All communication between services uses a text-protocol based on JSON for simplicity.
