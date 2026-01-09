package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.model.dto.response.OnlineInfoResponse;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.FriendshipRepository;
import com.chat.realtimechat.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserPresenceServiceImpl implements UserPresenceService {
    
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final RedisTemplate<String, OnlineInfoResponse> redisTemplate;
    
    private static final String KEY_PREFIX = "user:presence:";
    private static final Duration TTL = Duration.ofSeconds(60);

    @Override
    public void connectUser(String username) {
        updateUserStatus(username, UserStatus.ONLINE);
    }

    @Override
    public void disconnectUser(String username) {
        redisTemplate.delete(getKey(username));
    }

    @Override
    public void heartbeat(String username) {
        String key = getKey(username);
        OnlineInfoResponse info = redisTemplate.opsForValue().get(key);
        if (info != null) {
            redisTemplate.expire(key, TTL);
        } else {
            updateUserStatus(username, UserStatus.ONLINE);
        }
    }

    @Override
    public void updateUserStatus(String username, UserStatus status) {
        String key = getKey(username);

        if (status == UserStatus.OFFLINE) {
            redisTemplate.delete(key);
            return;
        }

        OnlineInfoResponse existing = redisTemplate.opsForValue().get(key);
        if (existing != null) {
            existing.setStatus(status);
            redisTemplate.opsForValue().set(key, existing, TTL);
        } else {
            userRepository.findByUsername(username).ifPresent(user ->
                    redisTemplate.opsForValue().set(key, new OnlineInfoResponse(
                            user.getName(),
                            user.getSurname(),
                            user.getUsername(),
                            status
                    ), TTL));
        }
    }

    @Override
    public OnlineInfoResponse getOnlineUser(String username) {
        return redisTemplate.opsForValue().get(getKey(username));
    }

    @Override
    public Map<String, OnlineInfoResponse> getOnlineUsers(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        List<User> friends = friendshipRepository.findFriendsByUserId(user.getId());
        
        if (friends.isEmpty()) {
            OnlineInfoResponse self = getOnlineUser(username);
            return self != null ? Map.of(username, self) : Collections.emptyMap();
        }

        List<String> keys = friends.stream()
                .map(u -> getKey(u.getUsername()))
                .collect(Collectors.toList());

        keys.add(getKey(username));

        List<OnlineInfoResponse> onlineFriends = redisTemplate.opsForValue().multiGet(keys);

        if (onlineFriends == null) return Collections.emptyMap();

        return onlineFriends.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        OnlineInfoResponse::getUsername,
                        Function.identity()
                ));
    }

    private String getKey(String username) {
        return KEY_PREFIX + username;
    }
}
