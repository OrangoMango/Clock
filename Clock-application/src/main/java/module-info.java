// File managed by WebFX (DO NOT EDIT MANUALLY)

module Clock.application {

    // Direct dependencies modules
    requires java.base;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.media;
    requires webfx.platform.resource;

    // Exported packages
    exports com.orangomango.clock;

    // Resources packages
    opens audio;
    opens images;

    // Provided services
    provides javafx.application.Application with com.orangomango.clock.Clock;

}