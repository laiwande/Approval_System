package cn.edu.shiep.backend.approvalsystem.controller;

import cn.edu.shiep.backend.approvalsystem.entity.Post;
import cn.edu.shiep.backend.approvalsystem.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/posts")
public class PostController {

    @Autowired
    private PostRepository postRepository;

    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postRepository.findAll();
        return ResponseEntity.ok(posts);
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        post.setCreateTime(LocalDateTime.now());
        if (post.getStatus() == null) {
            post.setStatus("0");
        }
        Post saved = postRepository.save(post);
        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        postRepository.deleteById(postId);
        return ResponseEntity.ok("岗位已删除");
    }
}
