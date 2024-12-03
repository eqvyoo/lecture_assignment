package com.weolbu.assignment.repository;

import com.weolbu.assignment.entity.Enrollment;
import com.weolbu.assignment.entity.Lecture;
import com.weolbu.assignment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment,Long> {
    boolean existsByLectureAndStudent(Lecture lecture, User user);
}
