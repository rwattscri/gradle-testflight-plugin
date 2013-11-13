package com.willowtreeaps.gradle.plugins.testflight

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestFlightPlugin implements Plugin<Project> {
    void apply(Project project) {

        project.extensions.create("testFlightArgs", TestFlightPluginExtension)

        project.task('testFlightUploadTask') << {
            println('TestFlight upload is: STARTED.')
            println("API token: ${project.testFlightArgs.testFlightApiToken}")
            println("Team token: ${project.testFlightArgs.testFlightTeamToken}")
            println("Distro list: ${project.testFlightArgs.testFlightDistroList}")
            println("Notify distro list: ${project.testFlightArgs.testFlightNotifyDistroList}")
            println("Notes: ${project.testFlightArgs.testFlightBuildNotes}")
            println("File path: ${project.testFlightArgs.filePath}")

            try {
                TestFlightUploader uploader = new TestFlightUploader()
                UploadRequest request = new UploadRequest(apiToken: project.testFlightArgs.testFlightApiToken,
                        teamToken: project.testFlightArgs.testFlightTeamToken,
                        buildNotes: project.testFlightArgs.testFlightBuildNotes,
                        distributionLists: project.testFlightArgs.testFlightDistroList,
                        file: new File(project.testFlightArgs.filePath),
                        notifyDistributionList: project.testFlightArgs.testFlightNotifyDistroList)

                UploadResult result = uploader.upload(request)
                if (result.isSucceeded()) {
                    println(result.getMessage())
                } else {
                    throw new GradleException("The TestFlight upload failed: ${result.getMessage()}")
                }
            } catch (Exception e) {
                String err = "There was an error executing the TestFlight upload task in the gradle-testflight-plugin."
                println(err, e)
                throw new GradleException(err, e)
            }

            println('TestFlight upload is: COMPLETE.')
        }
    }
}

class TestFlightPluginExtension {
    String testFlightApiToken
    String testFlightTeamToken
    String testFlightDistroList
    boolean testFlightNotifyDistroList
    String testFlightBuildNotes
    String filePath
}