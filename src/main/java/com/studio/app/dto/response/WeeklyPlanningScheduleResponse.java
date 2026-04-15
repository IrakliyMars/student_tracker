package com.studio.app.dto.response;

import com.studio.app.enums.StudioTimezone;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Flat weekly schedule row used by weekly planning UIs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeeklyPlanningScheduleResponse {

    /** Schedule identifier. */
    private Long id;

    /** Student identifier that owns this recurring slot. */
    private Long studentId;

    /** Convenience full name for planning views. */
    private String studentName;

    /** Student timezone used for recurring slot time. */
    private StudioTimezone timezone;

    /** Day of week for this recurring slot. */
    private DayOfWeek dayOfWeek;

    /** Start time in student's timezone. */
    private LocalTime startTime;

    /** End time in student's timezone. */
    private LocalTime endTime;

    /** Duration of each occurrence in minutes. */
    private Integer durationMinutes;
}

