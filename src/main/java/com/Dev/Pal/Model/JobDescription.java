package com.Dev.Pal.Model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class JobDescription {
    private List<String> categories;
    private String experience;
    private String level;

}
