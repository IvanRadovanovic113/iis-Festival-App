package com.festivalapp.repository;

import com.festivalapp.dto.StatisticsPhaseCountResponse;
import com.festivalapp.dto.StatisticsTypeCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatisticsQueryRepository {

    private static final String TOTAL_ADS_SQL = """
        SELECT fn_statistics_total_ads(CAST(? AS BIGINT), CAST(? AS DATE), CAST(? AS DATE), CAST(? AS BIGINT))
        """;

    private static final String PHASE_COUNTS_SQL = """
        SELECT phase_id, name, order_index, total_count
        FROM fn_statistics_phase_counts(CAST(? AS BIGINT), CAST(? AS DATE), CAST(? AS DATE), CAST(? AS BIGINT))
        ORDER BY order_index, name
        """;

    private static final String TYPE_COUNTS_SQL = """
        SELECT ad_type_id, name, total_count
        FROM fn_statistics_type_counts(CAST(? AS BIGINT), CAST(? AS DATE), CAST(? AS DATE), CAST(? AS BIGINT))
        ORDER BY name
        """;

    private final JdbcTemplate jdbcTemplate;

    public long getTotalAds(Long campaignId, LocalDate dateFrom, LocalDate dateTo, Long adTypeId) {
        Long result = jdbcTemplate.queryForObject(
            TOTAL_ADS_SQL,
            Long.class,
            campaignId,
            toSqlDate(dateFrom),
            toSqlDate(dateTo),
            adTypeId
        );
        return result == null ? 0L : result;
    }

    public List<StatisticsPhaseCountResponse> getPhaseCounts(Long campaignId, LocalDate dateFrom, LocalDate dateTo, Long adTypeId) {
        return jdbcTemplate.query(
            PHASE_COUNTS_SQL,
            (rs, rowNum) -> new StatisticsPhaseCountResponse(
                rs.getLong("phase_id"),
                rs.getString("name"),
                rs.getInt("order_index"),
                rs.getLong("total_count")
            ),
            campaignId,
            toSqlDate(dateFrom),
            toSqlDate(dateTo),
            adTypeId
        );
    }

    public List<StatisticsTypeCountResponse> getTypeCounts(Long campaignId, LocalDate dateFrom, LocalDate dateTo, Long adTypeId) {
        return jdbcTemplate.query(
            TYPE_COUNTS_SQL,
            (rs, rowNum) -> new StatisticsTypeCountResponse(
                rs.getLong("ad_type_id"),
                rs.getString("name"),
                rs.getLong("total_count")
            ),
            campaignId,
            toSqlDate(dateFrom),
            toSqlDate(dateTo),
            adTypeId
        );
    }

    private Date toSqlDate(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }
}
