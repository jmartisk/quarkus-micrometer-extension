package dev.ebullient.micrometer.runtime.binder.jaeger;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import io.jaegertracing.internal.metrics.Counter;
import io.jaegertracing.internal.metrics.Gauge;
import io.jaegertracing.internal.metrics.Timer;
import io.jaegertracing.spi.MetricsFactory;
import io.micrometer.core.instrument.ImmutableTag;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;

public class JaegerMicrometerMetricsFactory implements MetricsFactory {

    private MeterRegistry meterRegistry;

    public JaegerMicrometerMetricsFactory(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public Counter createCounter(String name, Map<String, String> tags) {
        io.micrometer.core.instrument.Counter counter = meterRegistry.counter(name, convertMapToIterableTags(tags));
        return new Counter() {
            @Override
            public void inc(long delta) {
                counter.increment(delta);
            }
        };
    }

    @Override
    public Timer createTimer(String name, Map<String, String> tags) {
        io.micrometer.core.instrument.Timer timer = meterRegistry.timer(name, convertMapToIterableTags(tags));
        return new Timer() {
            @Override
            public void durationMicros(long time) {
                timer.record(time, TimeUnit.MICROSECONDS);
            }
        };
    }

    @Override
    public Gauge createGauge(String name, Map<String, String> tags) {
        GaugeWithGet stateObject = new GaugeWithGet();
        meterRegistry.gauge(name, convertMapToIterableTags(tags), stateObject, GaugeWithGet::get);
        return stateObject;
    }

    static class GaugeWithGet implements Gauge {

        private AtomicLong value = new AtomicLong();

        @Override
        public void update(long amount) {
            value.set(amount);
        }

        public long get() {
            return value.get();
        }
    }

    private Iterable<Tag> convertMapToIterableTags(Map<String, String> map) {
        return map.entrySet().stream().map(entry -> new ImmutableTag(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
