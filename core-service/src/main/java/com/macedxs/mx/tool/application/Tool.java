package com.macedxs.mx.tool.application;

public interface Tool {

    ToolDefinition definition();

    ToolResult execute(ToolRequest request, ToolExecutionContext context);
}
