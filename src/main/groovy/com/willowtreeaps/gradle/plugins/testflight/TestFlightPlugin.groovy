package com.willowtreeaps.gradle.plugins.testflight

import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Plugin
import org.gradle.api.Project

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

class TestFlightTarget {
    //  The object must have a "name" field in order for the NamedDomainObjectContainer to work.
    String name
    String testFlightApiToken
    String testFlightTeamToken
    String testFlightDistroList
    boolean testFlightNotifyDistroList
    String testFlightBuildNotes
    String filePath

    UploadRequest request() {
        return new UploadRequest(apiToken: testFlightApiToken,
                teamToken: testFlightTeamToken,
                buildNotes: testFlightBuildNotes,
                distributionLists: testFlightDistroList,
                file: new File(filePath),
                notifyDistributionList: testFlightNotifyDistroList)
    }
}