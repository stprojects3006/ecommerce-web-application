package com.dharshi.userservice.services;

import com.dharshi.userservice.dtos.ApiResponseDto;
import com.dharshi.userservice.dtos.UserDto;
import com.dharshi.userservice.exceptions.ServiceLogicException;
import com.dharshi.userservice.exceptions.UserNotFoundException;
import com.dharshi.userservice.exceptions.UserAlreadyExistsException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


@Service
public interface UserService {
    ResponseEntity<ApiResponseDto<?>> existsUserById(String userId) throws ServiceLogicException;

    ResponseEntity<ApiResponseDto<?>> getUserById(String id) throws ServiceLogicException, UserNotFoundException;
    
    ResponseEntity<ApiResponseDto<?>> createUser(UserDto userDto) throws ServiceLogicException, UserAlreadyExistsException;
}
