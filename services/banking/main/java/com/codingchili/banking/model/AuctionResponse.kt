package com.codingchili.banking.model

import com.codingchili.core.protocol.ResponseStatus

/**
 * A response object for the auction microservice.
 */
class AuctionResponse(
    val inventory: Inventory? = null,
    val auctions: List<Auction>? = null,
    val notifications: List<Notification>? = null
) : ServerResponse(status = ResponseStatus.ACCEPTED) {

    companion object {
        fun inventory(inventory: Inventory): AuctionResponse {
            return AuctionResponse(inventory = inventory);
        }

        fun auctions(auctions: List<Auction>): AuctionResponse {
            return AuctionResponse(auctions = auctions);
        }

        fun notifications(notifications: List<Notification>): AuctionResponse {
            return AuctionResponse(notifications = notifications);
        }

        fun auction(auction: Auction): AuctionResponse {
            return AuctionResponse(auctions = listOf(auction))
        }
    }

}