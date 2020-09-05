# Auction service

- send (items/cash/message)
    - target account name only?

- deposit (items/cash, only available from instance)
    - only from instance, requires realm/instance token.

- withdraw (items/cash, only available from instance)
    - only from instance, requires realm/instance token.

- offer (put an item up for bidding)
    - initial price
    - item details
    - quantity
    - image: from cdn.

- bid (check funds, deposit funds into contract)
    - target item
    - bank account
    
 - query
    - filter on category
    - filter on price range
    - optional (filter on realm)

split resources per realm?
    - require realm token to access realm resources?
    - account wide bank?

problem:
- items are not centralized but distributed per character/realm
    fix: auction house works like a bank?

opportunities:
- bank implementation in auction house service?
    hides the issue with AH items having to be banked.
    
- bank implements post office?
    
- blockchain for offers/bids?
    - real money integration
    - can be used to buy game-tokens?