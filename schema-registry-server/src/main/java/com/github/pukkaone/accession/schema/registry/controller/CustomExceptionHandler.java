package com.github.pukkaone.accession.schema.registry.controller;

import com.github.pukkaone.accession.schema.registry.domain.NotFoundException;
import com.github.pukkaone.accession.schema.registry.domain.NotRegisteredException;
import com.github.pukkaone.accession.schema.registry.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Customizes response body on exception.
 */
@ControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

  private static final int NOT_FOUND_ERROR_CODE = 40403;
  private static final int NOT_REGISTERED_ERROR_CODE = 42202;

  /**
   * Customizes response body on {@link NotFoundException}.
   *
   * @param exception
   *     exception
   * @return response
   */
  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<Object> handleNotFound(NotFoundException exception) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .errorCode(NOT_FOUND_ERROR_CODE)
            .message(exception.getMessage())
            .build(),
        HttpStatus.NOT_FOUND);
  }

  /**
   * Customizes response body on {@link NotRegisteredException}.
   *
   * @param exception
   *     exception
   * @return response
   */
  @ExceptionHandler(NotRegisteredException.class)
  protected ResponseEntity<Object> handleNotRegistered(NotRegisteredException exception) {
    return new ResponseEntity<>(
        ErrorResponse.builder()
            .errorCode(NOT_REGISTERED_ERROR_CODE)
            .message(exception.getMessage())
            .build(),
        HttpStatus.UNPROCESSABLE_ENTITY);
  }
}
