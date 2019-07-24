package org.gbif.registry.ws.resources.collections;

import org.gbif.api.model.collections.Person;
import org.gbif.api.model.common.paging.Pageable;
import org.gbif.api.model.common.paging.PagingResponse;
import org.gbif.api.model.registry.search.collections.PersonSuggestResult;
import org.gbif.api.service.collections.PersonService;
import org.gbif.registry.persistence.mapper.collections.AddressMapper;
import org.gbif.registry.persistence.mapper.collections.PersonMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@RestController
@RequestMapping("/grscicoll/person")
public class PersonResource extends BaseCrudResource<Person> implements PersonService {

  private final PersonMapper personMapper;
  private final AddressMapper addressMapper;

  public PersonResource(PersonMapper personMapper, AddressMapper addressMapper) {
    // TODO: 2019-07-24 add EventBus
    super(personMapper, null, Person.class);
    this.personMapper = personMapper;
    this.addressMapper = addressMapper;
  }

  // TODO: 2019-07-24 implement Validate
  @Transactional
//  @Validate(groups = {PrePersist.class, Default.class})
  @Override
  public UUID create(@Valid @NotNull Person person) {
    checkArgument(person.getKey() == null, "Unable to create an entity which already has a key");

    if (person.getMailingAddress() != null) {
      checkArgument(person.getMailingAddress().getKey() == null, "Unable to create an address which already has a key");
      addressMapper.create(person.getMailingAddress());
    }

    person.setKey(UUID.randomUUID());
    personMapper.create(person);

    // TODO: 2019-07-24 implement EventBus stuff
//    eventBus.post(CreateCollectionEntityEvent.newInstance(person, Person.class));
    return person.getKey();
  }

  // TODO: 2019-07-24 implement these methods
  @Override
  public void delete(@NotNull UUID uuid) {
    throw new UnsupportedOperationException("not implemented yet!");
  }

  @Override
  public Person get(@NotNull UUID uuid) {
    throw new UnsupportedOperationException("not implemented yet!");
  }

  @Override
  public void update(@NotNull Person person) {
    throw new UnsupportedOperationException("not implemented yet!");
  }

  @Override
  public PagingResponse<Person> list(@Nullable String s, @Nullable UUID uuid, @Nullable UUID uuid1, @Nullable Pageable pageable) {
    throw new UnsupportedOperationException("not implemented yet!");
  }

  @Override
  public PagingResponse<Person> listDeleted(@Nullable Pageable pageable) {
    throw new UnsupportedOperationException("not implemented yet!");
  }

  @Override
  public List<PersonSuggestResult> suggest(@Nullable String s) {
    throw new UnsupportedOperationException("not implemented yet!");
  }
}
