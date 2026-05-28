package com.festivalapp.config;

import com.festivalapp.model.Festival;
import com.festivalapp.model.FestivalStatus;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.model.Stage;
import com.festivalapp.model.AdPhase;
import com.festivalapp.model.AdType;
import com.festivalapp.model.Ad;
import com.festivalapp.model.Campaign;
import com.festivalapp.model.eventorganization.EventReservationRequest;
import com.festivalapp.model.eventorganization.EventReservationStatus;
import com.festivalapp.model.eventorganization.EventResource;
import com.festivalapp.model.eventorganization.RequestResource;
import com.festivalapp.model.eventorganization.RequestResourceStatus;
import com.festivalapp.repository.eventorganization.EventReservationRequestRepository;
import com.festivalapp.repository.eventorganization.EventResourceRepository;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import com.festivalapp.prodaja.model.KupacTier;
import com.festivalapp.prodaja.model.TierConfig;
import com.festivalapp.prodaja.repository.TierConfigRepository;
import com.festivalapp.repository.FestivalRepository;
import com.festivalapp.repository.StageRepository;
import com.festivalapp.repository.AdPhaseRepository;
import com.festivalapp.repository.AdTypeRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import com.festivalapp.repository.UserRepository;
import com.festivalapp.repository.CampaignRepository;
import com.festivalapp.repository.AdRepository;
import com.festivalapp.service.AdVersionSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final FestivalRepository festivalRepository;
    private final StageRepository stageRepository;
    private final AdPhaseRepository adPhaseRepository;
    private final AdTypeRepository adTypeRepository;
    private final UserFestivalAssignmentRepository assignmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final TierConfigRepository tierConfigRepository;
    private final CampaignRepository campaignRepository;
    private final AdRepository adRepository;
    private final AdVersionSnapshotService adVersionSnapshotService;
    private final EventReservationRequestRepository reservationRequestRepository;
    private final EventResourceRepository eventResourceRepository;
    private final RequestResourceRepository requestResourceRepository;

    @Override
    public void run(ApplicationArguments args) {
        migrateKupciTierConstraint();
        migrateAssignmentRoleConstraint();
        migrateUsersRoleConstraint();
        createAdminUser();
        seedEventOrganizationRequests();
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

        Festival demoFestival = festivalRepository.findAll().stream()
            .findFirst()
            .orElseGet(() -> festivalRepository.save(Festival.builder()
                .name("Demo Festival")
                .location("Novi Sad")
                .status(FestivalStatus.UPCOMING)
                .startDate(LocalDate.now().plusMonths(1))
                .endDate(LocalDate.now().plusMonths(1).plusDays(3))
                .build()));

        if (stageRepository.findByFestival_FestivalId(demoFestival.getFestivalId()).isEmpty()) {
            stageRepository.save(Stage.builder()
                .name("Main Stage")
                .capacity(10000)
                .location("Central Zone")
                .festival(demoFestival)
                .build());
        }

        User eventOrganizer = userRepository.findByUsername("event.organizer")
            .orElseGet(() -> userRepository.save(User.builder()
                .username("event.organizer")
                .email("event.organizer@festivalapp.com")
                .password(passwordEncoder.encode("event123"))
                .role(null)
                .build()));

        if (eventOrganizer.getRole() != null) {
            eventOrganizer.setRole(null);
            userRepository.save(eventOrganizer);
        }

        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(eventOrganizer.getId())
            .orElse(UserFestivalAssignment.builder().user(eventOrganizer).build());
        assignment.setFestival(demoFestival);
        assignment.setRole(Role.EVENT_ORGANIZER);
        assignmentRepository.save(assignment);
        log.info("Event organizer user ready: event.organizer / event123");

        User festivalDirector = userRepository.findByUsername("festival.director")
            .orElseGet(() -> userRepository.save(User.builder()
                .username("festival.director")
                .email("festival.director@festivalapp.com")
                .password(passwordEncoder.encode("director123"))
                .role(null)
                .build()));

        if (festivalDirector.getRole() != null) {
            festivalDirector.setRole(null);
            userRepository.save(festivalDirector);
        }

        UserFestivalAssignment directorAssignment = assignmentRepository.findByUser_Id(festivalDirector.getId())
            .orElse(UserFestivalAssignment.builder().user(festivalDirector).build());
        directorAssignment.setFestival(demoFestival);
        directorAssignment.setRole(Role.FESTIVAL_DIRECTOR);
        assignmentRepository.save(directorAssignment);
        log.info("Festival director user ready: festival.director / director123");

        User festivalManager = userRepository.findByUsername("festival.manager")
            .orElseGet(() -> userRepository.save(User.builder()
                .username("festival.manager")
                .email("festival.manager@festivalapp.com")
                .password(passwordEncoder.encode("manager123"))
                .role(null)
                .build()));

        if (festivalManager.getRole() != null) {
            festivalManager.setRole(null);
            userRepository.save(festivalManager);
        }

        UserFestivalAssignment managerAssignment = assignmentRepository.findByUser_Id(festivalManager.getId())
            .orElse(UserFestivalAssignment.builder().user(festivalManager).build());
        managerAssignment.setFestival(demoFestival);
        managerAssignment.setRole(Role.FESTIVAL_MANAGER);
        assignmentRepository.save(managerAssignment);
        log.info("Festival manager user ready: festival.manager / manager123");

        User productDesigner = userRepository.findByUsername("product.designer")
            .orElseGet(() -> userRepository.save(User.builder()
                .username("product.designer")
                .email("product.designer@festivalapp.com")
                .password(passwordEncoder.encode("designer123"))
                .role(null)
                .build()));

        UserFestivalAssignment productDesignerAssignment = assignmentRepository.findByUser_Id(productDesigner.getId())
            .orElse(UserFestivalAssignment.builder().user(productDesigner).build());
        productDesignerAssignment.setFestival(demoFestival);
        productDesignerAssignment.setRole(Role.PRODUCT_DESIGNER);
        assignmentRepository.save(productDesignerAssignment);
        log.info("Product designer user ready: product.designer / designer123");

        User technicalSupport = userRepository.findByUsername("technical.support")
            .orElseGet(() -> userRepository.save(User.builder()
                .username("technical.support")
                .email("technical.support@festivalapp.com")
                .password(passwordEncoder.encode("support123"))
                .role(null)
                .build()));

        UserFestivalAssignment technicalSupportAssignment = assignmentRepository.findByUser_Id(technicalSupport.getId())
            .orElse(UserFestivalAssignment.builder().user(technicalSupport).build());
        technicalSupportAssignment.setFestival(demoFestival);
        technicalSupportAssignment.setRole(Role.TECHNICAL_SUPPORT);
        assignmentRepository.save(technicalSupportAssignment);
        log.info("Technical support user ready: technical.support / support123");

        AdPhase draft = ensurePhase("DRAFT", "Added basic info", 1, false, Role.PRODUCT_DESIGNER);
        AdPhase approvedTechnical = ensurePhase("APPROVED TECHNICAL", "Technical review completed", 2, true, Role.TECHNICAL_SUPPORT);
        AdPhase visuallyPrepared = ensurePhase("VISUALLY PREPARED", "Creative assets visually prepared", 3, false, Role.PRODUCT_DESIGNER);
        AdPhase approved = ensurePhase("APPROVED", "Final approval completed", 4, true, Role.TECHNICAL_SUPPORT);
        AdPhase rejected = ensurePhase("REJECTED", "Rejected after review", 5, true, Role.TECHNICAL_SUPPORT);
        AdPhase published = ensurePhase("PUBLISHED", "Published to the selected channel", 6, false, Role.PRODUCT_DESIGNER);

        ensureAdType("Animated", "Animated campaign assets for digital channels", "Video", draft, approvedTechnical, visuallyPrepared, approved, rejected, published);
        ensureAdType("Image", "Image-based campaign visuals and posters", "Image", draft, visuallyPrepared, approved, rejected, published);
        ensureAdType("Text", "Text-based copy for campaign communication", "Text", draft, approvedTechnical, approved, rejected, published);
        ensureAdType("Audio", "Audio spots and supporting sound assets", "Audio", draft, approvedTechnical, approved, rejected, published);

        Campaign demoCampaign = campaignRepository.findByFestival_FestivalId(demoFestival.getFestivalId())
            .orElseGet(() -> campaignRepository.save(Campaign.builder()
                .name("EXIT Campaign")
                .description("Default seeded campaign for creative workflow testing.")
                .startDate(demoFestival.getStartDate())
                .endDate(demoFestival.getEndDate())
                .festival(demoFestival)
                .managerUser(festivalManager)
                .build()));

        AdType textType = adTypeRepository.findAllByOrderByNameAsc().stream()
            .filter(type -> type.getName().equalsIgnoreCase("Text"))
            .findFirst()
            .orElseThrow();
        AdType imageType = adTypeRepository.findAllByOrderByNameAsc().stream()
            .filter(type -> type.getName().equalsIgnoreCase("Image"))
            .findFirst()
            .orElseThrow();
        AdType audioType = adTypeRepository.findAllByOrderByNameAsc().stream()
            .filter(type -> type.getName().equalsIgnoreCase("Audio"))
            .findFirst()
            .orElseThrow();

        ensureAd(demoCampaign, textType, draft, "Text EXIT 2026", "Seeded text ad for product designer testing.", "Initial text content");
        ensureAd(demoCampaign, imageType, visuallyPrepared, "Poster EXIT 2026", "Seeded image ad for product designer testing.", "poster-exit-2026.png");
        ensureAd(demoCampaign, audioType, approvedTechnical, "Audio EXIT 2026", "Seeded audio ad for technical support testing.", "audio-exit-2026.mp3");
    }

    private void seedEventOrganizationRequests() {
        Festival festival = festivalRepository.findAll().stream().findFirst().orElse(null);
        if (festival == null) {
            return;
        }

        Stage mainStage = ensureStage(festival, "Main Stage", 10000, "Central Zone");
        Stage stageTwo = ensureStage(festival, "Stage 2", 6000, "East Field");
        Stage smallStage = ensureStage(festival, "Small Stage", 2200, "Garden Zone");
        Stage vipStage = ensureStage(festival, "VIP Stage", 1200, "VIP Lounge");

        EventResource sound = ensureEventResource(festival, "Line array sound system", "Equipment", 6);
        EventResource lights = ensureEventResource(festival, "Intelligent lighting rig", "Equipment", 8);
        EventResource screens = ensureEventResource(festival, "LED screen panels", "Equipment", 10);
        EventResource security = ensureEventResource(festival, "Security crew", "Personnel", 40);
        EventResource technicians = ensureEventResource(festival, "Stage technicians", "Personnel", 24);
        EventResource specialFx = ensureEventResource(festival, "Smoke and pyrotechnics", "Special FX", 5);

        EventReservationRequest weeknd = ensureReservation(festival, mainStage, "The Weeknd",
            LocalDate.of(2026, 6, 15), LocalTime.of(20, 0), LocalTime.of(21, 30), EventReservationStatus.PENDING, "Headline set awaiting stage slot confirmation");
        ensureRequestResources(weeknd, List.of(sound, lights, screens, security, technicians));

        EventReservationRequest dua = ensureReservation(festival, stageTwo, "Dua Lipa",
            LocalDate.of(2026, 6, 16), LocalTime.of(19, 30), LocalTime.of(20, 45), EventReservationStatus.PENDING, "Dance-pop performance request");
        ensureRequestResources(dua, List.of(sound, lights, screens));

        EventReservationRequest marija = ensureReservation(festival, smallStage, "Marija B.",
            LocalDate.of(2026, 6, 17), LocalTime.of(18, 0), LocalTime.of(19, 0), EventReservationStatus.PENDING, "Local performer request");
        ensureRequestResources(marija, List.of(sound, technicians));

        EventReservationRequest coby = ensureReservation(festival, stageTwo, "Coby B.",
            LocalDate.of(2026, 6, 17), LocalTime.of(19, 0), LocalTime.of(19, 45), EventReservationStatus.APPROVED, "Approved rap performance");
        ensureRequestResources(coby, List.of(sound, lights, security, technicians));

        EventReservationRequest stromae = ensureReservation(festival, mainStage, "Stromae",
            LocalDate.of(2026, 6, 20), LocalTime.of(21, 0), LocalTime.of(22, 20), EventReservationStatus.PENDING, "Main stage request");
        ensureRequestResources(stromae, List.of(sound, lights, screens, security, technicians));

        EventReservationRequest portishead = ensureReservation(festival, smallStage, "Portishead",
            LocalDate.of(2026, 6, 21), LocalTime.of(18, 0), LocalTime.of(19, 10), EventReservationStatus.APPROVED, "Approved trip-hop performance");
        ensureRequestResources(portishead, List.of(sound, technicians));

        EventReservationRequest calvin = ensureReservation(festival, mainStage, "Calvin Harris",
            LocalDate.of(2026, 6, 18), LocalTime.of(21, 0), LocalTime.of(22, 0), EventReservationStatus.APPROVED, "Approved EDM performance");
        ensureRequestResources(calvin, List.of(sound, lights, screens, security, technicians, specialFx));

        EventReservationRequest djSnake = ensureReservation(festival, vipStage, "DJ Snake",
            LocalDate.of(2026, 6, 19), LocalTime.of(20, 0), LocalTime.of(20, 45), EventReservationStatus.APPROVED, "Approved VIP stage performance");
        ensureRequestResources(djSnake, List.of(sound, lights, specialFx));

        EventReservationRequest bicep = ensureReservation(festival, stageTwo, "Bicep",
            LocalDate.of(2026, 6, 22), LocalTime.of(22, 0), LocalTime.of(23, 0), EventReservationStatus.APPROVED, "Approved electronic set");
        ensureRequestResources(bicep, List.of(sound, lights, technicians, specialFx));

        EventReservationRequest massiveAttack = ensureReservation(festival, vipStage, "Massive Attack",
            LocalDate.of(2026, 6, 21), LocalTime.of(21, 0), LocalTime.of(21, 55), EventReservationStatus.PENDING, "VIP stage request");
        ensureRequestResources(massiveAttack, List.of(sound, lights, screens));

        EventReservationRequest tameImpala = ensureReservation(festival, mainStage, "Tame Impala",
            LocalDate.of(2026, 5, 10), LocalTime.of(20, 0), LocalTime.of(21, 30), EventReservationStatus.APPROVED, "Past performance");
        ensureRequestResources(tameImpala, List.of(sound, lights, screens, security, technicians));

        EventReservationRequest billie = ensureReservation(festival, mainStage, "Billie Eilish",
            LocalDate.of(2026, 5, 11), LocalTime.of(19, 0), LocalTime.of(20, 15), EventReservationStatus.APPROVED, "Past performance");
        ensureRequestResources(billie, List.of(sound, lights, screens, security));

        EventReservationRequest arctic = ensureReservation(festival, stageTwo, "Arctic Monkeys",
            LocalDate.of(2026, 5, 12), LocalTime.of(18, 0), LocalTime.of(19, 0), EventReservationStatus.APPROVED, "Past performance");
        ensureRequestResources(arctic, List.of(sound, lights, technicians));

        EventReservationRequest radiohead = ensureReservation(festival, mainStage, "Radiohead",
            LocalDate.of(2026, 5, 5), LocalTime.of(21, 0), LocalTime.of(22, 40), EventReservationStatus.APPROVED, "Past performance");
        ensureRequestResources(radiohead, List.of(sound, lights, screens, security, technicians, specialFx));

        EventReservationRequest trisha = ensureReservation(festival, stageTwo, "Thrisha Paytas",
            LocalDate.of(2026, 5, 6), LocalTime.of(19, 0), LocalTime.of(20, 15), EventReservationStatus.APPROVED, "Past performance");
        ensureRequestResources(trisha, List.of(sound, lights, technicians));

        log.info("Event organization reservation requests seeded");
    }

    private Stage ensureStage(Festival festival, String name, int capacity, String location) {
        return stageRepository.findByFestival_FestivalId(festival.getFestivalId()).stream()
            .filter(stage -> stage.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> stageRepository.save(Stage.builder()
                .name(name)
                .capacity(capacity)
                .location(location)
                .festival(festival)
                .build()));
    }

    private EventResource ensureEventResource(Festival festival, String name, String type, int totalQuantity) {
        return eventResourceRepository.findByFestival_FestivalIdOrderByNameAsc(festival.getFestivalId()).stream()
            .filter(resource -> resource.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> eventResourceRepository.save(EventResource.builder()
                .name(name)
                .type(type)
            .description("Seeded resource for event organization requests")
            .totalQuantity(totalQuantity)
            .shareable(false)
            .festival(festival)
            .build()));
    }

    private EventReservationRequest ensureReservation(
            Festival festival,
            Stage stage,
            String performerName,
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            EventReservationStatus status,
            String notes) {
        return reservationRequestRepository.findByFestival_FestivalIdOrderByPerformanceDateAscStartTimeAsc(festival.getFestivalId())
            .stream()
            .filter(request -> request.getPerformerName().equalsIgnoreCase(performerName)
                && request.getPerformanceDate().equals(date))
            .findFirst()
            .orElseGet(() -> reservationRequestRepository.save(EventReservationRequest.builder()
                .festival(festival)
                .performerName(performerName)
                .stage(stage)
                .performanceDate(date)
                .startTime(startTime)
                .endTime(endTime)
                .status(status)
                .notes(notes)
                .build()));
    }

    private void ensureRequestResources(EventReservationRequest request, List<EventResource> resources) {
        for (EventResource resource : resources) {
            if (requestResourceRepository.existsByReservationRequest_IdAndResource_Id(request.getId(), resource.getId())) {
                continue;
            }
            requestResourceRepository.save(RequestResource.builder()
                .reservationRequest(request)
                .resource(resource)
                .quantity(1)
                .status(request.getStatus() == EventReservationStatus.APPROVED
                    ? RequestResourceStatus.CONFIRMED
                    : RequestResourceStatus.REQUESTED)
                .build());
        }
    }

    private AdPhase ensurePhase(String name, String description, int orderIndex, boolean emailNotification, Role assignedRole) {
        return adPhaseRepository.findAll().stream()
            .filter(phase -> phase.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> adPhaseRepository.save(AdPhase.builder()
                .name(name)
                .description(description)
                .orderIndex(orderIndex)
                .emailNotification(emailNotification)
                .assignedRole(assignedRole)
                .build()));
    }

    private void ensureAdType(String name, String description, String contentType, AdPhase... phases) {
        boolean exists = adTypeRepository.findAll().stream()
            .anyMatch(adType -> adType.getName().equalsIgnoreCase(name));
        if (exists) {
            return;
        }
        adTypeRepository.save(AdType.builder()
            .name(name)
            .description(description)
            .contentType(contentType)
            .phases(java.util.Arrays.stream(phases).toList())
            .build());
    }

    private void ensureAd(Campaign campaign, AdType adType, AdPhase currentPhase, String name, String description, String contentValue) {
        boolean exists = adRepository.findAll().stream()
            .anyMatch(ad -> ad.getCampaign().getCampaignId().equals(campaign.getCampaignId()) && ad.getName().equalsIgnoreCase(name));
        if (exists) {
            return;
        }

        Ad ad = adRepository.save(Ad.builder()
            .campaign(campaign)
            .adType(adType)
            .currentPhase(currentPhase)
            .name(name)
            .description(description)
            .contentFileName(contentValue)
            .lastChangeDate(LocalDate.now())
            .versionNumber(1)
            .build());
        adVersionSnapshotService.captureSnapshot(ad);
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

    /**
     * Stari CHECK constraint na users.role možda ne uključuje BUYER koji je dodat
     * u našoj grani. Zamenjujemo ga novim koji pokriva cijeli Role enum.
     */
    private void migrateUsersRoleConstraint() {
        jdbcTemplate.execute(
            "ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check");
        jdbcTemplate.execute(
            "ALTER TABLE users ADD CONSTRAINT users_role_check " +
            "CHECK (role IS NULL OR role IN ('ADMIN','FESTIVAL_DIRECTOR','FESTIVAL_MANAGER','PRODUCT_DESIGNER'," +
            "'TECHNICAL_SUPPORT','SALES_DIRECTOR','SALES_MANAGER','EVENT_ORGANIZER'," +
            "'MARKETING_MANAGER','BUYER','NEGOTIATION_MANAGER'))");
        log.info("users_role_check constraint updated with all roles");
    }

    /**
     * Stari CHECK constraint na user_festival_assignments.role možda ne uključuje
     * sve uloge koje su dodate nakon merge-a (EVENT_ORGANIZER, SALES_DIRECTOR, itd.).
     * Zamenjujemo ga novim koji pokriva cijeli Role enum.
     */
    private void migrateAssignmentRoleConstraint() {
        jdbcTemplate.execute(
            "ALTER TABLE user_festival_assignments DROP CONSTRAINT IF EXISTS user_festival_assignments_role_check");
        jdbcTemplate.execute(
            "ALTER TABLE user_festival_assignments ADD CONSTRAINT user_festival_assignments_role_check " +
            "CHECK (role IN ('ADMIN','FESTIVAL_DIRECTOR','FESTIVAL_MANAGER','PRODUCT_DESIGNER'," +
            "'TECHNICAL_SUPPORT','SALES_DIRECTOR','SALES_MANAGER','EVENT_ORGANIZER'," +
            "'MARKETING_MANAGER','BUYER','NEGOTIATION_MANAGER'))");
        log.info("user_festival_assignments_role_check constraint updated with all roles");
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
