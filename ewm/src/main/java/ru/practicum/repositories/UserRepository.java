package ru.practicum.repositories;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.models.User;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User getUserByName(String name);

    @Query("SELECT u from User u where u.id in :ids")
    List<User> findAllByIds(List<Long> ids, PageRequest pageRequest);
}
