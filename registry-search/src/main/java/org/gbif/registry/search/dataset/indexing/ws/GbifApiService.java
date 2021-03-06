/*
 * Copyright 2020 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.registry.search.dataset.indexing.ws;

import org.gbif.api.model.checklistbank.DatasetMetrics;
import org.gbif.api.model.checklistbank.NameUsage;
import org.gbif.api.model.checklistbank.search.NameUsageSearchParameter;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.common.search.SearchResponse;
import org.gbif.api.model.occurrence.Occurrence;
import org.gbif.api.model.occurrence.search.OccurrenceSearchParameter;
import org.gbif.api.model.registry.Dataset;
import org.gbif.api.model.registry.Installation;
import org.gbif.api.model.registry.Organization;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;

/** Retrofit client to all the GBIF service call needed for dataset indexing. */
public interface GbifApiService {

  @GET("dataset")
  Call<PagingResponse<Dataset>> listDatasets(@QueryMap Map<String, String> options);

  @Streaming
  @GET("dataset/{datasetKey}/document")
  Call<ResponseBody> getMetadataDocument(@Path("datasetKey") String datasetKey);

  @GET("installation/{installationKey}")
  Call<Installation> getInstallation(@Path("installationKey") String installationKey);

  @GET("installation/{installationKey}/dataset")
  Call<PagingResponse<Dataset>> getInstallationDatasets(
      @Path("installationKey") String installationKey);

  @GET("organization/{organizationKey}/hostedDataset")
  Call<PagingResponse<Dataset>> getOrganizationHostedDatasets(
      @Path("organizationKey") String organizationKey, @QueryMap Map<String, String> options);

  @GET("organization/{organizationKey}/publishedDataset")
  Call<PagingResponse<Dataset>> getOrganizationPublishedDatasets(
      @Path("organizationKey") String organizationKey, @QueryMap Map<String, String> options);

  @GET("organization/{organizationKey}")
  Call<Organization> getOrganization(@Path("organizationKey") String organizationKey);

  @GET("occurrence/count")
  Call<Long> getDatasetRecordCount(@Query("datasetKey") String datasetKey);

  @GET("occurrence/count")
  Call<Long> getOccurrenceRecordCount();

  @GET("dataset/{datasetKey}/metrics")
  Call<DatasetMetrics> getDatasetSpeciesMetrics(@Path("datasetKey") String datasetKey);

  @GET("species/search")
  Call<SearchResponse<NameUsage, NameUsageSearchParameter>> speciesSearch(
      @QueryMap Map<String, Object> options);

  @GET("occurrence/search")
  Call<SearchResponse<Occurrence, OccurrenceSearchParameter>> occurrenceSearch(
      @QueryMap Map<String, Object> options);
}
