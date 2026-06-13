package hr.fer.zemris.ferko.application.usecase.notice;

import hr.fer.zemris.ferko.application.port.NoticeRepository;
import hr.fer.zemris.ferko.domain.model.Notice;
import java.time.LocalDateTime;
import java.util.List;

/** Publishes and lists portal announcements ("obavijesti"). */
public class NoticeService {

  private static final int MAX_LIMIT = 100;

  private final NoticeRepository noticeRepository;

  public NoticeService(NoticeRepository noticeRepository) {
    this.noticeRepository = noticeRepository;
  }

  public List<NoticeView> recent(int limit) {
    int capped = Math.min(Math.max(limit, 1), MAX_LIMIT);
    return noticeRepository.findRecent(capped).stream().map(NoticeService::toView).toList();
  }

  public List<NoticeView> forCourse(long courseId) {
    return noticeRepository.findByCourse(courseId).stream().map(NoticeService::toView).toList();
  }

  public long publish(Long courseId, String title, String body, boolean pinned, String authorName) {
    Notice saved =
        noticeRepository.save(
            new Notice(0L, courseId, title, body, authorName, LocalDateTime.now(), pinned));
    return saved.id();
  }

  private static NoticeView toView(Notice notice) {
    return new NoticeView(
        notice.id(),
        notice.courseId(),
        notice.title(),
        notice.body(),
        notice.authorName(),
        notice.createdAt(),
        notice.pinned());
  }
}
