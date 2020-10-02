# 1.4.5 (01.10.2020)

**Notes:**

This is maintenance release only.
There are no new features or any logical changes.
Main reason was to remove warnings and possible incompatibilities that appears while using this extension with java version >=9.

**Changes:**

- Improvement and polishing of README file
- Remove all usage of deprecated API
- Bump jaxb dependencies
- Bump mockito [1.9.5 -> 3.5.13] and fix related API changes
- Bump aws-sdk [1.11.495 -> 1.11.873]
- Bump wagon-provider-api [2.6 -> 3.4.0]
- Bump slf4j [1.7.6 -> 1.7.30]
- Bump junit [4.11 -> 4.13]
- Remove exclusions on dependencies in pom that are not existent (anymore)
- Adjust configuration of maven-javadoc-plugin to be able to deploy with java version >=9.

# Release 1.4.4: Fix artifacts on maven central

**Notes:**
 - This release is repair release for 1.4.3 version on maven central. There are no functional changes between 1.4.4 and 1.4.3.

**Changes:**
 - Artifact signing happens now on `package` phase (not `verify` as before).

# Release 1.4.3: Moving to 2iQ

**Notes:**
 - There are no functional changes between version 1.4.2 and 1.4.3. All changes are only related to formal needs to publish on maven central repo.
 - Due some errors, wrong artifacts were published on maven central. Please **do not use this version as dependency in your pom** if you take it from central repo. Please use 1.4.4 which is a fix only release for 1.4.3. However, version itself if correct and can be used if you build it from source.

**Changes:**
 - Rename package `fi.yle.*` -> `com.x2iq.*`
 - Remove all Yle specific content from pom.xml and README.md
 - Adjust project to fulfill requirements to publish on maven central repo
 - Add [maven-unleash-plugin](https://github.com/shillner/unleash-maven-plugin) to perform deployments
