package kopaczewski.glazer.bsiui.database.repositories;

import kopaczewski.glazer.bsiui.database.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Override
    <S extends Message> S save(S entity);

    @Query("select m from Message m where m.conversation.name = ?1 and m.person.personId = ?2")
    List<Message> findAllByConversation_NameAndPerson_personId(String conversationName, Long personId);

    @Query("select m from Message m where m.whoDoesntGetMessage like concat('%', ?1, '%')")
    List<Message> findAllByPerson_personIdAndWhoDoesntGetMessageContains(String login);

    @Transactional
    @Modifying
    @Query("update Message m set m.whoDoesntGetMessage = :whoDoesntGetMessage where m.messageId in :messageIds")
    int updateWhoDoesntGetMessageByMessageId(@Param("whoDoesntGetMessage") String whoDoesntGetMessage, @Param(
            "messageIds") List<Long> messageIds);


}