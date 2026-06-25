package org.nep.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.nep.common.exception.BusinessException;
import org.nep.system.entity.*;
import org.nep.system.mapper.*;
import org.nep.system.service.SocialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class SocialServiceImpl
        extends ServiceImpl<CommentMapper, Comment>
        implements SocialService {

    private final UserLikeMapper likeMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final NewsMapper newsMapper;
    private final KnowledgeMapper knowledgeMapper;

    public SocialServiceImpl(UserLikeMapper lm, UserFavoriteMapper fm,
                             NewsMapper nm, KnowledgeMapper km) {
        this.likeMapper = lm;
        this.favoriteMapper = fm;
        this.newsMapper = nm;
        this.knowledgeMapper = km;
    }

    @Override
    @Transactional
    public Comment postComment(Comment comment) {
        if (comment.getContent() == null || comment.getContent().isBlank())
            throw new BusinessException("评论内容不能为空");
        this.save(comment);
        return comment;
    }

    @Override
    public List<Comment> getComments(String targetType, Long targetId) {
        LambdaQueryWrapper<Comment> w = new LambdaQueryWrapper<>();
        w.eq(Comment::getTargetType, targetType)
         .eq(Comment::getTargetId, targetId)
         .orderByAsc(Comment::getCreateTime);
        return this.list(w);
    }

    @Override
    @Transactional
    public boolean toggleLike(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<UserLike> w = new LambdaQueryWrapper<>();
        w.eq(UserLike::getUserId, userId)
         .eq(UserLike::getTargetType, targetType)
         .eq(UserLike::getTargetId, targetId);
        UserLike existing = likeMapper.selectOne(w);

        if (existing != null) {
            // 取消点赞
            likeMapper.deleteById(existing.getId());
            updateTargetLikeCount(targetType, targetId, -1);
            return false;
        } else {
            // 点赞
            UserLike ul = new UserLike();
            ul.setUserId(userId);
            ul.setTargetType(targetType);
            ul.setTargetId(targetId);
            likeMapper.insert(ul);
            updateTargetLikeCount(targetType, targetId, 1);
            return true;
        }
    }

    private void updateTargetLikeCount(String targetType, Long targetId, int delta) {
        switch (targetType) {
            case "NEWS" -> {
                News n = newsMapper.selectById(targetId);
                if (n != null) {
                    n.setLikeCount(Math.max(0, (n.getLikeCount() == null ? 0 : n.getLikeCount()) + delta));
                    newsMapper.updateById(n);
                }
            }
            case "KNOWLEDGE" -> {
                Knowledge k = knowledgeMapper.selectById(targetId);
                if (k != null) {
                    k.setLikeCount(Math.max(0, (k.getLikeCount() == null ? 0 : k.getLikeCount()) + delta));
                    knowledgeMapper.updateById(k);
                }
            }
            case "COMMENT" -> {
                Comment c = this.getById(targetId);
                if (c != null) {
                    c.setLikeCount(Math.max(0, (c.getLikeCount() == null ? 0 : c.getLikeCount()) + delta));
                    this.updateById(c);
                }
            }
        }
    }

    @Override
    public boolean isLiked(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<UserLike> w = new LambdaQueryWrapper<>();
        w.eq(UserLike::getUserId, userId)
         .eq(UserLike::getTargetType, targetType)
         .eq(UserLike::getTargetId, targetId);
        return likeMapper.selectCount(w) > 0;
    }

    @Override
    public long getLikeCount(String targetType, Long targetId) {
        LambdaQueryWrapper<UserLike> w = new LambdaQueryWrapper<>();
        w.eq(UserLike::getTargetType, targetType)
         .eq(UserLike::getTargetId, targetId);
        return likeMapper.selectCount(w);
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<UserFavorite> w = new LambdaQueryWrapper<>();
        w.eq(UserFavorite::getUserId, userId)
         .eq(UserFavorite::getTargetType, targetType)
         .eq(UserFavorite::getTargetId, targetId);
        UserFavorite existing = favoriteMapper.selectOne(w);

        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            return false;
        } else {
            UserFavorite uf = new UserFavorite();
            uf.setUserId(userId);
            uf.setTargetType(targetType);
            uf.setTargetId(targetId);
            favoriteMapper.insert(uf);
            return true;
        }
    }

    @Override
    public boolean isFavorited(Long userId, String targetType, Long targetId) {
        LambdaQueryWrapper<UserFavorite> w = new LambdaQueryWrapper<>();
        w.eq(UserFavorite::getUserId, userId)
         .eq(UserFavorite::getTargetType, targetType)
         .eq(UserFavorite::getTargetId, targetId);
        return favoriteMapper.selectCount(w) > 0;
    }

    @Override
    public List<Map<String, Object>> getUserFavorites(Long userId) {
        LambdaQueryWrapper<UserFavorite> w = new LambdaQueryWrapper<>();
        w.eq(UserFavorite::getUserId, userId).orderByDesc(UserFavorite::getCreateTime);
        List<UserFavorite> favs = favoriteMapper.selectList(w);
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserFavorite f : favs) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("targetType", f.getTargetType());
            item.put("targetId", f.getTargetId());
            item.put("createTime", f.getCreateTime());
            result.add(item);
        }
        return result;
    }
}
