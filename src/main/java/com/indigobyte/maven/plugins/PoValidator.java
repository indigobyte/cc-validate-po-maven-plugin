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

import com.google.common.collect.LinkedHashMultiset;
import com.indigobyte.helper.Utils;
import com.indigobyte.javautil.Formatter;
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
import java.util.*;
import java.util.stream.Collectors;

@Mojo(name = "validate-po")
public class PoValidator extends AbstractMojo {
    private static Formatter formatter = new Formatter();

    @Parameter(property = "potFileName", required = true)
    private String potFileName;

    @Parameter(property = "poFileName", required = true)
    private String poFileName;

    @Parameter(property = "exactMatch", required = true)
    private boolean exactMatch;

    @Parameter(property = "skip", required = false, defaultValue = "false")
    private boolean skip;

//    public static void main(String[] args) throws MojoExecutionException {
//        validateFiles(
//                "c:\\ccrepos\\frontback2\\task-2081\\backend\\i18n\\src\\messages.pot",
//                "c:\\ccrepos\\frontback2\\task-2081\\backend\\i18n\\src\\messages_ru.po",
//                false
//        );
//    }

    private static List<Formatter.FormatSpecifier> getFormatSpecifiers(String str) {
        try {
            List<Formatter.FormatSpecifier> result = formatter.parse(str).stream()
                    .filter(p -> p instanceof Formatter.FormatSpecifier)
                    .map(p -> (Formatter.FormatSpecifier) p)
                    .collect(Collectors.toList());
            return result;
        } catch (UnknownFormatConversionException e) {
            System.err.println("Unable to parse string \"" + str + "\"");
            e.printStackTrace();
            throw e;
        }
    }

    @NotNull
    private static String getContextId(@NotNull Message potMessage) {
        return "ctx: \"" + potMessage.getMsgctxt() + "\", id: \"" + potMessage.getMsgid() + "\"";
    }

    private static void checkFormatMatching(@NotNull Message potMessage, @NotNull Message poMessage, @NotNull String potString, @NotNull String poString) throws MojoExecutionException {
        List<Formatter.FormatSpecifier> potFormats = getFormatSpecifiers(potString);
        List<Formatter.FormatSpecifier> poFormats = getFormatSpecifiers(poString);
        LinkedHashMultiset<Formatter.FormatSpecifier> potFormatSet = LinkedHashMultiset.create(potFormats);
        LinkedHashMultiset<Formatter.FormatSpecifier> poFormatSet = LinkedHashMultiset.create(poFormats);

        if (Objects.equals(potFormatSet, poFormatSet)) {
            return;
        }

        throw new MojoExecutionException("Format lists do not match: \"" +
                potString + "\" != \"" + poString + "\", " + getContextId(potMessage)
        );
    }

    private static void validateFiles(@NotNull String potFileName, @NotNull String poFileName, boolean exactMatch) throws MojoExecutionException {
        Map<MessageContextId, Message> potCatalog = loadCatalog(potFileName);
        Map<MessageContextId, Message> poCatalog = loadCatalog(poFileName);
        if (!Objects.equals(potCatalog.keySet(), poCatalog.keySet())) {
            throw new MojoExecutionException("Message lists do not match: " + Utils.createMessage(
                    potCatalog.keySet(),
                    poCatalog.keySet()
            ));
        }
        for (Map.Entry<MessageContextId, Message> entry : potCatalog.entrySet()) {
            MessageContextId messageContextId = entry.getKey();
            Message potMessage = entry.getValue();
            Message poMessage = poCatalog.get(messageContextId);
            if (!Objects.equals(LinkedHashMultiset.create(potMessage.getSourceReferences()), LinkedHashMultiset.create(poMessage.getSourceReferences()))) {
                throw new MojoExecutionException("Source references do not match: " + Utils.createMessage(
                        LinkedHashMultiset.create(potMessage.getSourceReferences()),
                        LinkedHashMultiset.create(poMessage.getSourceReferences())
                ) + ", " + getContextId(potMessage));
            }
            if (!Objects.equals(potMessage.getMsgidPlural(), poMessage.getMsgidPlural())) {
                throw new MojoExecutionException("Ids of plural forms do not match: \"" +
                        potMessage.getMsgidPlural() + "\" != \"" + poMessage.getMsgidPlural() + "\", " + getContextId(potMessage)
                );
            }
            if (exactMatch) {
                if (potMessage.getMsgidPlural() != null && !potMessage.getMsgidPlural().isEmpty()) { //With plural form
                    if (!Objects.equals(poMessage.getMsgid(), poMessage.getMsgstrPlural().get(0))) {
                        throw new MojoExecutionException("msgid does not match msgstr[0]: \"" +
                                poMessage.getMsgid() + "\" != \"" + poMessage.getMsgstrPlural().get(0) + "\", " + getContextId(potMessage)
                        );
                    }
                } else { //No plural form
                    if (!Objects.equals(poMessage.getMsgid(), poMessage.getMsgstr())) {
                        throw new MojoExecutionException("msgid does not match msgstr: \"" +
                                poMessage.getMsgid() + "\" != \"" + poMessage.getMsgstr() + "\", " + getContextId(potMessage)
                        );
                    }
                }
            } else {
                if (potMessage.getMsgidPlural() != null && !potMessage.getMsgidPlural().isEmpty()) { //With plural form
                    checkFormatMatching(potMessage, poMessage, potMessage.getMsgid(), poMessage.getMsgstrPlural().get(0));
                } else { //No plural form
                    checkFormatMatching(potMessage, poMessage, potMessage.getMsgid(), poMessage.getMsgstr());
                }
            }
            if (potMessage.getMsgidPlural() != null) {
                if (potMessage.getMsgstrPlural() == null || potMessage.getMsgstrPlural().size() != 2) {
                    throw new MojoExecutionException("POT Message " + getContextId(potMessage) + " must have 2 plural forms, but has none");
                }
                String potPluralStr = potMessage.getMsgidPlural();
                if (poMessage.getMsgstrPlural() == null || poMessage.getMsgstrPlural().size() < 2) {
                    throw new MojoExecutionException("PO Message " + getContextId(potMessage) + " must have at least 2 plural forms");
                }
                List<String> msgstrPlural = poMessage.getMsgstrPlural();
                for (int i = 1; i < msgstrPlural.size(); i++) {
                    String poPluralStr = msgstrPlural.get(i);
                    if (exactMatch) {
                        if (!Objects.equals(potMessage.getMsgidPlural(), poPluralStr)) {
                            throw new MojoExecutionException("msgid_plural does not match msgstr[1]: \"" +
                                    potMessage.getMsgidPlural() + "\" != \"" + poMessage.getMsgstr() + "\", " + getContextId(potMessage)
                            );
                        }
                    } else {
                        checkFormatMatching(potMessage, poMessage, potPluralStr, poPluralStr);
                    }
                }
            }
        }
    }

    @NotNull
    private static Map<MessageContextId, Message> loadCatalog(@NotNull String fileName) throws MojoExecutionException {
        PoParser parser = new PoParser();
        Catalog potCatalog;
        try {
            potCatalog = parser.parseCatalog(new File(fileName));
        } catch (IOException e) {
            throw new MojoExecutionException("Unable to parse file " + fileName, e);
        }
        Map<MessageContextId, Message> result = new HashMap<>();
        boolean nullContextMessageRead = false;
        for (Message message : potCatalog) {
            if (message.getMsgctxt() == null && !nullContextMessageRead) {
                nullContextMessageRead = true;
                continue;
            }
            Message oldValue = result.put(new MessageContextId(message.getMsgctxt(), message.getMsgid()), message);
            if (oldValue != null) {
                throw new MojoExecutionException("Duplicate message context id pair: old " + oldValue + ", new: " + message);
            }
        }
        return result;
    }

    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("PoValidator mojo has started");
        if (!skip) {
            validateFiles(potFileName, poFileName, exactMatch);
        } else {
            getLog().info("\"skip\" is set to \"true\", PO validation was skipped");
        }
    }

    private static class MessageContextId {
        @NotNull
        private final String context;
        @NotNull
        private final String id;

        public MessageContextId(@NotNull String context, @NotNull String id) {
            this.context = context;
            this.id = id;
        }

        @NotNull
        public String getContext() {
            return context;
        }

        @NotNull
        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            MessageContextId that = (MessageContextId) o;
            return context.equals(that.context) &&
                    id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(context, id);
        }

        @Override
        public String toString() {
            return "MessageContextId{" +
                    "context='" + context + '\'' +
                    ", id='" + id + '\'' +
                    '}';
        }
    }
}
