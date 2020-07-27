package com.github.skystardust.ultracore.core.configuration.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

public final class ConfigurationFormatConverter {
    private String content;

    private ConfigurationFormatConverter(String content) {
        this.content = content;
    }

    public static ConfigurationFormatConverter from(String jsonContent) {
        return new ConfigurationFormatConverter(jsonContent);
    }

    public String toJSON() throws JsonProcessingException {
        JsonNode jsonNode = new YAMLMapper().readTree(content);
        return jsonNode.toPrettyString();
    }

    public String toYAML() throws JsonProcessingException {
        JsonNode jsonNodeTree = new ObjectMapper().readTree(content);
        return new YAMLMapper().writeValueAsString(jsonNodeTree);
    }
}

