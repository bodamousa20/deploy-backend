package com.Dev.Pal.Repositary;

import com.Dev.Pal.Model.Job;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job,Long> {
}
