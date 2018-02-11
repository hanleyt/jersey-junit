import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import javax.ws.rs.client.WebTarget;

import static com.google.common.truth.Truth.assertThat;

class JerseyExtensionTest {

    @RegisterExtension
    JerseyExtension jerseyExtension = new JerseyExtension(this::configureJersey);

    private ResourceConfig configureJersey() {
        return new ResourceConfig();
    }

    @Test
    void parameters_are_injected(WebTarget target) {
        assertThat(target.getUri()).isNotNull();
    }
}