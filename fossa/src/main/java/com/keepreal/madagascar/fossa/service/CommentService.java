package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.CommentMessage;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.dao.CommentInfoRepository;
import com.keepreal.madagascar.fossa.model.CommentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
public class CommentService {

    private final CommentInfoRepository commentInfoRepository;
    private final LongIdGenerator idGenerator;

    @Autowired
    public CommentService(CommentInfoRepository commentInfoRepository,
                          LongIdGenerator idGenerator) {
        this.commentInfoRepository = commentInfoRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Retrieves comment message.
     *
     * @param commentInfo   {@link CommentInfo}.
     * @return  {@link CommentMessage}.
     */
    public CommentMessage getCommentMessage(CommentInfo commentInfo) {
        return CommentMessage.newBuilder()
                .setId(commentInfo.getId())
                .setFeedId(commentInfo.getFeedId())
                .setUserId(commentInfo.getUserId())
                .setContent(commentInfo.getContent())
                .setReplyToId(commentInfo.getReplyToId())
                .setCreatedAt(commentInfo.getCreatedTime())
                .setIsDeleted(commentInfo.getDeleted())
                .build();
    }

    /**
     *
     * @param feedId
     * @param commentCount
     * @return
     */
    public List<CommentMessage> getCommentsMessage(String feedId, int commentCount) {
        Pageable pageable = PageRequest.of(0, commentCount);
        List<CommentInfo> commentInfoList = commentInfoRepository.getCommentInfosByFeedIdAndDeletedIsFalseOrderByCreatedTimeDesc(feedId, pageable).getContent();

        return commentInfoList.stream().map(this::getCommentMessage).collect(Collectors.toList());
    }

    /**
     * Inserts the comment.
     *
     * @param commentInfo   {@link CommentInfo}.
     * @return  {@link CommentInfo}.
     */
    public CommentInfo insert(CommentInfo commentInfo) {
        commentInfo.setId(String.valueOf(idGenerator.nextId()));
        commentInfo.setCreatedTime(System.currentTimeMillis());
        return commentInfoRepository.insert(commentInfo);
    }

    /**
     * Updates the comment.
     *
     * @param commentInfo   {@link CommentInfo}.
     * @return  {@link CommentInfo}.
     */
    public CommentInfo update(CommentInfo commentInfo) {
        return commentInfoRepository.save(commentInfo);
    }

    /**
     * Retrieves comment by id and deleted is false.
     *
     * @param id    comment id.
     * @return  {@link CommentInfo}.
     */
    public CommentInfo findByIdAndDeletedIsFalse(String id) {
        return commentInfoRepository.findByIdAndDeletedIsFalse(id);
    }

    /**
     * Retrieves comments by ids.
     *
     * @param ids   ids.
     * @return  {@link CommentInfo}.
     */
    public List<CommentInfo> findByIdsAndDeletedIsFalse(Iterable<String> ids) {
        return this.commentInfoRepository.findByIdInAndDeletedIsFalse(ids);
    }

    /**
     * Retrieves pageabel comment by feed id order by create time desc.
     *
     * @param feedId    feed id.
     * @param pageable  {@link Pageable}.
     * @return  {@link CommentInfo}.
     */
    public Page<CommentInfo> getCommentInfosByFeedId(String feedId, Pageable pageable) {
        return commentInfoRepository.getCommentInfosByFeedIdAndDeletedIsFalseOrderByCreatedTimeDesc(feedId, pageable);
    }
}
