package com.Dev.Pal.Repositary;

import com.Dev.Pal.Model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    UserEntity findByEmail(String email);

   // UserSecretProjection findSecretByEmail(String email);

}
