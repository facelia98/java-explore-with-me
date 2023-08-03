package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.*;
import ru.practicum.dto.news.NewCategoryDto;
import ru.practicum.dto.news.NewCompilationDto;
import ru.practicum.dto.news.NewUserRequest;
import ru.practicum.dto.updates.UpdateCompilationRequest;
import ru.practicum.dto.updates.UpdateEventRequest;
import ru.practicum.enums.Status;
import ru.practicum.services.interfaces.CategoryService;
import ru.practicum.services.interfaces.CompilationService;
import ru.practicum.services.interfaces.EventService;
import ru.practicum.services.interfaces.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminController {
    private final CategoryService categoryService;
    private final EventService eventService;
    private final UserService userService;
    private final CompilationService compilationService;

    @PostMapping("/categories")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CategoryDto addNewCategory(@RequestBody @Valid NewCategoryDto dto) {
        log.info("POST Category request received to admin endpoint [/categories]");
        return categoryService.addNewCategory(dto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        log.info("DELETE Category request received to admin endpoint [/categories] with id = {}", catId);
        categoryService.deleteById(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @RequestBody @Valid CategoryDto dto) {
        log.info("PATCH Category request received to admin endpoint [/categories] with id = {}", catId);
        return categoryService.updateById(catId, dto);
    }

    @GetMapping("/events")
    public List<EventFullDto> getEventsAdmin(@RequestParam(name = "users", required = false) List<Long> users,
                                             @RequestParam(name = "states", required = false) List<Status> states,
                                             @RequestParam(name = "categories", required = false) List<Long> categories,
                                             @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                             @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                             @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                             @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        log.info("GET Events request received to admin endpoint [/events]");
        return eventService.getEventsAdmin(GetEventParametersDto.builder().rangeEnd(rangeEnd).rangeStart(rangeStart)
                .categories(categories).states(states).users(users).from(from).size(size).build());
    }

    @PatchMapping("/events/{eventId}")
    EventFullDto update(@PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventRequest updateEventRequest) {
        log.info("PATCH Event request received to admin endpoint [/events] for eventId = {}", eventId);
        return eventService.updateEventByAdmin(eventId, updateEventRequest);
    }

    @GetMapping("/users")
    public List<UserDto> findAll(@RequestParam(required = false) List<Long> ids,
                                 @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                 @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("GET Users request received to admin endpoint [/users] for ids = {}", ids);
        return userService.findAll(ids, from, size);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/users")
    public UserDto save(@Valid @RequestBody NewUserRequest userCreateDto) {
        log.info("POST User request received to admin endpoint [/users]");
        return userService.save(userCreateDto);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/users/{userId}")
    public void delete(@PathVariable Long userId) {
        log.info("DELETE User request received to admin endpoint [/users] for id = {}", userId);
        userService.deleteById(userId);
    }

    @PostMapping("/compilations")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompilationDto saveCompilation(@Valid @RequestBody NewCompilationDto compilationCreateDto) {
        log.info("POST Compilation request received to admin endpoint [/compilations]");
        return compilationService.saveCompilation(compilationCreateDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        log.info("DELETE Compilation request received to admin endpoint [/compilations] for id = {}", compId);
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCategory(@PathVariable Long compId, @RequestBody @Valid UpdateCompilationRequest compilationUpdateDto) {
        log.info("PATCH Compilation request received to admin endpoint [/compilations] with id = {}", compId);
        return compilationService.updateCompilation(compId, compilationUpdateDto);
    }
}
