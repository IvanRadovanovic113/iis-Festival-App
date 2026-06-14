package com.festivalapp.repository.eventorganization;

import com.festivalapp.model.eventorganization.RequestResource;
import com.festivalapp.model.eventorganization.RequestResourceStatus;
import com.festivalapp.model.Festival;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface RequestResourceRepository extends JpaRepository<RequestResource, Long> {
    @Query("""
        select requestResource
        from RequestResource requestResource
        where requestResource.reservationRequest.id = :reservationRequestId
        order by lower(coalesce(requestResource.resource.name, requestResource.requestedName)) asc
    """)
    List<RequestResource> findByReservationRequest_IdOrderByResource_NameAsc(@Param("reservationRequestId") Long reservationRequestId);

    List<RequestResource> findByReservationRequest_Festival(Festival festival);

    Optional<RequestResource> findByReservationRequest_IdAndResource_Id(Long reservationRequestId, Long resourceId);

    Optional<RequestResource> findByIdAndReservationRequest_Id(Long id, Long reservationRequestId);

    boolean existsByReservationRequest_IdAndResource_Id(Long reservationRequestId, Long resourceId);

    boolean existsByReservationRequest_IdAndRequestedNameIgnoreCase(Long reservationRequestId, String requestedName);

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
