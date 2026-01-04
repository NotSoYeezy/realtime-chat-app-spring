package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.Friendship;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    List<Friendship> findAllByUserIdAndStatus(Long userId, FriendshipStatus status);

    List<Friendship> findAllByFriendIdAndStatus(Long friendId, FriendshipStatus status);

    List<Friendship> findAllByUserIdAndFriendId(Long userId, Long friendId);

    List<Friendship> findAllByUserIdAndFriendIdOrUserIdAndFriendId(
            Long userId1, Long friendId1,
            Long userId2, Long friendId2
    );

    @Query("SELECT f.friend FROM Friendship f WHERE f.user.id = :userId AND f.status = com.chat.realtimechat.model.enums.FriendshipStatus.ACCEPTED UNION SELECT f.user FROM Friendship f WHERE f.friend.id = :userId AND f.status = com.chat.realtimechat.model.enums.FriendshipStatus.ACCEPTED")
    List<User> findFriendsByUserId(@Param("userId") Long userId);
}
