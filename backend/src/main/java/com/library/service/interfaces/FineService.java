package com.library.service.interfaces;

import com.library.dto.response.FineResponse;

import java.util.List;

public interface FineService {
    FineResponse getById(Long id);
    List<FineResponse> getAll();
    List<FineResponse> getByMemberId(Long memberId);
    List<FineResponse> getUnpaidFines();
    FineResponse payFine(Long fineId);
    FineResponse cancelFine(Long fineId, String reason);
}
