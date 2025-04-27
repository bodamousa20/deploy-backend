package com.Dev.Pal.Dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateUserRequest {
        //Alter The user to update the entire data of resume
    @NotEmpty
    private String career_name;

    @NotEmpty
    private List<String> education;

    @NotEmpty
    private String email;

    @NotEmpty
    private String github_username;

    @NotEmpty
    private String linkedin_url ;

    @NotEmpty
    private String location;

    @NotEmpty
    private String name;

    @NotEmpty
    private List<String> projects;

    @NotEmpty
    private List<String> skills;





}
