package com.laser.ordermanage.order.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record CreateCommentRequest (

    @NotEmpty(message = "댓글 내용은 필수 입력값입니다.")
    @Pattern(regexp = "^[\\s\\S]{0,200}$", message = "댓글 내용의 최대 글자수는 200자입니다.")
    String content

) {}
