package com.agro.user.infrastructure.persistence;

import com.agro.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}
