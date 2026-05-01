package com.studio.app.service.impl;

import com.studio.app.dto.request.WeeklyScheduleRequest;
import com.studio.app.dto.response.WeeklyPlanningScheduleResponse;
import com.studio.app.dto.response.WeeklyScheduleResponse;
import com.studio.app.entity.WeeklySchedule;
import com.studio.app.exception.BadRequestException;
import com.studio.app.exception.ConflictException;
import com.studio.app.exception.ResourceNotFoundException;
import com.studio.app.mapper.StudentMapper;
import com.studio.app.repository.StudentRepository;
import com.studio.app.repository.WeeklyScheduleRepository;
import com.studio.app.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of {@link ScheduleService}.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final WeeklyScheduleRepository scheduleRepository;
    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    /** {@inheritDoc} */
    @Override
    public List<WeeklyScheduleResponse> addSchedule(Long studentId, List<WeeklyScheduleRequest> request) {
        if (request == null || request.isEmpty()) {
            throw new BadRequestException("At least one schedule entry is required");
        }

        validateRequestItems(request);

        var student = studentRepository.findByIdAndDeletedFalse(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        if (student.isStoppedAttending()) {
            throw new BadRequestException("Cannot add schedules for a student who stopped attending");
        }

        Set<DayOfWeek> occupiedDays = new HashSet<>(scheduleRepository.findByStudentIdAndDeletedFalse(studentId)
                .stream()
                .map(WeeklySchedule::getDayOfWeek)
                .toList());

        return request.stream()
                .map(item -> {
                    validateDayAvailability(occupiedDays, item.getDayOfWeek());
                    occupiedDays.add(item.getDayOfWeek());
                    return studentMapper.toWeeklyScheduleResponse(scheduleRepository.save(buildSchedule(student, item)));
                })
                .toList();
    }

    private WeeklySchedule buildSchedule(com.studio.app.entity.Student student, WeeklyScheduleRequest request) {
        int resolvedDuration = resolveDurationMinutes(request);

        return WeeklySchedule.builder()
                .student(student)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .durationMinutes(resolvedDuration)
                .effectiveFromEpochDay(student.getStartDate() == null
                        ? LocalDate.now().toEpochDay()
                        : student.getStartDate().toEpochDay())
                .build();
    }

    private int resolveDurationMinutes(WeeklyScheduleRequest request) {
        var duration = request.getDurationMinutes();
        var endTime = request.getEndTime();

        if (duration == null && endTime == null) {
            throw new BadRequestException("Either durationMinutes or endTime is required");
        }

        if (duration != null && endTime == null) {
            if (duration < 15 || duration > 480) {
                throw new BadRequestException("Duration must be between 15 and 480 minutes");
            }
            return duration;
        }

        if (duration == null) {
            if (!endTime.isAfter(request.getStartTime())) {
                throw new BadRequestException("endTime must be after startTime");
            }

            long derivedDuration = ChronoUnit.MINUTES.between(request.getStartTime(), endTime);
            if (derivedDuration < 15 || derivedDuration > 480) {
                throw new BadRequestException("Derived duration must be between 15 and 480 minutes");
            }

            return (int) derivedDuration;
        }

        var expectedEndTime = request.getStartTime().plusMinutes(duration);
        if (!expectedEndTime.equals(endTime)) {
            throw new BadRequestException("durationMinutes and endTime must match startTime");
        }

        return duration;
    }

    private void validateDayAvailability(Set<DayOfWeek> occupiedDays, DayOfWeek dayOfWeek) {
        if (occupiedDays.contains(dayOfWeek)) {
            throw new ConflictException("Student already has a schedule on " + dayOfWeek);
        }
    }
    private void validateRequestItems(List<WeeklyScheduleRequest> request) {
        for (int i = 0; i < request.size(); i++) {
            var item = request.get(i);
            if (item == null) {
                throw new BadRequestException("Schedule entry at index " + i + " is required");
            }
            if (item.getDayOfWeek() == null) {
                throw new BadRequestException("Day of week is required");
            }
            if (item.getStartTime() == null) {
                throw new BadRequestException("Start time is required");
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<WeeklyScheduleResponse> getSchedulesForStudent(Long studentId) {
        studentRepository.findByIdAndDeletedFalse(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        return scheduleRepository.findByStudentIdAndDeletedFalse(studentId)
                .stream()
                .map(studentMapper::toWeeklyScheduleResponse)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional(readOnly = true)
    public List<WeeklyPlanningScheduleResponse> getSchedulesForWeeklyPlanning() {
        return scheduleRepository.findAllForWeeklyPlanning()
                .stream()
                .map(schedule -> WeeklyPlanningScheduleResponse.builder()
                        .id(schedule.getId())
                        .studentId(schedule.getStudent().getId())
                        .studentName(schedule.getStudent().getFirstName() + " " + schedule.getStudent().getLastName())
                        .timezone(schedule.getStudent().getTimezone())
                        .dayOfWeek(schedule.getDayOfWeek())
                        .startTime(schedule.getStartTime())
                        .durationMinutes(schedule.getDurationMinutes())
                        .endTime(schedule.getStartTime().plusMinutes(schedule.getDurationMinutes()))
                        .build())
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<WeeklyScheduleResponse> updateSchedule(Long studentId, Long scheduleId, List<WeeklyScheduleRequest> request) {
        if (request == null || request.isEmpty()) {
            throw new BadRequestException("At least one schedule entry is required");
        }

        validateRequestItems(request);

        var schedule = scheduleRepository.findByIdAndStudentIdAndDeletedFalse(scheduleId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("WeeklySchedule", scheduleId));

        if (schedule.getStudent().isStoppedAttending()) {
            throw new BadRequestException("Cannot update schedules for a student who stopped attending");
        }

        Set<DayOfWeek> occupiedDays = new HashSet<>(scheduleRepository.findByStudentIdAndDeletedFalse(studentId)
                .stream()
                .filter(item -> !item.getId().equals(scheduleId))
                .map(WeeklySchedule::getDayOfWeek)
                .toList());

        var responses = new java.util.ArrayList<WeeklyScheduleResponse>(request.size());

        var primary = request.getFirst();
        validateDayAvailability(occupiedDays, primary.getDayOfWeek());
        schedule.setDayOfWeek(primary.getDayOfWeek());
        schedule.setStartTime(primary.getStartTime());
        schedule.setDurationMinutes(resolveDurationMinutes(primary));
        occupiedDays.add(primary.getDayOfWeek());
        responses.add(studentMapper.toWeeklyScheduleResponse(scheduleRepository.save(schedule)));

        for (int i = 1; i < request.size(); i++) {
            var item = request.get(i);
            validateDayAvailability(occupiedDays, item.getDayOfWeek());
            occupiedDays.add(item.getDayOfWeek());
            responses.add(studentMapper.toWeeklyScheduleResponse(scheduleRepository.save(buildSchedule(schedule.getStudent(), item))));
        }

        return responses;
    }

    /** {@inheritDoc} */
    @Override
    public void removeSchedule(Long studentId, Long scheduleId) {
        var schedule = scheduleRepository.findByIdAndStudentIdAndDeletedFalse(scheduleId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("WeeklySchedule", scheduleId));
        schedule.setDeleted(true);
        scheduleRepository.save(schedule);
    }
}
