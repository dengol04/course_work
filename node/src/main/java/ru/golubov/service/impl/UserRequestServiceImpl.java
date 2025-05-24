package ru.golubov.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.golubov.dao.UserRequestDAO;
import ru.golubov.entity.UserRequest;
import ru.golubov.service.UserRequestService;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserRequestServiceImpl implements UserRequestService {
    private final UserRequestDAO userRequestDAO;
    @Override
    @Transactional
    public UserRequest saveUserRequest(Long userId, String requestText) {
        UserRequest request = UserRequest.builder()
                .userId(userId)
                .requestText(requestText)
                .createdAt(LocalDateTime.now())
                .build();

        UserRequest savedRequest = userRequestDAO.save(request);

        userRequestDAO.keepOnlyLast10Requests(userId);

        return savedRequest;
    }

    @Override
    public List<UserRequest> getLast10UserRequests(Long userId) {
        return userRequestDAO.findAllRequestsByUserId(userId)
                .stream()
                .limit(10)
                .collect(Collectors.toList());
    }
}
