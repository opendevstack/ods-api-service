package org.opendevstack.apiservice.persistence;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Minimal boot entry-point used only during tests.
 *
 * <p>
 * The {@code persistence} module is a library — it has no production
 * {@code @SpringBootApplication}. {@code @DataJpaTest} searches upward through
 * the package hierarchy for a class annotated with {@code @SpringBootConfiguration}.
 * This class satisfies that requirement without loading the full application context.
 * </p>
 */
@SpringBootApplication
public class PersistenceTestApplication {
    // No main method needed — this class is never executed, only discovered by
    // Spring Test's upward-package scan when bootstrapping @DataJpaTest contexts.
}
