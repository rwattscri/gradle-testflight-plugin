package com.willowtreeaps.gradle.plugins.testflight

import org.apache.http.HttpEntity
import org.apache.http.HttpHost
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntity
import org.apache.http.entity.mime.content.FileBody
import org.apache.http.entity.mime.content.StringBody
import org.apache.http.impl.client.DefaultHttpClient

/**
 * This class does the work of posting to the TestFlight API.
 * Note that the response is not used for anything other than
 * determining if the mobile artifact was successfully posted.
 */
class TestFlightUploader {
    private static final String HOST = 'testflightapp.com'

    private static final String POST = '/api/builds.json'

    public UploadResult upload(UploadRequest ur) {

        DefaultHttpClient httpClient = new DefaultHttpClient()

        HttpHost targetHost = new HttpHost(HOST)
        HttpPost httpPost = new HttpPost(POST)
        FileBody fileBody = new FileBody(ur.file)

        MultipartEntity entity = new MultipartEntity()
        entity.addPart('api_token', new StringBody(ur.apiToken))
        entity.addPart('team_token', new StringBody(ur.teamToken))
        entity.addPart('notes', new StringBody(ur.buildNotes))
        entity.addPart('file', fileBody)

        if (ur.distributionLists) {
            entity.addPart('distribution_lists', new StringBody(ur.distributionLists))
        }

        entity.addPart('notify', new StringBody(ur.notifyDistributionList && ur.distributionLists ? 'True' : 'False'))

        // Note we are hard coding this to always replace the app in test flight.
        entity.addPart('replace', new StringBody(Boolean.toString(ur.replace)))
        httpPost.setEntity(entity)

        HttpResponse response = httpClient.execute(targetHost, httpPost)
        HttpEntity resEntity = response.getEntity()

        InputStream is = resEntity.getContent()

        int statusCode = response.getStatusLine().getStatusCode()

        if (statusCode != 200 && statusCode != 201) {
            String responseBody = new Scanner(is).useDelimiter('\\A').next()
            return new UploadResult(succeeded: false, message: responseBody)
        }

        return new UploadResult(succeeded: true, message: 'The TestFlight upload was successful.')
    }
}

/**
 * Pogo that defines the params needed to perform a TestFlight post.
 */
class UploadRequest {
    String apiToken, teamToken, buildNotes, distributionLists
    File file
    boolean notifyDistributionList, replace
}

/**
 * Pogo that returns the basic response results information back to the caller.
 */
class UploadResult {
    boolean succeeded
    String message
}