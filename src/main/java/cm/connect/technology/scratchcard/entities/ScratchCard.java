package cm.connect.technology.scratchcard.entities;

import cm.connect.technology.scratchcard.enums.FormatScratchEnum;
import lombok.*;

import javax.persistence.*;
import java.util.Date;

@Entity

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ScratchCard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String gameName; // pour connaitre a quelle game il a ete genere
    private String boxValue; // contient les valeurs a gratter
    private double prizeAmount; // contient le montant a gagner si la carte est grattée
    private int poolsNumber;  // preciser a quelle session le jeu est lance parcequ'il y a generalement plusieurs session
    @Enumerated(EnumType.STRING)
    private FormatScratchEnum format;  // permet de definir le format des tickets gagnants
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;
    private Boolean isScratched;  // permet de definir si une card a deja ete utilise ou pas
    private Boolean isWinning;  // pour pouvoir faire un get de tous les ticket gagnant, il est specifier lors de la generation des card
    @OneToOne(cascade = CascadeType.ALL)
    private Ticket ticket; // un ticket est attribué à une seule card et une card est attribuée à un seul ticket
}
