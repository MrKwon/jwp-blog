package techcourse.myblog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import techcourse.myblog.controller.session.UserSessionManager;
import techcourse.myblog.domain.Article;
import techcourse.myblog.domain.Comment;
import techcourse.myblog.domain.User;
import techcourse.myblog.dto.CommentRequest;
import techcourse.myblog.dto.CommentResponse;
import techcourse.myblog.exception.UnauthorizedException;
import techcourse.myblog.service.ArticleService;
import techcourse.myblog.service.CommentService;

@RequestMapping("/comments")
@RestController
public class CommentRestController {

    private ArticleService articleService;
    private CommentService commentService;
    private UserSessionManager userSessionManager;

    @Autowired
    public CommentRestController(ArticleService articleService, CommentService commentService, UserSessionManager userSessionManager/*UserRepository userRepository*/) {
        this.articleService = articleService;
        this.commentService = commentService;
        this.userSessionManager = userSessionManager;
    }

    @GetMapping
    public List<CommentResponse> showAllComments(@RequestParam Long articleId) {
        List<Comment> savedComments = commentService.findAllByArticleId(articleId);
        List<CommentResponse> comments = new ArrayList<>();
        savedComments.forEach(savedComment -> {
            Long id = savedComment.getId();
            String contents = savedComment.getContents();
            Long authorId = savedComment.getAuthor().getId();
            String authorName = savedComment.getAuthor().getName();
            CommentResponse commentResponse = new CommentResponse(id, contents, authorId, authorName);
            comments.add(commentResponse);
        });
        return comments;
    }

    @GetMapping("/total")
    public int getCountOfComment(@RequestParam Long articleId) {
        return articleService.findCommentsByArticleId(articleId).size();
    }

    @PostMapping
    public CommentResponse save(@RequestBody CommentRequest commentRequest) {
        User user = userSessionManager.getUser();
        if (user == null) {
            throw new UnauthorizedException();
        }
        Article article = articleService.select(commentRequest.getArticleId());
        Comment comment = new Comment(commentRequest.getContents(), user, article);
        Comment savedComment = commentService.save(comment);
        CommentResponse commentResponse = new CommentResponse(
                savedComment.getId(),
                savedComment.getContents(),
                savedComment.getAuthor().getId(),
                savedComment.getAuthor().getName()
        );
        return commentResponse;
    }

    @PutMapping("/{commentId}")
    public CommentResponse put(@PathVariable Long commentId, @RequestBody CommentRequest commentRequest) {
        User user = userSessionManager.getUser();
        Comment editedComment = commentService.update(commentId, commentRequest, user);
        CommentResponse commentResponse = new CommentResponse(
                editedComment.getId(),
                editedComment.getContents(),
                editedComment.getAuthor().getId(),
                editedComment.getAuthor().getName()
        );
        return commentResponse;
    }

    @DeleteMapping("/{commentId}")
    public String delete(@PathVariable Long commentId) {
        User user = userSessionManager.getUser();
        commentService.delete2(commentId, user);
        return "success!";
    }

}
