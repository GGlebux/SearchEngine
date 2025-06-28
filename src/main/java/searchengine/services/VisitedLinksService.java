package searchengine.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;

import static java.util.Objects.requireNonNull;

@Service
public class VisitedLinksService {
    private final RedisTemplate<String, String> redis;

    @Autowired
    public VisitedLinksService(RedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    public void add(String domain, String path) {
        redis.opsForSet().add(domain, path);
    }

    public void clearVisited(Collection<String> domains) {
        redis.unlink(domains);
    }

    public boolean contains(String domain, String path) {
        return requireNonNull(redis.opsForSet().isMember(domain, path));
    }

}
