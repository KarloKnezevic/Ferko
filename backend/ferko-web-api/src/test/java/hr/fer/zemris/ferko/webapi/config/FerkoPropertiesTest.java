package hr.fer.zemris.ferko.webapi.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FerkoPropertiesTest {

  @Test
  void exposesSensibleDefaults() {
    FerkoProperties props = new FerkoProperties();

    assertThat(props.getGrading().getExcellent()).isEqualTo(88);
    assertThat(props.getGrading().getVeryGood()).isEqualTo(75);
    assertThat(props.getGrading().getGood()).isEqualTo(62);
    assertThat(props.getGrading().getSufficient()).isEqualTo(50);
    assertThat(props.getScheduler().getDefaultPopulationSize()).isEqualTo(60);
    assertThat(props.getScheduler().getDefaultIterations()).isEqualTo(5000);
    assertThat(props.getScheduler().getDefaultSeed()).isEqualTo(42);
    assertThat(props.getMail().isEnabled()).isFalse();
    assertThat(props.getMail().getFrom()).isEqualTo("ferko@fer.hr");
    assertThat(props.getSeed().getUsers().isEnabled()).isTrue();
    assertThat(props.getSeed().getAcademic().isEnabled()).isTrue();
    assertThat(props.getSecurity().getJwt().getPrincipalClaim()).isEqualTo("sub");
    assertThat(props.getSecurity().getJwt().getRolesClaim()).isEqualTo("roles");
    assertThat(props.getSecurity().getJwt().isAllowHmacDecoder()).isTrue();
    assertThat(props.getSecurity().getDevToken().isEnabled()).isFalse();
    assertThat(props.getSecurity().getLoginRateLimit().getMaxAttempts()).isEqualTo(10);
  }

  @Test
  void settersAreBindable() {
    FerkoProperties props = new FerkoProperties();

    props.getMail().setEnabled(true);
    props.getMail().setFrom("noreply@fer.hr");
    props.getGrading().setExcellent(90);
    props.getGrading().setVeryGood(78);
    props.getGrading().setGood(65);
    props.getGrading().setSufficient(51);
    props.getScheduler().setDefaultPopulationSize(120);
    props.getScheduler().setDefaultIterations(10000);
    props.getScheduler().setDefaultSeed(7);
    props.getSeed().getUsers().setEnabled(false);
    props.getSeed().getAcademic().setEnabled(false);
    props.getSeed().getAcademic().setMaxCourses(0);
    props.getSeed().getAcademic().setMaxStudents(0);
    FerkoProperties.Security.Jwt jwt = props.getSecurity().getJwt();
    jwt.setPrincipalClaim("preferred_username");
    jwt.setRolesClaim("authorities");
    jwt.setIssuerUri("https://idp.example/realms/fer");
    jwt.setJwkSetUri("https://idp.example/jwks");
    jwt.setHmacSecret("super-secret");
    jwt.setAllowHmacDecoder(false);
    props.getSecurity().getDevToken().setEnabled(true);
    props.getSecurity().getLoginRateLimit().setEnabled(true);
    props.getSecurity().getLoginRateLimit().setMaxAttempts(5);
    props.getSecurity().getLoginRateLimit().setWindowSeconds(30);

    assertThat(props.getMail().isEnabled()).isTrue();
    assertThat(props.getMail().getFrom()).isEqualTo("noreply@fer.hr");
    assertThat(props.getGrading().getExcellent()).isEqualTo(90);
    assertThat(props.getGrading().getVeryGood()).isEqualTo(78);
    assertThat(props.getGrading().getGood()).isEqualTo(65);
    assertThat(props.getGrading().getSufficient()).isEqualTo(51);
    assertThat(props.getScheduler().getDefaultPopulationSize()).isEqualTo(120);
    assertThat(props.getScheduler().getDefaultIterations()).isEqualTo(10000);
    assertThat(props.getScheduler().getDefaultSeed()).isEqualTo(7);
    assertThat(props.getSeed().getUsers().isEnabled()).isFalse();
    assertThat(props.getSeed().getAcademic().isEnabled()).isFalse();
    assertThat(props.getSeed().getAcademic().getMaxCourses()).isZero();
    assertThat(props.getSeed().getAcademic().getMaxStudents()).isZero();
    assertThat(jwt.getPrincipalClaim()).isEqualTo("preferred_username");
    assertThat(jwt.getRolesClaim()).isEqualTo("authorities");
    assertThat(jwt.getIssuerUri()).isEqualTo("https://idp.example/realms/fer");
    assertThat(jwt.getJwkSetUri()).isEqualTo("https://idp.example/jwks");
    assertThat(jwt.getHmacSecret()).isEqualTo("super-secret");
    assertThat(jwt.isAllowHmacDecoder()).isFalse();
    assertThat(props.getSecurity().getDevToken().isEnabled()).isTrue();
    assertThat(props.getSecurity().getLoginRateLimit().isEnabled()).isTrue();
    assertThat(props.getSecurity().getLoginRateLimit().getMaxAttempts()).isEqualTo(5);
    assertThat(props.getSecurity().getLoginRateLimit().getWindowSeconds()).isEqualTo(30);
  }
}
