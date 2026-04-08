package com.studio.app.service;

import com.studio.app.dto.request.WeeklyScheduleRequest;
import com.studio.app.dto.response.WeeklyScheduleResponse;

import java.util.List;

/**
 * Service interface for managing recurring weekly schedules.
 */
public interface ScheduleService {

    /**
     * Adds one or more recurring weekly slots for a student.
     *
     * @param studentId the student ID
     * @param request   schedule details for each slot
     * @return the created schedule entries
     */
    List<WeeklyScheduleResponse> addSchedule(Long studentId, List<WeeklyScheduleRequest> request);

    /**
     * Returns all active weekly schedule entries for a student.
     *
     * @param studentId the student ID
     * @return list of schedule responses
     */
    List<WeeklyScheduleResponse> getSchedulesForStudent(Long studentId);

    /**
     * Updates an existing weekly schedule slot and may add more slots.
     *
     * @param studentId  the student ID
     * @param scheduleId the schedule entry ID
     * @param request    updated details where first item updates scheduleId
     * @return updated/created schedule entries
     */
    List<WeeklyScheduleResponse> updateSchedule(Long studentId, Long scheduleId, List<WeeklyScheduleRequest> request);

    /**
     * Soft-deletes a recurring schedule entry.
     *
     * @param studentId  the student ID
     * @param scheduleId the schedule entry ID
     */
    void removeSchedule(Long studentId, Long scheduleId);
}
