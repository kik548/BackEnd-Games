package edu.uclm.esi.ds.games.http;

import java.io.IOException;

import org.apache.http.Header;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.ds.games.domain.GameName;
import edu.uclm.esi.ds.games.domain.Match;
import edu.uclm.esi.ds.games.exceptions.NotLoggedException;
import edu.uclm.esi.ds.games.services.APIService;
import edu.uclm.esi.ds.games.services.GameService;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("games")
@CrossOrigin("*")
public class GameController {
	@Autowired
	private GameService gameService;
	@Autowired
	private APIService apiService;

	@GetMapping("/requestGame")
	public Match requestGame(@RequestHeader(value="sessionId") String sessionId, @RequestParam String game) {
		JSONObject userJson;

		if (!checkGame(game))
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Game Not Found");
		
		try {
			userJson = apiService.getUser(sessionId);
			if (userJson == null) throw new NotLoggedException();
		} catch (IOException | NotLoggedException e) {
			throw new ResponseStatusException(HttpStatus.PERMANENT_REDIRECT, "Go to login page");
		}
		return this.gameService.requestGame(game, userJson);
	}
	
	private boolean checkGame(String game) {
		boolean exists = false;

		for (GameName name : GameName.values()) {
			if (game.equals(name.toString()))
				exists = true;
		}

		return exists;
	}
}
