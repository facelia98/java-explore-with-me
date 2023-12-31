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
import ru.practicum.repositories.UserRepository;
import ru.practicum.services.interfaces.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> findAll(List<Long> ids, int from, int size) {
        if (ids == null || ids.isEmpty()) {
            return userRepository.findAll(PageRequest.of(from / size, size))
                    .stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
        return userRepository.findAllByIds(ids, PageRequest.of(from / size, size))
                .stream()
                .filter(user -> ids.contains(user.getId()))
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDto save(NewUserRequest userCreateDto) {
        if (userRepository.getUserByName(userCreateDto.getName()) != null) {
            log.error("Duplicate user name!");
            throw new Conflict("Duplicate user name!");
        }
        checkEmail(userCreateDto.getEmail());
        return UserMapper.toUserDto(userRepository.save(UserMapper.toUser(userCreateDto)));
    }

    @Override
    @Transactional
    public void deleteById(Long userId) {
        if (!userRepository.existsById(userId)) {
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