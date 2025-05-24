package ru.golubov.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.golubov.entity.AppUser;

public interface AppUserDAO extends JpaRepository<AppUser, Long> {
    AppUser findAppUserByTelegramUserId(Long id);
}
