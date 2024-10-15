package com.colak.springtutorial.repository;

import com.colak.springtutorial.jpa.Post;
import io.r2dbc.spi.Blob;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@Testcontainers
@Slf4j
class PostRepositoryTest {

    @SuppressWarnings("resource")
    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:latest")
            .withCopyFileToContainer(MountableFile.forHostPath("docker-compose/init-db/init.sql"), "/docker-entrypoint-initdb.d/init.sql");


    // @ServiceConnection does not work
    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://"
                                               + postgreSQLContainer.getHost() + ":" + postgreSQLContainer.getFirstMappedPort()
                                               + "/" + postgreSQLContainer.getDatabaseName());
        registry.add("spring.r2dbc.username", () -> postgreSQLContainer.getUsername());
        registry.add("spring.r2dbc.password", () -> postgreSQLContainer.getPassword());
    }

    @Autowired
    private PostRepository repository;

    @Test
    void testByteBuffer() {
        String attachment = "testByteBuffer";
        Post post = Post.builder()
                .title("r2dbc")
                .attachment(ByteBuffer.wrap(attachment.getBytes()))
                .build();

        repository.save(post)
                .as(StepVerifier::create)
                .consumeNextWith(savedPost -> {
                            assertThat(savedPost.getTitle()).isEqualTo("r2dbc");
                            String savedAttachment = new String(savedPost.getAttachment().array());
                            assertThat(savedAttachment).isEqualTo(attachment);
                        }
                )
                .verifyComplete();
    }

    @Test
    void testByteArray() {
        String image = "testByteArray";
        Post post = Post.builder()
                .title("r2dbc")
                .coverImage(image.getBytes())
                .build();

        repository.save(post)
                .as(StepVerifier::create)
                .consumeNextWith(savedPost -> {
                            assertThat(savedPost.getTitle()).isEqualTo("r2dbc");
                            String attachment = new String(savedPost.getCoverImage());
                            assertThat(attachment).isEqualTo(image);
                        }
                )
                .verifyComplete();
    }

    @Test
    void testBlob() {
        String blob = "testBlob";
        var post = Post.builder()
                .title("r2dbc")
                .coverImageThumbnail(Blob.from(Mono.just(ByteBuffer.wrap(blob.getBytes()))))
                .build();

        repository.save(post)
                .as(StepVerifier::create)
                .consumeNextWith(savedPost -> {
                            assertThat(savedPost.getTitle()).isEqualTo("r2dbc");
                            CountDownLatch latch = new CountDownLatch(1);

                            Mono.from(savedPost.getCoverImageThumbnail().stream())
                                    .map(it -> new String(it.array()))
                                    .subscribe(it -> {
                                        assertThat(it).isEqualTo(blob);
                                        latch.countDown();
                                    });

                            try {
                                latch.await(1000, TimeUnit.MILLISECONDS);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .verifyComplete();
    }

}