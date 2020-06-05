package dev.ebullient.micrometer.runtime.binder.vertx;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Singleton;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.config.runtime.VertxConfig;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.lang.NonNull;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.VertxMetricsFactory;
import io.vertx.core.spi.metrics.HttpServerMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;

@Singleton
public class VertxMeterBinderAdapter extends MetricsOptions implements MeterBinder, VertxMetricsFactory, VertxMetrics {
    private static final Logger log = Logger.getLogger(VertxMeterBinderAdapter.class);

    final AtomicReference<MeterRegistry> meterRegistry = new AtomicReference<>();

    private VertxConfig config;

    public VertxMeterBinderAdapter() {
    }

    @Override
    public void bindTo(@NonNull MeterRegistry registry) {
        meterRegistry.set(registry);
    }

    public void setVertxConfig(VertxConfig config) {
        this.config = config;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public VertxMetricsFactory getFactory() {
        return this;
    }

    @Override
    public VertxMetrics metrics(VertxOptions vertxOptions) {
        return this;
    }

    @Override
    public MetricsOptions newOptions() {
        return this;
    }

    @Override
    public HttpServerMetrics<?, ?, ?> createHttpServerMetrics(HttpServerOptions options, SocketAddress localAddress) {
        log.debugf("Create HttpServerMetrics with options %s and address %s", options, localAddress);
        log.debugf("Bind registry %s to Vertx Metrics", meterRegistry.get());
        MeterRegistry registry = meterRegistry.get();
        if (registry == null) {
            throw new IllegalStateException("MeterRegistry was not resolved");
        }
        if (config == null) {
            throw new IllegalStateException("VertxConfig was not found");
        }
        return new VertxHttpServerMetrics(registry, config);
    }
}
