package mm.controller;

import mm.connection.ConnectionQueue;
import mm.connection.Joins;
import mm.service.MatchmakerService;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Controller
@CrossOrigin(origins = "*")
@RequestMapping(value = "matchmaker", method = RequestMethod.POST)
public class MatchmakerController {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MatchmakerController.class);

    @Autowired
    MatchmakerService matchmakerService;

    @RequestMapping(
            path = "join",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> join(@RequestParam("name") String name) {
        logger.info(name + " joins");
        Long gameId = matchmakerService.join(name);
        return new ResponseEntity<>(gameId.toString(), HttpStatus.OK);
    }

    @RequestMapping(
            path = "link",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @CrossOrigin(origins = "*")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> link(@RequestParam("playerCount") int playerCount) {
        logger.info("Asked for private link");
        String link = matchmakerService.getLink(playerCount);
        return new ResponseEntity<>(link, HttpStatus.OK);
    }

    @RequestMapping(
            path = "games",
            method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> games() {
        List<Map.Entry<String, Long>> games = Joins.getInstance().entrySet().stream().sorted(((a, b) -> (int) (a.getValue() - b.getValue()))).collect(Collectors.toList());
        return new ResponseEntity<>(games.toString(), HttpStatus.OK);
    }
}
