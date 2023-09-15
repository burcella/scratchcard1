package cm.connect.technology.scratchcard.repositories;


import cm.connect.technology.scratchcard.entities.ScratchFormatGain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScratchFormatGainRepository extends JpaRepository<ScratchFormatGain, String> {
    ScratchFormatGain findByName(String name);
}
