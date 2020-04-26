INSERT INTO orders (id, ticket_id, user_id, quantity)
VALUES (/*order.id*/'abcde',
        /*order.ticketId*/1,
        /*order.userId*/2,
        /*order.quantity*/3)
ON DUPLICATE KEY UPDATE id = id