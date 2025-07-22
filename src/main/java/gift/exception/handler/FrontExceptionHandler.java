package gift.exception.handler;

import gift.exception.auth.AuthenticationException;
import gift.exception.auth.AuthorizationException;
import gift.exception.conflict.DataConflictException;
import gift.exception.dto.ErrorResponseDto;
import gift.exception.notfound.EntityNotFoundException;
import gift.exception.option.InvalidOptionAccessException;
import gift.exception.option.OptionPolicyException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class FrontExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public String handleEntityNotFoundException(
            EntityNotFoundException ex,
            Model model,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorInfo", error);

        return "error";
    }

    @ExceptionHandler(DataConflictException.class)
    public String handleDataConflictException(
            DataConflictException ex,
            Model model,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorInfo", error);

        return "error";
    }

    @ExceptionHandler(AuthenticationException.class)
    public String handleAuthenticationException(
            AuthenticationException ex,
            Model model,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorInfo", error);

        return "error";
    }

    @ExceptionHandler(AuthorizationException.class)
    public String handleAuthorizationException(
            AuthorizationException ex,
            Model model,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorInfo", error);

        return "error";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            Model model,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorInfo", error);

        return "error";
    }

    @ExceptionHandler(InvalidOptionAccessException.class)
    public String handleInvalidOptionAccessException(
            InvalidOptionAccessException ex,
            Model model,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorInfo", error);

        return "error";
    }

    @ExceptionHandler(OptionPolicyException.class)
    public String handleOptionPolicyException(
            OptionPolicyException ex,
            Model model,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorInfo", error);

        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(
            Exception ex,
            Model model,
            HttpServletRequest request) {

        ErrorResponseDto error = new ErrorResponseDto(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );
        model.addAttribute("errorInfo", error);

        return "error";
    }
}