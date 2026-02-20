package hr.fer.zemris.ferko.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Constructor;
import org.junit.jupiter.api.Test;

class SecurityModuleMarkerTest {

  @Test
  void privateConstructorIsInvocableForCoverage() throws Exception {
    Constructor<SecurityModuleMarker> constructor =
        SecurityModuleMarker.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    SecurityModuleMarker marker = constructor.newInstance();

    assertNotNull(marker);
  }
}
