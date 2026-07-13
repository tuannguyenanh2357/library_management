package com.library.dto.request;

import com.library.entity.enums.FineStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FineUpdateRequest {
    FineStatus fineStatus;
}
