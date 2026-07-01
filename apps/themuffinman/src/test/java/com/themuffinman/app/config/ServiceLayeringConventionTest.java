package com.themuffinman.app.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceLayeringConventionTest {

    private static final Path WORKMARKET_SERVICE_ROOT = Path.of(
            "src/main/java/com/themuffinman/app/workmarket/service"
    );

    @Test
    void workmarketUseCasesExposeOneExecuteEntrypoint() throws Exception {
        List<Class<?>> useCaseClasses;
        try (var paths = Files.walk(WORKMARKET_SERVICE_ROOT)) {
            useCaseClasses = paths
                    .filter(path -> path.getFileName().toString().endsWith("UseCase.java"))
                    .map(ServiceLayeringConventionTest::toClassName)
                    .map(ServiceLayeringConventionTest::loadClass)
                    .toList();
        }

        for (Class<?> useCaseClass : useCaseClasses) {
            List<Method> publicMethods = List.of(useCaseClass.getDeclaredMethods()).stream()
                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                    .toList();
            assertEquals(1, publicMethods.size(), useCaseClass.getSimpleName() + " should expose one public orchestration method");
            assertEquals("execute", publicMethods.getFirst().getName(), useCaseClass.getSimpleName() + " should expose execute as its public orchestration method");
        }
    }

    private static String toClassName(Path path) {
        String relative = WORKMARKET_SERVICE_ROOT.relativize(path).toString()
                .replace(".java", "")
                .replace("/", ".")
                .replace("\\", ".");
        return "com.themuffinman.app.vision.service." + relative;
    }

    private static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException exception) {
            throw new IllegalStateException("Could not load " + className, exception);
        }
    }
}
