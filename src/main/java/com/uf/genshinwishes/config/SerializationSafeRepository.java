package com.uf.genshinwishes.config;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;

public class SerializationSafeRepository<S extends Session> implements SessionRepository<S> {
    private final SessionRepository<S> delegate;
    private final RedisTemplate<Object, Object> redisTemplate;
    private static final String BOUNDED_HASH_KEY_PREFIX = "spring:session:sessions:";

    public SerializationSafeRepository(SessionRepository<S> delegate,
                                       RedisTemplate<Object, Object> redisTemplate) {
        this.delegate = delegate;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public S createSession() {
        return delegate.createSession();
    }

    @Override
    public void save(S session) {
        delegate.save(session);
    }

    @Override
    public S findById(String id) {
        try {
            return delegate.findById(id);
        } catch (SerializationException e) {
            redisTemplate.delete(BOUNDED_HASH_KEY_PREFIX + "id");
            return null;
        }
    }

    @Override
    public void deleteById(String id) {
        delegate.deleteById(id);
    }
}
