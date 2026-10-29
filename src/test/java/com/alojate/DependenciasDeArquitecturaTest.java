package com.alojate;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class DependenciasDeArquitecturaTest {

    private static JavaClasses clases;

    @BeforeAll
    static void importarProyecto() {
        clases = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.alojate");
    }

    @Test
    void elDominioNoDependeDelFrameworkNiDeOtrasCapas() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..application..",
                        "..infrastructure..",
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..")
                .check(clases);
    }

    @Test
    void laAplicacionNoDependeDeInfraestructuraNiDelFramework() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..infrastructure..",
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..")
                .check(clases);
    }

    @Test
    void identidadYAlojamientosNoSeDependen() {
        noClasses()
                .that().resideInAPackage("com.alojate.identidad..")
                .should().dependOnClassesThat().resideInAPackage("com.alojate.alojamientos..")
                .allowEmptyShould(true)
                .check(clases);

        noClasses()
                .that().resideInAPackage("com.alojate.alojamientos..")
                .should().dependOnClassesThat().resideInAPackage("com.alojate.identidad..")
                .check(clases);
    }
}
