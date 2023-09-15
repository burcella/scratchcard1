package cm.connect.technology.scratchcard.services.impl;

import cm.connect.technology.scratchcard.dto.CreateGameDto;
import cm.connect.technology.scratchcard.dto.CreateSessionDto;
import cm.connect.technology.scratchcard.dto.ResponseDto;
import cm.connect.technology.scratchcard.entities.ScratchCard;
import cm.connect.technology.scratchcard.entities.ScratchInstanceGame;
import cm.connect.technology.scratchcard.entities.Session;
import cm.connect.technology.scratchcard.enums.StatusGameEnum;
import cm.connect.technology.scratchcard.exceptions.DAOException;
import cm.connect.technology.scratchcard.repositories.ScratchInstanceGameRepository;
import cm.connect.technology.scratchcard.services.ScratchInstanceGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class ScratchInstanceGameServiceImpl implements ScratchInstanceGameService {
    @Autowired
    ScratchInstanceGameRepository scratchInstanceGameRepository;
    @Override
    public ResponseDto<ScratchInstanceGame> createInstanceGame(CreateGameDto dto, Locale locale) {



        ResponseDto<ScratchInstanceGame> response = new ResponseDto<>();
        List<String> messages = new ArrayList<String>();
        try{
            // Calculer le nombre de minute entre la date de début et la date de fin de l'instance
            long nombreMinutes = Duration.between(dto.getStartDate(), dto.getEndDate()).toMinutes();

            //System.out.println("Nombre de minutes entre la date de début et la date de fin : " + nombreMinutes);

            // Calculer le nombre de minute par sessions à générer
            long nombreMinutesPerSession = nombreMinutes/ dto.getNumberOfSession();


            // on construire l'instance
            ScratchInstanceGame instanceGame = new ScratchInstanceGame();
            instanceGame = ScratchInstanceGame.builder()
                    .idSaasGame(dto.getIdSaasGame())
                    .createDate(LocalDateTime.now())
                    .name(dto.getName())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .numberOfSession(dto.getNumberOfSession())
                    .ticketPrice(dto.getTicketPrice())
                    .prizesDistributions(dto.getPrizesDistributions())
                    .scratchFormatGain(dto.getScratchFormatGain())
                    .sessions(new ArrayList<>())// creer une methode qui genere les sessions et l'importe ici
                    .status(StatusGameEnum.TRAITEMENT)
                    .scratchCards(new ArrayList<>())  // creer une methode qui genere les cartes
                    .createdBy("du token on recupere le nom de celui qui cree")
                    .totalCard(dto.getTotalCard())
                    .build();
            scratchInstanceGameRepository.save(instanceGame);

            // On genere toutes les sessions de l'instance et on les stocke en base de données
            // On initialise la date de debut de la premiere session
            LocalDateTime debutDeSession = dto.getStartDate();
            Session instanceGameSession= new Session();
            for (int i = 1; i <= dto.getNumberOfSession(); i++) {
                // On Calcule la date de fin pour chaque session
                LocalDateTime finDeSession = debutDeSession.plus(nombreMinutesPerSession, ChronoUnit.MINUTES);
                 instanceGameSession =  Session.builder()
                        .id(UUID.randomUUID().toString())
                        .instanceGameId(instanceGame.getId())
                        .startTime(debutDeSession)
                        .endTime(finDeSession)
                        .ouvert(false)
                        .build();
                //On ajoute la session cree dans l'Instance de jeu correspondant
                instanceGame.getSessions().add(instanceGameSession);
                // On met à jour la date de debut de session pour la session suivante
                debutDeSession = finDeSession;
            }
            // on save l'instanceGame avec ses sessions
            System.out.println("Nombre de minutes entre la date de début et la date de fin : " + nombreMinutes);
            //scratchInstanceGameRepository.save(instanceGame);
            System.out.println("Nombre de minutes entre la date de début et la date de fin : " + nombreMinutes);
            response.setBody(scratchInstanceGameRepository.save(instanceGame));;

            response.setStatus(HttpStatus.OK);
            response.setMessages(messages);



        }catch(Exception ignored){

        }

        return response;
    }

    @Override
    public ResponseDto<ScratchInstanceGame> startInstance(String name, Locale locale) {
        return null;
    }


    @Override
    public ResponseDto<ScratchInstanceGame> startInstance(ScratchInstanceGame instanceGame, Locale locale) {
        ResponseDto<ScratchInstanceGame> response = new ResponseDto<>();
        List<String> messages = new ArrayList<String>();
        try{
            ScratchInstanceGame game = new ScratchInstanceGame();
            game.setStartDate(instanceGame.getStartDate());
            response.setBody(game);
            response.setStatus(HttpStatus.OK);

            return response;

        } catch (Exception e) {
            // En cas d'erreur lors de l'accès à la base de données, lancez une DAOException
            throw new DAOException("Erreur lors du démarrage de l'instance", e);
        }


        }
    @Override
    public ResponseDto<List<ScratchInstanceGame>> getAll(Locale locale) throws DAOException {
        ResponseDto<List<ScratchInstanceGame>> response = new ResponseDto<>();
        List<String> messages = new ArrayList<>();
        try {
            List<ScratchInstanceGame> scratchFormatGains = scratchInstanceGameRepository.findAll();
            response.setBody(scratchFormatGains);
        } catch (DAOException e) {
            messages.add(e.getMessage());
            throw new DAOException(e);
        }
        if (!messages.isEmpty()) response.setStatus(HttpStatus.BAD_REQUEST);
        else response.setStatus(HttpStatus.OK);
        response.setMessages(messages);
        return response;
    }






    @Override
    public ResponseDto<ScratchInstanceGame> endInstance(ScratchInstanceGame instanceGame, Locale locale) {
        ResponseDto<ScratchInstanceGame> response = new ResponseDto<>();
        List<String> messages = new ArrayList<String>();
        try{
            ScratchInstanceGame game = new ScratchInstanceGame();
            game.setEndDate(instanceGame.getEndDate());
            response.setBody(game);
            response.setStatus(HttpStatus.OK);

            return response;

        } catch (Exception e) {
            // En cas d'erreur lors de l'accès à la base de données, lancez une DAOException
            throw new DAOException("Erreur lors de la fermeture de l'instance", e);
        }
    }

    @Override
    public ResponseDto<List<ScratchInstanceGame>> getAllInstance(Locale locale) {
        ResponseDto<List<ScratchInstanceGame>> response = new ResponseDto<>();
        List<String> messages = new ArrayList<>();
        try {
            List<ScratchInstanceGame> scratchInstanceGames = scratchInstanceGameRepository.findAll();
            response.setBody(scratchInstanceGames);
        } catch (DAOException e) {
            messages.add(e.getMessage());
            throw new DAOException(e);
        }
        if (!messages.isEmpty()) response.setStatus(HttpStatus.BAD_REQUEST);
        else response.setStatus(HttpStatus.OK);
        response.setMessages(messages);
        return response;
    }

    @Override
    public ResponseDto<ScratchInstanceGame> findInstanceByName(String name, Locale locale) {
        ResponseDto<ScratchInstanceGame> responseDto = new ResponseDto<>();
        List<String> messages = new ArrayList<>();
        try{
            ScratchInstanceGame InstanceGame= scratchInstanceGameRepository.findByName(name);
            if (InstanceGame != null){
                responseDto.setBody(InstanceGame);
            }else {
                messages.add("the name was not registered in Database");
//                messages.add(messageSource.getMessage("errors.not-found.gameSaas", null, locale));
               responseDto.setBody(null);
            } }catch (DAOException e){
            responseDto.setStatus(e.getStatus());
            messages.add(e.getMessage());
        }
        if(messages.isEmpty()){
            responseDto.setStatus(HttpStatus.OK);
        }else if (responseDto.getStatus() == null){
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
        }
        responseDto.setMessages(messages);
        return responseDto;
    }




    @Override
    public ResponseDto<ScratchInstanceGame> findInstanceByStartDate(Date startDate, Locale locale) {
        ResponseDto<ScratchInstanceGame> responseDto = new ResponseDto<>();
        List<String> messages = new ArrayList<>();
        try{
            ScratchInstanceGame InstanceGame= scratchInstanceGameRepository.findByStartDate(startDate);
            if (InstanceGame != null){
                responseDto.setBody(InstanceGame);
            }else {
                messages.add("no ictance was started in this starDate on the dataBase");

                responseDto.setBody(null);
            } }catch (DAOException e){
            responseDto.setStatus(e.getStatus());
            messages.add(e.getMessage());
        }
        if(messages.isEmpty()){
            responseDto.setStatus(HttpStatus.OK);
        }else if (responseDto.getStatus() == null){
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
        }
        responseDto.setMessages(messages);
        return responseDto;
    }


    @Override
    public ResponseDto<ScratchInstanceGame> findInstanceByEndDate(Date endDate, Locale locale) {
        ResponseDto<ScratchInstanceGame> responseDto = new ResponseDto<>();
        List<String> messages = new ArrayList<>();
        try{
            ScratchInstanceGame InstanceGame= scratchInstanceGameRepository.findByEndDate(endDate);
            if (InstanceGame != null){
                responseDto.setBody(InstanceGame);
            }else {
                messages.add("no instance was ended in this endDate on the dataBase");

                responseDto.setBody(null);
            } }catch (DAOException e){
            responseDto.setStatus(e.getStatus());
            messages.add(e.getMessage());
        }
        if(messages.isEmpty()){
            responseDto.setStatus(HttpStatus.OK);
        }else if (responseDto.getStatus() == null){
            responseDto.setStatus(HttpStatus.BAD_REQUEST);
        }
        responseDto.setMessages(messages);
        return responseDto;
    }

    @Override
    public List<Session> generateSession(int numberOfSession, int timeInstance, Session dto, Locale locale) {
        // Calculer le nombre de minute par sessions à générer
        long nombreMinutesPerSession = timeInstance/numberOfSession;

        // On initialise la date de debut de la premiere session
        LocalDateTime debutDeSession = dto.getStartTime();
        for (int i = 1; i <= numberOfSession; i++) {
            // On Calcule la date de fin pour la ieme session
            LocalDateTime finDeSession = debutDeSession.plus(nombreMinutesPerSession, ChronoUnit.MINUTES);
            Session session =  Session.builder()
                    .instanceGameId(null) // elle sera initia
                    .startTime(debutDeSession)
                    .endTime(finDeSession)
                    .ouvert(false)
                    .build();
            //On ajoute la session cree dans l'Instance de jeu correspondant
//            instanceGame.getInstanceGameSession().add(instanceGameSession);
            // On met à jour la date de debut de session pour la session suivante
            debutDeSession = finDeSession;
        }
        return null;
    }
}

