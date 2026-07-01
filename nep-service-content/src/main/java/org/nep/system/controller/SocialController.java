package org.nep.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nep.common.result.Result;
import org.nep.system.entity.Comment;
import org.nep.system.service.SocialService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "社交互动")
@RestController
@RequestMapping("/api/social")
public class SocialController {

    private final SocialService socialService;
    public SocialController(SocialService s) { this.socialService = s; }

    // ==================== 评论 ====================

    @Operation(summary = "发表评论/回复")
    @PostMapping("/comment")
    public Result<Comment> postComment(@RequestBody Comment comment) {
        return Result.ok("评论成功", socialService.postComment(comment));
    }

    @Operation(summary = "获取评论列表")
    @GetMapping("/comments")
    public Result<List<Comment>> getComments(@RequestParam String targetType,
                                             @RequestParam Long targetId) {
        return Result.ok(socialService.getComments(targetType, targetId));
    }

    // ==================== 点赞 ====================

    @Operation(summary = "切换点赞（点赞/取消点赞）")
    @PostMapping("/like")
    public Result<Map<String, Object>> toggleLike(@RequestParam Long userId,
                                                   @RequestParam String targetType,
                                                   @RequestParam Long targetId) {
        boolean liked = socialService.toggleLike(userId, targetType, targetId);
        long count = socialService.getLikeCount(targetType, targetId);
        return Result.ok(Map.of("liked", liked, "likeCount", count));
    }

    @Operation(summary = "检查是否已点赞")
    @GetMapping("/liked")
    public Result<Map<String, Object>> isLiked(@RequestParam Long userId,
                                                @RequestParam String targetType,
                                                @RequestParam Long targetId) {
        return Result.ok(Map.of("liked", socialService.isLiked(userId, targetType, targetId)));
    }

    // ==================== 收藏 ====================

    @Operation(summary = "切换收藏")
    @PostMapping("/favorite")
    public Result<Map<String, Object>> toggleFavorite(@RequestParam Long userId,
                                                       @RequestParam String targetType,
                                                       @RequestParam Long targetId) {
        boolean fav = socialService.toggleFavorite(userId, targetType, targetId);
        return Result.ok(Map.of("favorited", fav));
    }

    @Operation(summary = "检查是否已收藏")
    @GetMapping("/favorited")
    public Result<Map<String, Object>> isFavorited(@RequestParam Long userId,
                                                    @RequestParam String targetType,
                                                    @RequestParam Long targetId) {
        return Result.ok(Map.of("favorited", socialService.isFavorited(userId, targetType, targetId)));
    }

    @Operation(summary = "用户收藏列表")
    @GetMapping("/favorites")
    public Result<List<Map<String, Object>>> getFavorites(@RequestParam Long userId) {
        return Result.ok(socialService.getUserFavorites(userId));
    }
}
