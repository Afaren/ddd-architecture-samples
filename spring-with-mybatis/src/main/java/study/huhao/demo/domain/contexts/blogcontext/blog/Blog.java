package study.huhao.demo.domain.contexts.blogcontext.blog;

import study.huhao.demo.domain.contexts.blogcontext.blog.exceptions.NoNeedToPublishException;
import study.huhao.demo.domain.core.concepts.AggregateRoot;
import study.huhao.demo.domain.core.concepts.ValueObject;

import java.time.Instant;
import java.util.UUID;

public class Blog implements AggregateRoot {
    private final UUID id;
    private String title;
    private String body;
    private final UUID authorId;
    private Status status;
    private final Instant createdAt;
    private Instant savedAt;
    private PublishedBlog published;

    // 全参构造函数仅用于数据结构转换或持久化框架
    public Blog(UUID id, String title, String body, UUID authorId, Status status, Instant createdAt, Instant savedAt, PublishedBlog published) {
        this.id = id;
        this.title = title;
        this.body = body;
        this.authorId = authorId;
        this.status = status;
        this.createdAt = createdAt;
        this.savedAt = savedAt;
        this.published = published;
    }

    public UUID getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getBody() {
        return this.body;
    }

    public UUID getAuthorId() {
        return this.authorId;
    }

    public Status getStatus() {
        return this.status;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getSavedAt() {
        return this.savedAt;
    }

    public PublishedBlog getPublished() {
        return this.published;
    }

    /* 这是一个反例，因为PublishedBlog已经做了严格的访问控制符和封装，所以可以使用新实例来实现Immutable Object，
       但是集合类型因为Java提供了能够修改其内容的API，所以一定要使用Immutable List
    public PublishedBlog getPublished() {
        // return immutable object to follow the encapsulation principle
        return this.published == null ? null : new PublishedBlog(
                this.published.getTitle(),
                this.published.getBody(),
                this.published.getPublishedAt());
    }*/

    Blog(String title, String body, UUID authorId) {
        validateTitle(title);
        validateAuthor(authorId);

        this.id = UUID.randomUUID();
        this.title = title;
        this.body = body;
        this.authorId = authorId;
        this.status = Status.Draft;
        // FIXME: Java的Instant精度（纳秒）与MySQL的Timestamp精度（微秒）不一致，会导致从数据库取出的Instant精度降低。
        this.savedAt = this.createdAt = Instant.now();
    }

    void publish() {
        validateIsNeedToPublish();

        this.published = new PublishedBlog(this.title, this.body, Instant.now());
        this.status = Status.Published;
    }

    void saveDraft(String title, String body) {
        validateTitle(title);

        this.title = title;
        this.body = body;
        this.savedAt = Instant.now();
    }

    boolean isPublished() {
        return this.published != null;
    }

    private void validateAuthor(UUID author) {
        if (author == null) {
            throw new IllegalArgumentException("the author cannot be null");
        }
    }

    private void validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("the title cannot be null or no content");
        }
    }

    private void validateIsNeedToPublish() {
        if (this.status == Status.Published) {
            boolean noChange =
                    this.title.equals(this.published.getTitle()) && this.body.equals(this.published.getBody());
            if (noChange) {
                throw new NoNeedToPublishException();
            }
        }
    }

    public enum Status implements ValueObject {
        Draft,
        Published
    }

}
