package com.levely.auth.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(new ObjectMapper()))
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        validator.close();
    }

    @Test
    void businessExceptionReturnsErrorCodeResponse() throws Exception {
        mockMvc.perform(get("/business"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("U409"))
                .andExpect(jsonPath("$.message").value("이미 존재하는 사용자입니다."));
    }

    @Test
    void validationExceptionReturnsInvalidInputResponse() throws Exception {
        mockMvc.perform(post("/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("E400"))
                .andExpect(jsonPath("$.message").value("이메일은 필수 입니다."));
    }

    @Test
    void methodNotAllowedReturnsMethodNotAllowedResponse() throws Exception {
        mockMvc.perform(post("/business"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.code").value("E405"))
                .andExpect(jsonPath("$.message").value("허용되지 않은 HTTP 메서드입니다."));
    }

    @Test
    void unexpectedExceptionReturnsInternalServerErrorResponse() throws Exception {
        mockMvc.perform(get("/exception"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("E500"))
                .andExpect(jsonPath("$.message").value("서버 내부 오류가 발생했습니다."));
    }

    @RestController
    static class TestController {

        @GetMapping("/business")
        String businessException() {
            throw new BusinessException(ErrorCode.DUPLICATE_USER);
        }

        @PostMapping("/validation")
        String validationException(@Valid @RequestBody ValidationRequest request) {
            return "ok";
        }

        @GetMapping("/exception")
        String unexpectedException() {
            throw new RuntimeException("unexpected");
        }
    }

    record ValidationRequest(
            @NotBlank(message = "이메일은 필수 입니다.")
            String email
    ) {
    }
}
