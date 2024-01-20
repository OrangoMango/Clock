// File managed by WebFX (DO NOT EDIT MANUALLY)

module Clock.application {

    // Direct dependencies modules
    requires javafx.graphics;

    // Exported packages
    exports com.orangomango.clock;

    // Resources packages
    opens audio;
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.clock.Clock;

}