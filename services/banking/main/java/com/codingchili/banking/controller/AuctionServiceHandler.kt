package com.codingchili.banking.controller

import com.codingchili.banking.model.*
import com.codingchili.core.protocol.ResponseStatus
import io.vertx.core.Vertx
import java.text.NumberFormat
import java.time.Instant
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextInt

/**
 * Formats the given value using decimal separators and appends a currency icon.
 */
private fun formatValue(value: Int): String {
    return "${NumberFormat.getInstance().format(value)} Ξ"
}

/**
 * Local mock implementation of the auction service.
 *
 * Supports multiple users and randomly generates a starting
 * inventory for testing. The in-memory database is reset
 * when the application is restarted and all searches returns
 * the same hits.
 *
 * Error handling can be tested by searching for "error" or "null".
 * Most calls to the service are delayed by a time configured in the
 * MockData class. This allows testing the app under slower network
 * conditions to make sure user feedback is available. It also
 * ensures that no service calls are blocking the ui.
 */
class AuctionServiceHandler(val vertx: Vertx) : AuctionService {
    private var notifications = HashMap<String, ArrayList<Notification>>()
    private var inventory = HashMap<String, Inventory>()
    private var favorites = HashMap<String, MutableSet<Auction>>()
    private var auctions = ArrayList<Auction>()

    override fun search(user: String, query: QueryType, param: String?): List<Auction> {
        val fiveMinutes = 300
        val finished = auctions.filter { it.finished() }
        val active = auctions.filter { !it.finished() }
        return when (query) {
            QueryType.favorites -> auctions.filter { favorites(user).contains(it) }
            QueryType.sold -> finished.filter { it.seller == user && it.bids.isNotEmpty() }
            QueryType.active -> active.filter { it.seller == user }
            QueryType.not_sold -> finished.filter { it.seller == user && it.bids.isEmpty() }
            QueryType.won -> finished.filter { it.bids.isNotEmpty() && it.bids[0].owner == user }
            QueryType.lost -> finished.filter { it.bids.find { it.owner == user } != null }
            QueryType.leading -> active.filter { it.bids.take(1).any { it.owner == user } }
            QueryType.overbid -> active.filter { it.bids.drop(1).any { it.owner == user } }
            QueryType.armor_type -> active.filter { it.item.type == param!! }
            QueryType.weapon_type -> active.filter { it.item.type == param!! }
            QueryType.ending_soon -> active.filter { TimeUnit.MILLISECONDS.toSeconds(Instant.now().toEpochMilli() - it.end) < fiveMinutes }
            QueryType.quest -> active.filter { it.item.type == Type.quest }
            QueryType.text -> active.filter { it.item.name.toLowerCase().contains(param!!)
                    || it.item.description.toLowerCase().contains(param) }
            QueryType.slot -> active.filter { it.item.slot == param }
            QueryType.consumable -> active.filter { it.item.type == Type.consumable }
            QueryType.rarity -> active.filter { it.item.rarity == ItemRarity.valueOf(param!!) }
            QueryType.seller -> auctions.filter { it.seller == param }
        }
    }

    override fun favorite(user: String, auctionId: String, add: Boolean): ServerResponse {
        val auction = findById(auctionId)
        if (add) {
            favorites(user).add(auction)
        } else {
            favorites(user).remove(auction)
        }
        return ServerResponse(
                status = ResponseStatus.ACCEPTED,
                message = "ok"
        )
    }

    override fun favorites(user: String): MutableSet<Auction> {
        return favorites.computeIfAbsent(user) { HashSet<Auction>() }
    }

    override fun inventory(user: String): Inventory {
        return inventory.computeIfAbsent(user) {
            val funds = Random.nextInt(64_000..32_000_000)
            Inventory(
                    funds = funds,
                    liquidity = funds,
                    items = listOf(
                            // generate some random items to start with.
                            MockData.randomItem(),
                            MockData.randomItem(),
                            MockData.randomItem(),
                            MockData.randomItem(),
                            MockData.randomItem(),
                            MockData.randomItem()
                    )
            )
        }
    }

    private fun handleAuctionEnd(auction: Auction) {
        val high = auction.bids.firstOrNull()
        val seller = inventory(auction.seller)

        if (high != null) {
            val buyer = inventory(high.owner)
            buyer.items = buyer.items.plus(auction.item)

            buyer.funds -= high.value
            seller.funds += high.value

            notify(
                    auction.seller,
                    auction,
                    "<b>${auction.item.name}</b> was sold for <b>${formatValue(high.value)}</b>."
            )
            notify(
                    high.owner,
                    auction,
                    "Won auction for <b>${auction.item.name}</b> at <b>${formatValue(high.value)}</b>"
            )

            // notify all other losing bidders.
            auction.bids.filterNot { it.owner == high.owner }
                    .sortedByDescending { it.value }
                    .distinctBy { it.owner }
                    .forEach { bid ->
                        notify(
                                bid.owner,
                                auction,
                                "Lost auction for <b>${auction.item.name}</b>, winning bid was <b>${formatValue(
                                        high.value
                                )}</b>"
                        )
                    }
        } else {
            seller.items = seller.items.plus(auction.item)
            notify(
                    auction.seller,
                    auction,
                    "<b>${auction.item.name}</b> was not sold and returned to inventory."
            )
        }

        // notify all users that favorited the auction but did not place any bids.
        favorites.forEach { (user, favorites) ->
            if (auction in favorites && auction.bids.find { it.owner == user } == null) {
                notify(
                        user,
                        auction,
                        "Auction for <b>${auction.item.name}</b> finished at <b>${formatValue(high?.value ?: auction.initial)}</b>"
                )
            }
        }
    }

    private fun notify(user: String, auction: Auction, message: String) {
        notifications(user).add(
                0,
                Notification(
                        icon = auction.item.icon,
                        auctionId = auction.id,
                        message = message
                )
        )
    }

    override fun auction(seller: String, itemId: String, value: Int): Auction {
        val item = inventory(seller).items.first { it.id == itemId }
        val auction = Auction(item = item, initial = value, seller = seller)
        val inventory = inventory(seller)

        vertx.setTimer(auction.end - Instant.now().toEpochMilli()) {
            handleAuctionEnd(auction)
        }

        // verify that the item exists in inventory.
        val items = inventory.items.toMutableList()
        if (items.remove(item)) {
            inventory.items = items
            auctions.add(auction)
        } else {
            throw Exception("No longer in possession of the given item.")
        }
        return auction
    }

    override fun bid(owner: String, value: Int, auctionId: String): Auction {
        val auction = auctions.first { it.id == auctionId }
        val highestBid = auction.bids.firstOrNull()?.value ?: 0
        val inventory = inventory(owner)

        if (owner == auction.seller) {
            throw Exception("Cannot bid on own auction.")
        }

        // require new bid to be higher than last
        if (value > max(auction.initial, highestBid)) {

            // ensure that current liquidity is enough to place the bid
            if (inventory.liquidity > value) {
                inventory.liquidity -= value
                val bids = auction.bids.toMutableList()
                val lastBid = auction.bids.firstOrNull()

                // add the new bid to the auction.
                bids.add(0, Bid(owner = owner, value = value))
                auction.bids = bids

                // return the value of the last bid to its owners liquidity.
                if (lastBid != null) {
                    inventory(lastBid.owner).liquidity += lastBid.value
                }

                // notify the previous high bidder.
                val message =
                        "Outbid by <b>${owner}</b> on auction for <b>${auction.item.name}</b>, new bid <b>${formatValue(
                                value
                        )}</b>"
                auction.bids.filterNot { it.owner == owner }
                        .take(1)
                        .forEach {
                            notify(
                                    it.owner,
                                    auction,
                                    message
                            )
                        }
            } else {
                throw Exception("Funds are not enough to increase bid.")
            }
        } else {
            throw Exception("Bid is not high enough.")
        }
        return auction
    }

    override fun notifications(user: String): MutableList<Notification> {
        return notifications.computeIfAbsent(user) { ArrayList() }
    }

    override fun findById(auctionId: String): Auction {
        return auctions.find { it.id == auctionId } ?: throw Exception("Auction not found.")
    }
}
