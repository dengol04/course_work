package ru.golubov.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.golubov.entity.UserRequest;

import java.util.List;

public interface UserRequestDAO extends JpaRepository<UserRequest, Long> {
    @Query("SELECT ur FROM UserRequest ur WHERE ur.userId = :userId ORDER BY ur.createdAt DESC")
    List<UserRequest> findAllRequestsByUserId(Long userId);

    default void keepOnlyLast10Requests(Long userId) {
        List<UserRequest> last10 = findAllRequestsByUserId(userId);
        if (last10.size() > 10) {
            deleteAll(last10.subList(10, last10.size()));
        }
    }
}
