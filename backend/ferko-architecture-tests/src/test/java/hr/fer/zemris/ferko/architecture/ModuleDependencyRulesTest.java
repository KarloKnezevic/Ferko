package hr.fer.zemris.ferko.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import hr.fer.zemris.ferko.application.usecase.PingUseCase;
import hr.fer.zemris.ferko.domain.model.CourseCode;
import hr.fer.zemris.ferko.infrastructure.adapter.JdbcToDoTaskRepository;
import hr.fer.zemris.ferko.security.SecurityModuleMarker;
import hr.fer.zemris.ferko.webapi.FerkoWebApiApplication;

@AnalyzeClasses(
    packagesOf = {
      CourseCode.class,
      PingUseCase.class,
      JdbcToDoTaskRepository.class,
      SecurityModuleMarker.class,
      FerkoWebApiApplication.class
    },
    importOptions = {ImportOption.DoNotIncludeTests.class})
class ModuleDependencyRulesTest {

  private static final String DOMAIN = "..domain..";
  private static final String APPLICATION = "..application..";
  private static final String INFRASTRUCTURE = "..infrastructure..";
  private static final String SECURITY = "..security..";
  private static final String WEB_API = "..webapi..";

  @ArchTest
  static final ArchRule domain_does_not_depend_on_outer_layers =
      noClasses()
          .that()
          .resideInAPackage(DOMAIN)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(APPLICATION, INFRASTRUCTURE, SECURITY, WEB_API);

  @ArchTest
  static final ArchRule application_does_not_depend_on_infrastructure_or_interface_layers =
      noClasses()
          .that()
          .resideInAPackage(APPLICATION)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(INFRASTRUCTURE, SECURITY, WEB_API);

  @ArchTest
  static final ArchRule infrastructure_does_not_depend_on_web_or_security =
      noClasses()
          .that()
          .resideInAPackage(INFRASTRUCTURE)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(WEB_API, SECURITY);

  @ArchTest
  static final ArchRule security_does_not_depend_on_web_or_infrastructure =
      noClasses()
          .that()
          .resideInAPackage(SECURITY)
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage(WEB_API, INFRASTRUCTURE);

  @ArchTest
  static final ArchRule web_api_does_not_depend_on_domain_directly =
      noClasses()
          .that()
          .resideInAPackage(WEB_API)
          .should()
          .dependOnClassesThat()
          .resideInAPackage(DOMAIN);

  @ArchTest
  static final ArchRule core_packages_are_free_of_cycles =
      slices().matching("hr.fer.zemris.ferko.(*)..").should().beFreeOfCycles();
}
