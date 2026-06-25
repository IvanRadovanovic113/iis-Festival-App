package com.festivalapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventOrganizationFunctionInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createSummaryFunction();
        createTopResourcesFunction();
        createStageOccupancyFunction();
    }

    // 4 kartice
    private void createSummaryFunction() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_resource_analytics_summary(
                p_festival_id  BIGINT,
                p_year         INTEGER DEFAULT NULL,
                p_month        INTEGER DEFAULT NULL,
                p_stage_id     BIGINT  DEFAULT NULL
            )
            RETURNS TABLE (
                total_reservations       BIGINT,
                most_used_resource_name  TEXT,
                most_used_resource_count BIGINT,
                avg_stage_occupancy      NUMERIC,
                extra_resource_requests  BIGINT
            )
            AS $$
            DECLARE
                v_total_reservations       BIGINT;
                v_most_used_resource_name  TEXT;
                v_most_used_resource_count BIGINT;
                v_avg_stage_occupancy      NUMERIC;
                v_extra_resource_requests  BIGINT;
            BEGIN
                -- Ukupan broj rezervacija
                SELECT COUNT(*)
                INTO v_total_reservations
                FROM event_reservation_requests r
                WHERE r.festival_id = p_festival_id
                  AND (p_year    IS NULL OR EXTRACT(YEAR  FROM r.performance_date) = p_year)
                  AND (p_month   IS NULL OR EXTRACT(MONTH FROM r.performance_date) = p_month)
                  AND (p_stage_id IS NULL OR r.stage_id = p_stage_id);

                -- Najtrazeniji resurs
                SELECT er.name, COUNT(rr.id)
                INTO v_most_used_resource_name, v_most_used_resource_count
                FROM request_resources rr
                JOIN event_resources er ON er.id = rr.resource_id
                JOIN event_reservation_requests req ON req.id = rr.reservation_request_id
                WHERE req.festival_id = p_festival_id
                  AND rr.resource_id IS NOT NULL
                  AND (p_year    IS NULL OR EXTRACT(YEAR  FROM req.performance_date) = p_year)
                  AND (p_month   IS NULL OR EXTRACT(MONTH FROM req.performance_date) = p_month)
                  AND (p_stage_id IS NULL OR req.stage_id = p_stage_id)
                GROUP BY er.name
                ORDER BY COUNT(rr.id) DESC
                LIMIT 1;

                -- Prosecno zauzece bine = approved rezervacije / ukupno rezervacija * 100
                SELECT COALESCE(
                    ROUND(
                        100.0 * SUM(CASE WHEN r.status = 'APPROVED' THEN 1 ELSE 0 END)
                              / NULLIF(COUNT(*), 0),
                        1
                    ), 0
                )
                INTO v_avg_stage_occupancy
                FROM event_reservation_requests r
                WHERE r.festival_id = p_festival_id
                  AND (p_year    IS NULL OR EXTRACT(YEAR  FROM r.performance_date) = p_year)
                  AND (p_month   IS NULL OR EXTRACT(MONTH FROM r.performance_date) = p_month)
                  AND (p_stage_id IS NULL OR r.stage_id = p_stage_id);

                -- Custom resursi
                SELECT COUNT(*)
                INTO v_extra_resource_requests
                FROM request_resources rr
                JOIN event_reservation_requests req ON req.id = rr.reservation_request_id
                WHERE req.festival_id = p_festival_id
                  AND rr.resource_id IS NULL
                  AND (p_year    IS NULL OR EXTRACT(YEAR  FROM req.performance_date) = p_year)
                  AND (p_month   IS NULL OR EXTRACT(MONTH FROM req.performance_date) = p_month)
                  AND (p_stage_id IS NULL OR req.stage_id = p_stage_id);

                RETURN QUERY SELECT
                    v_total_reservations,
                    COALESCE(v_most_used_resource_name,  'None'),
                    COALESCE(v_most_used_resource_count, 0),
                    v_avg_stage_occupancy,
                    v_extra_resource_requests;
            END;
            $$ LANGUAGE plpgsql;
            """);
    }

    // Top 5 resursa
    private void createTopResourcesFunction() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_resource_top_resources(
                p_festival_id BIGINT,
                p_year        INTEGER DEFAULT NULL,
                p_month       INTEGER DEFAULT NULL,
                p_stage_id    BIGINT  DEFAULT NULL
            )
            RETURNS TABLE (
                resource_name  TEXT,
                request_count  BIGINT
            )
            AS $$
            BEGIN
                RETURN QUERY
                SELECT
                    er.name::TEXT,
                    COUNT(rr.id) AS request_count
                FROM request_resources rr
                JOIN event_resources er ON er.id = rr.resource_id
                JOIN event_reservation_requests req ON req.id = rr.reservation_request_id
                WHERE req.festival_id = p_festival_id
                  AND rr.resource_id IS NOT NULL
                  AND (p_year    IS NULL OR EXTRACT(YEAR  FROM req.performance_date) = p_year)
                  AND (p_month   IS NULL OR EXTRACT(MONTH FROM req.performance_date) = p_month)
                  AND (p_stage_id IS NULL OR req.stage_id = p_stage_id)
                GROUP BY er.name
                ORDER BY COUNT(rr.id) DESC
                LIMIT 5;
            END;
            $$ LANGUAGE plpgsql;
            """);
    }

    // Zauzetost svake bine
    private void createStageOccupancyFunction() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_resource_stage_occupancy(
                p_festival_id BIGINT,
                p_year        INTEGER DEFAULT NULL,
                p_month       INTEGER DEFAULT NULL
            )
            RETURNS TABLE (
                stage_id             BIGINT,
                stage_name           TEXT,
                total_reservations   BIGINT,
                approved_reservations BIGINT,
                occupancy_percent    NUMERIC
            )
            AS $$
            BEGIN
                RETURN QUERY
                SELECT
                    s.stage_id,
                    s.name::TEXT,
                    COUNT(r.id) AS total_reservations,
                    COUNT(CASE WHEN r.status = 'APPROVED' THEN 1 END) AS approved_reservations,
                    COALESCE(
                        ROUND(
                            100.0 * COUNT(CASE WHEN r.status = 'APPROVED' THEN 1 END)
                                  / NULLIF(COUNT(r.id), 0),
                            1
                        ), 0
                    ) AS occupancy_percent
                FROM stages s
                LEFT JOIN event_reservation_requests r
                    ON r.stage_id = s.stage_id
                    AND r.festival_id = p_festival_id
                    AND (p_year  IS NULL OR EXTRACT(YEAR  FROM r.performance_date) = p_year)
                    AND (p_month IS NULL OR EXTRACT(MONTH FROM r.performance_date) = p_month)
                WHERE s.festival_id = p_festival_id
                GROUP BY s.stage_id, s.name
                ORDER BY s.name;
            END;
            $$ LANGUAGE plpgsql;
            """);
    }
}
