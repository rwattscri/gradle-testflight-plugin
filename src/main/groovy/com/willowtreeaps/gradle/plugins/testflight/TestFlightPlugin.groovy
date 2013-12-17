package com.willowtreeaps.gradle.plugins.testflight

import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * The short name for this plugin is "testFlight".  It contains one task programmed directly
 * into the apply method.  The task's name is: testFlightConfig
 */
class TestFlightPlugin implements Plugin<Project> {
    void apply(Project project) {

        def targets = project.container(TestFlightTarget) { name ->
            new TestFlightTarget(name: name)
        }

        project.extensions.create("testFlightConfig", TestFlightPluginExtension, targets)

        project.task('testFlightUploadTask') << {
            println('TestFlight upload is: STARTED.')

            println("TestFlight Target: ${project.testFlightConfig.currentTarget}")

            TestFlightTarget currentParams = project.testFlightConfig.getTargetParams()

            println("API token: ${currentParams.testFlightApiToken}")
            println("Team token: ${currentParams.testFlightTeamToken}")
            println("Distro list: ${currentParams.testFlightDistroList}")
            println("Notify distro list: ${currentParams.testFlightNotifyDistroList}")
            println("Notes: ${currentParams.testFlightBuildNotes}")
            println("File path: ${currentParams.filePath}")
            println("Replace: ${currentParams.testFlightReplace}")

            try {
                TestFlightUploader uploader = new TestFlightUploader()

                UploadResult result = uploader.upload(currentParams.request())
                if (result.isSucceeded()) {
                    println(result.getMessage())
                } else {
                    throw new GradleException("The TestFlight upload failed: ${result.getMessage()}")
                }
            } catch (Exception e) {
                String err = "There was an error executing the TestFlight upload task in the gradle-testflight-plugin."
                throw new GradleException(err, e)
            }

            println('TestFlight upload is: COMPLETE.')
        }
    }
}

/**
 * This class provides the hook to create multiple TestFlight targets
 * in your build file.  The 'currentTarget' property is required and
 * is used as the default value if it is not otherwise set via some
 * other property you choose to pass in to your build.
 *
 * The 'currentTarget' value will be compared (case insensitive) to the
 * name value in a TestFlightTarget in order to choose which target
 * to which your mobile artifact will be posted.
 */
class TestFlightPluginExtension {
    String currentTarget
    final NamedDomainObjectContainer<TestFlightTarget> targets

    TestFlightPluginExtension(NamedDomainObjectContainer<TestFlightTarget> targets) {
        this.targets = targets
    }

    TestFlightTarget getTargetParams() {
        return targets.find { it ->
            it.name.equalsIgnoreCase(currentTarget)
        }
    }

    def targets(Closure closure) {
        targets.configure(closure)
    }
}

/**
 * This class defines the properties for your TestFlight configurations.
 */
class TestFlightTarget {
    //  The object must have a "name" field in order for the NamedDomainObjectContainer to work.
    String name
    String testFlightApiToken
    String testFlightTeamToken
    String testFlightDistroList
    boolean testFlightNotifyDistroList
    String testFlightBuildNotes
    String filePath
    boolean testFlightReplace

    UploadRequest request() {
        return new UploadRequest(apiToken: testFlightApiToken,
                teamToken: testFlightTeamToken,
                buildNotes: testFlightBuildNotes,
                distributionLists: testFlightDistroList,
                file: new File(filePath),
                notifyDistributionList: testFlightNotifyDistroList,
                replace: testFlightReplace)
    }
}