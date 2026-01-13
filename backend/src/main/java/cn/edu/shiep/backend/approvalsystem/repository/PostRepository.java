package cn.edu.shiep.backend.approvalsystem.repository;

import cn.edu.shiep.backend.approvalsystem.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByPostCode(String postCode);
    
    Optional<Post> findByPostName(String postName);
    
    List<Post> findByStatus(String status);
    
    List<Post> findByStatusOrderByPostSortAsc(String status);
}
