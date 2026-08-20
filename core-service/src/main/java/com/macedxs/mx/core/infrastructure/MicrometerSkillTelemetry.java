package com.macedxs.mx.core.infrastructure;

import com.macedxs.mx.core.application.SkillTelemetry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MicrometerSkillTelemetry implements SkillTelemetry {

    private final MeterRegistry meterRegistry;

    public MicrometerSkillTelemetry(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void routed(String skillName, double confidence) {
        counter("mx.skill.route", skillName, "selected").increment();
    }

    @Override
    public void completed(String skillName) {
        counter("mx.skill.execution", skillName, "completed").increment();
    }

    @Override
    public void failed(String skillName, Throwable failure) {
        counter("mx.skill.execution", skillName, "failed").increment();
    }

    private Counter counter(String name, String skillName, String outcome) {
        return Counter.builder(name)
                .description("MX skill lifecycle events")
                .tag("skill", skillName)
                .tag("outcome", outcome)
                .register(meterRegistry);
    }
}
