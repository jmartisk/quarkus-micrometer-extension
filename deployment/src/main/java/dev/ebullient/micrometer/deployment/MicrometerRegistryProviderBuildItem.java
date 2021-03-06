package dev.ebullient.micrometer.deployment;

import org.jboss.jandex.ClassInfo;

import dev.ebullient.micrometer.runtime.MicrometerRecorder;
import io.micrometer.core.instrument.MeterRegistry;
import io.quarkus.builder.item.MultiBuildItem;

public final class MicrometerRegistryProviderBuildItem extends MultiBuildItem {

    final Class<? extends MeterRegistry> clazz;

    public MicrometerRegistryProviderBuildItem(ClassInfo provider) {
        this(provider.name().toString());
    }

    public MicrometerRegistryProviderBuildItem(Class<?> providedRegistryClass) {
        this.clazz = (Class<? extends MeterRegistry>) providedRegistryClass;
    }

    public MicrometerRegistryProviderBuildItem(String registryClassName) {
        this.clazz = (Class<? extends MeterRegistry>) MicrometerRecorder.getClassForName(registryClassName);
    }

    public Class<? extends MeterRegistry> getRegistryClass() {
        return clazz;
    }

    @Override
    public String toString() {
        return "MicrometerRegistryProviderBuildItem{"
                + clazz
                + '}';
    }
}
