package org.gbif.registry.collections.sync.notification;

import org.gbif.api.model.collections.Collection;
import org.gbif.api.model.collections.CollectionEntity;
import org.gbif.api.model.collections.Institution;
import org.gbif.api.model.collections.Person;
import org.gbif.registry.collections.sync.ih.IHInstitution;
import org.gbif.registry.collections.sync.ih.IHStaff;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Issue {

  private static final String IH_OUTDATED_TITLE = "The IH %s is outdated";
  private static final String STAFF_CONFLICT_TITLE =
      "Multiple GrSciColl persons associated to the same IH staff";
  private static final String NEW_LINE = "\n";
  private static final List<String> DEFAULT_LABELS =
      Collections.singletonList("GrSciColl-IH conflict");

  private String title;
  private String body;
  private List<String> asignees;
  private List<String> labels;

  public static Issue createOutdatedIHStaffIssue(Person grSciCollPerson, IHStaff ihStaff) {
    return createOutdatedIHEntityIssue(grSciCollPerson, ihStaff.toString(), "staff");
  }

  public static Issue createOutdatedIHInstitutionIssue(
      Institution grSciCollInstitution, IHInstitution ihInstitution) {
    return createOutdatedIHEntityIssue(
        grSciCollInstitution, ihInstitution.toString(), "institution");
  }

  public static Issue createOutdatedIHCollectionIssue(
      Collection grSciCollCollection, IHInstitution ihInstitution) {
    return createOutdatedIHEntityIssue(grSciCollCollection, ihInstitution.toString(), "collection");
  }

  private static Issue createOutdatedIHEntityIssue(
      CollectionEntity grSciCollEntity, String ihEntityAsString, final String entityType) {

    // create body
    StringBuilder body = new StringBuilder();
    body.append("The IH ")
        .append(entityType)
        .append(":")
        .append(NEW_LINE)
        .append(ihEntityAsString)
        .append(NEW_LINE)
        .append("has a more up-to-date entity in GrSciColl:")
        .append(NEW_LINE)
        .append(grSciCollEntity)
        .append(
            "Please check which one should remain and sync them in both systems. Remember to sync the associated staff too.");

    return Issue.builder()
        .title(String.format(IH_OUTDATED_TITLE, entityType))
        .body(body.toString())
        .labels(DEFAULT_LABELS)
        .build();
  }

  public static Issue createConflict(List<CollectionEntity> entities, IHInstitution ihInstitution) {
    // create body
    StringBuilder body = new StringBuilder();
    body.append("The IH institution: \n")
        .append(ihInstitution)
        .append("\n")
        .append("have multiple candidates in GrSciColl: \n");
    entities.forEach(e -> body.append(e).append("\n"));
    body.append(
        "A IH institution should be associated to only one GrSciColl entity. Please resolve the conflict.");

    return Issue.builder()
        .title(STAFF_CONFLICT_TITLE)
        .body(body.toString())
        .labels(DEFAULT_LABELS)
        .build();
  }

  public static Issue createMultipleStaffIssue(Set<Person> persons, IHStaff ihStaff) {
    // create body
    StringBuilder body = new StringBuilder();
    body.append("The IH staff: \n")
        .append(ihStaff)
        .append("\n")
        .append("is associated to all the following GrSciColl persons: \n");
    persons.forEach(p -> body.append(p).append("\n"));
    body.append(
        "A IH staff should be associated to only one GrSciColl person. Please resolve the conflict.");

    return Issue.builder()
        .title(STAFF_CONFLICT_TITLE)
        .body(body.toString())
        .labels(DEFAULT_LABELS)
        .build();
  }
}
