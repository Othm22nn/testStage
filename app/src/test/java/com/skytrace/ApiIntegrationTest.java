package com.skytrace;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    String login(String name, String password) throws Exception {
        return json.readTree(mvc.perform(post("/api/auth/login").contentType("application/json")
                .content(json.writeValueAsString(java.util.Map.of("login", name, "motDePasse", password))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString()).get("token").asText();
    }

    @Test
    void completeJourneyAndRoleBoundaries() throws Exception {
        String admin = login("admin", "Test-admin-2026!");
        for (String role : new String[]{"AGENT_ENREGISTREMENT", "AGENT_MANUTENTION", "SUPERVISEUR"}) {
            mvc.perform(post("/api/utilisateurs").header("Authorization", "Bearer " + admin)
                    .contentType("application/json").content(json.writeValueAsString(java.util.Map.of(
                            "nom", role, "login", role, "motDePasse", "Test-agent-2026!", "role", role))))
                    .andExpect(status().isCreated());
        }
        String agent = login("AGENT_ENREGISTREMENT", "Test-agent-2026!");
        String handler = login("AGENT_MANUTENTION", "Test-agent-2026!");
        String supervisor = login("SUPERVISEUR", "Test-agent-2026!");
        mvc.perform(get("/api/bagages")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/utilisateurs").header("Authorization", "Bearer " + agent))
                .andExpect(status().isForbidden());

        JsonNode flight = json.readTree(mvc.perform(post("/api/vols").header("Authorization", "Bearer " + agent)
                .contentType("application/json").content("""
                    {"numeroVol":"AT-E2E","origine":"CMN","destination":"CDG","dateVol":"2026-10-01T12:00:00"}
                    """)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        JsonNode bag = json.readTree(mvc.perform(post("/api/bagages").header("Authorization", "Bearer " + agent)
                .contentType("application/json").content("{\"volId\":" + flight.get("id")
                        + ",\"nomPassager\":\"Passager test\",\"poids\":18.5}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        String code = bag.get("codeQr").asText();
        assertThat(bag.get("qrCodeBase64").asText()).startsWith("iVBOR");
        mvc.perform(delete("/api/vols/" + flight.get("id")).header("Authorization", "Bearer " + admin))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/scans").header("Authorization", "Bearer " + agent)
                .contentType("application/json").content("{\"codeQr\":\"" + code + "\"}"))
                .andExpect(status().isForbidden());
        for (String step : new String[]{"DEPOT_TAPIS", "TRI_TRANSFERT", "CHARGEMENT", "DECHARGEMENT", "LIVRAISON"}) {
            mvc.perform(post("/api/scans").header("Authorization", "Bearer " + handler)
                    .contentType("application/json").content("{\"codeQr\":\"" + code + "\"}"))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.statut").value(step));
        }
        mvc.perform(post("/api/scans").header("Authorization", "Bearer " + handler)
                .contentType("application/json").content("{\"codeQr\":\"" + code + "\"}"))
                .andExpect(status().isConflict());
        JsonNode anomaly = json.readTree(mvc.perform(post("/api/anomalies")
                .header("Authorization", "Bearer " + supervisor).contentType("application/json")
                .content("{\"bagageId\":" + bag.get("id") + ",\"typeAnomalie\":\"Controle test\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        mvc.perform(get("/api/bagages/suivi/" + code)).andExpect(status().isOk())
                .andExpect(jsonPath("$.livre").value(true)).andExpect(jsonPath("$.anomalieEnCours").value(true))
                .andExpect(jsonPath("$.historique.length()").value(5));
        mvc.perform(put("/api/anomalies/" + anomaly.get("id") + "/resoudre")
                .header("Authorization", "Bearer " + supervisor)).andExpect(status().isOk());
        mvc.perform(get("/api/bagages/suivi/" + code)).andExpect(jsonPath("$.anomalieEnCours").value(false));
    }

    @Test
    void malformedAndExpiredRequestsHaveUsefulStatuses() throws Exception {
        mvc.perform(get("/api/bagages").header("Authorization", "Bearer invalid")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType("application/json").content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"login\":\"admin\",\"motDePasse\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/bagages/suivi/UNKNOWN")).andExpect(status().isNotFound());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("UP"));
    }
}
