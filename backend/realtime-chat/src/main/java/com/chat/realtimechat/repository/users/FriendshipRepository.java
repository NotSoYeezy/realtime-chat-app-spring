package com.chat.realtimechat.repository.users;

import com.chat.realtimechat.model.entity.users.Friendship;
import com.chat.realtimechat.model.entity.users.User;
import com.chat.realtimechat.model.enums.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    Optional<Friendship> findByUserIdAndFriendId(Long userId, Long friendId);

    Page<Friendship> findAllByUserIdAndStatus(Long userId, FriendshipStatus status, Pageable pageable);

    Page<Friendship> findAllByFriendIdAndStatus(Long friendId, FriendshipStatus status, Pageable pageable);
    
    // Kept for backward compatibility if needed, but we should rely on Pageable versions
    List<Friendship> findAllByUserIdAndStatus(Long userId, FriendshipStatus status);
    List<Friendship> findAllByFriendIdAndStatus(Long friendId, FriendshipStatus status);

    List<Friendship> findAllByUserIdAndFriendId(Long userId, Long friendId);

    @Query("SELECT f FROM Friendship f WHERE (f.user.id = :userId AND f.status = :status) OR (f.friend.id = :userId AND f.status = :status)")
    Page<Friendship> findAllByUserIdOrFriendIdAndStatus(@Param("userId") Long userId, @Param("status") FriendshipStatus status, Pageable pageable);

    List<Friendship> findAllByUserIdAndFriendIdOrUserIdAndFriendId(
            Long userId1, Long friendId1,
            Long userId2, Long friendId2
    );

    @Query("SELECT f.friend FROM Friendship f WHERE f.user.id = :userId AND f.status = com.chat.realtimechat.model.enums.FriendshipStatus.ACCEPTED UNION SELECT f.user FROM Friendship f WHERE f.friend.id = :userId AND f.status = com.chat.realtimechat.model.enums.FriendshipStatus.ACCEPTED")
    List<User> findFriendsByUserId(@Param("userId") Long userId);
}
