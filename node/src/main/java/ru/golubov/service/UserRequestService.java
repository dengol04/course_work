package ru.golubov.service;

import ru.golubov.entity.UserRequest;

import java.util.List;

public interface UserRequestService {
    UserRequest saveUserRequest(Long userId, String requestText);
    List<UserRequest> getLast10UserRequests(Long userId);
}
