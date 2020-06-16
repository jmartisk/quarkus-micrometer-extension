package dev.ebullient.micrometer.runtime.binder.microprofile;

import javax.annotation.Priority;
import javax.enterprise.inject.Intercepted;
import javax.enterprise.inject.spi.Bean;
import javax.interceptor.*;

import org.eclipse.microprofile.metrics.annotation.Counted;

import io.micrometer.core.instrument.MeterRegistry;

@Counted
@Interceptor
@Priority(Interceptor.Priority.LIBRARY_BEFORE + 10)
public class CountedInterceptor {

    // Micrometer meter registry
    final MeterRegistry registry;
    final Bean<?> bean;

    CountedInterceptor(@Intercepted Bean<?> bean, MeterRegistry registry) {
        this.registry = registry;
        this.bean = bean;
    }

    @AroundConstruct
    Object countedConstructor(InvocationContext context) throws Exception {
        System.out.println("### context: " + context + ", constructor: " + context.getConstructor() + ", data: "
                + context.getContextData());
        return context.proceed();
    }

    @AroundInvoke
    Object countedMethod(InvocationContext context) throws Exception {
        System.out.println(
                "### context: " + context + ", method: " + context.getMethod() + ", data: " + context.getContextData());
        return context.proceed();
    }

    @AroundTimeout
    Object countedTimeout(InvocationContext context) throws Exception {
        System.out.println(
                "### context: " + context + ", method: " + context.getMethod() + ", data: " + context.getContextData());
        return context.proceed();
    }
}
