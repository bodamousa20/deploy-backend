package com.Dev.Pal.Repositary;

import com.Dev.Pal.Model.EmailToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTokenRepository extends JpaRepository<EmailToken,String> {


    EmailToken findByEmail(String email);
}

