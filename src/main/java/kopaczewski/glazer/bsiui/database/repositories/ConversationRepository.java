package kopaczewski.glazer.bsiui.database.repositories;

import kopaczewski.glazer.bsiui.database.entities.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Override
    List<Conversation> findAll();

    @Override
    <S extends Conversation> S save(S entity);

    @Query("select c from Conversation c where c.name = ?1")
    Optional<Conversation> findConversationByName(String name);
}