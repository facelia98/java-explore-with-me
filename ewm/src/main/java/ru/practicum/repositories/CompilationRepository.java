package ru.practicum.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.models.Compilation;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {
}
