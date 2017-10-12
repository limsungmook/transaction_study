package com.sungmook.transaction.entity_manager;

import com.sungmook.transaction.PostService;
import com.sungmook.transaction.UserService;
import com.sungmook.transaction.model.Post;
import com.sungmook.transaction.model.User;
import com.sungmook.transaction.repository.PostRepository;
import com.sungmook.transaction.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Java6Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        PropertySourcesPlaceholderConfigurer.class,
        TestConfiguration.class, TransactionTest.UserServiceImpl.class, TransactionTest.PostServiceImpl.class})
public class TransactionTest {

    @Service
    static class UserServiceImpl implements UserService {
        @Autowired
        PostService postService;
        @Autowired
        UserRepository userRepository;
        // EntityManagerFactory 가 inject 됨
        @PersistenceContext
        EntityManager entityManager;

        @Override
        public void save(boolean fireUserException, boolean firePostException) {

            User user1 = User.builder().name("user 1").build();
            User user2 = User.builder().name("user 2").build();

            // AbstractEntityManagerImpl 의 getProperties 를 Break Point 로 찍어서 확인해보면 EntityManager 의 실제 인스턴스가 어떤 것인지 볼 수 있다
            entityManager.getProperties();
            userRepository.save(user1);
            userRepository.save(user2);
            try {
                postService.save(user1, firePostException);
                postService.save(user2, firePostException);
            } catch (RuntimeException re){
                // nothing..
            }
            if (fireUserException) {
                throw new RuntimeException();
            }
        }
    }

    @Service
    static class PostServiceImpl implements PostService {
        @Autowired
        PostRepository postRepository;
        @PersistenceContext
        EntityManager entityManager;

        @Override
        public void save(User user, boolean firePostException) {
            entityManager.getProperties();
            Post post = Post.builder().content("Hello World").user(user).build();
            postRepository.save(post);
            if (firePostException) {
                throw new RuntimeException();
            }
        }
    }
    @After
    public void tearDown() throws Exception {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    @Test
    public void save() throws Exception {
        userService.save(false, false);
        assertThat(userRepository.findAll().size()).isEqualTo(2);
        assertThat(postRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    public void fired_exception_in_user() throws Exception {
        try {
            userService.save(true, false);
        } catch (Exception ex) {

        } finally {
            assertThat(userRepository.findAll().size()).isEqualTo(0);
            assertThat(postRepository.findAll().size()).isEqualTo(0);
        }
    }

    @Test
    public void fired_exception_in_post() throws Exception {
        try {
            userService.save(false, true);
        } catch (Exception ex) {

        } finally {
            assertThat(postRepository.findAll().size()).isEqualTo(0);
            assertThat(userRepository.findAll().size()).isEqualTo(0);
        }
    }
}
