package dev.ebullient.micrometer.deployment.binder;

import java.util.List;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.runtime.binder.jaeger.JaegerMeterBinderRecorder;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.jaeger.runtime.JaegerBuildTimeConfig;
import io.quarkus.jaeger.runtime.JaegerConfig;
import io.quarkus.runtime.ApplicationConfig;

public class JaegerBinderProcessor {

    private static final Logger LOGGER = Logger.getLogger(JaegerBinderProcessor.class.getName());

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void setupTracer(JaegerMeterBinderRecorder recorder,
            JaegerBuildTimeConfig buildTimeConfig,
            JaegerConfig runtimeConfig,
            ApplicationConfig appConfig,
            List<FeatureBuildItem> features) throws ReflectiveOperationException {

        // find out if Jaeger extension is present
        // Jaeger does not register a Capability at the moment, so this is a slightly more ugly way to detect it
        boolean jaegerDetected = false;
        for (FeatureBuildItem feature : features) {
            if (feature.getInfo().equals(FeatureBuildItem.JAEGER)) {
                jaegerDetected = true;
                break;
            }
        }
        if (!jaegerDetected) {
            LOGGER.debug("Jaeger not detected, not registering a tracer");
            return;
        }

        boolean metricsEnabled = buildTimeConfig.metricsEnabled;
        if (metricsEnabled) {
            LOGGER.debug("Setting up Jaeger tracer with Micrometer metrics factory");
            recorder.registerTracerWithMetrics(runtimeConfig, appConfig);
        } else {
            LOGGER.debug("Setting up Jaeger tracer with no metrics");
            recorder.registerTracerWithoutMetrics(runtimeConfig, appConfig);
        }
    }

}
