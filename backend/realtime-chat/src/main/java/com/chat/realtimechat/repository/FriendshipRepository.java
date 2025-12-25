package com.chat.realtimechat.repository;

import com.chat.realtimechat.model.entity.Friendship;
import com.chat.realtimechat.model.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    List<Friendship> findAllByUserIdAndStatus(Long userId, FriendshipStatus status);

    List<Friendship> findAllByFriendIdAndStatus(Long friendId, FriendshipStatus status);

    List<Friendship> findAllByUserIdAndFriendId(Long userId, Long friendId);

    List<Friendship> findAllByUserIdAndFriendIdOrUserIdAndFriendId(
            Long userId1, Long friendId1,
            Long userId2, Long friendId2
    );

}
