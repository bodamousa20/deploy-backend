package com.Dev.Pal.Dto;

import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

import java.util.List;

@Data
public class JobDto {
    private Long id ;
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
