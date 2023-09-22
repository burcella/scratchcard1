package cm.connect.technology.scratchcard.entities;

import cm.connect.technology.scratchcard.enums.StatusTicketEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Ticket implements Serializable {
    public static final long serializableUID = 1l;
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
   // private String categoryName;
   @Enumerated(EnumType.STRING)
    private StatusTicketEnum status;
    private Boolean isUse;
    private  double amount; // prix du ticket

}
