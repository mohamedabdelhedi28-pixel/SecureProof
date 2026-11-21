package tn.supcom.secureproof.api;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EndpointSecurityArchitectureTest {

    private static final List<Class<? extends Annotation>> HTTP_ANNOTATIONS = List.of(
            GET.class, POST.class, PUT.class, DELETE.class, PATCH.class
    );

    @Test
    void allJaxRsEndpointsMustDeclareExplicitSecurityAnnotations() throws Exception {
        List<Class<?>> classes = findClassesInPackage("tn.supcom.secureproof.api");
        List<String> violations = new ArrayList<>();

        for (Class<?> clazz : classes) {
            if (!clazz.isAnnotationPresent(Path.class)) {
                continue;
            }
            boolean classHasSecurity = hasSecurityAnnotation(clazz.getAnnotations());

            for (Method method : clazz.getDeclaredMethods()) {
                boolean isHttpEndpoint = HTTP_ANNOTATIONS.stream().anyMatch(method::isAnnotationPresent);
                if (isHttpEndpoint) {
                    boolean methodHasSecurity = hasSecurityAnnotation(method.getAnnotations());
                    if (!classHasSecurity && !methodHasSecurity) {
                        violations.add(clazz.getName() + "#" + method.getName()
                                + " — missing @RolesAllowed, @DenyAll, or @PermitAll (SEC-AUTHZ-01)");
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "Security annotation violations detected:\n - " + String.join("\n - ", violations));
    }

    private boolean hasSecurityAnnotation(Annotation[] annotations) {
        for (Annotation ann : annotations) {
            Class<? extends Annotation> type = ann.annotationType();
            if (type == RolesAllowed.class || type == DenyAll.class || type == PermitAll.class) {
                return true;
            }
        }
        return false;
    }

    private List<Class<?>> findClassesInPackage(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<Class<?>> classes = new ArrayList<>();
        while (resources.hasMoreElements()) {
            File dir = new File(resources.nextElement().toURI());
            if (dir.exists()) {
                scanDirectory(dir, packageName, classes);
            }
        }
        return classes;
    }

    private void scanDirectory(File dir, String packageName, List<Class<?>> classes) throws ClassNotFoundException {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), classes);
            } else if (file.getName().endsWith(".class") && !file.getName().endsWith("Test.class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
    }
}
