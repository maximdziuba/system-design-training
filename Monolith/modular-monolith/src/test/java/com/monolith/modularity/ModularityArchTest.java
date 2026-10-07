package com.monolith.modularity;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.monolith", importOptions = {ImportOption.DoNotIncludeTests.class})
public class ModularityArchTest {

    private static final Pattern MODULE_PATTERN = Pattern.compile("^com\\.monolith\\.(orders|payments|users|notifications)(\\..*)?$");

    /**
     * Rule 1: Modules must be free of cyclic dependencies.
     * Enforces Modularity rule: "Modules cannot have circular dependencies".
     */
    @ArchTest
    public static final ArchRule no_circular_dependencies_between_modules =
            slices().matching("com.monolith.(*)..")
                    .should().beFreeOfCycles()
                    .as("Modules (orders, payments, users, notifications) must be free of circular dependencies");

    /**
     * Rule 2: Cross-module dependencies are strictly allowed ONLY towards the public API (*.api.*).
     * Enforces Modularity rules:
     * - "Imports from external modules are allowed only from package *.api.*"
     * - "Imports from external module from *.infra.* are strictly forbidden"
     */
    @ArchTest
    public static final ArchRule inter_module_access_only_through_api =
            classes()
                    .that().resideInAPackage("com.monolith..")
                    .should(onlyAccessOtherModulesThroughApi())
                    .as("Dependencies between modules are allowed ONLY through *.api.* packages");

    private static ArchCondition<JavaClass> onlyAccessOtherModulesThroughApi() {
        return new ArchCondition<>("only access other modules through their *.api.* package") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                String sourceModule = extractModuleName(javaClass.getPackageName());
                if (sourceModule == null) {
                    return; // Class is outside specific domain modules (e.g. common, root)
                }

                for (Dependency dependency : javaClass.getDirectDependenciesFromSelf()) {
                    JavaClass targetClass = dependency.getTargetClass();
                    String targetModule = extractModuleName(targetClass.getPackageName());

                    // If depending on another module (cross-module dependency)
                    if (targetModule != null && !targetModule.equals(sourceModule)) {
                        String targetPackage = targetClass.getPackageName();
                        boolean isApiPackage = targetPackage.startsWith("com.monolith." + targetModule + ".api");

                        if (!isApiPackage) {
                            String message = String.format(
                                    "Violation: Class '%s' in module [%s] depends on '%s' in module [%s] " +
                                            "(non-api package '%s'). Cross-module access is only allowed via *.api.*! Description: %s",
                                    javaClass.getFullName(),
                                    sourceModule,
                                    targetClass.getFullName(),
                                    targetModule,
                                    targetPackage,
                                    dependency.getDescription()
                            );
                            events.add(SimpleConditionEvent.violated(dependency, message));
                        }
                    }
                }
            }
        };
    }

    private static String extractModuleName(String packageName) {
        Matcher matcher = MODULE_PATTERN.matcher(packageName);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
