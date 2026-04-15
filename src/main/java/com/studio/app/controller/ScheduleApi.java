package com.studio.app.controller;

import com.studio.app.constant.ApiConstants;
import com.studio.app.dto.request.WeeklyScheduleRequest;
import com.studio.app.dto.response.WeeklyPlanningScheduleResponse;
import com.studio.app.dto.response.WeeklyScheduleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API contract for student weekly schedule operations.
 * Provides endpoints to create, list, update, and soft-delete recurring weekly class slots.
 */
@Tag(name = "Student Schedules", description = "Manage a student's recurring weekly class schedule")
@RequestMapping(ApiConstants.STUDENTS)
public interface ScheduleApi {

    /**
     * Creates one or more recurring weekly class slots for the student.
     *
     * @param studentId the ID of the student
     * @param request   schedule details (day, time, duration) for each slot
     * @return created {@link WeeklyScheduleResponse} entries
     */
    @Operation(summary = "Add weekly schedules", description = "Creates one or more recurring weekly class slots for the student.")
    @PostMapping("/{studentId}/schedules")
    ResponseEntity<List<WeeklyScheduleResponse>> addSchedule(@PathVariable Long studentId,
                                                             @Valid @NotEmpty @RequestBody List<@Valid WeeklyScheduleRequest> request);

    /**
     * Returns all active weekly schedule slots for the student.
     *
     * @param studentId the ID of the student
     * @return a list of {@link WeeklyScheduleResponse} objects
     */
    @Operation(summary = "List schedules", description = "Returns all active weekly schedule slots for the student.")
    @GetMapping("/{studentId}/schedules")
    ResponseEntity<List<WeeklyScheduleResponse>> getSchedules(@PathVariable Long studentId);

    /**
     * Returns all active recurring weekly slots across students for planning UI.
     *
     * @return a list of {@link WeeklyPlanningScheduleResponse} rows
     */
    @Operation(summary = "List weekly planning schedules",
            description = "Returns all active recurring weekly schedule slots across students for weekly planning screens.")
    @GetMapping("/schedules/weekly-planning")
    ResponseEntity<List<WeeklyPlanningScheduleResponse>> getWeeklyPlanningSchedules();

    /**
     * Updates an existing weekly schedule slot and optionally creates additional ones.
     *
     * @param studentId  the ID of the student
     * @param scheduleId the ID of the schedule to update
     * @param request    updated schedule details where the first item updates scheduleId
     * @return updated/created {@link WeeklyScheduleResponse} entries
     */
    @Operation(summary = "Update schedules", description = "Updates an existing weekly schedule slot and optionally creates additional ones.")
    @PostMapping("/{studentId}/schedules/{scheduleId}")
    ResponseEntity<List<WeeklyScheduleResponse>> updateSchedule(@PathVariable Long studentId,
                                                                @PathVariable Long scheduleId,
                                                                @Valid @NotEmpty @RequestBody List<@Valid WeeklyScheduleRequest> request);

    /**
     * Soft-deletes a recurring schedule slot.
     *
     * @param studentId  the ID of the student
     * @param scheduleId the ID of the schedule to remove
     * @return 204 No Content on success
     */
    @Operation(summary = "Delete a schedule", description = "Soft-deletes a recurring schedule slot.")
    @PostMapping("/{studentId}/schedules/{scheduleId}/delete")
    ResponseEntity<Void> removeSchedule(@PathVariable Long studentId, @PathVariable Long scheduleId);
}
