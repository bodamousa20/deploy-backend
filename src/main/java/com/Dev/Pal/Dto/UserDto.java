package com.Dev.Pal.Dto;

import com.Dev.Pal.Model.Image;
import com.Dev.Pal.Model.Job;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UserDto {


    private Long id ;

    private String name ;

    private String careerName ;

    private String email ;

    private String location ;


    private String github ;

    private String linkedin ;
    private List<String> collegeName  ;
    private List<String>skills;

    private List<JobDto> savedJobs ;

    private ImageDto userCvImage ;

    private ImageDto UserImage ;

}
