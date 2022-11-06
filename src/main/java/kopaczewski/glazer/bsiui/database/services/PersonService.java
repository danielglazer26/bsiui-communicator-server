package kopaczewski.glazer.bsiui.database.services;

import kopaczewski.glazer.bsiui.database.entities.Person;
import kopaczewski.glazer.bsiui.database.repositories.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
public class PersonService {

    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public Person createNewPerson(String login, String password) {
        Optional<Person> optionalPerson = personRepository.findByLogin(login);
        if (optionalPerson.isEmpty()) {
            return personRepository.save(new Person(0L, login, makeHash(login, password), new HashSet<>()));
        } else {
            return null;
        }
    }

    private String makeHash(String login, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return new String(digest.digest((login + password).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ignore) {
        }
        return "";
    }

    public boolean makeAuthorization(String login, String password) {
        Optional<Person> person = getPersonByLogin(login);
        return person.filter(value -> Objects.equals(value.getPasswordHash(), makeHash(login, password))).isPresent();
    }

    public Optional<Person> getPersonByLogin(String login) {
        return personRepository.findByLogin(login);
    }

    public List<Person> getPeopleByLogins(List<String> logins) {
        return personRepository.findPeopleByLogins(logins);
    }

    public Optional<Person> getPersonById(Long id) {
        return personRepository.findPersonByPersonId(id);
    }

    public Person getSignInPersonById(Long id) {
        return personRepository.findSignInPersonByPersonId(id);
    }

    public List<Person> getAllPeople() {
        return personRepository.findAll();
    }


}
