package techcourse.myblog.dto;

import javax.validation.constraints.NotBlank;

import lombok.Getter;
import techcourse.myblog.domain.Comment;

@Getter
public class CommentDto implements DtoUtils<Comment> {
    @NotBlank(message = "내용을 입력해주세요.")
    private String contents;

    public CommentDto(final String contents) {
        this.contents = contents;
    }

    @Override
    public Comment toDomain() {
        return new Comment(contents);
    }
}
