package com.sungmook.transaction.open_in_view;

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
import org.springframework.boot.autoconfigure.web.DispatcherServletAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Java6Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {
        DispatcherServletAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        TransactionAutoConfiguration.class,
        PropertySourcesPlaceholderConfigurer.class,
        TestConfiguration.class, TransactionTest.UserServiceImpl.class, TransactionTest.PostServiceImpl.class, TransactionTest.TestController.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionTest {

    @RestController
    static class TestController {
        @Autowired
        private UserService userService;

        @GetMapping("/test")
        public String test(Boolean fireUserException, Boolean firePostException){
            userService.save(fireUserException, firePostException);
            return "Hello";
        }

        @GetMapping("/no-transaction")
        public String noTransaction(){
            return "Hello";
        }
    }


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
            try {
                postService.save(user1, firePostException);
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

        @Override
        @Transactional
        public void save(User user, boolean firePostException) {
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
    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void save() throws Exception {
        ResponseEntity<String> ret = restTemplate.getForEntity("/test?fireUserException={fireUserException}&firePostException={firePostException}", String.class, false, false);
        log.debug("Result : {}", ret.getBody());

        assertThat(userRepository.findAll().size()).isEqualTo(1);
        assertThat(postRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    public void invokeNoTransaction() throws Exception {
        ResponseEntity<String> ret = restTemplate.getForEntity("/no-transaction", String.class);
        log.debug("Result : {}", ret.getBody());
        restTemplate.getForEntity("/no-transaction", String.class);
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
