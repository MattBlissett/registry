package org.gbif.registry.ws.resources;

import org.gbif.api.model.common.DOI;
import org.gbif.api.model.common.GbifUser;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.common.search.Facet;
import org.gbif.api.model.occurrence.Download;
import org.gbif.api.model.registry.DatasetOccurrenceDownloadUsage;
import org.gbif.api.model.registry.PrePersist;
import org.gbif.api.service.common.IdentityAccessService;
import org.gbif.api.service.registry.OccurrenceDownloadService;
import org.gbif.api.vocabulary.Country;
import org.gbif.api.vocabulary.License;
import org.gbif.occurrence.query.TitleLookup;
import org.gbif.registry.doi.generator.DoiGenerator;
import org.gbif.registry.doi.handler.DataCiteDoiHandlerStrategy;
import org.gbif.registry.persistence.mapper.DatasetOccurrenceDownloadMapper;
import org.gbif.registry.persistence.mapper.OccurrenceDownloadMapper;
import org.gbif.registry.ws.guice.Trim;
import org.gbif.registry.ws.provider.PartialDate;
import org.gbif.ws.server.interceptor.NullToNotFound;
import org.gbif.ws.server.provider.CountryProvider;
import org.gbif.ws.util.ExtraMediaTypes;

import java.util.*;
import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.sun.jersey.api.NotFoundException;
import org.apache.bval.guice.Validate;
import org.mybatis.guice.transactional.Transactional;

import static org.gbif.registry.ws.security.UserRoles.ADMIN_ROLE;
import static org.gbif.registry.ws.util.DownloadSecurityUtils.checkUserIsInSecurityContext;
import static org.gbif.registry.ws.util.DownloadSecurityUtils.clearSensitiveData;

/**
 * Occurrence download resource/web service.
 */
@Singleton
@Path("occurrence/download")
@Produces({MediaType.APPLICATION_JSON, ExtraMediaTypes.APPLICATION_JAVASCRIPT})
@Consumes(MediaType.APPLICATION_JSON)
public class OccurrenceDownloadResource implements OccurrenceDownloadService {

  private final OccurrenceDownloadMapper occurrenceDownloadMapper;
  private final DatasetOccurrenceDownloadMapper datasetOccurrenceDownloadMapper;
  private final TitleLookup titleLookup;
  private final IdentityAccessService identityService;
  private final DataCiteDoiHandlerStrategy doiHandlingStrategy;
  private final DoiGenerator doiGenerator;

  //Page size to iterate over dataset usages
  private static final int USAGES_PAGE_SIZE = 400;

  // This Guice injection is only used for testing purpose
  @Inject(optional = true)
  @Named("guiceInjectedSecurityContext")
  @Context
  private SecurityContext securityContext;

  @Inject
  public OccurrenceDownloadResource(OccurrenceDownloadMapper occurrenceDownloadMapper, DatasetOccurrenceDownloadMapper datasetOccurrenceDownloadMapper,
    DoiGenerator doiGenerator, DataCiteDoiHandlerStrategy doiHandlingStrategy, IdentityAccessService identityService, TitleLookup titleLookup) {
    this.occurrenceDownloadMapper = occurrenceDownloadMapper;
    this.datasetOccurrenceDownloadMapper = datasetOccurrenceDownloadMapper;
    this.doiHandlingStrategy = doiHandlingStrategy;
    this.doiGenerator = doiGenerator;
    this.identityService = identityService;
    this.titleLookup = titleLookup;
  }

  @POST
  @Trim
  @Transactional
  @Validate(groups = {PrePersist.class, Default.class})
  @RolesAllowed(ADMIN_ROLE)
  @Override
  public void create(@Valid @NotNull @Trim Download occurrenceDownload) {
    occurrenceDownload.setDoi(doiGenerator.newDownloadDOI());
    occurrenceDownload.setLicense(License.UNSPECIFIED);
    occurrenceDownloadMapper.create(occurrenceDownload);
  }

  @GET
  @Path("{key}")
  @Nullable
  @NullToNotFound
  @Override
  public Download get(@PathParam("key") String key) {
    Download download = occurrenceDownloadMapper.get(key);
    if (download == null && DOI.isParsable(key)) { //maybe it's a DOI?
     download = occurrenceDownloadMapper.getByDOI(new DOI(key));
    }
    if (download != null) { // the user can request a non-existing download
      clearSensitiveData(securityContext, download);
    }
    return download;
  }

  /**
   * Lists all the downloads. This operation can be executed by role ADMIN only.
   */
  @GET
  @RolesAllowed(ADMIN_ROLE)
  @Override
  public PagingResponse<Download> list(@Context Pageable page, @Nullable @QueryParam("status") Set<Download.Status> status) {
    if(status == null ||status.isEmpty()) {
      return new PagingResponse<>(page, (long) occurrenceDownloadMapper.count(), occurrenceDownloadMapper.list(page));
    } else {
      return new PagingResponse<>(page, (long) occurrenceDownloadMapper.countByStatus(status), occurrenceDownloadMapper.listByStatus(page,status));
    }
  }


  @GET
  @Path("user/{user}")
  @NullToNotFound
  public PagingResponse<Download> listByUser(@PathParam("user") String user, @Context Pageable page, @Nullable @QueryParam("status")
  Set<Download.Status> status) {
    checkUserIsInSecurityContext(user, securityContext);
    return new PagingResponse<>(page, (long) occurrenceDownloadMapper.countByUser(user,status),
                                        occurrenceDownloadMapper.listByUser(user,page,status));
  }


  @PUT
  @Path("{key}")
  @Transactional
  @Override
  public void update(Download download) {
    // The current download is retrieved because its user could be modified during the update
    Download currentDownload = get(download.getKey());
    Preconditions.checkNotNull(currentDownload);
    checkUserIsInSecurityContext(currentDownload.getRequest().getCreator(), securityContext);
    GbifUser user = identityService.get(securityContext.getUserPrincipal().getName());
    doiHandlingStrategy.downloadChanged(download, currentDownload, user);
    occurrenceDownloadMapper.update(download);
  }

  @GET
  @Path("{key}/datasets")
  @Override
  @NullToNotFound
  public PagingResponse<DatasetOccurrenceDownloadUsage> listDatasetUsages(@PathParam("key") String downloadKey,
                                                                   @Context Pageable page){
    Download download = get(downloadKey);
    if(download != null) {
      return new PagingResponse<>(page,
                                   download.getNumberDatasets(),
                                   datasetOccurrenceDownloadMapper.listByDownload(downloadKey, page));
    }
    throw new NotFoundException();
  }

  @GET
  @Path("stats")
  @Override
  @NullToNotFound
  public Map<Integer,Map<Integer,Long>> getMonthlyStats(@Nullable @QueryParam("fromDate") @PartialDate Date fromDate,
                                           @Nullable @QueryParam("toDate") @PartialDate Date toDate,
                                           @Context Country country) {
    return groupByYear(occurrenceDownloadMapper.getMonthlyStats(fromDate, toDate, Optional.ofNullable(country).map(Country::getIso2LetterCode).orElse(null)));
  }

  @GET
  @Path("stats/downloadedRecords")
  @Override
  @NullToNotFound
  public Map<Integer, Map<Integer, Long>> getDownloadRecordsHostedByCountry(@Nullable @QueryParam("fromDate") @PartialDate Date fromDate,
                                                                            @Nullable @QueryParam("toDate") @PartialDate Date toDate,
                                                                            @Context Country country) {
    return groupByYear(occurrenceDownloadMapper.getDownloadedRecordsByCountry(fromDate, toDate, Optional.ofNullable(country).map(Country::getIso2LetterCode).orElse(null)));
  }

  /**
   * Aggregates the download statistics in tree structure of month grouped by year.
   */
  private Map<Integer,Map<Integer,Long>> groupByYear(List<Facet.Count> counts) {
    Map<Integer,Map<Integer,Long>> yearsGrouping = new HashMap<>();
    counts.forEach(count -> yearsGrouping.computeIfAbsent(Integer.valueOf(count.getName().substring(0,4)), year -> new TreeMap<>()).put(Integer.valueOf(count.getName().substring(5)), count.getCount()));
    return  yearsGrouping;
  }


}
