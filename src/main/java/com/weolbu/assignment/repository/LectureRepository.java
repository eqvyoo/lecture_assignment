package com.weolbu.assignment.repository;

import com.weolbu.assignment.entity.Lecture;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
    @Query("SELECT l FROM Lecture l ORDER BY (l.currentParticipants * 1.0 / l.maxParticipants) DESC")
    List<Lecture> findAllOrderByRate(Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_READ) // 비관적 읽기
    @Query("SELECT l FROM Lecture l WHERE l.lectureId = :lectureId")
    Optional<Lecture> findByIdForUpdate(@Param("lectureId") Long lectureId);
}
