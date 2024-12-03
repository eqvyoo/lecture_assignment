package com.weolbu.assignment.repository;

import com.weolbu.assignment.entity.Lecture;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

}
