package hr.fer.zemris.ferko.webapi.bootstrap;

import static org.assertj.core.api.Assertions.assertThat;

import hr.fer.zemris.ferko.webapi.bootstrap.RoomInference.RoomSpec;
import java.util.List;
import org.junit.jupiter.api.Test;

class BootstrapHelpersTest {

  @Test
  void staffNamesStripTitlesAndBuildUsernames() {
    List<String> names =
        StaffNames.parseNames("Prof. dr. sc. Davor Petrinović\nProf. dr. sc. Mladen Vučić");
    assertThat(names).containsExactly("Davor Petrinović", "Mladen Vučić");
    assertThat(StaffNames.toUsername("Davor Petrinović")).isEqualTo("davor.petrinovic");
    assertThat(StaffNames.toUsername("Mladen Vučić")).isEqualTo("mladen.vucic");
    assertThat(StaffNames.parseNames("")).isEmpty();
    assertThat(StaffNames.parseNames(null)).isEmpty();
    // A line that is only titles yields no usable name.
    assertThat(StaffNames.parseNames("Prof. dr. sc.")).isEmpty();
    assertThat(StaffNames.toUsername("...")).isBlank();
  }

  @Test
  void workloadParsesEctsFromLastTokenAndHours() {
    CourseWorkload workload = CourseWorkload.parse("2\n1\n1\n0\n4.0", 5);
    assertThat(workload.ects()).isEqualTo(4);
    assertThat(workload.weeklyContactHours()).isEqualTo(4); // 2 + 1 + 1 + 0
    assertThat(workload.hoursSummary()).isEqualTo("2+1+1+0");

    CourseWorkload rounded = CourseWorkload.parse("3\n0\n3\n0\n7.5", 5);
    assertThat(rounded.ects()).isEqualTo(8);

    CourseWorkload fallback = CourseWorkload.parse("", 6);
    assertThat(fallback.ects()).isEqualTo(6);
    assertThat(fallback.hoursSummary()).isEmpty();
  }

  @Test
  void roomInferenceClassifiesComputerLabsAndHalls() {
    RoomSpec computer = RoomInference.infer("A105");
    assertThat(computer.hasComputers()).isTrue();
    assertThat(computer.capacity()).isEqualTo(20);
    assertThat(computer.building()).isEqualTo("Siva zgrada (A)");

    RoomSpec pclab = RoomInference.infer("PCLAB1");
    assertThat(pclab.hasComputers()).isTrue();

    RoomSpec hall = RoomInference.infer("D272");
    assertThat(hall.hasComputers()).isFalse();
    assertThat(hall.capacity()).isEqualTo(150);
    assertThat(hall.building()).isEqualTo("D zgrada");

    RoomSpec unknown = RoomInference.infer("ZEMRIS");
    assertThat(unknown.hasComputers()).isFalse();
    assertThat(unknown.building()).isEqualTo("FER");

    // A single-digit "A1" is a lecture hall, not a computer classroom (needs A[12] + two digits).
    RoomSpec singleDigit = RoomInference.infer("A1");
    assertThat(singleDigit.hasComputers()).isFalse();
    assertThat(singleDigit.capacity()).isEqualTo(150);
  }
}
