package ru.golubov.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.golubov.entity.RawData;

public interface RawDataDAO extends JpaRepository<RawData, Long> {
}
