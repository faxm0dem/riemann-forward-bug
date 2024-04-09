; forward with timeout
(defn forward-timeout
  "Sends an event or a collection of events through a Riemann client. Times out after :timeout milliseconds."
  [client & {:keys [timeout] :or {timeout 5000}}]
  (fn stream [es]
    (if (map? es)
      (deref (riemann.client/send-event client es) timeout ::timeout)
      (deref (riemann.client/send-events client es) timeout ::timeout))))
