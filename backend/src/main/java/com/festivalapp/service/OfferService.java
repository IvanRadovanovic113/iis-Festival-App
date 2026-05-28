package com.festivalapp.service;

import com.festivalapp.dto.OfferDetailResponse;
import com.festivalapp.dto.OfferRequest;
import com.festivalapp.dto.OfferResponse;
import com.festivalapp.model.Offer;
import com.festivalapp.model.OfferStatus;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.model.Performer;
import com.festivalapp.repository.OfferRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import com.festivalapp.repository.PerformerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final PerformerRepository performerRepository;

    private void requireNegotiationManager(Long userId) {
        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Festival assignment is required"));
        
        if (assignment.getRole() != Role.NEGOTIATION_MANAGER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only negotiation managers can manage offers");
        }
    }

    // Kreiranje ponude u statusu DRAFT
    @Transactional
    public OfferDetailResponse createOffer(OfferRequest request, User user) {
        requireNegotiationManager(user.getId());

        Offer offer = Offer.builder()
                .price(request.getPrice())
                .performanceDate(request.getPerformanceDate())
                .location(request.getLocation().trim())
                .durationMinutes(request.getDurationMinutes())
                .status(OfferStatus.DRAFT)
                .additionalRequirements(request.getAdditionalRequirements() != null ? request.getAdditionalRequirements().trim() : null)
                .workflowTemplateId(request.getWorkflowTemplateId())
                .createdBy(user)
                .build();

        return OfferDetailResponse.from(offerRepository.save(offer));
    }

    // Izmena ponude (Dozvoljeno samo za DRAFT)
    @Transactional
    public OfferDetailResponse updateOffer(Long offerId, OfferRequest request, User user) {
        requireNegotiationManager(user.getId());

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found."));

        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Modification is only allowed for offers in DRAFT status. Current status: " + offer.getStatus());
        }

        offer.setPrice(request.getPrice());
        offer.setPerformanceDate(request.getPerformanceDate());
        offer.setLocation(request.getLocation().trim());
        offer.setDurationMinutes(request.getDurationMinutes());
        offer.setAdditionalRequirements(request.getAdditionalRequirements() != null ? request.getAdditionalRequirements().trim() : null);

        return OfferDetailResponse.from(offerRepository.save(offer));
    }

    // Objavljivanje ponude (DRAFT -> PUBLISHED) + Beleženje vremena
    @Transactional
    public OfferDetailResponse publishOffer(Long offerId, User user) {
        requireNegotiationManager(user.getId());

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found."));

        if (offer.getStatus() != OfferStatus.DRAFT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only DRAFT offers can be published.");
        }

        offer.setStatus(OfferStatus.PUBLISHED);
        offer.setPublishedAt(LocalDateTime.now());

        return OfferDetailResponse.from(offerRepository.save(offer));
    }

    // Pregled svih ponuda uz paginaciju i opcioni filter
    @Transactional(readOnly = true)
    public Page<OfferResponse> getOffers(OfferStatus status, String searchTerm, Pageable pageable, User user) {
        requireNegotiationManager(user.getId());

        String search = (searchTerm != null && !searchTerm.trim().isEmpty()) ? searchTerm.trim() : null;
        Page<Offer> offersPage;

        if (status != null && search != null) {
            offersPage = offerRepository.findByStatusAndLocationContainingIgnoreCase(status, search, pageable);
        } else if (status != null) {
            offersPage = offerRepository.findByStatus(status, pageable);
        } else if (search != null) {
            offersPage = offerRepository.findByLocationContainingIgnoreCase(search, pageable);
        } else {
            offersPage = offerRepository.findAll(pageable);
        }

        return offersPage.map(OfferResponse::from);
    }

    // Pregled svih detalja konkretne ponude
    @Transactional(readOnly = true)
    public OfferDetailResponse getOfferById(Long offerId, User user) {
        requireNegotiationManager(user.getId());

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found."));

        return OfferDetailResponse.from(offer);
    }

    // Arhiviranje ponude (Dozvoljeno iz DRAFT ili PUBLISHED)
    @Transactional
    public OfferDetailResponse archiveOffer(Long offerId, User user) {
        requireNegotiationManager(user.getId());

        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found."));

        if (offer.getStatus() != OfferStatus.DRAFT && offer.getStatus() != OfferStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Only DRAFT or PUBLISHED offers can be archived. Current status: " + offer.getStatus());
        }

        offer.setStatus(OfferStatus.ARCHIVED);
        offer.setArchivedAt(LocalDateTime.now());

        return OfferDetailResponse.from(offerRepository.save(offer));
    }

    // Veza izmedju ponuda i izvodjaca
    @Transactional
    public void addInterestedPerformer(Long offerId, Long performerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        
        Performer performer = performerRepository.findById(performerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found"));

        if (!offer.getInterestedPerformers().contains(performer)) {
            offer.getInterestedPerformers().add(performer);
            offerRepository.save(offer);
        }
    }

    @Transactional
    public void removeInterestedPerformer(Long offerId, Long performerId) {
        Offer offer = offerRepository.findById(offerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Offer not found"));
        
        Performer performer = performerRepository.findById(performerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Performer not found"));

        if (offer.getInterestedPerformers().contains(performer)) {
            offer.getInterestedPerformers().remove(performer);
            offerRepository.save(offer);
        }
    }
}