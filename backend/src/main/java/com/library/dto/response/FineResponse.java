package com.library.dto.response;

import com.library.entity.enums.FineStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class FineResponse {
    Long id;
    Long borrowingId;
    Long memberId;
    String memberName;
    String bookTitle;
    BigDecimal amount;
    String reason;
    FineStatus status;
    LocalDate issuedDate;
    LocalDate paidDate;
}
