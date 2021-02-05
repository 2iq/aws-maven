# 2.0.1 (05.02.2021)

**Bug Fixes:**

- Fix issue that baseDirectory was not used for getting resources (a85bf8d)

**Maintenance:**

- Update all http to https URLs in pom.xml (e349e1d)
- Add maven-enforcer-plugin (e0ffb68)
- Manage version of maven plugins in pluginManagement (e764620)
- Fix some formatting issues in CHANGELOG.md file (450357)

**Dependency updates:**

- Bump awssdk [2.15.2 -> 2.15.77] (3ac3e3b)
- Bump mockito [3.5.13 -> 3.7.7] (d5fab20)
- Bump wagon-provider-api [3.4.0 -> 3.4.2] (8e2255c)
- Bump maven-surefire-plugin [2.16 -> 3.0.0-M5] (8e2255c)
- Bump maven-resources-plugin [2.6 -> 3.1.0] (8e2255c)
- Bump maven-deploy-plugin [2.8.1 -> 3.0.0-M1] (8e2255c)
- Bump maven-compiler-plugin [3.1 -> 3.8.1] (8e2255c)

# 2.0.0 (05.10.2020)

**Changed:**

- Change min java version [7 -> 8]
- Switch aws-sdk from version 1.x to 2.x
- Small cleanups

**Notes:**

This is major release because min java version was changed from 7 to 8.
Java 8 was necessary to switch aws-sdk to version 2.
There is no change on API or general usage and no need to adjust anything on switching to version 2.0.0.

# 1.4.6 (02.10.2020)

**Changes:**

- Fix `Client is immutable when created with the builder` error

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
