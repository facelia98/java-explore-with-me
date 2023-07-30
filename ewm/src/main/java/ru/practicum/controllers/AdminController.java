package ru.practicum.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UserDto;
import ru.practicum.dto.news.NewCategoryDto;
import ru.practicum.dto.news.NewCompilationDto;
import ru.practicum.dto.news.NewUserRequest;
import ru.practicum.dto.updates.UpdateCompilationRequest;
import ru.practicum.dto.updates.UpdateEventAdminRequest;
import ru.practicum.services.CategoryService;
import ru.practicum.services.CompilationService;
import ru.practicum.services.EventService;
import ru.practicum.services.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminController {
    private final CategoryService categoryService;
    private final EventService eventService;
    private final UserService userService;
    private final CompilationService compilationService;

    @PostMapping("/categories")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CategoryDto addNewCategory(@RequestBody @Valid NewCategoryDto dto) {
        return categoryService.addNewCategory(dto);
    }

    @DeleteMapping("/categories/{catId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long catId) {
        categoryService.deleteById(catId);
    }

    @PatchMapping("/categories/{catId}")
    public CategoryDto updateCategory(@PathVariable Long catId,
                                      @RequestBody @Valid CategoryDto dto) {
        return categoryService.updateById(catId, dto);
    }

    @GetMapping("/events")
    public List<EventFullDto> getEventsAdmin(@RequestParam(name = "users", required = false) List<Long> users,
                                             @RequestParam(name = "states", required = false) List<String> states,
                                             @RequestParam(name = "categories", required = false) List<Long> categories,
                                             @RequestParam(name = "rangeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
                                             @RequestParam(name = "rangeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
                                             @RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                             @RequestParam(name = "size", defaultValue = "10") @Positive Integer size) {
        return eventService.getEventsAdmin(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/events/{eventId}")
    EventFullDto update(@PathVariable Long eventId,
                        @Valid @RequestBody UpdateEventAdminRequest updateEventAdminRequest) {
        return eventService.updateEventByAdmin(eventId, updateEventAdminRequest);
    }

    @GetMapping("/users")
    public List<UserDto> findAll(@RequestParam(required = false) List<Long> ids,
                                 @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                 @Positive @RequestParam(defaultValue = "10") int size) {
        return userService.findAll(ids, from, size);
    }

    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/users")
    public UserDto save(@Valid @RequestBody NewUserRequest userCreateDto) {
        return userService.save(userCreateDto);
    }

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping("/users/{userId}")
    public void delete(@PathVariable Long userId) {
        userService.deleteById(userId);
    }

    @PostMapping("/compilations")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CompilationDto saveCompilation(@Valid @RequestBody NewCompilationDto compilationCreateDto) {
        return compilationService.saveCompilation(compilationCreateDto);
    }

    @DeleteMapping("/compilations/{compId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compId) {
        compilationService.deleteCompilation(compId);
    }

    @PatchMapping("/compilations/{compId}")
    public CompilationDto updateCategory(@PathVariable Long compId, @RequestBody @Valid UpdateCompilationRequest compilationUpdateDto) {
        return compilationService.updateCompilation(compId, compilationUpdateDto);
    }
}
