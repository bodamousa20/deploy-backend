package com.Dev.Pal.Dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;



@Data
public class OtpValidationRequest {
    // getters and setters
    private String code;
    private Long id;

}
