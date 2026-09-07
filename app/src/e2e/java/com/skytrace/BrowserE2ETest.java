package com.skytrace;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

/** Real browser + HTTP + Spring Security + JPA, with a disposable database. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=${E2E_DB_URL:jdbc:h2:mem:browser;MODE=MSSQLServer;DB_CLOSE_DELAY=-1}")
@ActiveProfiles("test")
class BrowserE2ETest {
    @LocalServerPort int port;

    Locator button(Page page, String name) {
        return page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(name).setExact(true));
    }
    void login(Page page, String username, String password, String heading) {
        page.getByLabel("Identifiant", new Page.GetByLabelOptions().setExact(true)).fill(username);
        page.locator("#login-pass").fill(password);
        button(page, "Se connecter").click();
        assertThat(page.getByRole(AriaRole.HEADING, new Page.GetByRoleOptions().setName(heading))).isVisible();
    }
    void logout(Page page) { button(page, "Déconnexion").click(); }

    @Test
    void fourRolesAndPublicTrackingInTheBrowser() throws Exception {
        Files.createDirectories(Path.of("target/e2e"));
        List<String> errors = new ArrayList<>();
        try (NginxProxy proxy = new NginxProxy(port);
             Playwright playwright = Playwright.create();
             Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                     .setChannel(System.getProperty("browser.channel", "msedge")).setHeadless(true));
             BrowserContext context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1365, 900))) {
            Page page = context.newPage();
            page.onPageError(errors::add);
            page.onResponse(response -> { if (response.status() >= 500) errors.add(response.status() + " " + response.url()); });
            page.onConsoleMessage(message -> {
                if (message.type().equals("error")) System.err.println("Browser: " + message.text());
                if (message.text().contains("Refused to") || message.text().contains("MIME type")) errors.add(message.text());
            });
            String externalUrl = System.getenv("E2E_BASE_URL");
            Response navigation = page.navigate((externalUrl == null ? proxy.url : externalUrl) + "/");
            if (System.getenv("NGINX_BINARY") != null || externalUrl != null) {
                assertThat(navigation.headerValue("x-frame-options")).isEqualTo("DENY");
                assertThat(navigation.headerValue("content-security-policy")).contains("script-src 'self'");
            }
            assertThat(errors).as("Browser initialization").isEmpty();
            login(page, "admin", System.getenv().getOrDefault("E2E_ADMIN_PASSWORD", "Test-admin-2026!"), "Administration des utilisateurs");
            String[][] users = {{"enregistrement", "AGENT_ENREGISTREMENT"}, {"manutention", "AGENT_MANUTENTION"}, {"superviseur", "SUPERVISEUR"}};
            for (String[] user : users) {
                page.getByLabel("Nom complet").fill("Test " + user[0]);
                page.getByLabel("Identifiant de connexion").fill(user[0]);
                page.locator("#admin-pass").fill("Agent-E2E-2026!");
                page.getByLabel("Rôle", new Page.GetByLabelOptions().setExact(true)).selectOption(user[1]);
                button(page, "Créer l'utilisateur").click();
                assertThat(page.locator("#utilisateurs-body")).containsText("Test " + user[0]);
            }
            page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/e2e/01-administration.png")));
            logout(page);
            login(page, "enregistrement", "Agent-E2E-2026!", "Enregistrer un nouveau bagage");
            String passenger = "Test <img src=x onerror=alert(1)>";
            page.getByLabel("Nom du passager").fill(passenger);
            page.getByLabel("Numéro de vol").fill("AT-UI-01");
            page.getByLabel("Destination", new Page.GetByLabelOptions().setExact(true)).fill("Paris (CDG)");
            page.getByLabel("Poids (kg)").fill("18.5");
            button(page, "Enregistrer le bagage").click();
            assertThat(page.locator("#dernier-bagage")).containsText("Bagage enregistré");
            assertThat(page.locator(".tag-name")).hasText(passenger);
            assertThat(page.locator(".tag-name img")).hasCount(0);
            String code = page.locator(".tag-code").innerText();
            assertThat(page.getByRole(AriaRole.IMG, new Page.GetByRoleOptions().setName("QR du bagage"))).isVisible();
            page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/e2e/02-bagage.png")));
            logout(page);
            login(page, "manutention", "Agent-E2E-2026!", "Scanner un bagage");
            assertThat(page.locator("#select-scan")).containsText(code);
            page.locator("#select-scan").selectOption(code);
            for (String step : new String[]{"Dépôt sur tapis", "Tri & transfert", "Chargement", "Déchargement", "Livraison"}) {
                button(page, "Scanner — passer à l'étape suivante").click();
                assertThat(page.locator("#scan-preview > p").last()).hasText("Statut actuel : " + step);
            }
            button(page, "Scanner — passer à l'étape suivante").click();
            assertThat(page.locator("#message")).containsText("etape finale");
            logout(page);
            login(page, "superviseur", "Agent-E2E-2026!", "Tableau de bord superviseur");
            button(page, "Signaler").click();
            assertThat(page.locator("#stat-anomalies")).hasText("1");
            page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/e2e/03-anomalie.png")));
            button(page, "Résoudre").click();
            assertThat(page.locator("#stat-anomalies")).hasText("0");
            page.getByLabel("Rechercher un bagage").fill("introuvable");
            assertThat(page.locator("#table-empty")).hasText("Aucun résultat.");
            logout(page);
            button(page, "Passager").click();
            page.getByLabel("Code du bagage").fill(code);
            button(page, "Suivre").click();
            assertThat(page.locator("#suivi-passager")).containsText("Statut actuel : Livraison");
            assertThat(page.locator("#suivi-passager .history > div")).hasCount(5);
            page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/e2e/04-suivi.png")));
            page.setViewportSize(390, 844);
            page.screenshot(new Page.ScreenshotOptions().setPath(Path.of("target/e2e/05-mobile.png")).setFullPage(true));
            page.getByLabel("Code du bagage").fill("UNKNOWN");
            button(page, "Suivre").click();
            assertThat(page.locator("#message")).containsText("Aucun bagage");
            assertThat(errors).isEmpty();
        }
    }
}
