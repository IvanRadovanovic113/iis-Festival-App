package com.festivalapp.prodaja.dto;

import com.festivalapp.prodaja.model.Karta;
import com.festivalapp.prodaja.model.Kupovina;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PurchaseResponse {

    private Long kupovinaId;
    private LocalDateTime datum;
    private String festivalName;
    private String ticketTypeName;
    private Integer totalTickets;
    private BigDecimal finalPrice;
    private List<KartaDto> karte;

    @Getter
    @Builder
    public static class KartaDto {
        private Long kartaId;
        private String qrKod;
        private String status;

        public static KartaDto from(Karta k) {
            return KartaDto.builder()
                .kartaId(k.getKartaId())
                .qrKod(k.getQrKod())
                .status(k.getStatus().name())
                .build();
        }
    }

    public static PurchaseResponse from(Kupovina kupovina, List<Karta> karte) {
        return PurchaseResponse.builder()
            .kupovinaId(kupovina.getKupovinaId())
            .datum(kupovina.getDatum())
            .festivalName(kupovina.getTicketType().getFestival().getName())
            .ticketTypeName(kupovina.getTicketType().getName())
            .totalTickets(kupovina.getKolicina())
            .finalPrice(kupovina.getUkupnaCena())
            .karte(karte.stream().map(KartaDto::from).toList())
            .build();
    }
}
