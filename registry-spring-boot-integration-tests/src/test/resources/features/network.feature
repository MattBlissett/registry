@Network
Feature: Network functionality

  @NetworkCRUD
  Scenario: CRUD network
    When create new network "New org A"
    Then response status should be 201
    And network key is present in response
    When get network by key
    Then response status should be 200

    When update network "New org A"
    Then response status should be 200
    When get network by key
    Then response status should be 200

    When delete network "New org A" by key
    Then response status should be 200
    When get network by key
    Then response status should be 200

  @EditorCannotCreateNetwork
  Scenario: Editor is not allowed to create network
    When create new network "New org A" by editor "registry_editor" and password "welcome"
    Then response status should be 403
