/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.functions.worker.rest.api.v2;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.apache.pulsar.client.api.Message;
import org.apache.pulsar.common.io.ConnectorDefinition;
import org.apache.pulsar.functions.proto.Function;
import org.apache.pulsar.functions.proto.InstanceCommunication;
import org.apache.pulsar.functions.worker.rest.FunctionApiResource;
import org.apache.pulsar.functions.worker.rest.api.FunctionsImplV2;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Path("/functions")
public class FunctionApiV2Resource extends FunctionApiResource {

    protected final FunctionsImplV2 functions;

    public FunctionApiV2Resource() {
        this.functions = new FunctionsImplV2(this);
    }

    @POST
    @ApiOperation(value = "Creates a new Pulsar Function in cluster mode")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 400, message = "Invalid request (function already exists, etc.)"),
            @ApiResponse(code = 408, message = "Request timeout"),
            @ApiResponse(code = 200, message = "Pulsar Function successfully created")
    })
    @Path("/{tenant}/{namespace}/{functionName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response registerFunction(final @PathParam("tenant") String tenant,
                                     final @PathParam("namespace") String namespace,
                                     final @PathParam("functionName") String functionName,
                                     final @FormDataParam("data") InputStream uploadedInputStream,
                                     final @FormDataParam("data") FormDataContentDisposition fileDetail,
                                     final @FormDataParam("url") String functionPkgUrl,
                                     final @FormDataParam("functionDetails") String functionDetailsJson,
                                     final @FormDataParam("functionConfig") String functionConfigJson) {

        return functions.registerFunction(tenant, namespace, functionName, uploadedInputStream, fileDetail,
                functionPkgUrl, functionDetailsJson, functionConfigJson, clientAppId());
    }

    @PUT
    @ApiOperation(value = "Updates a Pulsar Function currently running in cluster mode")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 400, message = "Invalid request (function doesn't exist, etc.)"),
            @ApiResponse(code = 200, message = "Pulsar Function successfully updated")
    })
    @Path("/{tenant}/{namespace}/{functionName}")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response updateFunction(final @PathParam("tenant") String tenant,
                                   final @PathParam("namespace") String namespace,
                                   final @PathParam("functionName") String functionName,
                                   final @FormDataParam("data") InputStream uploadedInputStream,
                                   final @FormDataParam("data") FormDataContentDisposition fileDetail,
                                   final @FormDataParam("url") String functionPkgUrl,
                                   final @FormDataParam("functionDetails") String functionDetailsJson,
                                   final @FormDataParam("functionConfig") String functionConfigJson) {

        return functions.updateFunction(tenant, namespace, functionName, uploadedInputStream, fileDetail,
                functionPkgUrl, functionDetailsJson, functionConfigJson, clientAppId());
    }


    @DELETE
    @ApiOperation(value = "Deletes a Pulsar Function currently running in cluster mode")
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function doesn't exist"),
            @ApiResponse(code = 408, message = "Request timeout"),
            @ApiResponse(code = 200, message = "The function was successfully deleted")
    })
    @Path("/{tenant}/{namespace}/{functionName}")
    public Response deregisterFunction(final @PathParam("tenant") String tenant,
                                       final @PathParam("namespace") String namespace,
                                       final @PathParam("functionName") String functionName) {
        return functions.deregisterFunction(tenant, namespace, functionName, clientAppId());
    }

    @GET
    @ApiOperation(
            value = "Fetches information about a Pulsar Function currently running in cluster mode",
            response = Function.FunctionMetaData.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 408, message = "Request timeout"),
            @ApiResponse(code = 404, message = "The function doesn't exist")
    })
    @Path("/{tenant}/{namespace}/{functionName}")
    public Response getFunctionInfo(final @PathParam("tenant") String tenant,
                                    final @PathParam("namespace") String namespace,
                                    final @PathParam("functionName") String functionName) throws IOException {

        return functions.getFunctionInfo(
                tenant, namespace, functionName);
    }

    @GET
    @ApiOperation(
            value = "Displays the status of a Pulsar Function instance",
            response = InstanceCommunication.FunctionStatus.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 404, message = "The function doesn't exist")
    })
    @Path("/{tenant}/{namespace}/{functionName}/{instanceId}/status")
    public Response getFunctionInstanceStatus(final @PathParam("tenant") String tenant,
                                              final @PathParam("namespace") String namespace,
                                              final @PathParam("functionName") String functionName,
                                              final @PathParam("instanceId") String instanceId) throws IOException {

        return functions.getFunctionInstanceStatus(tenant, namespace, functionName, instanceId, uri.getRequestUri());
    }

    @GET
    @ApiOperation(
            value = "Displays the status of a Pulsar Function running in cluster mode",
            response = InstanceCommunication.FunctionStatus.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions")
    })
    @Path("/{tenant}/{namespace}/{functionName}/status")
    public Response getFunctionStatus(final @PathParam("tenant") String tenant,
                                      final @PathParam("namespace") String namespace,
                                      final @PathParam("functionName") String functionName) throws IOException {
        return functions.getFunctionStatusV2(
                tenant, namespace, functionName, uri.getRequestUri());
    }

    @GET
    @ApiOperation(
            value = "Lists all Pulsar Functions currently deployed in a given namespace",
            response = String.class,
            responseContainer = "Collection"
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions")
    })
    @Path("/{tenant}/{namespace}")
    public Response listFunctions(final @PathParam("tenant") String tenant,
                                  final @PathParam("namespace") String namespace) {
        return functions.listFunctions( tenant, namespace);
    }

    @POST
    @ApiOperation(
            value = "Triggers a Pulsar Function with a user-specified value or file data",
            response = Message.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 408, message = "Request timeout"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/{tenant}/{namespace}/{functionName}/trigger")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response triggerFunction(final @PathParam("tenant") String tenant,
                                    final @PathParam("namespace") String namespace,
                                    final @PathParam("functionName") String functionName,
                                    final @FormDataParam("data") String triggerValue,
                                    final @FormDataParam("dataStream") InputStream triggerStream,
                                    final @FormDataParam("topic") String topic) {
        return functions.triggerFunction(tenant, namespace, functionName, triggerValue, triggerStream, topic);
    }

    @GET
    @ApiOperation(
            value = "Fetch the current state associated with a Pulsar Function",
            response = String.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 404, message = "The key does not exist"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    @Path("/{tenant}/{namespace}/{functionName}/state/{key}")
    public Response getFunctionState(final @PathParam("tenant") String tenant,
                                     final @PathParam("namespace") String namespace,
                                     final @PathParam("functionName") String functionName,
                                     final @PathParam("key") String key) {
        return functions.getFunctionState(tenant, namespace, functionName, key);
    }

    @POST
    @ApiOperation(value = "Restart function instance", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{functionName}/{instanceId}/restart")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restartFunction(final @PathParam("tenant") String tenant,
                                    final @PathParam("namespace") String namespace, final @PathParam("functionName") String functionName,
                                    final @PathParam("instanceId") String instanceId) {
        return functions.restartFunctionInstance(tenant, namespace, functionName, instanceId, uri.getRequestUri());
    }

    @POST
    @ApiOperation(value = "Restart all function instances", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{functionName}/restart")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response restartFunction(final @PathParam("tenant") String tenant,
                                    final @PathParam("namespace") String namespace, final @PathParam("functionName") String functionName) {
        return functions.restartFunctionInstances(tenant, namespace, functionName);
    }

    @POST
    @ApiOperation(value = "Stop function instance", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{functionName}/{instanceId}/stop")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response stopFunction(final @PathParam("tenant") String tenant,
                                 final @PathParam("namespace") String namespace, final @PathParam("functionName") String functionName,
                                 final @PathParam("instanceId") String instanceId) {
        return functions.stopFunctionInstance(tenant, namespace, functionName, instanceId, uri.getRequestUri());
    }

    @POST
    @ApiOperation(value = "Stop all function instances", response = Void.class)
    @ApiResponses(value = { @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 404, message = "The function does not exist"),
            @ApiResponse(code = 500, message = "Internal server error") })
    @Path("/{tenant}/{namespace}/{functionName}/stop")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response stopFunction(final @PathParam("tenant") String tenant,
                                 final @PathParam("namespace") String namespace, final @PathParam("functionName") String functionName) {
        return functions.stopFunctionInstances(tenant, namespace, functionName);
    }

    @POST
    @ApiOperation(
            value = "Uploads Pulsar Function file data",
            hidden = true
    )
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFunction(final @FormDataParam("data") InputStream uploadedInputStream,
                                   final @FormDataParam("path") String path) {
        return functions.uploadFunction(uploadedInputStream, path);
    }

    @GET
    @ApiOperation(
            value = "Downloads Pulsar Function file data",
            hidden = true
    )
    @Path("/download")
    public Response downloadFunction(final @QueryParam("path") String path) {
        return functions.downloadFunction(path);
    }

    @GET
    @ApiOperation(
            value = "Fetches a list of supported Pulsar IO connectors currently running in cluster mode",
            response = List.class
    )
    @ApiResponses(value = {
            @ApiResponse(code = 403, message = "The requester doesn't have admin permissions"),
            @ApiResponse(code = 400, message = "Invalid request"),
            @ApiResponse(code = 408, message = "Request timeout")
    })
    @Path("/connectors")
    public List<ConnectorDefinition> getConnectorsList() throws IOException {
        return functions.getListOfConnectors();
    }
}
