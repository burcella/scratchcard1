package cm.connect.technology.scratchcard.repositories;

import cm.connect.technology.scratchcard.entities.ScratchFormatGain;
import cm.connect.technology.scratchcard.entities.ScratchInstanceGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

public interface ScratchInstanceGameRepository extends JpaRepository<ScratchInstanceGame,Long> {
    ScratchInstanceGame findByName(String name);

    ScratchInstanceGame findByStartDate(Date startDate);

    ScratchInstanceGame findByEndDate(Date endDate);
}
