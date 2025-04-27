package com.Dev.Pal.Services;


import com.Dev.Pal.Dto.JobDto;
import com.Dev.Pal.Dto.SaveJobRequest;
import com.Dev.Pal.Dto.UpdateUserRequest;
import com.Dev.Pal.Dto.UserDto;
import com.Dev.Pal.Exception.ResourceNotFoundException;
import com.Dev.Pal.Model.Image;
import com.Dev.Pal.Model.Job;
import com.Dev.Pal.Model.NlpPojo;
import com.Dev.Pal.Model.UserEntity;
import com.Dev.Pal.Repositary.ImageRepository;
import com.Dev.Pal.Repositary.JobRepository;
import com.Dev.Pal.Repositary.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;

import java.util.HashMap;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServices  {


    private final UserRepository userRepository;

    private final JobRepository jobRepository;
    private final RestTemplate restTemplate;
    private  final OtpServices otpServices ;

    private final ImageRepository imageRepository;

    private final ModelMapper modelMapper;
    private final EmailServices emailService; // ✅ Use EmailService instead of UserController


    @Transactional
    public HashMap<String, Object> cvExtractionNlpModel(MultipartFile cv) throws IOException {
        // call the nlp api from abdo after ahmed finish
    String NlpUrl = "https://scrappingall-production.up.railway.app/process-resume";
    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    body.add("file",cv.getResource());
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

    ResponseEntity<String> response = restTemplate.exchange(NlpUrl, HttpMethod.POST, requestEntity, String.class);
        System.out.println(response);

        HashMap<String, Object> result =  saveUserOrRetrieve ( response , cv);
    //get the result of nlp + file and send to saveUserOrRetrieve fun

        //get result and send to controller

        return  result ;
    }



   public void validateGithub (UserEntity user , String github) {

       String githubUrl = "https://api.github.com/users/" + github;
       HttpHeaders headers = new HttpHeaders();


       ResponseEntity<String> response = null;
       try {
           // Make the HTTP request
           response = restTemplate.getForEntity(githubUrl, String.class);

           // Check if the request was successful (2xx response code)
           if (response.getStatusCode().is2xxSuccessful()) {
               user.setGithub(github);
               System.out.println("GitHub user exists: " + github);
           } else {
               user.setGithub("");
               System.out.println("GitHub user not found: " + github);
           }
       } catch (RestClientException e) {
           // Handle error during HTTP request
           System.err.println("Error occurred while making the request to GitHub API: " + e.getMessage());
           user.setGithub("");
       }
   }


    // finedTuned Result from NLP Model ( Non Rest Method )
    @Transactional
    public HashMap<String, Object> saveUserOrRetrieve(ResponseEntity<String> response, MultipartFile cv) throws IOException {
        String responseBody = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        NlpPojo pojo = objectMapper.readValue(responseBody, NlpPojo.class);
        NlpPojo.Data nlp = pojo.getData();
        byte[] cvImg = convertPdfToImage(cv.getBytes());
        HashMap<String, Object> result = new HashMap<>();

        UserEntity user = userRepository.findByEmail(nlp.getEmail());
        if (user == null) {
            UserEntity newUser = new UserEntity();
            try {

                newUser.setName(nlp.getName());
                newUser.setEmail(nlp.getEmail());
                newUser.setCollegeName(nlp.getEducation());
                newUser.setLocation(nlp.getLocation());
                newUser.setCareerName(nlp.getCareer_name());
                newUser.setSkills(nlp.getSkills());
                newUser.setLinkedin(nlp.getLinkedin_url());
                newUser.setProjects(nlp.getProjects());

                newUser.setGithub(
                        nlp.getGithub_username() !=null ?  nlp.getGithub_username() : ""
                );

                // Create and save the Image entity first
                Image cvImage = new Image();
                cvImage.setFileName(cv.getOriginalFilename());
                cvImage.setFileType(cv.getContentType());
                cvImage.setImage(cvImg);
                imageRepository.save(cvImage) ;
                String Link = "http://localhost:8080/api/v1/user/download-img/" +cvImage.getId() ;
                cvImage.setDownloadUrl(Link);
                Image savedCvImage = imageRepository.save(cvImage); // Save and get the persisted entity

                newUser.setUserCvImage(savedCvImage); // Associate the saved Image entity

                UserEntity savedUser = userRepository.save(newUser);

                //return convertUserToDto(savedUser);
                result.put("email", savedUser.getEmail());
                result.put("id", savedUser.getId().toString());
                return result;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            user.setName(nlp.getName());
            user.setEmail(nlp.getEmail());
            user.setCollegeName(nlp.getEducation());
            user.setLocation(nlp.getLocation());
            user.setCareerName(nlp.getCareer_name());
            user.setSkills(nlp.getSkills());
            result.put("email", user.getEmail());
            result.put("id", user.getId().toString());
            return result ;
           // return convertUserToDto(userRepository.save(user));

        }
    }

    public UserEntity getUser(Long id) {
        return userRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("the user with id is not found "));
    }

    public Job saveJobByUserId(SaveJobRequest request, Long id ) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        try {
            Job newJob = new Job();
            newJob.setJobLink(request.getJobLink());
            newJob.setDatePosted(request.getDatePosted());
            newJob.setImage_url(request.getImage_url());
            newJob.setExperience(request.getExperience());
            newJob.setCategories(request.getCategories());
            newJob.setJob_type(request.getJob_type());
            newJob.setLevel(request.getLevel());
            newJob.setJob_title(request.getJob_title());
            newJob.setLocation(request.getLocation());
            newJob.setCompanyName(request.getCompanyName());

            Job savedJob = jobRepository.save(newJob);
            user.getSavedJobs().add(savedJob);
            userRepository.save(user);
            return savedJob;
        } catch (Exception e) {
            throw new RuntimeException("Error saving job: " + e.getMessage());
        }
    }
// single job

    public JobDto getSavedJob(Long jobId) {
     Job job =  jobRepository.findById(jobId).orElseThrow(()->new ResourceNotFoundException("the user is not found "));
       return convertToDtoMethod(job);
    }

    //convert job to dto
    public JobDto convertToDtoMethod(Job job) {
        JobDto jobDto = modelMapper.map(job, JobDto.class);
        jobDto.setImage_url(job.getImage_url()); // Explicitly setting it
        return jobDto;
    }

    public UserDto convertUserToDto(UserEntity user) {
   List<Job> jobs =   user.getSavedJobs();
   if(jobs == null){
      return modelMapper.map(user, UserDto.class);

   }
   else {
       List<JobDto> jobDtos = jobs.stream().map(this::convertToDtoMethod).toList();

       UserDto userDto = modelMapper.map(user, UserDto.class);
       userDto.setSavedJobs(jobDtos);

       return userDto;
   }
    }
    //get User Jobs
    public List<JobDto> getUserJobs(Long userId) {
        UserEntity user =  userRepository.findById(userId).orElseThrow(()->new ResourceNotFoundException("The user is not found"));
        List<Job> jobs = user.getSavedJobs();

        return jobs.stream().map(this::convertToDtoMethod).toList();
    }

    public void deleteUserJobs(Long userId, Long jobId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("The user is not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("The job is not found"));

        if (user.getSavedJobs().contains(job)) {
            user.getSavedJobs().remove(job); // Remove the job from the user's saved jobs
            userRepository.save(user); // Save the updated user entity
        } else {
            throw new ResourceNotFoundException("The job is not found in the user's saved jobs");
        }
    }

    @Transactional
    public UserDto updateUser(Long userID, UpdateUserRequest updateUserRequest){
        UserEntity user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException("User with Id " + userID + " is not found"));

        user.setCareerName(updateUserRequest.getCareer_name());
        user.setEmail(updateUserRequest.getEmail());
        user.setLocation(updateUserRequest.getLocation());
        user.setGithub(updateUserRequest.getGithub_username());
        user.setLinkedin(updateUserRequest.getLinkedin_url());
        user.setName(updateUserRequest.getName());
        user.setSkills(updateUserRequest.getSkills());
        user.setCollegeName(updateUserRequest.getEducation());
        user.setProjects(updateUserRequest.getProjects());
        user.setIsVerified(Boolean.TRUE);
        return convertUserToDto(userRepository.save(user));
    }

    @Transactional
    public static byte[] convertPdfToImage(byte[] pdfBytes) throws IOException {
        try (InputStream inputStream = new ByteArrayInputStream(pdfBytes);
             PDDocument document = PDDocument.load(inputStream)) {

            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "PNG", outputStream);

            return outputStream.toByteArray();
        }
    }









}

