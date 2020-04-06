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
