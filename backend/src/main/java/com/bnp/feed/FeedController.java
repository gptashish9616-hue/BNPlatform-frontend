package com.bnp.feed;

import com.bnp.common.ApiResponse;
import com.bnp.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;
    private final CurrentUser currentUser;

    public record CreatePostRequest(String content, String imageUrl) {}
    public record CommentRequest(String content) {}

    @GetMapping
    public ApiResponse<List<Post>> feed() {
        return ApiResponse.ok(feedService.feed());
    }

    @PostMapping
    public ApiResponse<Post> create(@RequestBody CreatePostRequest req) {
        return ApiResponse.ok("Post created", feedService.createPost(currentUser.id(), req.content(), req.imageUrl()));
    }

    @PostMapping("/{id}/like")
    public ApiResponse<Map<String, Boolean>> like(@PathVariable Long id) {
        boolean liked = feedService.toggleLike(currentUser.id(), id);
        return ApiResponse.ok(Map.of("liked", liked));
    }

    @GetMapping("/{id}/comments")
    public ApiResponse<List<PostComment>> comments(@PathVariable Long id) {
        return ApiResponse.ok(feedService.comments(id));
    }

    @PostMapping("/{id}/comments")
    public ApiResponse<PostComment> comment(@PathVariable Long id, @RequestBody CommentRequest req) {
        return ApiResponse.ok("Comment added", feedService.comment(currentUser.id(), id, req.content()));
    }
}
