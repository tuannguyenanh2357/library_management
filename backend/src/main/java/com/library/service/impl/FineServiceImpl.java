package com.library.service.impl;

import com.library.dto.response.FineResponse;
import com.library.entity.Fines;
import com.library.entity.enums.FineStatus;
import com.library.mapper.FineMapper;
import com.library.repository.FineRepository;
import com.library.service.interfaces.FineService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

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
                .orElseThrow(() -> new RuntimeException("Fine not found"));
        return mapper.toResponse(fine);
    }

    @Override
    public List<FineResponse> getAll() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    @Override
    public List<FineResponse> getByMemberId(Long memberId) {
        return repository.findByBorrowing_Member_Id(memberId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public List<FineResponse> getUnpaidFines() {
        return repository.findByStatus(FineStatus.UNPAID).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public FineResponse payFine(Long fineId) {
        Fines fine = repository.findById(fineId).orElseThrow(() -> new RuntimeException("Fine not found"));
        fine.setStatus(FineStatus.PAID);
        fine.setPaidDate(LocalDate.now());
        repository.save(fine);
        return mapper.toResponse(fine);
    }
}
