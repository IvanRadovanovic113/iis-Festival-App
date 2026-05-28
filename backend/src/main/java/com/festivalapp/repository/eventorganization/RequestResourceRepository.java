package com.festivalapp.repository.eventorganization;

import com.festivalapp.model.eventorganization.RequestResource;
import com.festivalapp.model.eventorganization.RequestResourceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RequestResourceRepository extends JpaRepository<RequestResource, Long> {
    List<RequestResource> findByReservationRequest_IdOrderByResource_NameAsc(Long reservationRequestId);

    Optional<RequestResource> findByReservationRequest_IdAndResource_Id(Long reservationRequestId, Long resourceId);

    boolean existsByReservationRequest_IdAndResource_Id(Long reservationRequestId, Long resourceId);

    void deleteByReservationRequest_IdAndResource_Id(Long reservationRequestId, Long resourceId);

    void deleteByResource_Id(Long resourceId);

    @Query("""
        select coalesce(sum(requestResource.quantity), 0)
        from RequestResource requestResource
        where requestResource.resource.id = :resourceId
          and requestResource.status = :status
          and requestResource.reservationRequest.status = com.festivalapp.model.eventorganization.EventReservationStatus.APPROVED
          and requestResource.reservationRequest.id <> :excludedRequestId
          and requestResource.reservationRequest.performanceDate = :performanceDate
          and requestResource.reservationRequest.startTime < :endTime
          and requestResource.reservationRequest.endTime > :startTime
    """)
    Integer sumOverlappingQuantityByResource(
        @Param("resourceId") Long resourceId,
        @Param("excludedRequestId") Long excludedRequestId,
        @Param("performanceDate") LocalDate performanceDate,
        @Param("startTime") LocalTime startTime,
        @Param("endTime") LocalTime endTime,
        @Param("status") RequestResourceStatus status
    );
}
