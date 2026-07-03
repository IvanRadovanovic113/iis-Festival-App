package com.festivalapp.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NegotiationAnalyticsInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        createNegotiationEfficiencyFunction();
        createAverageDurationFunction();
        createOfferOutcomeFunction();
        createOfferDurationFunction();
        createNegotiationAuditTrigger();
    }

    // 1. Efikasnost pregovora (Ukupno, Uspešno, Procenat)
    private void createNegotiationEfficiencyFunction() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_negotiation_efficiency(p_start_date DATE, p_end_date DATE)
            RETURNS TABLE(total_count BIGINT, successful_count BIGINT, success_percentage NUMERIC) AS $$
            BEGIN
                RETURN QUERY
                SELECT 
                    COUNT(*)::BIGINT,
                    SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END)::BIGINT,
                    ROUND(100.0 * SUM(CASE WHEN status = 'COMPLETED' THEN 1 ELSE 0 END) / NULLIF(COUNT(*), 0), 2)
                FROM negotiations
                WHERE created_at BETWEEN p_start_date AND p_end_date;
            END;
            $$ LANGUAGE plpgsql;
        """);
    }

    // 2. Prosečno trajanje pregovora po intervalima (trend)
    private void createAverageDurationFunction() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_negotiation_duration_trend(p_start_date DATE, p_end_date DATE, p_interval TEXT)
            RETURNS TABLE(interval_label TEXT, avg_duration NUMERIC) AS $$
            BEGIN
                RETURN QUERY
                SELECT 
                    TO_CHAR(created_at, p_interval),
                    ROUND(AVG(EXTRACT(EPOCH FROM (finished_at - created_at))/3600)::NUMERIC, 2)
                FROM negotiations
                WHERE finished_at IS NOT NULL 
                  AND created_at BETWEEN p_start_date AND p_end_date
                GROUP BY 1 ORDER BY 1;
            END;
            $$ LANGUAGE plpgsql;
        """);
    }

    // 3. Ishodi ponuda (Nikad prešle u pregovore, Propale, Uspešne)
    private void createOfferOutcomeFunction() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_offer_outcomes(p_start_date DATE, p_end_date DATE)
            RETURNS TABLE(outcome TEXT, count BIGINT) AS $$
            BEGIN
                RETURN QUERY
                SELECT 
                    CASE 
                        -- Ako nema pregovora, ponuda je u nekom od statusa inicijalne faze
                        WHEN n.id IS NULL THEN 
                            CASE 
                                WHEN o.status = 'DRAFT' THEN 'DRAFT (NEVER STARTED)'
                                WHEN o.status = 'PUBLISHED' THEN 'PUBLISHED (WAITING)'
                                WHEN o.status = 'ARCHIVED' THEN 'ARCHIVED (CLOSED)'
                                ELSE 'OTHER OFFER STATUS'
                            END
                        -- Ako ima pregovora, gledamo status pregovora
                        WHEN n.status = 'ACTIVE' THEN 'ACTIVE NEGOTIATION'
                        WHEN n.status = 'COMPLETED' THEN 'SUCCESSFUL CONTRACT'
                        WHEN n.status = 'FAILED' THEN 'FAILED NEGOTIATION'
                        ELSE 'UNKNOWN'
                    END as outcome,
                    COUNT(*)::BIGINT as count
                FROM offers o
                LEFT JOIN negotiations n ON o.offer_id = n.offer_id
                WHERE o.created_at::DATE BETWEEN p_start_date AND p_end_date
                GROUP BY 1;
            END;
            $$ LANGUAGE plpgsql;
        """);
    }

    // 4. Trajanje ponuda po intervalima
    private void createOfferDurationFunction() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_offer_duration_trend(p_start_date DATE, p_end_date DATE, p_interval TEXT)
            RETURNS TABLE(interval_label TEXT, avg_days NUMERIC) AS $$
            BEGIN
                RETURN QUERY
                SELECT 
                    TO_CHAR(created_at, p_interval),
                    ROUND(AVG(EXTRACT(EPOCH FROM (archived_at - created_at))/86400)::NUMERIC, 2)
                FROM offers
                WHERE archived_at IS NOT NULL
                AND created_at BETWEEN p_start_date AND p_end_date
                GROUP BY 1 ORDER BY 1;
            END;
            $$ LANGUAGE plpgsql;
        """);
    }

    private void createNegotiationAuditTrigger() {
        jdbcTemplate.execute("""
            CREATE TABLE negotiation_overdue_log (
                negotiation_id BIGINT PRIMARY KEY REFERENCES negotiations(id) ON DELETE CASCADE,
                created_at TIMESTAMP NOT NULL,
                deadline TIMESTAMP NOT NULL,
                overdue_hours NUMERIC,
                current_status VARCHAR(50),
                last_updated TIMESTAMP DEFAULT NOW()
            );
        """);

        // Kreiranje funkcije za trigger
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_track_overdue_negotiations()
            RETURNS TRIGGER AS $$
            DECLARE
                v_deadline TIMESTAMP;
                v_overdue_hours NUMERIC;
            BEGIN
                v_deadline := NEW.created_at + INTERVAL '72 hours';

                IF NEW.status IN ('COMPLETED', 'FAILED') THEN
                    DELETE FROM negotiation_overdue_log WHERE negotiation_id = NEW.id;
                
                ELSIF NEW.status = 'ACTIVE' AND v_deadline < NOW() THEN
                    v_overdue_hours := ROUND(EXTRACT(EPOCH FROM (NOW() - v_deadline))/3600, 2);
                    
                    INSERT INTO negotiation_overdue_log (negotiation_id, created_at, deadline, overdue_hours, current_status)
                    VALUES (NEW.id, NEW.created_at, v_deadline, v_overdue_hours, NEW.status::text)
                    ON CONFLICT (negotiation_id) DO UPDATE 
                    SET overdue_hours = EXCLUDED.overdue_hours, 
                        last_updated = NOW();
                END IF;

                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
        """);

        // Kreiranje samog triggera
        jdbcTemplate.execute("""
            CREATE TRIGGER trg_negotiation_overdue
            AFTER INSERT OR UPDATE ON negotiations
            FOR EACH ROW
            EXECUTE FUNCTION fn_track_overdue_negotiations();
        """);
    }
}