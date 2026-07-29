package com.ssafy.ozz;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(
        packages = "com.ssafy.ozz",
        importOptions = ImportOption.DoNotIncludeTests.class
)
class ModuleBoundaryTests {

    @ArchTest
    static final ArchRule domainModulesAreAcyclic = slices()
            .matching("com.ssafy.ozz.(*)..")
            .should().beFreeOfCycles();

    @ArchTest
    static final ArchRule domainCodeDoesNotUseServiceDiscoveryOrFeign = noClasses()
            .that().resideOutsideOfPackage("com.ssafy.ozz.monolith..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework.cloud.openfeign..",
                    "com.netflix.discovery..",
                    "feign.."
            );
}
