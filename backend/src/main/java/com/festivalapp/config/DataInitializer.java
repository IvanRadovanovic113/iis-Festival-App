package com.festivalapp.config;

import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.prodaja.model.KupacTier;
import com.festivalapp.prodaja.model.TierConfig;
import com.festivalapp.prodaja.repository.TierConfigRepository;
import com.festivalapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final TierConfigRepository tierConfigRepository;

    @Override
    public void run(ApplicationArguments args) {
        migrateKupciTierConstraint();
        createAdminUser();
        seedTierConfig();
        createTriggers();
    }

    // ─── Admin korisnik ──────────────────────────────────────────────────────

    private void createAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                .username("admin")
                .email("admin@festivalapp.com")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .build();
            userRepository.save(admin);
            log.info("Admin user created: admin / admin123");
        }
    }

    // ─── DB migracije ────────────────────────────────────────────────────────

    /**
     * Stari CHECK constraint dozvoljava samo BRONZE/SILVER/GOLD.
     * Zamenjujemo ga novim koji uključuje i STANDARD.
     */
    private void migrateKupciTierConstraint() {
        jdbcTemplate.execute(
            "ALTER TABLE kupci DROP CONSTRAINT IF EXISTS kupci_tier_check");
        jdbcTemplate.execute(
            "ALTER TABLE kupci ADD CONSTRAINT kupci_tier_check " +
            "CHECK (tier IN ('STANDARD','BRONZE','SILVER','GOLD'))");
        log.info("kupci_tier_check constraint updated to include STANDARD");
    }

    // ─── Tier konfiguracija ──────────────────────────────────────────────────

    private void seedTierConfig() {
        if (tierConfigRepository.count() == 0) {
            tierConfigRepository.saveAll(List.of(
                TierConfig.builder().tier(KupacTier.BRONZE).minTickets(5).discountPercent(5).build(),
                TierConfig.builder().tier(KupacTier.SILVER).minTickets(20).discountPercent(10).build(),
                TierConfig.builder().tier(KupacTier.GOLD).minTickets(50).discountPercent(15).build()
            ));
            log.info("Tier config seeded: BRONZE(5 karata/5%), SILVER(20/10%), GOLD(50/15%)");
        }
    }

    // ─── DB Trigeri ──────────────────────────────────────────────────────────

    private void createTriggers() {
        createSoldCountTrigger();
        createUkupnoKupovinesTrigger();
        log.info("DB triggers created/updated");
    }

    /**
     * Nakon INSERT na karte → inkrement ticket_types.sold_count za 1.
     * Radi JOIN kupovine → ticket_types.
     */
    private void createSoldCountTrigger() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_update_sold_count()
            RETURNS TRIGGER AS $$
            BEGIN
                UPDATE ticket_types tt
                SET sold_count = sold_count + 1
                FROM kupovine k
                WHERE k.kupovina_id = NEW.kupovina_id
                  AND k.ticket_type_id = tt.ticket_type_id;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
            """);

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_sold_count ON karte;");
        jdbcTemplate.execute("""
            CREATE TRIGGER trg_sold_count
            AFTER INSERT ON karte
            FOR EACH ROW
            EXECUTE FUNCTION fn_update_sold_count();
            """);
    }

    /**
     * Nakon INSERT na karte → inkrement kupci.ukupno_kupovina za 1.
     * Radi JOIN kupovine → kupci.
     */
    private void createUkupnoKupovinesTrigger() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_update_ukupno_kupovina()
            RETURNS TRIGGER AS $$
            BEGIN
                UPDATE kupci kc
                SET ukupno_kupovina = ukupno_kupovina + 1
                FROM kupovine k
                WHERE k.kupovina_id = NEW.kupovina_id
                  AND k.kupac_id = kc.kupac_id;
                RETURN NEW;
            END;
            $$ LANGUAGE plpgsql;
            """);

        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_ukupno_kupovina ON karte;");
        jdbcTemplate.execute("""
            CREATE TRIGGER trg_ukupno_kupovina
            AFTER INSERT ON karte
            FOR EACH ROW
            EXECUTE FUNCTION fn_update_ukupno_kupovina();
            """);
    }
}
