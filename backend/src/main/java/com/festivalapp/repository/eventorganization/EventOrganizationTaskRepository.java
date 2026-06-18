package com.festivalapp.repository.eventorganization;

import com.festivalapp.model.Festival;
import com.festivalapp.model.eventorganization.EventOrganizationTask;
import com.festivalapp.model.eventorganization.EventOrganizationTaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventOrganizationTaskRepository extends JpaRepository<EventOrganizationTask, Long> {
    @Query("""
        select task
        from EventOrganizationTask task
        left join task.requestResource rr
        left join rr.resource res
        where rr.reservationRequest.festival = :festival
        order by rr.reservationRequest.performanceDate asc,
                 lower(coalesce(res.name, rr.requestedName)) asc
    """)
    List<EventOrganizationTask> findByFestival(@Param("festival") Festival festival);

    @Query("""
        select task
        from EventOrganizationTask task
        left join task.requestResource rr
        left join rr.resource res
        where rr.reservationRequest.festival = :festival
          and task.status = :status
        order by rr.reservationRequest.performanceDate asc,
                 lower(coalesce(res.name, rr.requestedName)) asc
    """)
    List<EventOrganizationTask> findByFestivalAndStatus(
        @Param("festival") Festival festival,
        @Param("status") EventOrganizationTaskStatus status
    );

    @Query("""
        select task
        from EventOrganizationTask task
        left join task.requestResource rr
        left join rr.resource res
        where task.status = :status
        order by rr.reservationRequest.performanceDate asc,
                 lower(coalesce(res.name, rr.requestedName)) asc
    """)
    List<EventOrganizationTask> findByStatusOrdered(@Param("status") EventOrganizationTaskStatus status);

    @Query("""
        select task
        from EventOrganizationTask task
        left join task.requestResource rr
        left join rr.resource res
        order by rr.reservationRequest.performanceDate asc,
                 lower(coalesce(res.name, rr.requestedName)) asc
    """)
    List<EventOrganizationTask> findAllOrdered();

    Optional<EventOrganizationTask> findByRequestResource_Id(Long requestResourceId);

    boolean existsByRequestResource_Id(Long requestResourceId);
}
