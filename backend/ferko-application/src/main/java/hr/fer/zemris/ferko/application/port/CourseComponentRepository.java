package hr.fer.zemris.ferko.application.port;

import hr.fer.zemris.ferko.domain.model.CourseComponent;
import java.util.List;

/** Persistence port for course content components ("KOMPONENTE"). */
public interface CourseComponentRepository {

  CourseComponent save(CourseComponent component);

  List<CourseComponent> findByCourse(long courseId);
}
