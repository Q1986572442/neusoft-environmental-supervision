package org.nep.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.nep.system.entity.Comment;

import java.util.List;
import java.util.Map;

public interface SocialService extends IService<Comment> {

    /** 发表评论或回复 */
    Comment postComment(Comment comment);

    /** 获取某目标的评论列表（含回复嵌套） */
    List<Comment> getComments(String targetType, Long targetId);

    /** 切换点赞状态（点赞/取消）返回是否已点赞 */
    boolean toggleLike(Long userId, String targetType, Long targetId);

    /** 检查是否已点赞 */
    boolean isLiked(Long userId, String targetType, Long targetId);

    /** 获取目标的点赞数 */
    long getLikeCount(String targetType, Long targetId);

    /** 切换收藏状态 */
    boolean toggleFavorite(Long userId, String targetType, Long targetId);

    /** 检查是否已收藏 */
    boolean isFavorited(Long userId, String targetType, Long targetId);

    /** 获取用户收藏列表 */
    List<Map<String, Object>> getUserFavorites(Long userId);
}
