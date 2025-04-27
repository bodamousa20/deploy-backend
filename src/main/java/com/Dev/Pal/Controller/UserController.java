package com.Dev.Pal.Controller;


import com.Dev.Pal.Dto.OtpValidationRequest;
import com.Dev.Pal.Dto.SaveJobRequest;
import com.Dev.Pal.Dto.UpdateUserRequest;
import com.Dev.Pal.Dto.UserDto;
import com.Dev.Pal.Exception.ResourceNotFoundException;
import com.Dev.Pal.Model.Image;
import com.Dev.Pal.Model.Job;
import com.Dev.Pal.Model.UserEntity;
import com.Dev.Pal.Repositary.ImageRepository;
import com.Dev.Pal.Repositary.UserRepository;
import com.Dev.Pal.Response.ApiResponse;
import com.Dev.Pal.Services.OtpServices;
import com.Dev.Pal.Services.UserServices;
import com.Dev.Pal.Services.YouTubeService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/user")
@AllArgsConstructor
public class UserController {
    private final UserServices userServices ;
    private  final OtpServices otpServices ;
    private final YouTubeService youTubeService ;
    private  final ImageRepository imageRepository ;
    private  final UserRepository userRepository ;
    private final JwtDecoder jwtDecoder ;

    @PostMapping("/create-user")
    public ResponseEntity<ApiResponse> saveUser(

            @RequestParam("cv") MultipartFile Cv
    ) {
        try {

            return ResponseEntity.ok(new ApiResponse("success",
                    userServices.cvExtractionNlpModel(Cv)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(e.getMessage(), null));
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse>getUser(@PathVariable Long id , @RequestHeader("Authorization") String authorizationHeader ){
        try {
            Long userIdFromToken = otpServices.getUserIdFromAuthorizationHeader(authorizationHeader);

            if (!userIdFromToken.equals(id)) {
                throw new AccessDeniedException("Unauthorized access");
            }

            UserEntity user =   userServices.getUser(id);
          userServices.convertUserToDto(user);
            return ResponseEntity.ok(new ApiResponse("success", userServices.convertUserToDto(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(),null));

        }
    }


   @PostMapping("/saveJob")
    public ResponseEntity<ApiResponse> updateUser(@ModelAttribute SaveJobRequest request , @RequestParam("userId") Long id, @RequestHeader("Authorization") String authorizationHeader){
        try {
            Long userIdFromToken = otpServices.getUserIdFromAuthorizationHeader(authorizationHeader);
            if (!userIdFromToken.equals(id)) {
                throw new AccessDeniedException("Unauthorized access");
            }
            Job job = userServices.saveJobByUserId(request,id);
            return ResponseEntity.ok(new ApiResponse("success",userServices.convertToDtoMethod(job) ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(),null));

        }
    }


    @PutMapping("/update-user")
    public ResponseEntity<ApiResponse> updateUser(@Valid @ModelAttribute UpdateUserRequest request , @RequestParam("userId") Long id, @RequestHeader("Authorization") String authorizationHeader){
        try {
            Long userIdFromToken = otpServices.getUserIdFromAuthorizationHeader(authorizationHeader);
            if (!userIdFromToken.equals(id)) {
                throw new AccessDeniedException("Unauthorized access");
            }

            UserDto user = userServices.updateUser(id,request);
            return ResponseEntity.ok(new ApiResponse("Updated Successfully",user ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(e.getMessage(),null));

        }
    }


    @GetMapping("/jobs/{userId}")
    public ResponseEntity<ApiResponse>getUserJobs(@PathVariable Long userId,@RequestHeader("Authorization") String authorizationHeader){
        try {
            Long userIdFromToken = otpServices.getUserIdFromAuthorizationHeader(authorizationHeader);

            if (!userIdFromToken.equals(userId)) {
                throw new AccessDeniedException("Unauthorized access");
            }
            return ResponseEntity.ok(new ApiResponse("success",userServices.getUserJobs(userId)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("failed",null));

        }
    }

    @DeleteMapping("/delete-job")
    public ResponseEntity<ApiResponse>deleteJob(@RequestParam("userId") Long userId,@RequestParam("jobId") Long jobId  ){
        try {

            userServices.deleteUserJobs(userId,jobId);
            return ResponseEntity.ok(new ApiResponse("deleted successfully",null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse("failed",null));

        }
    }
        //GET METHOD FOR RETUNR CV IAMGE FROM DB

    @GetMapping("/download-img/{id}")
    public ResponseEntity<byte[]> getCvImage(@PathVariable Long id) {
        Image image = imageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found"));

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(image.getImage());
    }



    @GetMapping("/setup-qrcode")
    public ResponseEntity<ApiResponse> setup2FA(@RequestParam("id") Long id ) throws Exception {
        ApiResponse apiResponse = new ApiResponse() ;
        apiResponse.setMessage("Success");
        apiResponse.setData(otpServices.generateSecretKey(id));
        return ResponseEntity.ok(apiResponse) ;

    }


    @PutMapping("/check-email")
    public ResponseEntity<ApiResponse>validateEmail(@RequestParam("id")Long id ,@RequestParam("email")String newEmail){
       UserEntity user =  userRepository.findById(id).orElseThrow(()->new RuntimeException("Error has happend")) ;
       if(user == null){
           return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                   .body(
                           new ApiResponse("Failure this user is not found " , null)
                   );
       }
       else{
           user.setEmail(newEmail);
           userRepository.save(user);
         return  ResponseEntity.ok (new ApiResponse(" Email Updated Successfully " , newEmail));

       }

    }




    @PostMapping("/validate")
    public ResponseEntity<ApiResponse> validateOtp(@RequestBody OtpValidationRequest request) {
        try {
            Map<String, Object> result = otpServices.validateQrCode(request.getCode(), request.getId());
            return ResponseEntity.ok(new ApiResponse("success", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse(e.getMessage(), null));
        }
    }






/*
    @PostMapping("/validateByEmail")  // المسار الفرعي لعمل validate للكود
    public ResponseEntity<Map<String, Object>> validateOtpByEmail(@RequestParam("code") String code, @RequestParam String email) {
        try {
            Map<String, Object> response = otpServices.validateQrCodeByEmail(code, email ) ;
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));  // لو حصل خطأ في التحقق
        }
    }

*/


}
