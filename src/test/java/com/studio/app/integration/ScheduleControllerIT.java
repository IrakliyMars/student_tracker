package com.studio.app.integration;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(BaseIntegrationTest.StubCurrencyConfig.class)
class ScheduleControllerIT extends BaseIntegrationTest {

    @Nested
    class AddSchedule {

        @Test
        void shouldCreateAndReturn201() throws Exception {
            mockMvc.perform(post("/api/students/1/schedules")
                            .contentType(JSON)
                            .content("""
                                    [
                                      {
                                        "dayOfWeek": "FRIDAY",
                                        "startTime": "16:00",
                                        "durationMinutes": 45
                                      }
                                    ]
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].studentId").value(1))
                    .andExpect(jsonPath("$[0].dayOfWeek").value("FRIDAY"))
                    .andExpect(jsonPath("$[0].startTime").value("16:00:00"))
                    .andExpect(jsonPath("$[0].durationMinutes").value(45))
                    .andExpect(jsonPath("$[0].endTime").value("16:45:00"));
        }

        @Test
        void shouldCreateUsingEndTimeOnly() throws Exception {
            mockMvc.perform(post("/api/students/1/schedules")
                            .contentType(JSON)
                            .content("""
                                    [
                                      {
                                        "dayOfWeek": "FRIDAY",
                                        "startTime": "16:00",
                                        "endTime": "16:45"
                                      }
                                    ]
                                    """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].dayOfWeek").value("FRIDAY"))
                    .andExpect(jsonPath("$[0].startTime").value("16:00:00"))
                    .andExpect(jsonPath("$[0].durationMinutes").value(45))
                    .andExpect(jsonPath("$[0].endTime").value("16:45:00"));
        }

        @Test
        void shouldReturn400WhenDurationAndEndTimeMismatch() throws Exception {
            mockMvc.perform(post("/api/students/1/schedules")
                            .contentType(JSON)
                            .content("""
                                    [
                                      {
                                        "dayOfWeek": "FRIDAY",
                                        "startTime": "16:00",
                                        "durationMinutes": 60,
                                        "endTime": "16:45"
                                      }
                                    ]
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn409ForDuplicateDay() throws Exception {
            // Student 1 already has MONDAY
            mockMvc.perform(post("/api/students/1/schedules")
                            .contentType(JSON)
                            .content("""
                                    [
                                      {
                                        "dayOfWeek": "MONDAY",
                                        "startTime": "16:00",
                                        "durationMinutes": 45
                                      }
                                    ]
                                    """))
                    .andExpect(status().isConflict());
        }

        @Test
        void shouldReturn400ForMissingDay() throws Exception {
            mockMvc.perform(post("/api/students/1/schedules")
                            .contentType(JSON)
                            .content("""
                                    [
                                      {
                                        "startTime": "16:00",
                                        "durationMinutes": 45
                                      }
                                    ]
                                    """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400ForStoppedAttendingStudent() throws Exception {
            mockMvc.perform(patch("/api/students/1")
                            .contentType(JSON)
                            .content("""
                                    { "stoppedAttending": true }
                                    """))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/students/1/schedules")
                            .contentType(JSON)
                            .content("""
                                    [
                                      {
                                        "dayOfWeek": "FRIDAY",
                                        "startTime": "16:00",
                                        "durationMinutes": 45
                                      }
                                    ]
                                    """))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetSchedules {

        @Test
        void shouldReturnAllSchedulesForStudent() throws Exception {
            mockMvc.perform(get("/api/students/1/schedules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].dayOfWeek", containsInAnyOrder("MONDAY", "WEDNESDAY")))
                    .andExpect(jsonPath("$[*].endTime", everyItem(notNullValue())));
        }

        @Test
        void shouldReturnEmptyForStudentWithNoSchedules() throws Exception {
            mockMvc.perform(get("/api/students/3/schedules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    class GetWeeklyPlanningSchedules {

        @Test
        void shouldReturnAllActiveRegularSchedules() throws Exception {
            mockMvc.perform(get("/api/students/schedules/weekly-planning"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[*].studentId", containsInAnyOrder(1, 1, 2)))
                    .andExpect(jsonPath("$[*].studentName", hasItems("Ana García", "Ivan Petrov")))
                    .andExpect(jsonPath("$[*].timezone", everyItem(notNullValue())))
                    .andExpect(jsonPath("$[*].endTime", everyItem(notNullValue())));
        }

        @Test
        void shouldExcludeStoppedAttendingStudents() throws Exception {
            mockMvc.perform(patch("/api/students/2")
                            .contentType(JSON)
                            .content("""
                                    { "stoppedAttending": true }
                                    """))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/students/schedules/weekly-planning"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].studentId", everyItem(is(1))));
        }
    }

    @Nested
    class UpdateSchedule {

        @Test
        void shouldUpdateDayAndTime() throws Exception {
            mockMvc.perform(post("/api/students/1/schedules/1")
                            .contentType(JSON)
                            .content("""
                                    [
                                      {
                                        "dayOfWeek": "THURSDAY",
                                        "startTime": "11:00",
                                        "durationMinutes": 90
                                      }
                                    ]
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].dayOfWeek").value("THURSDAY"))
                    .andExpect(jsonPath("$[0].startTime").value("11:00:00"))
                    .andExpect(jsonPath("$[0].durationMinutes").value(90))
                    .andExpect(jsonPath("$[0].endTime").value("12:30:00"));
        }

        @Test
        void shouldReturn404ForNonExistentSchedule() throws Exception {
            mockMvc.perform(post("/api/students/1/schedules/999")
                            .contentType(JSON)
                            .content("""
                                    [
                                      {
                                        "dayOfWeek": "FRIDAY",
                                        "startTime": "11:00",
                                        "durationMinutes": 60
                                      }
                                    ]
                                    """))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class RemoveSchedule {

        @Test
        void shouldReturn204() throws Exception {
            mockMvc.perform(post("/api/students/1/schedules/1/delete"))
                    .andExpect(status().isNoContent());

            // Verify it's no longer returned
            mockMvc.perform(get("/api/students/1/schedules"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        void shouldReturn404ForNonExistentSchedule() throws Exception {
            mockMvc.perform(post("/api/students/1/schedules/999/delete"))
                    .andExpect(status().isNotFound());
        }
    }
}

