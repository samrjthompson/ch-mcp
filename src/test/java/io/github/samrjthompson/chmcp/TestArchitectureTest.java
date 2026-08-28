package io.github.samrjthompson.chmcp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@AnalyzeClasses(packages = "io.github.samrjthompson.chmcp")
class TestArchitectureTest {

    @ArchTest
    static final ArchRule unitTestsMustNotUseIntegrationTestMachinery = noClasses().that()
            .haveSimpleNameEndingWith("Test")
            .should()
            .beAnnotatedWith(SpringBootTest.class)
            .orShould()
            .beAnnotatedWith(Testcontainers.class)
            .orShould()
            .dependOnClassesThat()
            .resideInAnyPackage("com.github.tomakehurst.wiremock..", "org.wiremock..")
            .because("only *IT classes may use a Spring context, Testcontainers, or WireMock");

    @ArchTest
    static final ArchRule springContextTestsMustBeIntegrationTests = classes().that()
            .areAnnotatedWith(SpringBootTest.class)
            .or()
            .areAnnotatedWith(Testcontainers.class)
            .should()
            .haveSimpleNameEndingWith("IT")
            .because("classes using a Spring context or Testcontainers must run under failsafe, named *IT");
}
