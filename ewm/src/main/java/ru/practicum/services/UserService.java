package ru.practicum.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.news.NewUserRequest;
import ru.practicum.exceptions.Conflict;
import ru.practicum.exceptions.NotFoundException;
import ru.practicum.exceptions.ValidationException;
import ru.practicum.mappers.UserMapper;
import ru.practicum.models.User;
import ru.practicum.repositories.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public List<UserDto> findAll(List<Long> ids, int from, int size) {
        return userRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto save(NewUserRequest userCreateDto) {
        if (userRepository.getUserByName(userCreateDto.getName()) != null) {
            log.error("Duplicate user name!");
            throw new Conflict("Duplicate user name!");
        }

        checkEmail(userCreateDto.getEmail());
        User user = UserMapper.toUser(userCreateDto);
        return UserMapper.toUserDto(userRepository.save(user));
    }

    public void deleteById(Long userId) {
        log.info("DELETE user request received for id = {}", userId);
        if (userRepository.getById(userId) == null) {
            log.warn("User not found for id = {}", userId);
            throw new NotFoundException("User not found for id = " + userId);
        }
        userRepository.deleteById(userId);
    }

    private void checkEmail(String email) {
        if (!email.contains("@")) {
            log.error("Email validation error: invalid format");
            throw new ValidationException("Invalid email format");
        } else if (email.isBlank()) {
            log.error("Email validation error: empty");
            throw new ValidationException("Email is empty");
        }
    }
}
