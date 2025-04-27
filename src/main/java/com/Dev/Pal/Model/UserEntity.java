package com.Dev.Pal.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Random;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class UserEntity {

    @Id
    private Long id = generateRandomId();


 //   @NotEmpty(message = "Name is Required")
  //  @Size(min = 4)
    private String name ;

 // @NotEmpty(message = "career name is Required")
  private String careerName ;

  // @Email(message = "Email is invalid")
    //@Column(unique = true, nullable = false)
    private String email ;

     //@NotEmpty
    private String location ;

  //@NotEmpty(message = "Github is invalid or not exist")
    private String github ;

  //@NotEmpty(message = "linkedin is invalid or not exist")
    private String linkedin ;


    private List<String> collegeName  ;

    private List<String>skills;
    private List<String>projects ;
    private Boolean isVerified = Boolean.FALSE ;

    @ManyToMany
    @JoinTable(
            name = "user_saved_jobs",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "job_id")
    )
    @JsonIgnore
    private List<Job> savedJobs ;

    @OneToOne(cascade = CascadeType.ALL)  // Ensures image is saved properly
    @JoinColumn(name = "cv_image", referencedColumnName = "id")
    private Image userCvImage ;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_image_id", referencedColumnName = "id")
    private Image UserImage ;



    private String secret_key ;


    private static Long generateRandomId() {
        Random random = new Random();
        return (long) (100000 + random.nextInt(900000));

    }



}
