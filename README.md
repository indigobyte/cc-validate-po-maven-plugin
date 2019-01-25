# mavendeployplugin

Maven plugin for validation of `.po` file against given `.pot` one.

## Introduction

### Usage
In pom.file 
```xml
<plugin>
    <groupId>com.indigobyte.maven.plugins</groupId>
    <artifactId>cc-validate-po-maven-plugin</artifactId>
    <version>${validate.po.plugin.version}</version>
    <executions>
        <execution>
            <id>validate-po</id>
            <phase>process-resources</phase>
            <goals>
                <goal>validate-po</goal>
            </goals>
            <configuration>
                <potFileName>${project.basedir}/../i18n/src/messages.pot</potFileName>
                <poFileName>${project.basedir}/../i18n/src/messages_en.po</poFileName>
                <exactMatch>true</exactMatch>
                <allowFuzzy>false</allowFuzzy>
                <allowEmptyTranslations>false</allowEmptyTranslations>
                <skip>${i18n.disabled}</skip>
            </configuration>
        </execution>
    </executions>
</plugin>
```

where `${validate.po.plugin.version}` is the version of the plugin and `${i18n.disabled}` is `true` or `false`.

### Plugin configuration

|parameter|description|required|
|---|---|---|
|potFileName|path to the `.pot` file|yes|
|poFileName|path to the `.po` file|yes|
|exactMatch|<ul><li>if `true`, then:<ul><li>for messages without plural form: `msgstr` must be identical to `msgid`</li><li>for messages with plural form: <ul><li>`msgstr[0]` must be identical to `msgid`,</li><li>`msgstr[1]` must be identical to `msgid_plural`</li></ul></li></ul></li><li>if `false`, same as in `true` case, but instead of string matching format specifiers matching is required. That is, in order of strings `str` and `str2` to match, all format specifiers extracted from `str` must match those of `str2` (format specifiers may be present in any order) </li></ul>|yes|
|allowEmptyTranslations|<ul><li>if `true`, then empty translations will not trigger validation error (although if `exactMatch` is `true`, it will still trigger an error since translation must match message ID and that one is not empty).</li><li>if `false`, then all empty translations will be deemed invalid and will result in failed validation.</li></ul>|yes|
|allowFuzzy|<ul><li>if `true`, then fuzzy translations will not trigger validation error.</li><li>if `false`, then all fuzzy translations will be deemed invalid and will result in failed validation.</li></ul>|yes|
|skip|if `true`, plugin execution is skipped (i.e. ignore all other parameters and don't do anything)|no, default value is `false`|
    

### How to create new version of plugin

1. Let's say git repository is cloned into local folder `C:\cc-validate-po-maven-plugin`.
1. Clone repository to separate folder, e.g. `C:\mvn-repo`.
1. Switch to `mvn-repo` branch in that folder.
1. Go back to folder `C:\cc-validate-po-maven-plugin`
1. Change version in `pom.xml`.
1. Run `mvn clean install -DmvnRepo=C:\mvn-repo`. (when running on Cygwin, `\`, must be escaped: `mvn clean install -DmvnRepo=C:\\mvn-repo`)
1. Go to folder `C:\mvn-repo`.
1. Commit and push `mvn-repo` branch to server. 

### Maven: how to build and install locally, without uploading to remote repository

    mvn clean install
    
