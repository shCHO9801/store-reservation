package com.zerobase.storereservation.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.storereservation.util.LoggingUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.ErrorResponse;

import java.io.IOException;

/**
 * CustomAuthenticationEntryPoint
 * - 인증 실패 시 클라이언트에 JSON 형식의 에러 응답을 반환
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    // JSON 응답 생성을 위한 ObjectMapper 인스턴스
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 로깅을 위한 유틸 클래스
    private final LoggingUtil loggingUtil = new LoggingUtil();

    /**
     * 인증 실패 시 호출되는 메서드
     * - HTTP 401 Unauthorized 응답을 클라이언트에 반환
     *
     * @param request       HTTP 요청
     * @param response      HTTP 응답
     * @param authException 인증 예외
     * @throws IOException      입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        loggingUtil.logError("AUTHENTICATION FAILURE", "Request URI: " + request.getRequestURI());

        sendErrorResponse(response);
    }

    // ==== Private Helper Methods ====

    /**
     * 인증 실패 응답 생성 및 반환
     *
     * @param response HTTP 응답 객체
     * @throws IOException 입출력 예외
     */
    private void sendErrorResponse(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ExceptionController.ExceptionResponse errorResponse = createErrorResponse();
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);

        loggingUtil.logSuccess("SEND ERROR RESPONSE", "Response sent with status 401");
    }

    /**
     * 인증 실패 응답 객체 생성
     *
     * @return ExceptionResponse 객체
     */
    private ExceptionController.ExceptionResponse createErrorResponse() {
        return new ExceptionController.ExceptionResponse(
                HttpServletResponse.SC_UNAUTHORIZED,
                "AUTH-001",
                "인증이 필요합니다."
        );
    }
}
