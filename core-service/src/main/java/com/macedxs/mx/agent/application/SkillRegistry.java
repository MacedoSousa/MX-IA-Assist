package com.macedxs.mx.agent.application;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class SkillRegistry {

    private final Map<String, Skill> skills = new LinkedHashMap<>();

    public void register(Skill skill) {
        if (skill == null || skill.definition() == null) {
            throw new IllegalArgumentException("Skill is required");
        }

        String key = normalize(skill.definition().name());
        if (skills.containsKey(key)) {
            throw new IllegalArgumentException("Skill already registered: " + skill.definition().name());
        }
        skills.put(key, skill);
    }

    public Skill getRequired(String name) {
        Skill skill = skills.get(normalize(name));
        if (skill == null) {
            throw new IllegalArgumentException("Skill not registered: " + name);
        }
        return skill;
    }

    public Collection<Skill> all() {
        return Collections.unmodifiableCollection(skills.values());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Skill name is required");
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
