package com.weolbu.assignment.repository;

import com.weolbu.assignment.entity.Lecture;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("SELECT l FROM Lecture l ORDER BY (l.currentParticipants * 1.0 / l.maxParticipants) DESC")
    List<Lecture> findAllOrderByRate(Pageable pageable);
}
