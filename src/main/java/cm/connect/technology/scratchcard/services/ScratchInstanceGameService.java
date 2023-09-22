package cm.connect.technology.scratchcard.services;

import cm.connect.technology.scratchcard.dto.CreateGameDto;
import cm.connect.technology.scratchcard.dto.CreateSessionDto;
import cm.connect.technology.scratchcard.dto.ResponseDto;
import cm.connect.technology.scratchcard.entities.ScratchInstanceGame;
import cm.connect.technology.scratchcard.entities.Session;
import cm.connect.technology.scratchcard.entities.Ticket;
import cm.connect.technology.scratchcard.exceptions.DAOException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public interface ScratchInstanceGameService {

    ResponseDto<ScratchInstanceGame> createInstanceGame(CreateGameDto createGameDto, Locale locale);


    ResponseDto<ScratchInstanceGame> startInstance(LocalDateTime startDate, Locale locale);

    ResponseDto<ScratchInstanceGame> endInstance(LocalDateTime startDate, Locale locale);
    ResponseDto<ScratchInstanceGame> desactiveSession(LocalDateTime startDate, Locale locale);

    ResponseDto<List<ScratchInstanceGame>> getAll(Locale locale) throws DAOException;


    ResponseDto<List<ScratchInstanceGame>> getAllInstance(Locale locale);
    ResponseDto<ScratchInstanceGame> findInstanceByName(String name, Locale locale);
    ResponseDto<ScratchInstanceGame> findInstanceByStartDate(Date startDate, Locale locale);

    ResponseDto<ScratchInstanceGame> findInstanceByStartDate(LocalDateTime startDate, Locale locale);

    ResponseDto<ScratchInstanceGame> findInstanceByEndDate(Date endDate, Locale locale);

    ResponseDto<ScratchInstanceGame> findInstanceByEndDate(LocalDateTime endDate, Locale locale);

    List<Session> generateSession(int numberOfSession, int timeInstance, Session dto, Locale locale);
    ResponseDto<ScratchInstanceGame> generateTicket(Ticket ticket, Locale locale);


}
