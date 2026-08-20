package com.macedxs.mx.agent.skill.development;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ToolCallParserTest {

    private final ToolCallParser parser = new ToolCallParser(new ObjectMapper());

    @Test
    void shouldParseOnlyTheExplicitMxMarker() {
        var result = parser.parse("[MX_TOOL_CALL]{\"toolName\":\"workspace.list\",\"arguments\":{}}[/MX_TOOL_CALL]");

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().toolName()).isEqualTo("workspace.list");
        assertThat(result.orElseThrow().arguments()).isEmpty();
    }

    @Test
    void shouldRejectFreeFormJsonAndMalformedCalls() {
        assertThat(parser.parse("{\"toolName\":\"workspace.list\",\"arguments\":{}}"))
                .isEmpty();
        assertThat(parser.parse("[MX_TOOL_CALL]{not-json}[/MX_TOOL_CALL]"))
                .isEmpty();
        assertThat(parser.parse("[MX_TOOL_CALL]{\"toolName\":\"workspace.list\"}[/MX_TOOL_CALL]"))
                .isEmpty();
    }
}
