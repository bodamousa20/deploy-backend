package com.Dev.Pal.Model;

import lombok.*;

import java.util.List;



@Getter
@Data
public class NlpPojo {
    private Data data;

    public void setData(Data data) {
        this.data = data;
    }
    @lombok.Data
    public static class Data {
        private String career_name;
        private List<String> education;
        private String email;
        private String github_username;
        private String linkedin_url ;
        private String location;
        private String name;
        private List<String> projects;
        private List<String> skills;

}
    }
