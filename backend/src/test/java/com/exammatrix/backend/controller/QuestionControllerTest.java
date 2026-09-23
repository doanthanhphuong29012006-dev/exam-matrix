package com.exammatrix.backend.controller;

import com.exammatrix.backend.dto.response.QuestionAvailabilityResponse;
import com.exammatrix.backend.entity.enums.Difficulty;
import com.exammatrix.backend.entity.enums.ExamType;
import com.exammatrix.backend.service.QuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class QuestionControllerTest {

    @Mock
    private QuestionService questionService;

    @InjectMocks
    private QuestionController questionController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(questionController).build();
    }

    @Test
    @DisplayName("GET /questions/availability without examType should default to OBJECTIVE")
    void getAvailability_defaultExamType_callsServiceWithObjective() throws Exception {
        when(questionService.getAvailability(1, ExamType.OBJECTIVE))
                .thenReturn(List.of(
                        new QuestionAvailabilityResponse(1, Difficulty.EASY, 10L)
                ));

        mockMvc.perform(get("/questions/availability")
                        .param("subjectId", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chapterId").value(1))
                .andExpect(jsonPath("$[0].difficulty").value("EASY"))
                .andExpect(jsonPath("$[0].available").value(10));

        verify(questionService).getAvailability(1, ExamType.OBJECTIVE);
    }

    @Test
    @DisplayName("GET /questions/availability with examType=ESSAY should call service with ESSAY")
    void getAvailability_withEssayExamType_callsServiceWithEssay() throws Exception {
        when(questionService.getAvailability(1, ExamType.ESSAY))
                .thenReturn(List.of(
                        new QuestionAvailabilityResponse(1, Difficulty.HARD, 4L)
                ));

        mockMvc.perform(get("/questions/availability")
                        .param("subjectId", "1")
                        .param("examType", "ESSAY")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].chapterId").value(1))
                .andExpect(jsonPath("$[0].difficulty").value("HARD"))
                .andExpect(jsonPath("$[0].available").value(4));

        verify(questionService).getAvailability(1, ExamType.ESSAY);
    }
}
