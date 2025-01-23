package org.chomookun.fintics.etf;

import org.chomookun.arch4j.core.common.support.SpringApplicationInstaller;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

import java.util.Arrays;

@SpringBootApplication(
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
public class FinticsEtfApplication {

    /**
     * runs application
     * @param args arguments
     */
    public static void main(String[] args) {

        // install
        if(Arrays.asList(args).contains("install")) {
            SpringApplicationInstaller.install(FinticsEtfApplication.class, args);
            System.exit(0);
        }

        // runs
        new SpringApplicationBuilder(FinticsEtfApplication.class)
                .web(WebApplicationType.SERVLET)
                .registerShutdownHook(true)
                .run(args);
    }

}
