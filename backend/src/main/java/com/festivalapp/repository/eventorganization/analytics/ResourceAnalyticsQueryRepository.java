package com.festivalapp.repository.eventorganization.analytics;

import com.festivalapp.dto.eventorganization.analytics.ResourceAnalyticsResponse;
import com.festivalapp.dto.eventorganization.analytics.ResourceStageOccupancyResponse;
import com.festivalapp.dto.eventorganization.analytics.ResourceTopResourceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ResourceAnalyticsQueryRepository {

    private final JdbcTemplate jdbcTemplate;

    public ResourceAnalyticsResponse getSummary(Long festivalId, Integer year, Integer month, Long stageId) {
        String sql = """
            SELECT total_reservations, most_used_resource_name, most_used_resource_count,
                   avg_stage_occupancy, extra_resource_requests
            FROM fn_resource_analytics_summary(?, ?, ?, ?)
            """;

        return jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
            new ResourceAnalyticsResponse(
                rs.getLong("total_reservations"),
                rs.getString("most_used_resource_name"),
                rs.getLong("most_used_resource_count"),
                rs.getDouble("avg_stage_occupancy"),
                rs.getLong("extra_resource_requests"),
                List.of(),
                List.of()
            ),
            festivalId, year, month, stageId
        );
    }

    public List<ResourceTopResourceResponse> getTopResources(Long festivalId, Integer year, Integer month, Long stageId) {
        String sql = """
            SELECT resource_name, request_count
            FROM fn_resource_top_resources(?, ?, ?, ?)
            """;

        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new ResourceTopResourceResponse(
                rs.getString("resource_name"),
                rs.getLong("request_count")
            ),
            festivalId, year, month, stageId
        );
    }

    public List<ResourceStageOccupancyResponse> getStageOccupancy(Long festivalId, Integer year, Integer month) {
        String sql = """
            SELECT stage_id, stage_name, total_reservations, approved_reservations, occupancy_percent
            FROM fn_resource_stage_occupancy(?, ?, ?)
            """;

        return jdbcTemplate.query(sql,
            (rs, rowNum) -> new ResourceStageOccupancyResponse(
                rs.getLong("stage_id"),
                rs.getString("stage_name"),
                rs.getLong("total_reservations"),
                rs.getLong("approved_reservations"),
                rs.getDouble("occupancy_percent")
            ),
            festivalId, year, month
        );
    }
}
