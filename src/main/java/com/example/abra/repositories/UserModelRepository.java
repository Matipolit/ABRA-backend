package com.example.abra.repositories;

import com.example.abra.models.UserModel;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserModelRepository extends JpaRepository<UserModel, String> {
    Optional<UserModel> findByLogin(String login);
    boolean existsByLogin(String login);
}
