package com.festivalapp.repository;

import com.festivalapp.model.Negotiation;
import com.festivalapp.model.NegotiationStatus;
import org.springframework.data.jpa.domain.Specification;

public class NegotiationSpecification {

    public static Specification<Negotiation> hasStatus(NegotiationStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Negotiation> hasPerformerId(Long performerId) {
        return (root, query, cb) -> performerId == null ? null : cb.equal(root.get("performer").get("id"), performerId);
    }

    public static Specification<Negotiation> hasOfferId(Long offerId) {
        return (root, query, cb) -> offerId == null ? null : cb.equal(root.get("offer").get("id"), offerId);
    }

    public static Specification<Negotiation> hasCurrentStateId(Long stateId) {
        return (root, query, cb) -> stateId == null ? null : cb.equal(root.get("currentState").get("id"), stateId);
    }
}