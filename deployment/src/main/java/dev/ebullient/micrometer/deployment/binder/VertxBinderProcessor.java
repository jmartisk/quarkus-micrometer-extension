package dev.ebullient.micrometer.deployment.binder;

import java.util.function.BooleanSupplier;

import javax.interceptor.Interceptor;

import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderAdapter;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderContainerFilter;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterBinderRecorder;
import dev.ebullient.micrometer.runtime.binder.vertx.VertxMeterFilter;
import dev.ebullient.micrometer.runtime.config.MicrometerConfig;
import dev.ebullient.micrometer.runtime.config.runtime.VertxConfig;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.resteasy.common.spi.ResteasyJaxrsProviderBuildItem;
import io.quarkus.vertx.core.deployment.VertxOptionsConsumerBuildItem;
import io.quarkus.vertx.http.deployment.FilterBuildItem;

public class VertxBinderProcessor {

    static final String METRIC_OPTIONS_CLASS_NAME = "io.vertx.core.metrics.MetricsOptions";
    static final Class<?> METRIC_OPTIONS_CLASS = MicrometerRecorder.getClassForName(METRIC_OPTIONS_CLASS_NAME);

    static class VertxBinderEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            return METRIC_OPTIONS_CLASS != null && mConfig.checkBinderEnabledWithDefault(mConfig.binder.vertx);
        }
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    AdditionalBeanBuildItem createVertxAdapters() {
        // Add Vertx meter adapters
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(VertxMeterBinderAdapter.class)
                .addBeanClass(VertxMeterBinderContainerFilter.class)
                .setUnremovable().build();
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    ResteasyJaxrsProviderBuildItem createVertxFilters() {
        return new ResteasyJaxrsProviderBuildItem(VertxMeterBinderContainerFilter.class.getName());
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    FilterBuildItem addVertxMeterFilter() {
        return new FilterBuildItem(new VertxMeterFilter(), 10);
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    @Record(value = ExecutionTime.STATIC_INIT)
    VertxOptionsConsumerBuildItem build(VertxMeterBinderRecorder recorder) {
        return new VertxOptionsConsumerBuildItem(recorder.configureMetricsAdapter(), Interceptor.Priority.LIBRARY_AFTER);
    }

    @BuildStep(onlyIf = VertxBinderEnabled.class)
    @Record(value = ExecutionTime.RUNTIME_INIT)
    void setVertxConfig(VertxMeterBinderRecorder recorder, VertxConfig config) {
        recorder.setVertxConfig(config);
    }
}
