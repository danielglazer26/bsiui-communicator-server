package kopaczewski.glazer.bsiui.database.repositories;

import kopaczewski.glazer.bsiui.database.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Override
    <S extends Message> S save(S entity);

    @Query("select m from Message m where m.conversation.name = ?1")
    List<Message> findAllByConversation_Name(String conversationName);

    @Query("select m from Message m where m.whoDoesntGetMessage like concat('%', ?1, '%')")
    List<Message> findAllByPerson_personIdAndWhoDoesntGetMessageContains(String login);

    @Modifying
    @Query("update Message m set m.whoDoesntGetMessage = :whoDoesntGetMessage where m.messageId = :messageId")
    int updateWhoDoesntGetMessageByMessageId(@Param("whoDoesntGetMessage") String whoDoesntGetMessage,
                                             @Param("messageId") Long messageId);


}