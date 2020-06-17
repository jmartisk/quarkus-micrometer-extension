package dev.ebullient.micrometer.runtime.binder.jaeger;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

import org.jboss.logging.Logger;

import io.jaegertracing.internal.metrics.NoopMetricsFactory;
import io.jaegertracing.spi.MetricsFactory;
import io.micrometer.core.instrument.MeterRegistry;
import io.opentracing.util.GlobalTracer;
import io.quarkus.arc.Arc;
import io.quarkus.jaeger.runtime.JaegerConfig;
import io.quarkus.jaeger.runtime.JaegerDeploymentRecorder;
import io.quarkus.jaeger.runtime.QuarkusJaegerTracer;
import io.quarkus.runtime.ApplicationConfig;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class JaegerMeterBinderRecorder {

    private static final QuarkusJaegerTracer quarkusTracer = new QuarkusJaegerTracer();
    private static final Optional UNKNOWN_SERVICE_NAME = Optional.of("quarkus/unknown");
    private static final Logger log = Logger.getLogger(JaegerDeploymentRecorder.class);

    synchronized public void registerTracerWithoutMetrics(JaegerConfig jaeger, ApplicationConfig appConfig)
            throws ReflectiveOperationException {
        registerTracer(jaeger, appConfig, new NoopMetricsFactory());
    }

    synchronized public void registerTracerWithMetrics(JaegerConfig jaeger, ApplicationConfig appConfig)
            throws ReflectiveOperationException {
        registerTracer(jaeger, appConfig,
                new JaegerMicrometerMetricsFactory(Arc.container().instance(MeterRegistry.class).get()));
    }

    private void registerTracer(JaegerConfig jaeger, ApplicationConfig appConfig, MetricsFactory metricsFactory)
            throws ReflectiveOperationException {
        if (!jaeger.serviceName.isPresent()) {
            if (appConfig.name.isPresent()) {
                jaeger.serviceName = appConfig.name;
            } else {
                jaeger.serviceName = UNKNOWN_SERVICE_NAME;
            }
        }
        initTracerConfig(jaeger);

        // FIXME: hacks to be able to call package-private methods

        //        quarkusTracer.setMetricsFactory(metricsFactory);
        Method setMetricsFactory = QuarkusJaegerTracer.class.getDeclaredMethod("setMetricsFactory", MetricsFactory.class);
        setMetricsFactory.setAccessible(true);
        setMetricsFactory.invoke(quarkusTracer, metricsFactory);

        //        quarkusTracer.reset();
        Method reset = QuarkusJaegerTracer.class.getDeclaredMethod("reset");
        reset.setAccessible(true);
        reset.invoke(quarkusTracer);

        // we MUST make sure that the Jaeger extension did not register a tracer
        if (!GlobalTracer.isRegistered()) {
            log.debugf("Registering tracer to GlobalTracer %s", quarkusTracer);
            GlobalTracer.register(quarkusTracer);
        } else {
            throw new IllegalStateException(
                    "It appears that another extension already registered a tracer: " + GlobalTracer.get());
        }
    }

    private void initTracerConfig(JaegerConfig jaeger) throws ReflectiveOperationException {
        initTracerProperty("JAEGER_ENDPOINT", jaeger.endpoint, uri -> uri.toString());
        initTracerProperty("JAEGER_AUTH_TOKEN", jaeger.authToken, token -> token);
        initTracerProperty("JAEGER_USER", jaeger.user, user -> user);
        initTracerProperty("JAEGER_PASSWORD", jaeger.password, pw -> pw);
        initTracerProperty("JAEGER_AGENT_HOST", jaeger.agentHostPort, address -> address.getHostName());
        initTracerProperty("JAEGER_AGENT_PORT", jaeger.agentHostPort, address -> String.valueOf(address.getPort()));
        initTracerProperty("JAEGER_REPORTER_LOG_SPANS", jaeger.reporterLogSpans, log -> log.toString());
        initTracerProperty("JAEGER_REPORTER_MAX_QUEUE_SIZE", jaeger.reporterMaxQueueSize, size -> size.toString());
        initTracerProperty("JAEGER_REPORTER_FLUSH_INTERVAL", jaeger.reporterFlushInterval,
                duration -> String.valueOf(duration.toMillis()));
        initTracerProperty("JAEGER_SAMPLER_TYPE", jaeger.samplerType, type -> type);
        initTracerProperty("JAEGER_SAMPLER_PARAM", jaeger.samplerParam, param -> param.toString());
        initTracerProperty("JAEGER_SAMPLER_MANAGER_HOST_PORT", jaeger.samplerManagerHostPort, hostPort -> hostPort.toString());
        initTracerProperty("JAEGER_SERVICE_NAME", jaeger.serviceName, name -> name);
        initTracerProperty("JAEGER_TAGS", jaeger.tags, tags -> tags.toString());
        initTracerProperty("JAEGER_PROPAGATION", jaeger.propagation, format -> format.toString());
        initTracerProperty("JAEGER_SENDER_FACTORY", jaeger.senderFactory, sender -> sender);

        // FIXME calling a package-private method
        //        quarkusTracer.setLogTraceContext(jaeger.logTraceContext);
        Method setLogTraceContext = QuarkusJaegerTracer.class.getDeclaredMethod("setLogTraceContext", boolean.class);
        setLogTraceContext.setAccessible(true);
        setLogTraceContext.invoke(quarkusTracer, jaeger.logTraceContext);
    }

    private <T> void initTracerProperty(String property, Optional<T> value, Function<T, String> accessor) {
        if (value.isPresent()) {
            System.setProperty(property, accessor.apply(value.get()));
        }
    }

}
