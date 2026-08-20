package com.macedxs.mx.core.application;

public interface SkillTelemetry {

    void routed(String skillName, double confidence);

    void completed(String skillName);

    void failed(String skillName, Throwable failure);

    static SkillTelemetry noop() {
        return new SkillTelemetry() {
            @Override
            public void routed(String skillName, double confidence) {
            }

            @Override
            public void completed(String skillName) {
            }

            @Override
            public void failed(String skillName, Throwable failure) {
            }
        };
    }
}
