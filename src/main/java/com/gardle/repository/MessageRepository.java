package com.gardle.repository;

import com.gardle.domain.Message;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.MessageType;
import org.checkerframework.common.aliasing.qual.Unique;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


/**
 * Spring Data  repository for the Message entity.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long>, JpaSpecificationExecutor<Message> {

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Message m WHERE m.thread= :thread AND (m.userFrom = :user OR m.userTo = :user)")
    boolean canUserAccessThread(@Param("user") User user, @Unique @Param("thread") UUID thread);

    @Query("SELECT DISTINCT(m.thread) FROM Message m WHERE (m.userFrom = :user1 AND m.userTo = :user2) OR (m.userFrom = :user2 AND m.userTo = :user1)")
    UUID threadBetweenTwoUsers(@Param("user1") User user1, @Param("user2") User user2);

    @Query(value = "SELECT m.* FROM message m JOIN (SELECT a.thread, max(a.created_date) as created_date FROM message a WHERE a.user_to_id = :user OR a.user_from_id = :user GROUP BY a.thread) as s ON m.thread = s.thread AND m.created_date = s.created_date", nativeQuery = true)
    Page<Message> getLatestThreadMessagesForUser(@Param("user") User user, Pageable pageable);

    default List<Message> getUnreadNotificationsForUser(User user) {
        return this.getUnreadNotificationsForUserWithParam(user, MessageType.USER.toString()); //Workaround for native queries not taking enums
    }

    @Query(value = "SELECT m.* FROM message m JOIN " +
        "(SELECT a.thread, max(a.created_date) as created_date FROM message a WHERE (a.user_to_id = :user OR a.user_from_id = :user) AND a.type = :userType GROUP BY a.thread) as s " +
        "ON m.thread = s.thread AND m.created_date = s.created_date WHERE m.opened = false AND m.user_to_id = :user "  +
        "UNION SELECT * FROM message WHERE opened = false AND type != :userType AND user_to_id = :user ORDER BY created_date DESC ", nativeQuery = true)
    List<Message> getUnreadNotificationsForUserWithParam(@Param("user") User user, @Param("userType") String userType);

    Page<Message> getByThread(UUID thread, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Message m SET m.opened = true WHERE m.thread = :thread AND m.userTo = :user AND m.opened = false")
    void updateThreadToOpenedForUser(@Param("user") User user, @Param("thread") UUID thread);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Message m SET m.opened = true WHERE m.type <> com.gardle.domain.enumeration.MessageType.USER AND m.userTo = :user AND m.opened = false")
    void updateSystemMessagesToOpenedForUser(@Param("user") User user);
}
