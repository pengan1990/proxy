# proxy
proxy is against mysql protocol which support insert, delete, update, select common grammer from SQL93 std


## proxy

>insert: complete insert with batch, which will send to back connections by classify rows against a transaction
>delete: send to all back connections against a transaction
>update: without to set route column, all others is to route to the specified connections
>select: support limit, order by, group by without having, or combination with three of these

