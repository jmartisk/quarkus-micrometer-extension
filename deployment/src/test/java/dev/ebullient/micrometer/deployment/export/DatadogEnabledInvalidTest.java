package dev.ebullient.micrometer.deployment.export;

import javax.inject.Inject;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.validate.ValidationException;
import io.quarkus.test.QuarkusUnitTest;

public class DatadogEnabledInvalidTest {
    static final String REGISTRY_CLASS_NAME = "io.micrometer.datadog.DatadogMeterRegistry";
    static final Class<?> REGISTRY_CLASS = MicrometerRecorder.getClassForName(REGISTRY_CLASS_NAME);

    @RegisterExtension
    static final QuarkusUnitTest config = new QuarkusUnitTest()
            .withConfigurationResource("test-logging.properties")
            .overrideConfigKey("quarkus.micrometer.binder-enabled-default", "false")
            .overrideConfigKey("quarkus.micrometer.export.datadog.enabled", "true")
            .overrideConfigKey("quarkus.micrometer.registry-enabled-default", "false")
            .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
                    .addClass(DatadogRegistryProcessor.REGISTRY_CLASS))
            .assertException(t -> {
                Assertions.assertEquals(ValidationException.class.getName(), t.getClass().getName());
            });

    @Inject
    MeterRegistry registry;

    @Test
    public void testMeterRegistryPresent() {
        Assertions.fail("Runtime should not have initialized with missing apiKey");
    }
}
