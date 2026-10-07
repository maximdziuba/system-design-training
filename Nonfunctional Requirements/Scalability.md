# Scalability

Possibility of system to process bigger amount of requests without degradation.

## Loading Types

- Requests (RPS) - how many requests are processed
- Data / storage

## Scalability Measurement

- **Size** - is it easy to add CPU / RAM / node
- **Administrative** - how system handles the growth of commands / clients
- **Geo** - work in different regions, closer to the client

## Ways to Scale

- **Vertical (Scale up)** - adding resources to one node. Easy, but has the threshold of hardware and can be expensive.
- **Horizontal (Scale out)** - adding nodes and divide the load. Cheap per node, but needs consistency, replication and state management.