package ru.practicum.services.interfaces;

import ru.practicum.dto.UserDto;
import ru.practicum.dto.news.NewUserRequest;

import java.util.List;

public interface UserService {

    List<UserDto> findAll(List<Long> ids, int from, int size);

    UserDto save(NewUserRequest userCreateDto);

    void deleteById(Long userId);

}