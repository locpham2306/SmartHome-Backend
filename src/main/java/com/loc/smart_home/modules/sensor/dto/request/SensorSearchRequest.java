package com.loc.smart_home.modules.sensor.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SensorSearchRequest {

    @NotBlank(message = "field is required")
    @Pattern(
            regexp = "all|time|Light|Temperature|Humidity",
            message = "field must be all, time, Light, Temperature or Humidity"
    )
    private String field = "all";

    @Size(
            max = 100,
            message = "keywords must not exceed 100 characters"
    )
    private String keywords;
}