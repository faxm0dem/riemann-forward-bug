# riemann-forward-debug

This riemann setup simply exposes a problem with `forward` to a `tcp-client`.
The topology is the following:

```
forwarder -> backend -> dashboard
```

Run the stack:

```
docker compose up
```

1. Attach to riemann-dash on `forwarder` : localhost:4444 query `tagged "riemann" and service =~ "%queue used%"`.
2. Attach to riemann-dash on `dashboard` : localhost:8888 same query
3. Simulate a network lag `docker compose pause backend`

You should see:

1. That the queue used on `forwarder` goes up quite quickly.
2. That the metrics from `forwarder` disappear on `dashboard`
3. That everything goes back to normal when running `docker compose unpause backend` 

Right, `async-queues` do their job everything is fine.

Now repeat the experiment, but instead of `pause backend` do `kill backend`. In that case, the queue doesn't go up, and running `up backend` doesn't recover the metrics on the `dashboard`'s websocket.
Also, if doing `pause`, then `kill`, the queue goes up, but `up` doesn't make it go down again. It's like the `tcp-client` never recovers from a dead riemann server.

## EDIT

In fact, it does recover if only `kill` is done.
However, if `pause`, *then* `kill` is issued - effectively simulating a dying server *during* an active connection, the client never recovers.

### 2024-04-09

There seems to be *one* message transmitted to the dashboard in the latter case, just about when the queue on the forwarder is full.

Also, what is not clear to me is why the queue on the forwarder is never increasing when killing the backend. It is only increasing when pausing it (e.g. when there is an open connection). So it seems only when backend is lagging the queue is working. When dead, the queue is never used.

Apparently the *one* message transmitted to the dash when queue is full is only sent if `max-pool-size` of the forwarder is set to `2`. If set to `1` it doesn't get through. Also, when `2` the queue resets to 0 whereas when `1` it stays full.

On further inspection, it's not just one message sent when queue is full, it's the full queue contents (all old messages) that's released at once !

