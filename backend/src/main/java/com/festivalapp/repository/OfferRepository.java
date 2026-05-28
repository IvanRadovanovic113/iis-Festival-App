package com.festivalapp.repository;

import com.festivalapp.model.Offer;
import com.festivalapp.model.OfferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

    // 1. Filtriranje samo po statusu
    Page<Offer> findByStatus(OfferStatus status, Pageable pageable);

    // 2. Filtriranje i po statusu i po lokaciji (Case-Insensitive)
    Page<Offer> findByStatusAndLocationContainingIgnoreCase(OfferStatus status, String location, Pageable pageable);

    // 3. Pretraga po lokaciji kroz sve statuse (Case-Insensitive)
    Page<Offer> findByLocationContainingIgnoreCase(String location, Pageable pageable);
}