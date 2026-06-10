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
        where task.requestResource.reservationRequest.festival = :festival
        order by task.requestResource.reservationRequest.performanceDate asc,
                 lower(coalesce(task.requestResource.resource.name, task.requestResource.requestedName)) asc
    """)
    List<EventOrganizationTask> findByFestival(@Param("festival") Festival festival);

    @Query("""
        select task
        from EventOrganizationTask task
        where task.requestResource.reservationRequest.festival = :festival
          and task.status = :status
        order by task.requestResource.reservationRequest.performanceDate asc,
                 lower(coalesce(task.requestResource.resource.name, task.requestResource.requestedName)) asc
    """)
    List<EventOrganizationTask> findByFestivalAndStatus(
        @Param("festival") Festival festival,
        @Param("status") EventOrganizationTaskStatus status
    );

    @Query("""
        select task
        from EventOrganizationTask task
        where task.status = :status
        order by task.requestResource.reservationRequest.performanceDate asc,
                 lower(coalesce(task.requestResource.resource.name, task.requestResource.requestedName)) asc
    """)
    List<EventOrganizationTask> findByStatusOrdered(@Param("status") EventOrganizationTaskStatus status);

    @Query("""
        select task
        from EventOrganizationTask task
        order by task.requestResource.reservationRequest.performanceDate asc,
                 lower(coalesce(task.requestResource.resource.name, task.requestResource.requestedName)) asc
    """)
    List<EventOrganizationTask> findAllOrdered();

    Optional<EventOrganizationTask> findByRequestResource_Id(Long requestResourceId);

    boolean existsByRequestResource_Id(Long requestResourceId);
}
