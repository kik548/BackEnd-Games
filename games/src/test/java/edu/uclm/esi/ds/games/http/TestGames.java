package edu.uclm.esi.ds.games.http;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import edu.uclm.esi.ds.games.domain.GameName;
@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@TestMethodOrder(OrderAnnotation.class)
public class TestGames {

	@Autowired
	private MockMvc server;
	
	@Test @Order(1)
	void testRedirecc() throws Exception {
		ResultActions response = 
				createRequest(GameName.nm.toString(), "Maria");
		response.andExpect(status().isPermanentRedirect());
		
	}

	@Test @Order(2)
	void testRequestMatch() throws Exception {
		register("Pepe");
		String sessionId = login("Pepe");
		String payload = sendRequest(sessionId);
		JSONObject jsonPepe = new JSONObject(payload);

		register("Ana");
		sessionId = login("Ana");
		String payloadAna = sendRequest(sessionId);
		assertFalse(jsonPepe.getBoolean("ready"));
		JSONObject jsonAna = new JSONObject(payloadAna);
		assertTrue(jsonAna.getBoolean("ready"));
		assertTrue(jsonAna.getJSONArray("players").length() == 2);

	}

	private String login(String player) throws JSONException {
		JSONObject jso = new JSONObject();
		jso.put("name",player);
		jso.put("pwd",player);
		String sessionId = null;
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
		    HttpPut request = new HttpPut("http://localhost:8080/users/login");
		    StringEntity params = new StringEntity(jso.toString());
		    request.addHeader("content-type", "application/json");
		    request.setEntity(params);
		    HttpResponse response = httpClient.execute(request);
		    Header[] w=response.getHeaders("sessionId");
		    sessionId = w[0].getValue();
		} catch (Exception ex) {
		}
		return sessionId;
	}

	private ResultActions createRequest(String game, String player) throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.get("/games/requestGame?game=" + game)
				.sessionAttr("sessionId", player);
		ResultActions response = this.server.perform(request);
		return response;
	}

	private String sendRequest(String player) throws Exception, UnsupportedEncodingException {
		ResultActions response = createRequest(GameName.nm.toString(), player);
		MvcResult result = response.andExpect(status().isOk()).andReturn();
		MockHttpServletResponse http = result.getResponse();
		String payload = http.getContentAsString();
		return payload;
	}

	@Test @Order(3)
	void RejectRequest() throws Exception {
		RequestBuilder request = MockMvcRequestBuilders.get("/games/requestGame?game=trivial");
		this.server.perform(request).andExpect(status().isNotFound());
	}
	private void register(String player) throws Exception {
		JSONObject jso = new JSONObject();
		jso.put("name",player);
		jso.put("email", player);
		jso.put("pwd1",player);
		jso.put("pwd2",player);
		
		HttpClient httpClient = HttpClientBuilder.create().build();
		try {
		    HttpPost request = new HttpPost("http://localhost:8080/users/register");
		    StringEntity params = new StringEntity(jso.toString());
		    request.addHeader("content-type", "application/json");
		    request.setEntity(params);
		    HttpResponse response = httpClient.execute(request);
		} catch (Exception ex) {
		}
	}
}
