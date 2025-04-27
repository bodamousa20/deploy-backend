package com.Dev.Pal.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveJobRequest {

    private String job_title ;
    private String job_type ;
    private String companyName ;
    private String location ;
    private String datePosted ;
    private String jobLink ;
    private String image_url ;

    private List<String> categories ;
    private String experience ;
    private String level ;
}
