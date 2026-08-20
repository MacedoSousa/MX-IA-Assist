package com.macedxs.mx.tool.controller;

import com.macedxs.mx.tool.dto.ToolDTO;
import com.macedxs.mx.tool.entity.ToolEntity;
import com.macedxs.mx.tool.service.ToolService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tools")
public class ToolController {

    private final ToolService toolService;

    public ToolController(ToolService toolService) {
        this.toolService = toolService;
    }

    @GetMapping
    public ResponseEntity<List<ToolDTO>> listAvailableTools() {
        List<ToolDTO> tools = toolService.getAvailableTools()
                .stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(tools);
    }

    private ToolDTO entityToDto(ToolEntity entity) {
        return new ToolDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getEnabled()
        );
    }
}
