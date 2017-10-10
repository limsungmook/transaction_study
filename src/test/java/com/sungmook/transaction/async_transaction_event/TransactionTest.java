package com.sungmook.transaction.async_transaction_event;

import com.sungmook.transaction.TransactionCommittedEvent;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.TimeUnit;

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
        PostEventualService postService;
        @Autowired
        UserRepository userRepository;
        @Autowired
        private ApplicationEventPublisher publisher;

        @Override
        @Transactional
        public void save(boolean fireUserException, boolean firePostException) {

            User user1 = User.builder().name("user 1").build();
            User user2 = User.builder().name("user 2").build();

            userRepository.save(user1);
            userRepository.save(user2);

            publisher.publishEvent(new TransactionCommittedEvent(user1, firePostException));
            publisher.publishEvent(new TransactionCommittedEvent(user2, firePostException));

            if (fireUserException) {
                throw new RuntimeException();
            }
        }
    }

    interface PostEventualService {
        @Async
        @TransactionalEventListener
        void save(TransactionCommittedEvent userSaveEvent);
    }

    @Service
    static class PostServiceImpl implements PostEventualService {
        @Autowired
        PostRepository postRepository;


        @Override
        @Transactional
        public void save(TransactionCommittedEvent userSaveEvent) {
            Post post = Post.builder().content("Hello World").user(userSaveEvent.getUser()).build();
            postRepository.save(post);
            if (userSaveEvent.isFirePostException()) {
                throw new RuntimeException();
            }
        }
    }

    @Autowired
    private UserService userService;
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
    public void save() throws Exception {
        userService.save(false, false);
        TimeUnit.SECONDS.sleep(2);
        assertThat(userRepository.findAll().size()).isEqualTo(2);
        assertThat(postRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    public void fired_exception_in_user() throws Exception {
        try {
            userService.save(true, false);
            TimeUnit.SECONDS.sleep(2);
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
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception ex) {

        } finally {
            assertThat(userRepository.findAll().size()).isEqualTo(2);
            assertThat(postRepository.findAll().size()).isEqualTo(0);
        }
    }
}
