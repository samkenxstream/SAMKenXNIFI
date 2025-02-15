/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.registry.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import java.util.List;
import java.util.SortedSet;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.lang3.StringUtils;
import org.apache.nifi.extension.ExtensionMetadata;
import org.apache.nifi.registry.event.EventService;
import org.apache.nifi.registry.exception.ResourceNotFoundException;
import org.apache.nifi.registry.extension.bundle.BundleVersionFilterParams;
import org.apache.nifi.registry.extension.bundle.BundleVersionMetadata;
import org.apache.nifi.registry.extension.repo.ExtensionRepoArtifact;
import org.apache.nifi.registry.extension.repo.ExtensionRepoBucket;
import org.apache.nifi.registry.extension.repo.ExtensionRepoExtensionMetadata;
import org.apache.nifi.registry.extension.repo.ExtensionRepoGroup;
import org.apache.nifi.registry.extension.repo.ExtensionRepoVersion;
import org.apache.nifi.registry.extension.repo.ExtensionRepoVersionSummary;
import org.apache.nifi.registry.web.service.ServiceFacade;
import org.apache.nifi.registry.web.service.StreamingContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/extension-repository")
@Api(
    value = "extension repository",
    authorizations = {@Authorization("Authorization")},
    tags = {"Swagger Resource"}
)
@SwaggerDefinition(tags = {
    @Tag(name = "Swagger Resource", description = "Interact with extension bundles via the hierarchy of bucket/group/artifact/version.")
})
public class ExtensionRepoResource extends ApplicationResource {

    public static final String CONTENT_DISPOSITION_HEADER = "content-disposition";

    @Autowired
    public ExtensionRepoResource(final ServiceFacade serviceFacade, final EventService eventService) {
        super(serviceFacade, eventService);
    }

    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get extension repo buckets",
            notes = "Gets the names of the buckets the current user is authorized for in order to browse the repo by bucket. " + NON_GUARANTEED_ENDPOINT,
            response = ExtensionRepoBucket.class,
            responseContainer = "List"
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoBuckets() {
        final SortedSet<ExtensionRepoBucket> repoBuckets = serviceFacade.getExtensionRepoBuckets(getBaseUri());
        return Response.status(Response.Status.OK).entity(repoBuckets).build();
    }

    @GET
    @Path("{bucketName}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get extension repo groups",
            notes = "Gets the groups in the extension repository in the given bucket. " + NON_GUARANTEED_ENDPOINT,
            response = ExtensionRepoGroup.class,
            responseContainer = "List",
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoGroups(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName
    ) {
        final SortedSet<ExtensionRepoGroup> repoGroups = serviceFacade.getExtensionRepoGroups(getBaseUri(), bucketName);
        return Response.status(Response.Status.OK).entity(repoGroups).build();
    }

    @GET
    @Path("{bucketName}/{groupId}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get extension repo artifacts",
            notes = "Gets the artifacts in the extension repository in the given bucket and group. " + NON_GUARANTEED_ENDPOINT,
            response = ExtensionRepoArtifact.class,
            responseContainer = "List",
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoArtifacts(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group id")
                final String groupId
    ) {
        final SortedSet<ExtensionRepoArtifact> repoArtifacts = serviceFacade.getExtensionRepoArtifacts(getBaseUri(), bucketName, groupId);
        return Response.status(Response.Status.OK).entity(repoArtifacts).build();
    }

    @GET
    @Path("{bucketName}/{groupId}/{artifactId}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get extension repo versions",
            notes = "Gets the versions in the extension repository for the given bucket, group, and artifact. " + NON_GUARANTEED_ENDPOINT,
            response = ExtensionRepoVersionSummary.class,
            responseContainer = "List",
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoVersions(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId
    ) {
        final SortedSet<ExtensionRepoVersionSummary> repoVersions = serviceFacade.getExtensionRepoVersions(
                getBaseUri(), bucketName, groupId, artifactId);
        return Response.status(Response.Status.OK).entity(repoVersions).build();
    }

    @GET
    @Path("{bucketName}/{groupId}/{artifactId}/{version}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get extension repo version",
            notes = "Gets information about the version in the given bucket, group, and artifact. " + NON_GUARANTEED_ENDPOINT,
            response = ExtensionRepoVersion.class,
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoVersion(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId,
            @PathParam("version")
            @ApiParam("The version")
                final String version
    ) {
        final ExtensionRepoVersion repoVersion = serviceFacade.getExtensionRepoVersion(
                getBaseUri(), bucketName, groupId, artifactId, version);
        return Response.ok(repoVersion).build();
    }

    @GET
    @Path("{bucketName}/{groupId}/{artifactId}/{version}/extensions")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get extension repo extensions",
            notes = "Gets information about the extensions in the given bucket, group, artifact, and version. " + NON_GUARANTEED_ENDPOINT,
            response = ExtensionMetadata.class,
            responseContainer = "List",
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoVersionExtensions(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId,
            @PathParam("version")
            @ApiParam("The version")
                final String version
    ) {

        final List<ExtensionRepoExtensionMetadata> extensionRepoExtensions =
                serviceFacade.getExtensionRepoExtensions(
                        getBaseUri(), bucketName, groupId, artifactId, version);

        return Response.ok(extensionRepoExtensions).build();
    }

    @GET
    @Path("{bucketName}/{groupId}/{artifactId}/{version}/extensions/{name}")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            value = "Get extension repo extension",
            notes = "Gets information about the extension with the given name in " +
                    "the given bucket, group, artifact, and version. " + NON_GUARANTEED_ENDPOINT,
            response = org.apache.nifi.extension.manifest.Extension.class,
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoVersionExtension(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId,
            @PathParam("version")
            @ApiParam("The version")
                final String version,
            @PathParam("name")
            @ApiParam("The fully qualified name of the extension")
                final String name
    ) {
        final org.apache.nifi.extension.manifest.Extension extension =
                serviceFacade.getExtensionRepoExtension(
                        getBaseUri(), bucketName, groupId, artifactId, version, name);
        return Response.ok(extension).build();
    }

    @GET
    @Path("{bucketName}/{groupId}/{artifactId}/{version}/extensions/{name}/docs")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(
            value = "Get extension repo extension docs",
            notes = "Gets the documentation for the extension with the given name in " +
                    "the given bucket, group, artifact, and version. " + NON_GUARANTEED_ENDPOINT,
            response = String.class,
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoVersionExtensionDocs(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId,
            @PathParam("version")
            @ApiParam("The version")
                final String version,
            @PathParam("name")
            @ApiParam("The fully qualified name of the extension")
                final String name
    ) {
        final StreamingOutput streamingOutput = serviceFacade.getExtensionRepoExtensionDocs(
                getBaseUri(), bucketName, groupId, artifactId, version, name);
        return Response.ok(streamingOutput).build();
    }

    @GET
    @Path("{bucketName}/{groupId}/{artifactId}/{version}/extensions/{name}/docs/additional-details")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_HTML)
    @ApiOperation(
            value = "Get extension repo extension details",
            notes = "Gets the additional details documentation for the extension with the given name in " +
                    "the given bucket, group, artifact, and version. " + NON_GUARANTEED_ENDPOINT,
            response = String.class,
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoVersionExtensionAdditionalDetailsDocs(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId,
            @PathParam("version")
            @ApiParam("The version")
                final String version,
            @PathParam("name")
            @ApiParam("The fully qualified name of the extension")
                final String name
    ) {
        final StreamingOutput streamingOutput = serviceFacade.getExtensionRepoExtensionAdditionalDocs(
                getBaseUri(), bucketName, groupId, artifactId, version, name);
        return Response.ok(streamingOutput).build();
    }

    @GET
    @Path("{bucketName}/{groupId}/{artifactId}/{version}/content")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    @ApiOperation(
            value = "Get extension repo version content",
            notes = "Gets the binary content of the bundle with the given bucket, group, artifact, and version. " + NON_GUARANTEED_ENDPOINT,
            response = byte[].class,
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoVersionContent(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId,
            @PathParam("version")
            @ApiParam("The version")
                final String version
    ) {
        final StreamingContent streamingContent = serviceFacade.getExtensionRepoVersionContent(
            bucketName, groupId, artifactId, version);

        final String filename = streamingContent.getFilename();
        final StreamingOutput streamingOutput = streamingContent.getOutput();

        return Response.ok(streamingOutput)
                .header(CONTENT_DISPOSITION_HEADER,"attachment; filename = " + filename)
                .build();
    }

    @GET
    @Path("{bucketName}/{groupId}/{artifactId}/{version}/sha256")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            value = "Get extension repo version checksum",
            notes = "Gets the hex representation of the SHA-256 digest for the binary content of the bundle " +
                    "with the given bucket, group, artifact, and version." + NON_GUARANTEED_ENDPOINT,
            response = String.class,
            extensions = {
                    @Extension(name = "access-policy", properties = {
                            @ExtensionProperty(name = "action", value = "read"),
                            @ExtensionProperty(name = "resource", value = "/buckets/{bucketId}") })
            }
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getExtensionRepoVersionSha256(
            @PathParam("bucketName")
            @ApiParam("The bucket name")
                final String bucketName,
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId,
            @PathParam("version")
            @ApiParam("The version")
                final String version
    ) {
        final String sha256Hex = serviceFacade.getExtensionRepoVersionSha256(bucketName, groupId, artifactId, version);
        return Response.ok(sha256Hex, MediaType.TEXT_PLAIN).build();
    }

    @GET
    @Path("{groupId}/{artifactId}/{version}/sha256")
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            value = "Get global extension repo version checksum",
            notes = "Gets the hex representation of the SHA-256 digest for the binary content with the given bucket, group, artifact, and version. " +
                    "Since the same group-artifact-version can exist in multiple buckets, this will return the checksum of the first one returned. " +
                    "This will be consistent since the checksum must be the same when existing in multiple buckets. " + NON_GUARANTEED_ENDPOINT,
            response = String.class
    )
    @ApiResponses({
            @ApiResponse(code = 400, message = HttpStatusMessages.MESSAGE_400),
            @ApiResponse(code = 401, message = HttpStatusMessages.MESSAGE_401),
            @ApiResponse(code = 403, message = HttpStatusMessages.MESSAGE_403),
            @ApiResponse(code = 404, message = HttpStatusMessages.MESSAGE_404),
            @ApiResponse(code = 409, message = HttpStatusMessages.MESSAGE_409) })
    public Response getGlobalExtensionRepoVersionSha256(
            @PathParam("groupId")
            @ApiParam("The group identifier")
                final String groupId,
            @PathParam("artifactId")
            @ApiParam("The artifact identifier")
                final String artifactId,
            @PathParam("version")
            @ApiParam("The version")
                final String version
    ) {
        // Since we are using the filter params which are optional in the service layer, we need to validate these path params here

        if (StringUtils.isBlank(groupId)) {
            throw new IllegalArgumentException("Group id cannot be null or blank");
        }

        if (StringUtils.isBlank(artifactId)) {
            throw new IllegalArgumentException("Artifact id cannot be null or blank");
        }

        if (StringUtils.isBlank(version)) {
            throw new IllegalArgumentException("Version cannot be null or blank");
        }

        final BundleVersionFilterParams filterParams = BundleVersionFilterParams.of(groupId, artifactId, version);

        final SortedSet<BundleVersionMetadata> bundleVersions = serviceFacade.getBundleVersions(filterParams);
        if (bundleVersions.isEmpty()) {
            throw new ResourceNotFoundException("An extension bundle version does not exist with the specific group, artifact, and version");
        } else {
            BundleVersionMetadata latestVersionMetadata = null;
            for (BundleVersionMetadata versionMetadata : bundleVersions) {
                if (latestVersionMetadata == null || versionMetadata.getTimestamp() > latestVersionMetadata.getTimestamp()) {
                    latestVersionMetadata = versionMetadata;
                }
            }
            return Response.ok(latestVersionMetadata.getSha256(), MediaType.TEXT_PLAIN).build();
        }
    }

}
