package hr.fer.zemris.ferko.application.usecase.notice;

import hr.fer.zemris.ferko.application.port.NoticeRepository;
import hr.fer.zemris.ferko.application.usecase.access.AccessControlService;
import hr.fer.zemris.ferko.domain.model.Notice;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Publishes, lists and deletes portal announcements ("obavijesti"). */
public class NoticeService {

  private static final int MAX_LIMIT = 100;

  /** Roles that may delete a faculty-wide (course-less) notice. */
  private static final Set<String> CAN_DELETE_FACULTY_WIDE = Set.of("ADMIN", "STUSLU");

  /** Outcome of a delete request, mapped to an HTTP status by the web layer. */
  public enum DeleteOutcome {
    DELETED,
    NOT_FOUND,
    FORBIDDEN
  }

  private final NoticeRepository noticeRepository;
  private final AccessControlService accessControl;

  public NoticeService(NoticeRepository noticeRepository, AccessControlService accessControl) {
    this.noticeRepository = noticeRepository;
    this.accessControl = accessControl;
  }

  public List<NoticeView> recent(int limit, String username, Collection<String> roles) {
    int capped = Math.min(Math.max(limit, 1), MAX_LIMIT);
    return noticeRepository.findRecent(capped).stream()
        .map(notice -> toView(notice, username, roles))
        .toList();
  }

  public List<NoticeView> forCourse(long courseId, String username, Collection<String> roles) {
    return noticeRepository.findByCourse(courseId).stream()
        .map(notice -> toView(notice, username, roles))
        .toList();
  }

  public long publish(Long courseId, String title, String body, boolean pinned, String authorName) {
    Notice saved =
        noticeRepository.save(
            new Notice(0L, courseId, title, body, authorName, LocalDateTime.now(), pinned));
    return saved.id();
  }

  /**
   * Deletes a notice on behalf of the given user. A faculty-wide notice may be removed by ADMIN or
   * STUSLU; a course notice by a global role or by staff teaching that course. Students never pass.
   */
  public DeleteOutcome delete(long noticeId, String username, Collection<String> roles) {
    Optional<Notice> existing = noticeRepository.findById(noticeId);
    if (existing.isEmpty()) {
      return DeleteOutcome.NOT_FOUND;
    }
    if (!canDelete(existing.get(), username, roles)) {
      return DeleteOutcome.FORBIDDEN;
    }
    noticeRepository.deleteById(noticeId);
    return DeleteOutcome.DELETED;
  }

  private boolean canDelete(Notice notice, String username, Collection<String> roles) {
    if (notice.courseId() == null) {
      return roles != null && roles.stream().anyMatch(CAN_DELETE_FACULTY_WIDE::contains);
    }
    return accessControl.canManageCourse(username, roles, notice.courseId());
  }

  private NoticeView toView(Notice notice, String username, Collection<String> roles) {
    return new NoticeView(
        notice.id(),
        notice.courseId(),
        notice.title(),
        notice.body(),
        notice.authorName(),
        notice.createdAt(),
        notice.pinned(),
        canDelete(notice, username, roles));
  }
}
