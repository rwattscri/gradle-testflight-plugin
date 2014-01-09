gradle-testflight-plugin
========================

<p>This plugin is designed to run when you invoke the task:</p>

<p>$ gradle testFlightUploadTask</p>

<p>Visit http://testflightapp.com to set up your TestFlight account.</p>

<p>The below configuration is based on publishing an Android app's .apk file.</p>

<pre>
<code>
    buildscript {
        repositories {
            mavenCentral()

            // If you are using a specific repository that you or someone else hosts and requires
            // authentication, add these properties: repoUsername, repoPassword, and releaseRepoUrl
            // to your gradle.properties file, or pass them using the -P arguments on the command line.
            maven {
                credentials {
                    username repoUsername
                    password repoPassword
                }
                url releaseRepoUrl
            }
        }
        dependencies {
            classpath 'com.willowtreeapps.gradle.plugins:gradle-testflight-plugin:1.0'
        }
    }

    apply plugin: 'testFlight'

    // This is an optional helper method to get the file path of an Android .apk file.  Note that
    // the file name is based on a convention we like, and you should put in the file name according
    // to your build.  The verify step is for assurance that the .apk has actually been created
    // before you try to post it to TestFlight.
    final getApkPath(boolean verify) {
        def fileName = "${buildDir}/apk/${archivesBaseName}-${version}-signed-aligned.apk"
        File f = file(fileName)
        if (verify) {
            assert f.exists()
        }
        return fileName
    }

    // testFlightConfig corresponds to the plugin's extension that exposes the configuration to your
    // build.gradle file.

    testFlightConfig {

        // currentTarget is a required configuration and should be set with your default target.
        // Note its name is compared to the named targets below, but is a case insensitive comparison.

        currentTarget = 'Internal'

        // Add -PtestFlightTarget= some target such as 'internal' or 'external' to specify which specific
        // test flight account you want to upload to.

        // -PtestFlightTarget=external

        if (project.hasProperty('testFlightTarget')) {
            currentTarget = testFlightTarget
        }

        // The date and notes params are just here to illustrate an example of creating notes you may
        // want to pass with your upload.
        String date = new Date(System.currentTimeMillis()).format("hh:mm aa")
        String notes = "APK pushed from the gradle-testflight-plugin to the [${currentTarget}] target at ${date}."

        // The plugin allows you to configure any number of TestFlight targets to which you may want
        // to post your mobile app.  The property values to right assume you have either configured
        // them into your gradle.properties file or are passing them in on the command line.

        // This is an example of two targets arbitrarily named internal and external that have been configured
        // where the internal target is for my testers, and the external target is for my clients.

        targets {
            internal {
                testFlightApiToken = internalApiToken
                testFlightTeamToken = internalTeamToken
                testFlightDistroList = 'Internal'
                testFlightNotifyDistroList = false
                testFlightBuildNotes = notes
                testFlightReplace = true // defaults to true
                filePath = getApkPath(true)
            }

            external {
                testFlightApiToken = externalApiToken
                testFlightTeamToken = externalTeamToken
                testFlightDistroList = externalDistroList
                testFlightNotifyDistroList = true
                testFlightBuildNotes = 'Release notes you want to share with your client.'
                testFlightReplace = true // defaults to true
                filePath = getApkPath(true)
            }
        }
    }

</pre>
</code>