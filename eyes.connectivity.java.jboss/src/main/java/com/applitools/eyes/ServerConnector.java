/*
 * Applitools SDK for Selenium integration.
 */
package com.applitools.eyes;

import com.applitools.IResourceUploadListener;
import com.applitools.RenderingInfo;
import com.applitools.renderingGrid.*;
import com.applitools.utils.ArgumentGuard;
import com.applitools.utils.GeneralUtils;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides an API for communication with the Applitools agent
 */
public class ServerConnector extends RestClient
        implements IServerConnector {

    private String apiKey = null;
    private RenderingInfo renderingInfo;

    /***
     * @param logger A logger instance.
     * @param serverUrl The URI of the Eyes server.
     */
    public ServerConnector(Logger logger, URI serverUrl) {
        super(logger, serverUrl, TIMEOUT);

        endPoint = endPoint.path(API_PATH);
    }

    /***
     * @param logger A logger instance.
     */
    public ServerConnector(Logger logger) {
        this(logger, GeneralUtils.getDefaultServerUrl());
    }

    /***
     * @param serverUrl The URI of the Eyes server.
     */
    public ServerConnector(URI serverUrl) {
        this(null, serverUrl);
    }

    public ServerConnector(){
        this((Logger)null);
    }

    /**
     * Sets the API key of your applitools Eyes account.
     *
     * @param apiKey The api key to set.
     */
    public void setApiKey(String apiKey) {
        ArgumentGuard.notNull(apiKey, "apiKey");
        this.apiKey = apiKey;
    }

    /**
     * @return The currently set API key or {@code null} if no key is set.
     */
    public String getApiKey() {
        return this.apiKey != null ? this.apiKey : System.getenv("APPLITOOLS_API_KEY");
    }

    /**
     * Sets the proxy settings to be used by the rest client.
     * @param proxySettings The proxy settings to be used by the rest client.
     * If {@code null} then no proxy is set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setProxy(ProxySettings proxySettings) {
        setProxyBase(proxySettings);
        // After the server is updated we must make sure the endpoint refers
        // to the correct path.
        endPoint = endPoint.path(API_PATH);
    }

    /**
     *
     * @return The current proxy settings used by the rest client,
     * or {@code null} if no proxy is set.
     */
    @SuppressWarnings("UnusedDeclaration")
    public ProxySettings getProxy() {
        return getProxyBase();
    }

    /**
     * Sets the current server URL used by the rest client.
     * @param serverUrl The URI of the rest server.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setServerUrl(URI serverUrl) {
        setServerUrlBase(serverUrl);
        // After the server is updated we must make sure the endpoint refers
        // to the correct path.
        endPoint = endPoint.path(API_PATH);
    }

    /**
     * @return The URI of the eyes server.
     */
    @SuppressWarnings("UnusedDeclaration")
    public URI getServerUrl() {
        return getServerUrlBase();
    }

    /**
     * Starts a new running session in the agent. Based on the given parameters,
     * this running session will either be linked to an existing session, or to
     * a completely new session.
     *
     * @param sessionStartInfo The start parameters for the session.
     * @return RunningSession object which represents the current running
     *         session
     * @throws EyesException For invalid status codes, or if response parsing
     *          failed.
     */
    public RunningSession startSession(SessionStartInfo sessionStartInfo)
            throws EyesException {

        ArgumentGuard.notNull(sessionStartInfo, "sessionStartInfo");

        logger.verbose("Using JBoss for REST API calls.");

        String postData;
        Response response;
        int statusCode;
        List<Integer> validStatusCodes;
        boolean isNewSession;
        RunningSession runningSession;

        try {

            // since the web API requires a root property for this message
            jsonMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
            postData = jsonMapper.writeValueAsString(sessionStartInfo);

            // returning the root property addition back to false (default)
            jsonMapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        } catch (IOException e) {
            throw new EyesException("Failed to convert " +
                    "sessionStartInfo into Json string!", e);
        }

        try {
            Invocation.Builder request = endPoint.queryParam("apiKey", getApiKey()).
                    request(MediaType.APPLICATION_JSON);
            response = sendWithRetry(HttpMethod.POST, request, Entity.json(postData), null);
        } catch (RuntimeException e) {
            logger.log("Server request failed: " + e.getMessage());
            throw e;
        }

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());
        validStatusCodes.add(Response.Status.CREATED.getStatusCode());

        runningSession = parseResponseWithJsonData(response, validStatusCodes,
                RunningSession.class);

        // If this is a new session, we set this flag.
        statusCode = response.getStatus();
        isNewSession = (statusCode == Response.Status.CREATED.getStatusCode());
        runningSession.setIsNewSession(isNewSession);

        return runningSession;
    }

    /**
     * Stops the running session.
     *
     * @param runningSession The running session to be stopped.
     * @return TestResults object for the stopped running session
     * @throws EyesException For invalid status codes, or if response parsing
     *          failed.
     */
    public TestResults stopSession(final RunningSession runningSession,
                                   final boolean isAborted, final boolean save)
            throws EyesException {

        ArgumentGuard.notNull(runningSession, "runningSession");

        final String sessionId = runningSession.getId();
        Response response;
        List<Integer> validStatusCodes;
        TestResults result;

        HttpMethodCall delete = new HttpMethodCall() {
            public Response call() {

                String currentTime = GeneralUtils.toRfc1123(
                        Calendar.getInstance(TimeZone.getTimeZone("UTC")));

                // Building the request
                Invocation.Builder invocationBuilder = endPoint.path(sessionId)
                        .queryParam("apiKey", getApiKey())
                        .queryParam("aborted", String.valueOf(isAborted))
                        .queryParam("updateBaseline", String.valueOf(save))
                        .request(MediaType.APPLICATION_JSON)
                        .header("Eyes-Expect", "202-accepted")
                        .header("Eyes-Date", currentTime);

                // Actually perform the method call and return the result
                return invocationBuilder.delete();
            }
        };

        response = sendLongRequest(delete, "stopSession");

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());

        result = parseResponseWithJsonData(response, validStatusCodes,
                TestResults.class);
        return result;
    }

    @Override
    public void deleteSession(TestResults testResults) {
        ArgumentGuard.notNull(testResults, "testResults");

        Invocation.Builder invocationBuilder = restClient.target(serverUrl)
                .path("/api/sessions/batches/")
                .path(testResults.getBatchId())
                .path("/")
                .path(testResults.getId())
                .queryParam("apiKey", getApiKey())
                .queryParam("AccessToken", testResults.getSecretToken())
                .request(MediaType.APPLICATION_JSON);

        Response response = invocationBuilder.delete();
    }

    /**
     * Matches the current window (held by the WebDriver) to the expected
     * window.
     *
     * @param runningSession The current agent's running session.
     * @param matchData Encapsulation of a capture taken from the application.
     * @return The results of the window matching.
     * @throws EyesException For invalid status codes, or response parsing
     * failed.
     */
    public MatchResult matchWindow(RunningSession runningSession,
                                   MatchWindowData matchData)
            throws EyesException {

        ArgumentGuard.notNull(runningSession, "runningSession");
        ArgumentGuard.notNull(matchData, "data");

        Response response;
        List<Integer> validStatusCodes;
        MatchResult result;
        String jsonData;

        // since we rather not add an empty "tag" param
        WebTarget runningSessionsEndpoint =
                endPoint.path(runningSession.getId());

        // Serializing data into JSON (we'll treat it as binary later).
        // IMPORTANT This serializes everything EXCEPT for the screenshot (which
        // we'll add later).
        try {
            jsonData = jsonMapper.writeValueAsString(matchData);
        } catch (IOException e) {
            throw new EyesException("Failed to serialize data for matchWindow!",
                                    e);
        }

        // Convert the JSON to binary.
        byte[] jsonBytes;
        ByteArrayOutputStream jsonToBytesConverter = new ByteArrayOutputStream();
        try {
            jsonToBytesConverter.write(
                    jsonData.getBytes(DEFAULT_CHARSET_NAME));
            jsonToBytesConverter.flush();
            jsonBytes = jsonToBytesConverter.toByteArray();
        } catch (IOException e) {
            throw new EyesException("Failed create binary data from JSON!", e);
        }

        // Getting the screenshot's bytes (notice this can be either
        // compressed/uncompressed form).
        byte[] screenshot = Base64.decodeBase64(
                matchData.getAppOutput().getScreenshot64());

        // Ok, let's create the request data
        ByteArrayOutputStream requestOutputStream = new ByteArrayOutputStream();
        DataOutputStream requestDos = new DataOutputStream(requestOutputStream);
        byte[] requestData;
        try {
            requestDos.writeInt(jsonBytes.length);
            requestDos.flush();
            requestOutputStream.write(jsonBytes);
            requestOutputStream.write(screenshot);
            requestOutputStream.flush();

            // Ok, get the data bytes
            requestData = requestOutputStream.toByteArray();

            // Release the streams
            requestDos.close();
        } catch (IOException e) {
            throw new EyesException("Failed send check window request!", e);
        }

        // Sending the request
        Invocation.Builder request = runningSessionsEndpoint.queryParam("apiKey", getApiKey()).
                request(MediaType.APPLICATION_JSON);
        response = sendWithRetry(HttpMethod.POST, request, Entity.entity(requestData,
                MediaType.APPLICATION_OCTET_STREAM), null);

        // Ok, let's create the running session from the response
        validStatusCodes = new ArrayList<>(1);
        validStatusCodes.add(Response.Status.OK.getStatusCode());

        result = parseResponseWithJsonData(response, validStatusCodes,
                MatchResult.class);

        return result;

    }

    @Override
    public void downloadString(final URL uri, final boolean isSecondRetry, final IDownloadListener listener) {

        Client client = ClientBuilder.newBuilder().build();

        WebTarget target = client.target(uri.toString());

        Invocation.Builder request = target.request(MediaType.WILDCARD);

        request.async().get(new InvocationCallback<String>() {
            @Override
            public void completed(String response) {
                logger.verbose(uri + " - completed");
                listener.onDownloadComplete(response);
            }

            @Override
            public void failed(Throwable throwable) {
                GeneralUtils.logExceptionStackTrace(new Exception(throwable));
                if (!isSecondRetry) {
                    logger.verbose("Entring retry");
                    downloadString(uri, true, listener);
                }
                else {
                    listener.onDownloadFailed();
                }
            }
        });


    }

    @Override
    public String postDomSnapshot(String domJson) {

        WebTarget target = restClient.target(serverUrl).path((RUNNING_DATA_PATH)).queryParam("apiKey", getApiKey());

        byte[] resultStream = GeneralUtils.getGzipByteArrayOutputStream(domJson);

        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        Response response = sendWithRetry(HttpMethod.POST, request, Entity.entity(resultStream,
                MediaType.APPLICATION_OCTET_STREAM), null);

        String entity = response.getHeaderString("Location");

        return entity;
    }

    private Response sendWithRetry(String method, Invocation.Builder request, Entity entity, AtomicInteger retiresCounter) {

        if (retiresCounter == null) {

            retiresCounter = new AtomicInteger(0);

        }

        Response response = null;
        try {
            switch (method) {
                case HttpMethod.POST:
                    response = request.post(entity);
                    break;
                case HttpMethod.PUT:
                    response = request.put(entity);

            }

            return response;
        } catch (Exception e) {

            GeneralUtils.logExceptionStackTrace(e);
            try {

                Thread.sleep(THREAD_SLEEP_MILLIS);

            } catch (InterruptedException e1) {
                GeneralUtils.logExceptionStackTrace(e);
            }

            if (retiresCounter.incrementAndGet() < NUM_OF_RETRIES) {

                return sendWithRetry(method, request, entity, retiresCounter);
            } else {
                throw e;
            }
        }

    }


    @Override
    public void getRenderInfo() {
        WebTarget target = restClient.target(serverUrl).path((RENDER_INFO_PATH)).queryParam("apiKey", getApiKey());
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);

        // Ok, let's create the running session from the response
        List<Integer> validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());


        Response response = request.get();
        renderingInfo = parseResponseWithJsonData(response, validStatusCodes, RenderingInfo.class);
    }

    @Override
    public List<RunningRender> render(RenderRequest... renderRequests) {

        ArgumentGuard.notNull(renderRequests, "renderRequests");
        this.logger.verbose("ServerConnector.render called with "+renderRequests);

        WebTarget target = restClient.target(renderingInfo.getServiceUrl()).path((RENDER_STATUS));
        if (renderRequests.length > 1) {
            target.matrixParam("render-id", renderRequests);
        }
        else{
            target.queryParam("render-id", renderRequests);
        }
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", renderingInfo.getAccessToken());

        // Ok, let's create the running session from the response
        List<Integer> validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());
        validStatusCodes.add(Response.Status.NOT_FOUND.getStatusCode());

        Response response = request.head();
        if (validStatusCodes.contains(response.getStatus())) {
            this.logger.verbose("ServerConnector.checkResourceExists - request succeeded");
            return Arrays.asList(response.readEntity(RunningRender[].class));
        }

        throw new EyesException("ServerConnector.checkResourceExists - unexpected status (" + response.getStatus() + ")");
    }

    @Override
    public boolean renderCheckResource(RunningRender runningRender, RGridResource resource) {

        ArgumentGuard.notNull(runningRender, "runningRender");
        ArgumentGuard.notNull(resource, "resource");
        this.logger.verbose("ServerConnector.checkResourceExists called with resource#" + resource.getSha256hash() + " for render: " + runningRender.getRenderId());


        WebTarget target = restClient.target(renderingInfo.getServiceUrl()).path((RESOURCES_SHA_256) + resource.getSha256hash()).queryParam("render-id", runningRender.getRenderId());
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", renderingInfo.getAccessToken());

        // Ok, let's create the running session from the response
        List<Integer> validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());
        validStatusCodes.add(Response.Status.NOT_FOUND.getStatusCode());


        Response response = request.head();
        if (validStatusCodes.contains(response.getStatus())) {
            this.logger.verbose("ServerConnector.checkResourceExists - request succeeded");
            return response.getStatus() == Response.Status.OK.getStatusCode();
        }

        throw new EyesException("ServerConnector.checkResourceExists - unexpected status (" + response.getStatus() + ")");
    }


    @Override
    public void renderPutRequest(final RunningRender runningRender, final RGridResource resource, final boolean isRetryOn, final IResourceUploadListener listener) {
        ArgumentGuard.notNull(runningRender, "runningRender");
        ArgumentGuard.notNull(resource, "resource");
        ArgumentGuard.notNull(resource.getContent(), "resource.getContent()");

        this.logger.verbose("ServerConnector.checkResourceExists called with resource#" + resource.getSha256hash() + " for render: " + runningRender.getRenderId());

        WebTarget target = restClient.target(renderingInfo.getServiceUrl()).path((RESOURCES_SHA_256) + resource.getSha256hash()).queryParam("render-id", runningRender.getRenderId());
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);
        target.request(MediaType.APPLICATION_OCTET_STREAM_TYPE);

        request.async().get(new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                // on complete
                listener.onUploadComplete(response.getStatus() == Response.Status.OK.getStatusCode());
            }

            @Override
            public void failed(Throwable throwable) {
                // on fail
                if (isRetryOn) {
                    logger.verbose("Async GET failed - entering retry");
                    renderPutRequest(runningRender, resource, false, listener);
                } else {
                    listener.onUploadFailed();
                }
            }
        });

    }

    @Override
    public RenderStatusResults renderStatus(RunningRender runningRender) {
        List<RenderStatusResults> renderStatusResults = renderStatusById(runningRender.getRenderId());
        if(!renderStatusResults.isEmpty()){
            return renderStatusResults.get(0);
        }
        return null;
    }

    @Override
    public List<RenderStatusResults> renderStatusById(String... renderIds) {

        ArgumentGuard.notNull(renderIds, "renderIds");
        this.logger.verbose("ServerConnector.renderStatus called for render: " + renderIds);

        WebTarget target = restClient.target(renderingInfo.getServiceUrl()).path((RENDER_STATUS));
        if (renderIds.length > 1) {
            target.matrixParam("render-id", renderIds);
        }
        else{
            target.queryParam("render-id", renderIds);
        }
        Invocation.Builder request = target.request(MediaType.APPLICATION_JSON);
        request.header("X-Auth-Token", renderingInfo.getAccessToken());

        // Ok, let's create the running session from the response
        List<Integer> validStatusCodes = new ArrayList<>();
        validStatusCodes.add(Response.Status.OK.getStatusCode());
        validStatusCodes.add(Response.Status.NOT_FOUND.getStatusCode());

        Response response = request.head();
        if (validStatusCodes.contains(response.getStatus())) {
            this.logger.verbose("ServerConnector.checkResourceExists - request succeeded");
            return Arrays.asList(response.readEntity(RenderStatusResults[].class));
        }

        throw new EyesException("ServerConnector.checkResourceExists - unexpected status (" + response.getStatus() + ")");
    }

}
