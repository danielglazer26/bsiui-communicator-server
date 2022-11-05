package kopaczewski.glazer.bsiui.database.repositories;

import kopaczewski.glazer.bsiui.database.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Optional;

public interface PersonRepository extends JpaRepository<Person, Long> {
    @Override
    <S extends Person> S save(S entity);

    @Query("select p from Person p where p.login = ?1")
    Optional<Person> findByLogin(@Nullable String login);

    @Query("select p from Person p where p.login in :logins")
    List<Person> findPeopleByLogins(@Param("logins") List<String> logins);

    Optional<Person> findPersonByPersonId(Long personId);

    @Override
    List<Person> findAll();

   /* @Transactional
    @Modifying
    @Query("update Person p set p.userConversations = :userConversations where p.login = :login")
    void updateConversationByLogin(@Param("userConversations") Conversation userConversations, @Param("login") String login);*/


}