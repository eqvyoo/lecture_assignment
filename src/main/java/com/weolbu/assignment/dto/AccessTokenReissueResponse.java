package com.weolbu.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccessTokenReissueResponse {
    private String accessToken;
}