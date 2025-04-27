package com.Dev.Pal.Services;

import com.Dev.Pal.Model.JobResponse;
import com.Dev.Pal.Model.JobResult;
import com.Dev.Pal.Model.UserEntity;
import com.Dev.Pal.Repositary.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class EmailServices {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    @Async
    public void sendHtmlEmail(String receiver, String link) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("devpal.team@gmail.com");
        helper.setTo(receiver);
        helper.setSubject("Welcome to DevPal");

        String emailContent = readTemplate("/templates/Devpal_Email.html");
        if (emailContent == null) return;

        emailContent = emailContent.replace("{{link}}", link);
        helper.setText(emailContent, true);
        helper.addInline("dynamic-image", new ClassPathResource("static/Devpal_logo.PNG"));

        mailSender.send(message);
    }

    @Async
    public void sendCronJobEmailWithJobs(String receiver, String link, List<JobResult> jobs) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setFrom("devpal.team@gmail.com");
        helper.setTo(receiver);
        helper.setSubject("Job Opportunities from DevPal");

        String emailContent = readTemplate("/templates/DevpalCron.html");
        String jobCardTemplate = readTemplate("/templates/JobCard.html");

        if (emailContent == null || jobCardTemplate == null) return;

        StringBuilder jobDetails = new StringBuilder();
        for (JobResult job : jobs) {
            String jobHtml = jobCardTemplate
                    .replace("{{image_url}}", job.getImage_url())
                    .replace("{{job_title}}", job.getJob_title())
                    .replace("{{location}}", job.getLocation())
                    .replace("{{job_url}}", job.getJob_url());

            jobDetails.append(jobHtml);
        }

        emailContent = emailContent.replace("{{link}}", link);
        emailContent = emailContent.replace("{{job_count}}", String.valueOf(jobs.size()));
        emailContent = emailContent.replace("{{jobs_list}}", jobDetails.toString());

        helper.setText(emailContent, true);
        helper.addInline("dynamic-image", new ClassPathResource("static/Devpal_logo.PNG"));

        mailSender.send(message);
    }
    //only verfied
    @Scheduled(cron = "0 * * * * *")
    public void sendEmailCronJobs() throws IOException, MessagingException {
        List<UserEntity> userEntities = userRepository.findAll();

        for (UserEntity currentUser : userEntities) {
           Boolean currentUserIsVerified =  currentUser.getIsVerified() ;
           if(!currentUserIsVerified) continue;

           String careerName = currentUser.getCareerName();
           String scrapedJobs = extracted(careerName);

            if (scrapedJobs == null) continue;

            ObjectMapper objectMapper = new ObjectMapper();
            JobResponse jobResponse = objectMapper.readValue(scrapedJobs, JobResponse.class);

            if (jobResponse.getResult() == null || jobResponse.getResult().isEmpty()) {
                System.out.println("No jobs found for " + currentUser.getEmail());
                continue;
            }

            sendCronJobEmailWithJobs(currentUser.getEmail(), "https://devpal-alpha.vercel.app/start", jobResponse.getResult());
        }
    }

    private String extracted(String careerName) {
        System.out.println("starting cron job for: " + careerName);
        String nlpUrl = "https://scrappingall-production.up.railway.app/scrape-jobs";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("query", careerName);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(nlpUrl, HttpMethod.GET, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                System.err.println("Job scraping failed with status: " + response.getStatusCode());
            }

        } catch (Exception e) {
            System.err.println("Error while fetching jobs: " + e.getMessage());
        }
        return null;
    }

    private String readTemplate(String path) {
        try (InputStream inputStream = getClass().getResourceAsStream(path)) {
            if (inputStream == null) {
                System.err.println("Template not found: " + path);
                return null;
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Failed to load template: " + path + " -> " + e.getMessage());
            return null;
        }
    }
}
