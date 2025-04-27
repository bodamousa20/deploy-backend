package com.Dev.Pal.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JobResponse {
    @JsonProperty("scraped-jobs")
    private int scrapedJobs;
    private List<JobResult> result;

    public int getScrapedJobs() {
        return scrapedJobs;
    }

    public void setScrapedJobs(int scrapedJobs) {
        this.scrapedJobs = scrapedJobs;
    }

    public List<JobResult> getResult() {
        return result;
    }

    public void setResult(List<JobResult> result) {
        this.result = result;
    }
}
