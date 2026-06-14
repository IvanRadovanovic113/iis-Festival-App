package com.festivalapp.service;

import com.festivalapp.model.User;
import com.festivalapp.prodaja.model.Karta;
import com.festivalapp.prodaja.model.Kupovina;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketMailService {

    private final JavaMailSender javaMailSender;
    private final QrCodeService qrCodeService;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.from:}")
    private String fromAddress;

    private static final int QR_SIZE = 300;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Async
    public void sendTicketEmail(User user, Kupovina kupovina, List<Karta> karte) {
        if (!mailEnabled) {
            log.info("Mail delivery skipped (app.mail.enabled=false). Kupovina #{}", kupovina.getKupovinaId());
            return;
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Cannot send ticket email — user {} has no email", user.getUsername());
            return;
        }

        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            if (fromAddress != null && !fromAddress.isBlank()) {
                helper.setFrom(fromAddress);
            }
            helper.setTo(user.getEmail());
            helper.setSubject("Vaše karte za " + kupovina.getTicketType().getFestival().getName());

            StringBuilder html = new StringBuilder();
            html.append("<html><body style='font-family:sans-serif;color:#222;'>")
                .append("<h2>Hvala na kupovini!</h2>")
                .append("<p>Festival: <strong>").append(esc(kupovina.getTicketType().getFestival().getName())).append("</strong></p>")
                .append("<p>Tip karte: <strong>").append(esc(kupovina.getTicketType().getName())).append("</strong></p>")
                .append("<p>Datum kupovine: ").append(kupovina.getDatum().format(DATE_FMT)).append("</p>")
                .append("<p>Ukupna cena: <strong>").append(kupovina.getUkupnaCena()).append(" RSD</strong></p>")
                .append("<p>Broj karata: ").append(karte.size()).append("</p>")
                .append("<hr/>");

            for (Karta karta : karte) {
                String cid = "qr-" + karta.getKartaId();
                html.append("<div style='margin:20px 0;'>")
                    .append("<p style='margin:4px 0;font-size:13px;color:#555;'>Karta #").append(karta.getKartaId()).append("</p>")
                    .append("<img src='cid:").append(cid).append("' width='").append(QR_SIZE).append("' height='").append(QR_SIZE).append("' alt='QR kod'/>")
                    .append("</div>");
            }

            html.append("</body></html>");
            helper.setText(html.toString(), true);

            for (Karta karta : karte) {
                byte[] qrBytes = qrCodeService.generatePng(karta.getQrKod(), QR_SIZE);
                helper.addInline("qr-" + karta.getKartaId(), new ByteArrayResource(qrBytes), "image/png");
            }

            javaMailSender.send(message);
            log.info("Ticket email sent to {} for kupovina #{}", user.getEmail(), kupovina.getKupovinaId());

        } catch (MessagingException | com.google.zxing.WriterException | java.io.IOException e) {
            log.warn("Failed to send ticket email to {} for kupovina #{}", user.getEmail(), kupovina.getKupovinaId(), e);
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
