package dev.ebullient.micrometer.runtime.binder.vertx;

import org.jboss.logging.Logger;

import io.vertx.core.Context;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class VertxMeterFilter implements Handler<RoutingContext> {
    private static final Logger log = Logger.getLogger(VertxMeterFilter.class);

    @Override
    public void handle(RoutingContext event) {
        final Context context = Vertx.currentContext();
        log.debugf("Handling event %s with context %s", event, context);

        context.put(VertxHttpServerMetrics.METER_ROUTING_CONTEXT, event);
        event.addBodyEndHandler(new Handler<Void>() {
            @Override
            public void handle(Void x) {
                context.remove(VertxHttpServerMetrics.METER_ROUTING_CONTEXT);
            }
        });

        event.next();
    }
}
