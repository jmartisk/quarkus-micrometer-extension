package dev.ebullient.it.micrometer.mpmetrics;

import org.eclipse.microprofile.metrics.annotation.Counted;

@Counted
public class CountedResource {

    CountedResource() {
    }

    public void called() {
    }
}
