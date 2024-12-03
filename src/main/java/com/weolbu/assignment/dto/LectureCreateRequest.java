package com.weolbu.assignment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LectureCreateRequest {
    @NotBlank(message = "강의명을 입력해주세요.")
    private String title;

    @NotNull(message = "최대 수강 인원을 입력해주세요.")
    @Min(value = 1, message = "최소 수강 인원은 1명 이상이어야 합니다.")
    private Integer maxParticipants;

    @NotNull(message = "가격을 입력해주세요.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Integer price;

}
