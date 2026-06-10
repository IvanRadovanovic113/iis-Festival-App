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
import com.festivalapp.model.eventorganization.StageResource;
import com.festivalapp.repository.eventorganization.EventReservationRequestRepository;
import com.festivalapp.repository.eventorganization.EventResourceRepository;
import com.festivalapp.repository.eventorganization.RequestResourceRepository;
import com.festivalapp.repository.eventorganization.StageResourceRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
    private final StageResourceRepository stageResourceRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        migrateKupciTierConstraint();
        migrateAssignmentRoleConstraint();
        migrateUsersRoleConstraint();
        migrateAdContentColumns();
        normalizeWorkflowPhaseAssignments();
        createStatisticsFunctions();
        migrateStageResourcesStageForeignKey();
        migrateEventReservationReviewNoteColumn();
        migrateRequestResourcesForCustomRequests();
        createAdminUser();
        seedEventOrganizationRequests();
        assignUnassignedEventResources();
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

        AdPhase managerReview = ensurePhase("MANAGER REVIEW", "Festival manager reviews the ad brief and workflow handoff.", 1, false, Role.FESTIVAL_MANAGER);
        AdPhase designContent = ensurePhase("DESIGN CONTENT", "Product designer prepares text and visual content.", 2, true, Role.PRODUCT_DESIGNER);
        AdPhase technicalContent = ensurePhase("TECHNICAL CONTENT", "Technical support prepares audio content.", 2, true, Role.TECHNICAL_SUPPORT);
        AdPhase directorApproval = ensurePhase("DIRECTOR APPROVAL", "Festival director performs the final approval.", 3, true, Role.FESTIVAL_DIRECTOR);
        AdPhase published = ensurePhase("PUBLISHED", "Published to the selected campaign channel.", 4, false, Role.FESTIVAL_MANAGER);

        ensureAdType("Animated", "Animated campaign assets for digital channels", "Video", managerReview, designContent, directorApproval, published);
        ensureAdType("Image", "Image-based campaign visuals and posters", "Image", managerReview, designContent, directorApproval, published);
        ensureAdType("Text", "Text-based copy for campaign communication", "Text", managerReview, designContent, directorApproval, published);
        ensureAdType("Audio", "Audio spots and supporting sound assets", "Audio", managerReview, technicalContent, directorApproval, published);

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

        ensureAd(demoCampaign, textType, managerReview, festivalManager, "Text EXIT 2026", "Seeded text ad waiting for manager review.", "Initial text content");
        ensureAd(demoCampaign, textType, designContent, productDesigner, "Text Promo EXIT 2026", "Seeded text ad for product designer testing.", "Draft campaign copy");
        ensureAd(demoCampaign, imageType, directorApproval, productDesigner, "Poster EXIT 2026", "Seeded image ad waiting for director approval.", "poster-exit-2026.png");
        ensureAd(demoCampaign, audioType, technicalContent, technicalSupport, "Audio EXIT 2026", "Seeded audio ad for technical support testing.", "audio-exit-2026.mp3");

        seedExpandedCampaignAds(festivalManager, festivalDirector, productDesigner, technicalSupport);
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
        ensureCustomRequestResource(coby, "Custom LED floor panels", "Equipment", 1);

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
        ensureCustomRequestResource(djSnake, "Hologram projector", "Equipment", 1);

        EventReservationRequest bicep = ensureReservation(festival, stageTwo, "Bicep",
            LocalDate.of(2026, 6, 22), LocalTime.of(22, 0), LocalTime.of(23, 0), EventReservationStatus.APPROVED, "Approved electronic set");
        ensureRequestResources(bicep, List.of(sound, lights, technicians, specialFx));

        EventReservationRequest massiveAttack = ensureReservation(festival, vipStage, "Massive Attack",
            LocalDate.of(2026, 6, 21), LocalTime.of(21, 0), LocalTime.of(21, 55), EventReservationStatus.PENDING, "VIP stage request");
        ensureRequestResources(massiveAttack, List.of(sound, lights, screens));

        EventReservationRequest florence = ensureReservation(festival, mainStage, "Florence + The Machine",
            LocalDate.of(2026, 6, 23), LocalTime.of(20, 30), LocalTime.of(21, 45), EventReservationStatus.PENDING, "Indie rock headline request");
        ensureRequestResources(florence, List.of(sound, lights, screens, security, technicians));

        EventReservationRequest disclosure = ensureReservation(festival, stageTwo, "Disclosure",
            LocalDate.of(2026, 6, 23), LocalTime.of(22, 0), LocalTime.of(23, 0), EventReservationStatus.PENDING, "Electronic live set request");
        ensureRequestResources(disclosure, List.of(sound, lights, specialFx, technicians));

        EventReservationRequest sigrid = ensureReservation(festival, smallStage, "Sigrid",
            LocalDate.of(2026, 6, 24), LocalTime.of(18, 30), LocalTime.of(19, 20), EventReservationStatus.PENDING, "Pop showcase request");
        ensureRequestResources(sigrid, List.of(sound, lights, technicians));

        EventReservationRequest kaytranada = ensureReservation(festival, vipStage, "Kaytranada",
            LocalDate.of(2026, 6, 24), LocalTime.of(21, 0), LocalTime.of(22, 0), EventReservationStatus.PENDING, "VIP dance set request");
        ensureRequestResources(kaytranada, List.of(sound, lights, specialFx));

        EventReservationRequest lorde = ensureReservation(festival, mainStage, "Lorde",
            LocalDate.of(2026, 6, 25), LocalTime.of(19, 45), LocalTime.of(20, 45), EventReservationStatus.PENDING, "Alternative pop performance request");
        ensureRequestResources(lorde, List.of(sound, lights, screens, security));

        EventReservationRequest overlapAurora = ensureReservation(festival, mainStage, "Aurora",
            LocalDate.of(2026, 6, 26), LocalTime.of(18, 0), LocalTime.of(19, 0), EventReservationStatus.PENDING, "Overlap test request on Main Stage");
        ensureRequestResources(overlapAurora, List.of(sound, lights, technicians));

        EventReservationRequest overlapFoals = ensureReservation(festival, mainStage, "Foals",
            LocalDate.of(2026, 6, 26), LocalTime.of(18, 30), LocalTime.of(19, 30), EventReservationStatus.PENDING, "Overlap test request on Main Stage");
        ensureRequestResources(overlapFoals, List.of(sound, lights, screens));

        EventReservationRequest overlapM83 = ensureReservation(festival, mainStage, "M83",
            LocalDate.of(2026, 6, 26), LocalTime.of(19, 15), LocalTime.of(20, 15), EventReservationStatus.PENDING, "Overlap test request on Main Stage");
        ensureRequestResources(overlapM83, List.of(sound, lights, specialFx));

        EventReservationRequest overlapRaye = ensureReservation(festival, mainStage, "Raye",
            LocalDate.of(2026, 6, 26), LocalTime.of(20, 0), LocalTime.of(21, 0), EventReservationStatus.PENDING, "Overlap test request on Main Stage");
        ensureRequestResources(overlapRaye, List.of(sound, screens, security));

        EventReservationRequest overlapJungle = ensureReservation(festival, mainStage, "Jungle",
            LocalDate.of(2026, 6, 26), LocalTime.of(20, 45), LocalTime.of(21, 45), EventReservationStatus.PENDING, "Overlap test request on Main Stage");
        ensureRequestResources(overlapJungle, List.of(sound, lights, technicians, specialFx));

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

    private void assignUnassignedEventResources() {
        for (Festival festival : festivalRepository.findAll()) {
            List<Stage> stages = stageRepository.findByFestival_FestivalId(festival.getFestivalId());
            if (stages.isEmpty()) {
                continue;
            }

            for (EventResource resource : eventResourceRepository.findByFestival_FestivalIdOrderByNameAsc(festival.getFestivalId())) {
                if (stageResourceRepository.existsByResource_Id(resource.getId())) {
                    continue;
                }

                stages.stream()
                    .filter(stage -> !stageResourceRepository.existsByStage_StageIdAndResource_NameIgnoreCase(
                        stage.getStageId(),
                        resource.getName()
                    ))
                    .findFirst()
                    .ifPresent(stage -> stageResourceRepository.save(StageResource.builder()
                        .stage(stage)
                        .resource(resource)
                        .quantity(resource.getTotalQuantity())
                        .build()));
            }
        }
        log.info("Unassigned event resources linked to available stages");
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

    private void ensureCustomRequestResource(EventReservationRequest request, String name, String type, int quantity) {
        if (requestResourceRepository.existsByReservationRequest_IdAndRequestedNameIgnoreCase(request.getId(), name)) {
            return;
        }
        requestResourceRepository.save(RequestResource.builder()
            .reservationRequest(request)
            .requestedName(name)
            .requestedType(type)
            .quantity(quantity)
            .status(RequestResourceStatus.REQUESTED)
            .build());
    }

    private AdPhase ensurePhase(String name, String description, int orderIndex, boolean emailNotification, Role assignedRole) {
        AdPhase phase = adPhaseRepository.findAll().stream()
            .filter(item -> item.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> AdPhase.builder().name(name).build());

        phase.setDescription(description);
        phase.setOrderIndex(orderIndex);
        phase.setEmailNotification(emailNotification);
        phase.setAssignedRole(assignedRole);
        return adPhaseRepository.save(phase);
    }

    private void ensureAdType(String name, String description, String contentType, AdPhase... phases) {
        AdType adType = adTypeRepository.findAll().stream()
            .filter(item -> item.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> AdType.builder().name(name).build());
        adType.setDescription(description);
        adType.setContentType(contentType);
        adType.setPhases(new ArrayList<>(java.util.Arrays.asList(phases)));
        adTypeRepository.save(adType);
    }

    private void ensureAd(Campaign campaign, AdType adType, AdPhase currentPhase, User lastEditedByUser, String name, String description, String contentValue) {
        Ad ad = adRepository.findAll().stream()
            .filter(item -> item.getCampaign().getCampaignId().equals(campaign.getCampaignId()) && item.getName().equalsIgnoreCase(name))
            .findFirst()
            .orElseGet(() -> Ad.builder().name(name).versionNumber(1).build());

        ad.setCampaign(campaign);
        ad.setAdType(adType);
        ad.setCurrentPhase(currentPhase);
        ad.setName(name);
        ad.setDescription(description);
        ad.setContentFileName(contentValue);
        ad.setLastChangeDate(LocalDate.now());
        ad.setLastEditedByUser(lastEditedByUser);
        ad.setLastEditedPhase(currentPhase);
        ad.setLastEditedRole(currentPhase.getAssignedRole());
        ad.setLastEditedAt(java.time.LocalDateTime.now());
        Ad savedAd = adRepository.save(ad);
        adVersionSnapshotService.captureSnapshot(savedAd);
    }

    private void seedExpandedCampaignAds(
        User defaultFestivalManager,
        User festivalDirector,
        User productDesigner,
        User technicalSupport
    ) {
        final int targetTotalAds = 100;
        long currentAds = adRepository.count();
        if (currentAds >= targetTotalAds) {
            log.info("Expanded campaign ad seed skipped because {} ads already exist", currentAds);
            return;
        }

        List<Campaign> campaigns = campaignRepository.findAll().stream()
            .sorted(Comparator.comparing(Campaign::getCampaignId))
            .toList();
        if (campaigns.isEmpty()) {
            return;
        }

        List<AdType> adTypes = adTypeRepository.findAllByOrderByNameAsc();
        if (adTypes.isEmpty()) {
            return;
        }

        List<SeedTemplate> templates = buildSeedTemplates(adTypes);
        if (templates.isEmpty()) {
            return;
        }

        int remainingAds = (int) (targetTotalAds - currentAds);
        int templateIndex = 0;
        int cycle = 1;

        while (remainingAds > 0) {
            for (Campaign campaign : campaigns) {
                if (remainingAds <= 0) {
                    break;
                }

                SeedTemplate template = templates.get(templateIndex % templates.size());
                User editor = resolveEditorForPhase(
                    template.phase(),
                    campaign.getManagerUser() != null ? campaign.getManagerUser() : defaultFestivalManager,
                    festivalDirector,
                    productDesigner,
                    technicalSupport
                );

                String name = "%s %s %02d".formatted(
                    template.adType().getName(),
                    normalizeForSeedName(template.phase().getName()),
                    cycle
                );
                String description = "Seeded %s ad in phase %s for richer dashboard statistics.".formatted(
                    template.adType().getName().toLowerCase(Locale.ROOT),
                    template.phase().getName()
                );
                String contentValue = buildSeedContentValue(template.adType(), campaign, cycle);

                ensureAd(campaign, template.adType(), template.phase(), editor, name, description, contentValue);

                remainingAds--;
                templateIndex++;
            }
            cycle++;
        }

        log.info("Expanded campaign ads seeded up to approximately {} total ads", targetTotalAds);
    }

    private List<SeedTemplate> buildSeedTemplates(List<AdType> adTypes) {
        List<SeedTemplate> templates = new ArrayList<>();
        for (AdType adType : adTypes) {
            List<AdPhase> phases = adType.getPhases();
            for (AdPhase phase : phases) {
                templates.add(new SeedTemplate(adType, phase));
            }
        }
        return templates;
    }

    private User resolveEditorForPhase(
        AdPhase phase,
        User festivalManager,
        User festivalDirector,
        User productDesigner,
        User technicalSupport
    ) {
        return switch (phase.getAssignedRole()) {
            case FESTIVAL_MANAGER -> festivalManager;
            case FESTIVAL_DIRECTOR -> festivalDirector;
            case PRODUCT_DESIGNER -> productDesigner;
            case TECHNICAL_SUPPORT -> technicalSupport;
            default -> festivalManager;
        };
    }

    private String buildSeedContentValue(AdType adType, Campaign campaign, int cycle) {
        String campaignSlug = normalizeForSeedName(campaign.getName()).toLowerCase(Locale.ROOT);
        return switch (adType.getContentType()) {
            case "Text" -> "Seeded copy %02d for %s".formatted(cycle, campaignSlug);
            case "Image" -> "seeded-%s-image-%02d.png".formatted(campaignSlug, cycle);
            case "Audio" -> "seeded-%s-audio-%02d.mp3".formatted(campaignSlug, cycle);
            case "Video" -> "seeded-%s-video-%02d.mp4".formatted(campaignSlug, cycle);
            default -> "seeded-content-%02d".formatted(cycle);
        };
    }

    private String normalizeForSeedName(String value) {
        return value.toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
    }

    private record SeedTemplate(AdType adType, AdPhase phase) {
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

    private void migrateStageResourcesStageForeignKey() {
        jdbcTemplate.execute(
            "ALTER TABLE stage_resources DROP CONSTRAINT IF EXISTS fk2u66vfasyd73286d218p2cp3k");
        log.info("Legacy stage_resources foreign key to bine removed");
    }

    private void migrateEventReservationReviewNoteColumn() {
        jdbcTemplate.execute(
            "ALTER TABLE event_reservation_requests DROP COLUMN IF EXISTS review_note");
        log.info("Legacy event reservation review_note column removed");
    }

    private void migrateRequestResourcesForCustomRequests() {
        jdbcTemplate.execute(
            "ALTER TABLE request_resources ALTER COLUMN resource_id DROP NOT NULL");
        log.info("request_resources.resource_id migrated to allow custom one-time requested resources");
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

    private void migrateAdContentColumns() {
        jdbcTemplate.execute("ALTER TABLE ads ALTER COLUMN content_file_name TYPE TEXT");
        jdbcTemplate.execute("ALTER TABLE ad_versions ALTER COLUMN content_value TYPE TEXT");
        log.info("Ad content columns migrated to TEXT");
    }

    private void normalizeWorkflowPhaseAssignments() {
        jdbcTemplate.update("""
            UPDATE ad_phases
            SET assigned_role = 'FESTIVAL_DIRECTOR'
            WHERE lower(name) LIKE '%direktor%' OR lower(name) LIKE '%director%'
            """);
        jdbcTemplate.update("""
            UPDATE ad_phases
            SET assigned_role = 'FESTIVAL_MANAGER'
            WHERE lower(name) LIKE '%menager%' OR lower(name) LIKE '%manager%'
            """);
        log.info("Workflow phase assignments normalized by phase name");
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

    private void createStatisticsFunctions() {
        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_filtered_ads_for_statistics(
                p_campaign_id BIGINT DEFAULT NULL,
                p_date_from DATE DEFAULT NULL,
                p_date_to DATE DEFAULT NULL,
                p_ad_type_id BIGINT DEFAULT NULL
            )
            RETURNS TABLE (
                ad_id BIGINT,
                phase_id BIGINT,
                ad_type_id BIGINT
            )
            AS $$
            DECLARE
                sql_query TEXT;
            BEGIN
                sql_query := '
                    SELECT a.ad_id, a.phase_id, a.ad_type_id
                    FROM ads a
                    WHERE 1 = 1
                ';

                IF p_campaign_id IS NOT NULL THEN
                    sql_query := sql_query || format(' AND a.campaign_id = %s', p_campaign_id);
                END IF;

                IF p_date_from IS NOT NULL THEN
                    sql_query := sql_query || format(' AND a.last_change_date >= %L', p_date_from);
                END IF;

                IF p_date_to IS NOT NULL THEN
                    sql_query := sql_query || format(' AND a.last_change_date <= %L', p_date_to);
                END IF;

                IF p_ad_type_id IS NOT NULL THEN
                    sql_query := sql_query || format(' AND a.ad_type_id = %s', p_ad_type_id);
                END IF;

                RETURN QUERY EXECUTE sql_query;
            END;
            $$ LANGUAGE plpgsql;
            """);

        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_statistics_total_ads(
                p_campaign_id BIGINT DEFAULT NULL,
                p_date_from DATE DEFAULT NULL,
                p_date_to DATE DEFAULT NULL,
                p_ad_type_id BIGINT DEFAULT NULL
            )
            RETURNS BIGINT
            AS $$
            DECLARE
                total_ads BIGINT;
            BEGIN
                SELECT COUNT(*)
                INTO total_ads
                FROM fn_filtered_ads_for_statistics(p_campaign_id, p_date_from, p_date_to, p_ad_type_id);

                RETURN total_ads;
            END;
            $$ LANGUAGE plpgsql;
            """);

        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_statistics_phase_counts(
                p_campaign_id BIGINT DEFAULT NULL,
                p_date_from DATE DEFAULT NULL,
                p_date_to DATE DEFAULT NULL,
                p_ad_type_id BIGINT DEFAULT NULL
            )
            RETURNS TABLE (
                phase_id BIGINT,
                name TEXT,
                order_index INTEGER,
                total_count BIGINT
            )
            AS $$
            BEGIN
                RETURN QUERY
                SELECT
                    p.phase_id,
                    p.name::TEXT,
                    p.order_index,
                    COALESCE(COUNT(fa.ad_id), 0)::BIGINT AS total_count
                FROM ad_phases p
                LEFT JOIN fn_filtered_ads_for_statistics(p_campaign_id, p_date_from, p_date_to, p_ad_type_id) fa
                    ON fa.phase_id = p.phase_id
                GROUP BY p.phase_id, p.name, p.order_index
                ORDER BY p.order_index, p.name;
            END;
            $$ LANGUAGE plpgsql;
            """);

        jdbcTemplate.execute("""
            CREATE OR REPLACE FUNCTION fn_statistics_type_counts(
                p_campaign_id BIGINT DEFAULT NULL,
                p_date_from DATE DEFAULT NULL,
                p_date_to DATE DEFAULT NULL,
                p_ad_type_id BIGINT DEFAULT NULL
            )
            RETURNS TABLE (
                ad_type_id BIGINT,
                name TEXT,
                total_count BIGINT
            )
            AS $$
            BEGIN
                RETURN QUERY
                SELECT
                    t.ad_type_id,
                    t.name::TEXT,
                    COALESCE(COUNT(fa.ad_id), 0)::BIGINT AS total_count
                FROM ad_types t
                LEFT JOIN fn_filtered_ads_for_statistics(p_campaign_id, p_date_from, p_date_to, p_ad_type_id) fa
                    ON fa.ad_type_id = t.ad_type_id
                GROUP BY t.ad_type_id, t.name
                ORDER BY t.name;
            END;
            $$ LANGUAGE plpgsql;
            """);

        log.info("Statistics PL/pgSQL functions created/updated");
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
