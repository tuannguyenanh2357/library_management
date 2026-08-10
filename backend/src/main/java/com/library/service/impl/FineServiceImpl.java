package com.library.service.impl;

import com.library.dto.response.FineResponse;
import com.library.entity.Fines;
import com.library.entity.enums.FineStatus;
import com.library.exception.AppException;
import com.library.exception.ErrorCode;
import com.library.exception.ResourceNotFoundException;
import com.library.mapper.FineMapper;
import com.library.repository.FineRepository;
import com.library.service.interfaces.FineService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {
    private final FineRepository repository;
    private final FineMapper mapper;

    @Override
    public FineResponse getById(Long id) {
        Fines fine = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "Khoản phạt không tồn tại"));
        return mapper.toResponse(fine);
    }

    @Override
    public List<FineResponse> getAll() {
        return repository.findAllWithRelations().stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<FineResponse> getByMemberId(Long memberId) {
        return repository.findByMemberIdWithRelations(memberId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<FineResponse> getUnpaidFines() {
        return repository.findByStatusWithRelations(FineStatus.UNPAID).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FineResponse payFine(Long fineId) {
        Fines fine = repository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "Khoản phạt không tồn tại"));
        fine.setStatus(FineStatus.PAID);
        fine.setPaidDate(LocalDate.now());
        repository.save(fine);
        return mapper.toResponse(fine);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FineResponse cancelFine(Long fineId, String reason) {
        Fines fine = repository.findById(fineId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESOURCE_NOT_FOUND, "Khoản phạt không tồn tại"));
        if (fine.getStatus() != FineStatus.UNPAID) {
            throw new AppException(ErrorCode.INVALID_REQUEST, "Chỉ có thể miễn khoản phạt chưa thanh toán");
        }
        fine.setStatus(FineStatus.CANCELLED);
        if (reason != null && !reason.isBlank()) {
            fine.setReason(fine.getReason() + " [Miễn phạt: " + reason + "]");
        }
        repository.save(fine);
        return mapper.toResponse(fine);
    }
}
