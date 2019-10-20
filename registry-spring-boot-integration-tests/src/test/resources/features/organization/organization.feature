@Organization
Feature: Organization functionality

  Background:
    # see scripts/organization
    Given node 'UK Node' and node 'UK Node 2'
    And seven organizations in 'UK Node'

  Scenario Outline: Organization suggest
    When call suggest organizations with query "<query>"
    Then response status should be 200
    And <number> organization(s) should be suggested

    Scenarios:
      | query | number |
      | The   | 7      |
      | ORG   | 1      |
      | Stuff | 0      |

  Scenario Outline: List organizations by country
    When call list organizations by country <country>
    Then response status should be 200
    And <number> organization(s) should be listed

    Scenarios:
      | country | number |
      | ANGOLA  | 2      |
      | ARMENIA | 0      |
      | DENMARK | 1      |
      | GERMANY | 1      |
      | FRANCE  | 2      |

    # TODO perform full CRUD flow?
  Scenario: Create an organization
    When create a new organization "New org A" for "UK Node"
    Then response status should be 201
    When get organization by id
    Then response status should be 200

  @OrganizationEndorsement
  Scenario: Organization endorsement
    Given 0 organization(s) endorsed for "UK Node 2"
    And 7 organization(s) pending endorsement in total
    When create a new organization "New org B" for "UK Node 2"
    Then 0 organization(s) endorsed for "UK Node 2"
    And 1 organization(s) pending endorsement for "UK Node 2"
    And 8 organization(s) pending endorsement in total
    When endorse organization "New org B"
    Then 1 organization(s) endorsed for "UK Node 2"
    And 0 organization(s) pending endorsement for "UK Node 2"
    And 7 organization(s) pending endorsement in total

  @CreateOrganizationWithKey
  Scenario: Organization can't be created with key present
    When create a new organization for "UK Node" with key
    Then response status should be 422
