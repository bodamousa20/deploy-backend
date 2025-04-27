package com.Dev.Pal.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @ManyToMany(mappedBy = "savedJobs")
    private Set<UserEntity> users = new HashSet<>();

}
