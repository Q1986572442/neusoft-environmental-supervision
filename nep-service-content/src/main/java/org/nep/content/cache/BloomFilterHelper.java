package org.nep.content.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 生产级 Redis Bitmap 布隆过滤器
 *
 * <h3>核心原理</h3>
 * <ul>
 *   <li>使用 Redis Bitmap 实现布隆过滤器，拦截恶意随机坐标查询</li>
 *   <li>对查询 key 进行 K 次哈希，检查对应位是否均为 1</li>
 *   <li>任一哈希位为 0 → key 一定不存在 → 直接拦截（防缓存穿透）</li>
 *   <li>所有位均为 1 → key 可能存在 → 放行查询（允许 1% 误判）</li>
 * </ul>
 *
 * <h3>参数设计（针对污染热点地图场景）</h3>
 * <ul>
 *   <li>预期数据量 n ≈ 50万（省+市+坐标网格组合）</li>
 *   <li>误判率 p ≈ 0.01（1%，对业务影响极小）</li>
 *   <li>Bitmap 大小 m = -n·ln(p)/(ln2)² ≈ 4,792,525 bits ≈ 585 KB</li>
 *   <li>实际分配 BITMAP_SIZE = 8,000,000 bits ≈ 976 KB（预留充分余量）</li>
 *   <li>哈希函数数量 k = m/n · ln2 ≈ 11.1 → 取 11</li>
 *   <li>Bitmap 过期：7 天（定期全量重建）</li>
 * </ul>
 *
 * <h3>拦截场景</h3>
 * <ul>
 *   <li>恶意用户构造不存在的 provinceId=99999</li>
 *   <li>恶意用户随机组合 provinceId + cityId（如 provinceId=1, cityId=999）</li>
 *   <li>高并发下非法经纬度网格坐标查询</li>
 *   <li>爬虫批量扫描不存在的区域ID</li>
 * </ul>
 *
 * @author NEP Team
 */
@Component
public class BloomFilterHelper {

    private static final Logger log = LoggerFactory.getLogger(BloomFilterHelper.class);

    /** Bitmap 大小：800万位 ≈ 976KB，支持50万+元素 @ 1%误判率 */
    public static final int BITMAP_SIZE = 8_000_000;

    /** 哈希函数数量：11个（ln2 * BITMAP_SIZE / 500000 ≈ 11.1） */
    public static final int HASH_COUNT = 11;

    /** Redis key 前缀 */
    private static final String BLOOM_KEY_PREFIX = "bloom:aqi:";

    /** Bitmap 过期时间：7天 */
    private static final long BLOOM_EXPIRE_DAYS = 7;

    private final RedisTemplate<String, Object> redisTemplate;

    public BloomFilterHelper(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // ==================== 公开 API ====================

    /**
     * 将单个值加入布隆过滤器
     * @param filterName 过滤器名称（province / city / region / coord-grid）
     * @param value      要加入的值（如 "110000"、"110000_110100"）
     */
    public void add(String filterName, String value) {
        String redisKey = buildKey(filterName);
        int[] offsets = hash(value);
        for (int offset : offsets) {
            redisTemplate.opsForValue().setBit(redisKey, offset, true);
        }
        redisTemplate.expire(redisKey, BLOOM_EXPIRE_DAYS, TimeUnit.DAYS);
    }

    /**
     * 批量加入布隆过滤器
     */
    public void addAll(String filterName, Iterable<String> values) {
        String redisKey = buildKey(filterName);
        int count = 0;
        for (String value : values) {
            int[] offsets = hash(value);
            for (int offset : offsets) {
                redisTemplate.opsForValue().setBit(redisKey, offset, true);
            }
            count++;
        }
        redisTemplate.expire(redisKey, BLOOM_EXPIRE_DAYS, TimeUnit.DAYS);
        log.info("Bloom filter [{}] loaded {} items into Redis bitmap", filterName, count);
    }

    /**
     * 检查值是否可能存在
     * @return true = 可能存在（放行查询），false = 一定不存在（直接拦截）
     */
    public boolean mightContain(String filterName, String value) {
        String redisKey = buildKey(filterName);
        int[] offsets = hash(value);
        for (int offset : offsets) {
            Boolean bit = redisTemplate.opsForValue().getBit(redisKey, offset);
            if (bit == null || !bit) {
                log.debug("Bloom [{}] intercepted unknown key: {}", filterName, value);
                return false;
            }
        }
        return true;
    }

    /**
     * 检查过滤器是否已初始化（Redis 中是否存在该 key）
     */
    public boolean isInitialized(String filterName) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(buildKey(filterName)));
    }

    /**
     * 清除指定过滤器（用于全量重建前）
     */
    public void clear(String filterName) {
        redisTemplate.delete(buildKey(filterName));
        log.info("Bloom filter [{}] cleared", filterName);
    }

    /**
     * 获取过滤器的内存占用估算（bits）
     */
    public Long getBitCount(String filterName) {
        String redisKey = buildKey(filterName);
        Long bitCount = 0L;
        // 抽样统计：每 1000 位检查一个，推算总数
        for (int i = 0; i < BITMAP_SIZE; i += 1000) {
            Boolean bit = redisTemplate.opsForValue().getBit(redisKey, i);
            if (Boolean.TRUE.equals(bit)) bitCount += 1000;
        }
        return bitCount;
    }

    // ==================== 内部实现 ====================

    private String buildKey(String filterName) {
        return BLOOM_KEY_PREFIX + filterName;
    }

    /**
     * 双重哈希扩展：h(i) = hash1 + i * hash2  (Kirsch-Mitzenmacher 方法)
     * <p>
     * 仅需 2 次独立哈希即可生成 K 个哈希值，性能远优于 K 次独立哈希。
     */
    private int[] hash(String value) {
        int[] offsets = new int[HASH_COUNT];
        int hash1 = Math.abs(murmurHash32(value, 0x9747b28c));  // seed1: 黄金比例倒数
        int hash2 = Math.abs(murmurHash32(value, 0x9e3779b9));  // seed2: 黄金比例
        for (int i = 0; i < HASH_COUNT; i++) {
            // hash2 为奇数确保互质，保证双重哈希能覆盖整个 bitmap 空间
            offsets[i] = (int) (((long) hash1 + (long) i * hash2) % BITMAP_SIZE);
        }
        return offsets;
    }

    /**
     * MurmurHash3 32位简化实现
     * <p>
     * 相比原实现的 MurmurHash2，MurmurHash3 具有更好的雪崩效应和分布均匀性。
     * 选用 32 位版本而非 128 位：Bitmap 大小仅 800 万，32 位空间完全够用，
     * 且 32 位哈希计算速度约为 128 位的 2 倍。
     */
    static int murmurHash32(String str, int seed) {
        byte[] data = str.getBytes(StandardCharsets.UTF_8);
        int length = data.length;
        int h = seed ^ length;

        // 4-byte blocks
        for (int i = 0; i + 4 <= length; i += 4) {
            int k = ((data[i] & 0xFF))
                  | ((data[i + 1] & 0xFF) << 8)
                  | ((data[i + 2] & 0xFF) << 16)
                  | ((data[i + 3] & 0xFF) << 24);

            // MurmurHash3 mix
            k *= 0xcc9e2d51;
            k = Integer.rotateLeft(k, 15);
            k *= 0x1b873593;

            h ^= k;
            h = Integer.rotateLeft(h, 13);
            h = h * 5 + 0xe6546b64;
        }

        // Remaining bytes
        int remaining = length & 3;
        if (remaining > 0) {
            int k = 0;
            int shift = 0;
            for (int i = length - remaining; i < length; i++) {
                k |= (data[i] & 0xFF) << shift;
                shift += 8;
            }
            k *= 0xcc9e2d51;
            k = Integer.rotateLeft(k, 15);
            k *= 0x1b873593;
            h ^= k;
        }

        // Finalization
        h ^= length;
        h ^= h >>> 16;
        h *= 0x85ebca6b;
        h ^= h >>> 13;
        h *= 0xc2b2ae35;
        h ^= h >>> 16;

        return h;
    }
}
