package dev.ebullient.micrometer.deployment.export;

import java.util.function.BooleanSupplier;

import org.jboss.logging.Logger;

import dev.ebullient.micrometer.deployment.MicrometerRegistryProviderBuildItem;
import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import dev.ebullient.micrometer.runtime.config.MicrometerConfig;
import dev.ebullient.micrometer.runtime.config.PrometheusConfig;
import dev.ebullient.micrometer.runtime.export.PrometheusMeterRegistryProvider;
import dev.ebullient.micrometer.runtime.export.handlers.PrometheusHandler;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;

/**
 * Add support for the Promethus Meter Registry. Note that the registry may not
 * be available at deployment time for some projects: Avoid direct class
 * references.
 */
public class PrometheusRegistryProcessor {
    private static final Logger log = Logger.getLogger(PrometheusRegistryProcessor.class);

    static final String REGISTRY_CLASS_NAME = "io.micrometer.prometheus.PrometheusMeterRegistry";
    static final Class<?> REGISTRY_CLASS = MicrometerRecorder.getClassForName(REGISTRY_CLASS_NAME);

    static class PrometheusEnabled implements BooleanSupplier {
        MicrometerConfig mConfig;

        public boolean getAsBoolean() {
            return REGISTRY_CLASS != null && mConfig.checkRegistryEnabledWithDefault(mConfig.export.prometheus);
        }
    }

    @BuildStep(onlyIf = PrometheusEnabled.class, loadsApplicationClasses = true)
    MicrometerRegistryProviderBuildItem createPrometheusRegistry(CombinedIndexBuildItem index,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        // Add the Prometheus Registry Producer
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(PrometheusMeterRegistryProvider.class)
                .setUnremovable().build());

        // Include the PrometheusMeterRegistry in a possible CompositeMeterRegistry
        return new MicrometerRegistryProviderBuildItem(REGISTRY_CLASS);
    }

    @BuildStep(onlyIf = PrometheusEnabled.class)
    void createPrometheusRoute(BuildProducer<RouteBuildItem> routes,
            MicrometerConfig mConfig) {

        PrometheusConfig pConfig = mConfig.export.prometheus;
        log.debug("PROMETHEUS CONFIG: " + pConfig);

        // Exact match for resources matched to the root path
        routes.produce(new RouteBuildItem(pConfig.path, new PrometheusHandler()));

        // Match paths that begin with the deployment path
        String matchPath = pConfig.path + (pConfig.path.endsWith("/") ? "*" : "/*");
        routes.produce(new RouteBuildItem(matchPath, new PrometheusHandler()));
    }
}
