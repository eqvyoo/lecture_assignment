package com.weolbu.assignment.dto;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LectureSearchResponse {
    private Long lectureId;
    private String title;
    private Integer price;
    private String instructorName;
    private Integer currentParticipants;
    private Integer maxParticipants;
}