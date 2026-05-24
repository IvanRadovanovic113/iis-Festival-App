package com.festivalapp.config;

import com.festivalapp.model.Festival;
import com.festivalapp.model.FestivalStatus;
import com.festivalapp.model.Role;
import com.festivalapp.model.Stage;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.model.AdPhase;
import com.festivalapp.model.AdType;
import com.festivalapp.repository.FestivalRepository;
import com.festivalapp.repository.StageRepository;
import com.festivalapp.repository.AdPhaseRepository;
import com.festivalapp.repository.AdTypeRepository;
import com.festivalapp.repository.UserFestivalAssignmentRepository;
import com.festivalapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

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

    @Override
    public void run(ApplicationArguments args) {
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
        ensureAdType("Text", "Text-based copy for campaign communication", "Text", draft, approvedTechnical, approved, rejected, published);
        ensureAdType("Audio", "Audio spots and supporting sound assets", "Audio", draft, approvedTechnical, approved, rejected, published);
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
}
