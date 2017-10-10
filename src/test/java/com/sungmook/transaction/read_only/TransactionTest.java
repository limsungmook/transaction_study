package com.sungmook.transaction.read_only;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Java6Assertions.assertThat;

@Slf4j
@ActiveProfiles(profiles = "mysql")
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

        @Override
        @Transactional
        public void save(boolean fireUserException, boolean firePostException) {
            User user1 = User.builder().name("user 1").build();
            userRepository.save(user1);
            postService.save(user1, firePostException);
        }
    }

    @Service
    static class PostServiceImpl implements PostService {
        @Autowired
        PostRepository postRepository;

        @Override
        @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
        public void save(User user, boolean firePostException) {
            Post post = Post.builder().content("Hello World").build();
            postRepository.save(post);
        }
    }

    @Autowired
    private UserService userService;
    @Autowired
    private PostService postService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PostRepository postRepository;

    @After
    public void tearDown() throws Exception {
        postRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void test__save() throws Exception {
        userService.save(false, false);
        assertThat(userRepository.findAll().size()).isEqualTo(1);
        assertThat(postRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    public void test__save_post() throws Exception {
        postService.save(null, false);
        assertThat(postRepository.findAll().size()).isEqualTo(0);
    }

}
