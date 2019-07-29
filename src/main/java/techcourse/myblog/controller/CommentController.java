package techcourse.myblog.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.view.RedirectView;

import techcourse.myblog.controller.argumentresolver.Redirection;
import techcourse.myblog.controller.argumentresolver.UserSession;
import techcourse.myblog.dto.CommentDto;
import techcourse.myblog.service.CommentService;

@Controller
@RequestMapping("/articles/{articleId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/")
    public RedirectView save(@PathVariable long articleId,
                             CommentDto commentDto,
                             UserSession userSession) {
        commentService.save(commentDto.toDomain(), userSession.getUser(), articleId);
        return new RedirectView("/articles/" + articleId);
    }

    @DeleteMapping("/{commentId}")
    public RedirectView delete(@PathVariable long commentId,
                               UserSession userSession,
                               Redirection redirection) {
        commentService.delete(commentId, userSession.getUser());
        return new RedirectView(redirection.getRedirectUrl());
    }
}

// - [ ] 댓글 수정 기능 구현 ``PUT /comments/{comment_id}``
// - [ ] 수정/삭제는 댓글 작성자만 가능