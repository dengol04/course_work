package ru.golubov.ionet;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ChatCompletionResponse(
        @JsonProperty("choices") List<Choice> choices
) {
}
