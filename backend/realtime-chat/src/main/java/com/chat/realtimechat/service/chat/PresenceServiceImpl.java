package com.chat.realtimechat.service.chat;

import com.chat.realtimechat.model.dto.response.FriendPresenceEvent;
import com.chat.realtimechat.model.entity.User;
import com.chat.realtimechat.model.enums.UserStatus;
import com.chat.realtimechat.repository.UserRepository;
import com.chat.realtimechat.service.FriendshipService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

    private static final String PRESENCE_PREFIX = "presence:user:";
    private static final String STATUS_PREFIX   = "status:user:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redis;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void connectUser(String username) {
        String key = presenceKey(username);
        redis.opsForValue().increment(key);
        redis.expire(key, TTL);
    }

    @Override
    public void disconnectUser(String username) {
        String key = presenceKey(username);
        Long value = redis.opsForValue().decrement(key);
        if (value == null || value <= 0) {
            redis.delete(key);
        }
    }

    @Override
    public void heartbeat(String username) {
        redis.expire(presenceKey(username), TTL);
    }


    @Override
    public void setUserStatus(String username, UserStatus status) {
        redis.opsForValue().set(statusKey(username), status.name());
    }


    @Override
    public UserStatus getResolvedStatus(String username) {

        boolean online = redis.hasKey(presenceKey(username));
        if (!online) {
            return UserStatus.OFFLINE;
        }

        String raw = redis.opsForValue().get(statusKey(username));
        if (raw == null) {
            return UserStatus.ONLINE;
        }

        return UserStatus.valueOf(raw);
    }

    private String presenceKey(String username) {
        return PRESENCE_PREFIX + username;
    }

    private String statusKey(String username) {
        return STATUS_PREFIX + username;
    }
}
