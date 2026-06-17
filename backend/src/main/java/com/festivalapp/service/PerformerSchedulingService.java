package com.festivalapp.service;

import com.festivalapp.dto.PerformerSchedulingResponse;
import com.festivalapp.model.*;
import com.festivalapp.repository.ContractRepository;
import com.festivalapp.repository.StageRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PerformerSchedulingService {

    private final ContractRepository contractRepository;
    private final StageRepository stageRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;

    @Transactional(readOnly = true)
    public List<PerformerSchedulingResponse> getAllScheduling(User user) {
        requireSalesDirector(user);
        return contractRepository.findAll().stream()
            .map(PerformerSchedulingResponse::from)
            .toList();
    }

    @Transactional
    public PerformerSchedulingResponse assignStage(Long contractId, Long stageId, User user) {
        requireSalesDirector(user);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));

        Stage stage = stageRepository.findById(stageId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Stage not found"));

        contract.setStage(stage);
        contract.setSchedulingStatus(PerformerSchedulingStatus.STAGE_ASSIGNED);
        contractRepository.save(contract);

        return PerformerSchedulingResponse.from(contract);
    }

    @Transactional
    public void unassignStage(Long contractId, User user) {
        requireSalesDirector(user);

        Contract contract = contractRepository.findById(contractId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));

        contract.setStage(null);
        contract.setSchedulingStatus(PerformerSchedulingStatus.NOT_ASSIGNED);
        contractRepository.save(contract);
    }

    private void requireSalesDirector(User user) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment required"));

        if (assignment.getRole() != Role.SALES_DIRECTOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only sales directors can manage performer scheduling");
        }
    }
}
