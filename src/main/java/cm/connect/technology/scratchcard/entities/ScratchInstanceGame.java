package cm.connect.technology.scratchcard.entities;

import cm.connect.technology.scratchcard.enums.StatusGameEnum;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "INSTANCE_SCRATCH_GAME")
public class ScratchInstanceGame {
    @Id
    @Column(nullable = false, name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "ID_SAASGAME", nullable = false)
    private Long idSaasGame;
    @Column(nullable = false, name = "NAME")
    private String name;
    @Column(nullable = false, name = "CREATE_DATE")
    private LocalDateTime createDate;
    @Column(nullable = false, name = "START_DATE")
    private LocalDateTime startDate;
    @Column(nullable = false, name = "END_DATE")
    private LocalDateTime endDate;
    @Column(nullable = false, name = "POOLS_NUMBER")
    private int numberOfSession;
    @Column(nullable = false, name = "TICKET_PRICE")
    private double ticketPrice;
    @Column(nullable = false, name = "CREATED_BY")
    private String createdBy;
    @ManyToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="PRIZES_DISTRIBUTION")
    private List<PrizesDistribution> prizesDistributions;
//    @Column(nullable = false, name = "TOTAL_TICKET_PRICE")
//    private Long totalTicketPrice;
   // @Column(name = "FORMAT_GAIN")
    @OneToOne(cascade = CascadeType.ALL)
    private ScratchFormatGain scratchFormatGain;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="INSTANCE_SESSION")
    private List<Session> sessions;
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name="SCRATCH_CARD")
    private List<ScratchCard> scratchCards;
    @Column(nullable = true, name = "STATUS")
    @Enumerated(EnumType.STRING)
    private StatusGameEnum status;

    private Long totalCard;

}
