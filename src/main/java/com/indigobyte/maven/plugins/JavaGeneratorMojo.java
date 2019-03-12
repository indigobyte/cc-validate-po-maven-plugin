package com.indigobyte.maven.plugins;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.fedorahosted.tennera.jgettext.Catalog;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.PoParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Mojo(name = "generate-java")
public class JavaGeneratorMojo extends AbstractMojo {

    @Parameter(property = "outputDir", required = true)
    private String outputDir;

    @Parameter(property = "poFileName", required = true)
    private String poFileName;

    @Parameter(property = "resourceName", required = true)
    private String resourceName;

    @Parameter(property = "language", required = true)
    private String language;

    @Parameter(property = "skip", required = false, defaultValue = "false")
    private boolean skip;

    public static void main(String[] args) throws MojoExecutionException {
        if (args.length == 4) {
            generateJavaFile(
                    args[0],
                    args[1],
                    args[2],
                    args[3]
            );
        } else {
            System.out.println("Usage: java -cp cc-validate-po-maven-plugin.jar com.indigobyte.maven.plugins.JavaGeneratorMojo <folder for generated Java files> <resource name> <language> <po file>\n" +
                    "example:\n" +
                    "java -cp cc-validate-po-maven-plugin.jar com.indigobyte.maven.plugins.JavaGeneratorMojo ./i18n/generated com.indigobyte.i18n.messages en messages_en.po"
            );

            System.exit(1);
        }
    }

    private static void generateJavaFile(
            @NotNull String outputDir,
            @NotNull String resourceName,
            @NotNull String language,
            @NotNull String poFileName
    ) throws MojoExecutionException {
        JavaGenerator javaGenerator = new JavaGenerator();

        PoParser parser = new PoParser();
        Catalog potCatalog;
        try {
            potCatalog = parser.parseCatalog(new File(poFileName));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to parse file " + poFileName, e);
        }
        Message poProperties = null;

        for (Message message : potCatalog) {
            if (message.getMsgctxt() == null) {
                poProperties = message;
                continue;
            }
            javaGenerator.addInitLine(message);
        }

        Path path = Paths.get(outputDir);
        String[] packageNames = resourceName.split("\\.");
        for (int i = 0; i < packageNames.length - 1; ++i) {
            path = path.resolve(packageNames[i]);
        }
        path = path.normalize();
        String className = packageNames[packageNames.length - 1] + "_" + language;
        path = path.resolve(className + ".java");
        int lastDotPos = resourceName.lastIndexOf('.');
        String fullPackagePath = "";
        if (lastDotPos != -1) {
            fullPackagePath = resourceName.substring(0, lastDotPos);
        }
        String pluralEval = null;
        if (poProperties != null) {
            for (String str : poProperties.getMsgstr().split("\n")) {
                if (str.startsWith("Plural-Forms:")) {
                    String pluralFormPrefix = "plural=";
                    int pluralFormulaPos = str.indexOf(pluralFormPrefix);
                    if (pluralFormulaPos == -1) {
                        throw new MojoExecutionException("Invalid PO file: \"Plural-Forms\" was found, but \"plural=\" is missing " + poFileName);
                    }
                    pluralEval = str.substring(pluralFormulaPos + pluralFormPrefix.length());
                    pluralEval = pluralEval.trim();
                    if (pluralEval.endsWith(";")) {
                        pluralEval = pluralEval.substring(0, pluralEval.length() - 1);
                    }
                    if (!pluralEval.contains("?")) {
                        pluralEval += "? 0 : 1";
                    }
                }
            }
        }

        String javaCode = javaGenerator.getText(fullPackagePath, className, pluralEval);
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, javaCode.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to write generated Java code to file " + path, e);
        }
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("JavaGeneratorMojo has started");
        if (!skip) {
            generateJavaFile(outputDir, resourceName, language, poFileName);
        } else {
            getLog().info("\"skip\" is set to \"true\", Java code generation from PO file was skipped");
        }
    }
}
