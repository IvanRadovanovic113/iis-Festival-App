package com.festivalapp.config;

import com.festivalapp.model.Bina;
import com.festivalapp.model.Festival;
import com.festivalapp.model.FestivalStatus;
import com.festivalapp.model.Role;
import com.festivalapp.model.User;
import com.festivalapp.model.UserFestivalAssignment;
import com.festivalapp.repository.BinaRepository;
import com.festivalapp.repository.FestivalRepository;
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
    private final BinaRepository binaRepository;
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
            log.info("Admin korisnik kreiran: admin / admin123");
        }

        Festival demoFestival = festivalRepository.findAll().stream()
            .findFirst()
            .orElseGet(() -> festivalRepository.save(Festival.builder()
                .naziv("Demo Festival")
                .lokacija("Novi Sad")
                .status(FestivalStatus.NADOLAZECI)
                .datumPocetka(LocalDate.now().plusMonths(1))
                .datumZavrsetka(LocalDate.now().plusMonths(1).plusDays(3))
                .build()));

        if (binaRepository.findByFestival_FestivalId(demoFestival.getFestivalId()).isEmpty()) {
            binaRepository.save(Bina.builder()
                .naziv("Main Stage")
                .kapacitet(10000)
                .lokacija("Central Zone")
                .festival(demoFestival)
                .build());
        }

        User eventOrganizer = userRepository.findByUsername("event.organizer")
            .orElseGet(() -> userRepository.save(User.builder()
                .username("event.organizer")
                .email("event.organizer@festivalapp.com")
                .password(passwordEncoder.encode("event123"))
                .role(Role.EVENT_ORGANIZER)
                .build()));

        if (eventOrganizer.getRole() != Role.EVENT_ORGANIZER) {
            eventOrganizer.setRole(Role.EVENT_ORGANIZER);
            userRepository.save(eventOrganizer);
        }

        UserFestivalAssignment assignment = assignmentRepository.findByUser_Id(eventOrganizer.getId())
            .orElse(UserFestivalAssignment.builder().user(eventOrganizer).build());
        assignment.setFestival(demoFestival);
        assignment.setRole(Role.EVENT_ORGANIZER);
        assignmentRepository.save(assignment);
        log.info("Event organizer user ready: event.organizer / event123");
    }
}
