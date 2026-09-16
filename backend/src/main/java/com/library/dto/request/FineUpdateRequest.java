package com.library.dto.request;

import com.library.entity.enums.FineStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FineUpdateRequest {
    @NotNull(message = "Trạng thái phạt không được để trống")
    FineStatus fineStatus;

    String reason;
}
