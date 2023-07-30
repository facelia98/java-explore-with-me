package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User getUserByName(String name);
}
