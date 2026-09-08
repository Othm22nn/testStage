package com.skytrace;

import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import static org.assertj.core.api.Assertions.assertThat;

/** Lightweight source-level boundary check: no extra architecture framework. */
class ArchitectureTest {
    @Test
    void domainDependenciesRemainOneWay() throws Exception {
        Map<String, Set<String>> allowed = Map.of(
                "shared", Set.of(), "utilisateurs", Set.of("shared"),
                "auth", Set.of("utilisateurs", "shared"), "vols", Set.of("shared"),
                "bagages", Set.of("vols", "shared"), "scans", Set.of("bagages", "utilisateurs", "shared"),
                "anomalies", Set.of("bagages", "shared"), "suivi", Set.of("bagages", "scans", "anomalies", "shared"));
        Path root = Path.of("src/main/java/com/skytrace");
        Pattern reference = Pattern.compile("\\bcom\\.skytrace\\.(\\w+)\\.");
        try (var files = Files.walk(root)) {
            for (Path file : files.filter(p -> p.toString().endsWith(".java")).toList()) {
                Path relative = root.relativize(file);
                if (relative.getNameCount() == 1) continue;
                String domain = relative.getName(0).toString();
                assertThat(allowed).containsKey(domain);
                var matcher = reference.matcher(Files.readString(file));
                while (matcher.find()) {
                    String dependency = matcher.group(1);
                    if (!domain.equals(dependency)) assertThat(allowed.get(domain))
                            .as("%s must not depend on %s", file, dependency).contains(dependency);
                }
            }
        }
    }
}
