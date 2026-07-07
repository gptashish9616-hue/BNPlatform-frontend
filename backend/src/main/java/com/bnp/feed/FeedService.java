package com.bnp.feed;

import com.bnp.common.exception.BadRequestException;
import com.bnp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final PostRepository postRepository;
    private final PostCommentRepository commentRepository;
    private final PostLikeRepository likeRepository;

    @Transactional
    public Post createPost(Long authorId, String content, String imageUrl) {
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Post content cannot be empty");
        }
        return postRepository.save(Post.builder()
                .authorId(authorId)
                .content(content)
                .imageUrl(imageUrl)
                .build());
    }

    public List<Post> feed() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> byUser(Long authorId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    private Post requirePost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", postId));
    }

    /** Toggle a like. Returns true if now liked, false if unliked. */
    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        Post post = requirePost(postId);
        var existing = likeRepository.findByPostIdAndUserId(postId, userId);
        boolean liked;
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            liked = false;
        } else {
            likeRepository.save(PostLike.builder().postId(postId).userId(userId).build());
            liked = true;
        }
        post.setLikeCount((int) likeRepository.countByPostId(postId));
        postRepository.save(post);
        return liked;
    }

    @Transactional
    public PostComment comment(Long authorId, Long postId, String content) {
        Post post = requirePost(postId);
        if (content == null || content.isBlank()) {
            throw new BadRequestException("Comment cannot be empty");
        }
        PostComment comment = commentRepository.save(PostComment.builder()
                .postId(postId)
                .authorId(authorId)
                .content(content)
                .build());
        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        return comment;
    }

    public List<PostComment> comments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }
}
