package dev.ebullient.micrometer.deployment.binder.mpmetrics;

import io.quarkus.arc.deployment.BeanDefiningAnnotationBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.IndexView;
import org.jboss.logging.Logger;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.gizmo.ClassOutput;

/**
 * Create beans to handle <code>@Counted</code> annotations.
 *
 * It is ok to import and use classes that reference MP Metrics classes.
 *
 * A counter will be created for methods (or constructors) annotated with {@literal @}Counted.
 * Each time the method is invoked, the counter will be incremented.
 *
 * If a class is annotated with {@literal @}Counted, counters will be created for each method
 * and the constructor. Each time a method is invoked, the related counter will be incremented.
 */
public class CountedAnnotationHandler {
    private static final Logger log = Logger.getLogger(CountedAnnotationHandler.class);

    static void processCountedAnnotations(IndexView index, ClassOutput classOutput) {
        // @Counted applies to classes and methods
        for (AnnotationInstance annotation : index.getAnnotations(MetricDotNames.COUNTED_ANNOTATION)) {
            AnnotationTarget target = annotation.target();
            if (target.kind() == AnnotationTarget.Kind.METHOD) {
                log.debugf("## METHOD: %s, %s", annotation, target);
            } else if (target.kind() == AnnotationTarget.Kind.CLASS) {
                log.debugf("## CLASS: %s, %s", annotation, target);
            }
        }
    }

}
