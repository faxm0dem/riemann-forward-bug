# riemann-forward-bug

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

