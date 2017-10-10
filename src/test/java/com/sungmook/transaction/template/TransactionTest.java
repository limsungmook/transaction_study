package com.sungmook.transaction.template;

import com.sungmook.transaction.PostService;
import com.sungmook.transaction.UserService;
import com.sungmook.transaction.model.Post;
import com.sungmook.transaction.model.User;
import com.sungmook.transaction.repository.PostRepository;
import com.sungmook.transaction.repository.UserRepository;
import com.sungmook.transaction.simple.TestConfiguration;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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
        UserRepository userRepository;
        @Autowired
        PostRepository postRepository;

        @Override
        public void save(boolean fireUserException, boolean firePostException) {

            User user1 = User.builder().name("user 1").build();
            User user2 = User.builder().name("user 2").build();
            Post post1 = Post.builder().content("Hello World").user(user1).build();

            userRepository.save(user1);
            userRepository.save(user2);
            postRepository.save(post1);
            if (fireUserException) {
                throw new RuntimeException();
            }
        }
    }

    @Service
    static class PostServiceImpl implements PostService {

        @Override
        public void save(User user, boolean firePostException) {

        }
    }

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void save() throws Exception {
        transactionTemplate.execute((TransactionCallback<Void>) status -> {
            userService.save(false, false);
            return null;
        });
        assertThat(userRepository.findAll().size()).isEqualTo(2);
    }

    @Test
    public void fired_exception_when_save() throws Exception {
        try {
            transactionTemplate.execute((TransactionCallback<Void>) status -> {
                userService.save(true, false);
                return null;
            });
        } catch (Exception ex) {
            // do nothing
        }
        assertThat(userRepository.findAll().size()).isEqualTo(0);
    }
}
