package cm.connect.technology.scratchcard.services.impl;

import cm.connect.technology.scratchcard.dto.*;
import cm.connect.technology.scratchcard.entities.*;
import cm.connect.technology.scratchcard.enums.StatusGameEnum;
import cm.connect.technology.scratchcard.enums.SymbolEnnum;
import cm.connect.technology.scratchcard.exceptions.DAOException;
import cm.connect.technology.scratchcard.repositories.ScratchCardRepositories;
import cm.connect.technology.scratchcard.repositories.ScratchInstanceGameRepository;
import cm.connect.technology.scratchcard.services.ScratchInstanceGameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static java.util.Locale.filter;

@Component
public class ScratchInstanceGameServiceImpl implements ScratchInstanceGameService {
    @Autowired
    private ScratchInstanceGameRepository scratchInstanceGameRepository;
    @Autowired
    private ScratchCardRepositories scratchCardRepositories;
    @Override
    public ResponseDto<ScratchInstanceGame> createInstanceGame(CreateGameDto dto, Locale locale) {



        ResponseDto<ScratchInstanceGame> response = new ResponseDto<>();
        List<String> messages = new ArrayList<String>();
        try {
            // Calculer le nombre de minute entre la date de début et la date de fin de l'instance

            long nombreMinutes = Duration.between(dto.getStartDate(), dto.getEndDate()).toMinutes();

            // Calculer le nombre de minute par sessions à générer
            long nombreMinutesPerSession = nombreMinutes / dto.getNumberOfSession();

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
                    .status(StatusGameEnum.TRAITEMENT)
                    .createdBy("du token on recupere le nom de celui qui cree")
                    .totalCard(dto.getTotalCard())
                    .build();
            ScratchInstanceGame instanceGame1 = scratchInstanceGameRepository.save(instanceGame);
            response.setBody(instanceGame1);

            // On genere toutes les sessions de l'instance et on les stocke en base de données
            // On initialise la date de debut de la premiere session
            LocalDateTime debutDeSession = dto.getStartDate();
            List<Session> sessionList = new ArrayList<>();
            for (int i = 1; i <= dto.getNumberOfSession(); i++) {
                // On Calcule la date de fin pour chaque session
                System.out.println("fhjg");
                LocalDateTime finDeSession = debutDeSession.plus(nombreMinutesPerSession, ChronoUnit.MINUTES);
                Session instanceGameSession = Session.builder()
//                        .id(UUID.randomUUID().toString())
                        .instanceGameId(instanceGame1.getId())
                        .startTime(debutDeSession)
                        .endTime(finDeSession)
                        .ouvert(false)
                        .build();
                //On ajoute la session cree dans l'Instance de jeu correspondant


                sessionList.add(instanceGameSession);

                // On met à jour la date de debut de session pour la session suivante
                debutDeSession = finDeSession;
                ScratchInstanceGame instanceGame2 = scratchInstanceGameRepository.save(instanceGame1);

            }
             instanceGame1.setSessions(sessionList);

            List<ScratchCard> cardList = new ArrayList<>(createCard(dto.getScratchFormatGain(), instanceGame1.getId(), dto.getPrizesDistributions(), dto.getTotalCard(), locale));

            instanceGame1.setScratchCards(cardList);

            ScratchInstanceGame instanceGame3 = scratchInstanceGameRepository.save(instanceGame1);
            response.setBody(instanceGame3);
        }catch(Exception e){
          //  throw new RuntimeException(e.getMessage());
        }
        return response;
    }



    @Override
    public ResponseDto<ScratchInstanceGame> startInstance(LocalDateTime startDate, Locale locale) {
        ResponseDto<ScratchInstanceGame> response = new ResponseDto<>();
        List<String> messages = new ArrayList<String>();



        try {
            ScratchInstanceGame scratchInstanceGame = scratchInstanceGameRepository.findByStartDate(startDate);
            if (scratchInstanceGame != null){
            scratchInstanceGame.setStatus(StatusGameEnum.EN_COURS);
            scratchInstanceGame.getSessions().get(0).setOuvert(true);
            scratchInstanceGameRepository.save(scratchInstanceGame);
            response.setBody(scratchInstanceGame);
            }else{
                messages.add("date was not registered in Database");
//                messages.add(messageSource.getMessage("errors.not-found.gameSaas", null, locale));
                response.setBody(null);
            }

        }catch (Exception e) {
            response.setStatus( HttpStatus.BAD_REQUEST);
          messages.add(e.getMessage());
        }
        if(messages.isEmpty()){
            response.setStatus(HttpStatus.OK);
        }else if (response.getStatus() == null){
            response.setStatus(HttpStatus.BAD_REQUEST);
        }
        response.setMessages(messages);
        return response;

    }


    public List<ScratchCard> createCard(ScratchFormatGain scratchFormatGain, Long instanceGameId, List<PrizesDistribution> prizesDistributions, Long totalCard, Locale locale) {


        List<ScratchCard> scratchCards = new ArrayList<>();

        for (PrizesDistribution prizesDistribution : prizesDistributions) {
            Long cardNumber = prizesDistribution.getNumberOfTicket();

            for (int n = 0; n < cardNumber; n++) {

                String[] value = new String[scratchFormatGain.getTotalNumber()];// je crais le nombre de case
                List<String> listOfSymbol = new ArrayList<>(SymbolEnnum.distribution); //j ennumere tous les symboles
                listOfSymbol.remove(prizesDistribution.getSymbol());// je retire le montant a gagner dans la liste de symbole
                for (int e = 0; e < scratchFormatGain.getRepeatingNumber(); e++) {// je positione le symbol qui se repete
                    value[e] = prizesDistribution.getSymbol();
                }
                Collections.shuffle(listOfSymbol);// je fais un melange aleatoire
                int i = 0;
                for (int e = scratchFormatGain.getRepeatingNumber(); e < scratchFormatGain.getTotalNumber(); e++) {
                    value[e] = listOfSymbol.get(i);
                    i++;
                    if (i == 4) i = 0;
                }
                List<String> list = Arrays.asList(value);
                Collections.shuffle(list);
                String delim = ", ";
                String boxValue1 = String.join(delim, list);

                ScratchCard scratchCard = ScratchCard.builder()
                        .boxValue(boxValue1)
                        .isScratched(false)
                        .instanceGameId(instanceGameId)
                        .isWinning(true)
                        .prizeAmount(prizesDistribution.getAmount())
                        .build();
                System.out.println(scratchCard);
               // scratchCardRepositories.save(scratchCard);
                scratchCards.add(scratchCard);

            }
        }
        List<String> value2 = new ArrayList<>();
        Long winNumber = 0L;
        List<String> listOfSymbol = new ArrayList<>(SymbolEnnum.distribution); //j ennumere tous les symboles
        for (PrizesDistribution prizesDistribution : prizesDistributions) {
            winNumber = winNumber + prizesDistribution.getNumberOfTicket();

            for (Long n = winNumber; n < totalCard; n++) {
                String[] value = new String[scratchFormatGain.getTotalNumber()];
                int f = 0;

                for (int e = 0; e < scratchFormatGain.getTotalNumber(); e++) {
                   // value2.add(listOfSymbol.get(f));
                    value[e] = listOfSymbol.get(f);
                    f++;
                    if (f == 6) f = 0;

                }
                List<String> list1 = Arrays.asList(value);
                Collections.shuffle(list1);
                String delim = ", ";
                String boxValue = String.join(delim, list1);

                ScratchCard scratchCard = ScratchCard.builder()
                        .boxValue(boxValue)
                        .isScratched(false)
                        .instanceGameId(instanceGameId)
                        .isWinning(false)
                        .prizeAmount(null)
                        .build();
                scratchCards.add(scratchCard);
                System.out.println(scratchCard);
                scratchCardRepositories.save(scratchCard);
                System.out.println(scratchCard);



            }
            Collections.shuffle(scratchCards);
        }

        return scratchCards;
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
    public ResponseDto<ScratchInstanceGame> endInstance(LocalDateTime endDate, Locale locale) {
        ResponseDto<ScratchInstanceGame> response = new ResponseDto<>();
        List<String> messages = new ArrayList<String>();
        try {
            System.out.println("scratchInstanceGame");
            ScratchInstanceGame scratchInstanceGame = scratchInstanceGameRepository.findByEndDate(endDate);

            if (scratchInstanceGame != null){
                System.out.println(scratchInstanceGame);

                scratchInstanceGame.setStatus(StatusGameEnum.TERMINER);
                scratchInstanceGame.getSessions().get(scratchInstanceGame.getNumberOfSession()).setOuvert(false);
                scratchInstanceGameRepository.save(scratchInstanceGame);
                response.setBody(scratchInstanceGame);
            }else{
                messages.add("date was not registered in Database");
//                messages.add(messageSource.getMessage("errors.not-found.gameSaas", null, locale));
                response.setBody(null);

        }
        }catch (Exception e) {
            response.setStatus( HttpStatus.BAD_REQUEST);
            messages.add(e.getMessage());
        }
        if(messages.isEmpty()){
            response.setStatus(HttpStatus.OK);
        }else if (response.getStatus() == null){
            response.setStatus(HttpStatus.BAD_REQUEST);
        }
        response.setMessages(messages);
        return response;
    }

    @Override
    public ResponseDto<ScratchInstanceGame> desactiveSession(LocalDateTime startDate, Locale locale) {

        ResponseDto<ScratchInstanceGame> response = new ResponseDto<>();
        List<String> messages = new ArrayList<String>();
        try{
           ScratchInstanceGame scratchInstanceGame = scratchInstanceGameRepository.findByStartDate(startDate);


           if (scratchInstanceGame!= null){

            List<Session> session=scratchInstanceGame.getSessions();

            for (int i = 0 ;i<(scratchInstanceGame.getNumberOfSession()/2);i++){
                if (session.get(i).getEndTime().equals(LocalDateTime.now())){
                    session.get(i).setOuvert(false);
                    session.get(i+1).setOuvert(true);
                } 
                 else {
                   messages.add("the name was not registered in Database");
//                messages.add(messageSource.getMessage("errors.not-found.gameSaas", null, locale));
                   response.setBody(null);
               }
            }
           }
        }catch (DAOException e){
                   response.setStatus(e.getStatus());
                   messages.add(e.getMessage());
               }
               if(messages.isEmpty()){
                   response.setStatus(HttpStatus.OK);
               }else if (response.getStatus() == null){
                   response.setStatus(HttpStatus.BAD_REQUEST);
               }
               response.setMessages(messages);
        return response;
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

        return null;
    }


    @Override
    public ResponseDto<ScratchInstanceGame> findInstanceByStartDate(LocalDateTime startDate, Locale locale) {
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
        return null;
    }


    @Override
    public ResponseDto<ScratchInstanceGame> findInstanceByEndDate(LocalDateTime endDate, Locale locale) {
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

   // @Override
    public ResponseDto<ScratchInstanceGame> generateTicket(Ticket dto, Locale locale) throws RuntimeException{

        ResponseDto<ScratchInstanceGame> responseDto = new ResponseDto<>();
        List<String> messages = new ArrayList<>();
        List<Ticket> ticketList = new ArrayList<>();
      //  List<PrizesDistribution>  prizesDistributions = dto.getPrizesDistributions();

      //  ScratchInstanceGame scratchInstanceGame = scratchInstanceGameRepository.findBy

      //  for (int i = 1; i <= ; i++)


            return null;
    }


}

