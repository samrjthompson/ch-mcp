package io.github.samrjthompson.chmcp;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.springframework.ai.tool.annotation.Tool;

@AnalyzeClasses(packages = "io.github.samrjthompson.chmcp", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule onlyClientAndConfigTalkHttpDirectly = noClasses().that()
            .resideOutsideOfPackage("..client..")
            .and()
            .resideOutsideOfPackage("..config..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("java.net.http..")
            .because("outbound calls must be centralised in the client so the shared rate limit stays in one place");

    @ArchTest
    static final ArchRule noSpringHttpClient = noClasses().should()
            .dependOnClassesThat()
            .resideInAPackage("org.springframework.web.client..")
            .because("outbound HTTP must use java.net.http, not Spring's REST client");

    @ArchTest
    static final ArchRule featuresTalkThroughServices = noClasses().that()
            .resideOutsideOfPackage("..service..")
            .and()
            .resideOutsideOfPackage("..client..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..client..")
            .because("only a feature's service layer may talk to the shared client, never api/tool layers directly");

    @ArchTest
    static final ArchRule toolClassesExposeAtMostOneTool =
            classes().that(declareAToolMethod()).should(exposeAtMostOneToolMethod());

    @ArchTest
    static final ArchRule toolClassesDoNotHandleExceptionsThemselves = noClasses().that(declareAToolMethod())
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..common.exception..")
            .because("exception translation is centralised in ToolExceptionAspect, not handled per tool");

    @ArchTest
    static final ArchRule toolClassesLiveInADomainToolPackage = classes().that(declareAToolMethod())
            .should()
            .resideInAPackage("..tool")
            .andShould()
            .resideOutsideOfPackage("..mcp..")
            .because("a tool belongs to the domain that owns it, in that domain's `tool` package, "
                    + "not a shared framework package like mcp");

    @ArchTest
    static final ArchRule noGenericExceptions = noClasses().that()
            .areNotAssignableTo(RuntimeException.class)
            .should(constructAGenericExceptionDirectly())
            .because(
                    "errors must use one of the app's specific exception types, never RuntimeException or Exception directly");

    private static DescribedPredicate<JavaClass> declareAToolMethod() {
        return new DescribedPredicate<>("declare a @Tool method") {
            @Override
            public boolean test(JavaClass javaClass) {
                return javaClass.getMethods().stream().anyMatch(method -> method.isAnnotatedWith(Tool.class));
            }
        };
    }

    private static ArchCondition<JavaClass> exposeAtMostOneToolMethod() {
        return new ArchCondition<>("expose at most one @Tool method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                long toolMethodCount =
                        javaClass.getMethods().stream().filter(method -> method.isAnnotatedWith(Tool.class)).count();
                if (toolMethodCount > 1) {
                    String message = "%s declares %d @Tool methods, expected at most 1".formatted(javaClass.getName(),
                            toolMethodCount);
                    events.add(SimpleConditionEvent.violated(javaClass, message));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> constructAGenericExceptionDirectly() {
        return new ArchCondition<>("construct a generic RuntimeException or Exception directly") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getConstructorCallsFromSelf()
                        .stream()
                        .filter(call -> isGenericExceptionType(call.getTargetOwner()))
                        .forEach(call -> events.add(SimpleConditionEvent.violated(call,
                                "%s constructs a generic %s directly instead of a specific exception type".formatted(
                                        call.getOrigin().getFullName(), call.getTargetOwner().getSimpleName()))));
            }
        };
    }

    private static boolean isGenericExceptionType(JavaClass javaClass) {
        return javaClass.isEquivalentTo(RuntimeException.class) || javaClass.isEquivalentTo(Exception.class);
    }
}
