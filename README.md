# Mutable Bunnies [![Build Status](https://travis-ci.com/codingchili/mutable-bunnies-server.svg?branch=master)](https://travis-ci.com/codingchili/mutable-bunnies-server)

A microservices based distributed 2d MMORPG with Hazelcast and Vert.x on Java 11, currently 20k LOC + configuration.

#### Game clients

- [Web based client](https://github.com/codingchili/mutable-bunnies-client)
- [Android auction client](https://github.com/codingchili/mutable-bunnies-auctions)

The backend is developed using the chili-core framework
- [chili-core framework](https://github.com/codingchili/chili-core)

#### Game architecture

![architecture.png](images/architecture.png)

First [demo video](https://www.youtube.com/watch?v=TlFcvCJb9lw) now available! 

If you are a part of the team please include the client prototype submodule.
```console
# cloning the repository:
git clone --recursive <repository-url>

# if already cloned:
git submodule update --init --recursive
```

## Building

- Requires node/npm

Builds the project and runs all tests.
```console
./gradlew build
```


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

Communication between services is done over the cluster/local bus, other available transports such as websock, tcp, udp and rest is available but not recommended unless a service is not able to join the cluster. Services that needs to authenticate another service should use the AuthenticationGenerator to generate pre-shared keys, secrets and generate tokens. This may be invoked from the standard launcher in chili-core, using the `--generate` commandline argument.

All communication between services in different JVM's uses a text-protocol based on JSON for simplicity.



## Background
This is a 2D MMORPG server that is in development, which focuses on

- ease of development: simple event passing and handler-based server development.
- fast feedback loop: fast startup times, fast client load times, unit-testable.
- simple and extendable: scripted events, yaml-based npc/item/spell/affliction configurations. 

### Progress

Overview of the server implementation progress

Core mechanics
- [X] movement
- [X] player chat
- [X] spell engine (cooldown, learned spells, charges)
- [X] dialog engine
- [X] affliction engine
- [X] cross realm/instance chat
- [X] friend lists
- [X] instance travel
- [X] instance loading
- [ ] crafting
- [X] questing
- [ ] dynamically deployed instances

Npcs/Entities
- [X] scripted npcs/entities
- [X] support for animations
- [X] spawn entities/npcs from script
- [X] NPC initiated dialogs
- [X] NPC/interaction dialogs

##### Audience :fire:
* Game developers seeking to implement a 2D RPG multiplayer game.
* Aspiring game developers with an interest in backend development.
* Players who are into simplistic 2D MMORPG's.

##### Great software :blue_heart:
To make this project a reality we use only great software.

On the backend
- [CQEngine](https://github.com/npgall/cqengine) primary persistence store.
- [Hazelcast](https://hazelcast.com/) for cluster discovery.
- [Vert.x](https://vertx.io/) for threading and transports.
- [chili-core](https://github.com/codingchili/chili-core) for maximum ease of development.

##### Contributing :purple_heart:
Do you have an idea for a spell? a new player class? an NPC? gameplay mechanics? story? anything really, we are open 24/7 for contributions. Just submit an issue or a PR.
