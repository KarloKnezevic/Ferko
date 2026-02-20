const state = {
  me: null,
  workspace: null,
  activeSection: "dashboard",
  tableState: {},
  status: null,
};

const elements = {
  loginView: document.getElementById("login-view"),
  portalView: document.getElementById("portal-view"),
  loginForm: document.getElementById("login-form"),
  loginUsername: document.getElementById("login-username"),
  loginPassword: document.getElementById("login-password"),
  loginError: document.getElementById("login-error"),
  credentialCards: document.getElementById("credential-cards"),
  portalKicker: document.getElementById("portal-kicker"),
  portalTitle: document.getElementById("portal-title"),
  portalSubtitle: document.getElementById("portal-subtitle"),
  sessionPill: document.getElementById("session-pill"),
  logoutButton: document.getElementById("logout-button"),
  mainNav: document.getElementById("main-nav"),
  statusArea: document.getElementById("status-area"),
  sections: {
    dashboard: document.getElementById("section-dashboard"),
    courses: document.getElementById("section-courses"),
    calendar: document.getElementById("section-calendar"),
    points: document.getElementById("section-points"),
    exams: document.getElementById("section-exams"),
    exchange: document.getElementById("section-exchange"),
    administration: document.getElementById("section-administration"),
  },
};

const SECTION_ORDER = [
  "dashboard",
  "courses",
  "calendar",
  "points",
  "exams",
  "exchange",
  "administration",
];

const STATIC_DEMO_ACCOUNTS = [
  { username: "student.ana", password: "ferko123", role: "STUDENT", fullName: "Ana Horvat" },
  {
    username: "lecturer.marko",
    password: "ferko123",
    role: "LECTURER",
    fullName: "Marko Cupic",
  },
  {
    username: "assistant.iva",
    password: "ferko123",
    role: "ASSISTANT",
    fullName: "Iva Kovacevic",
  },
  { username: "stuslu.sara", password: "ferko123", role: "STUSLU", fullName: "Sara Peric" },
  { username: "admin.ferko", password: "ferko123", role: "ADMIN", fullName: "Ferko Admin" },
];

async function api(path, options = {}) {
  const method = options.method || "GET";
  const headers = {};
  if (options.body !== undefined) {
    headers["Content-Type"] = "application/json";
  }

  const response = await fetch(path, {
    method,
    credentials: "same-origin",
    headers,
    body: options.body !== undefined ? JSON.stringify(options.body) : undefined,
  });

  const raw = await response.text();
  let parsed;
  try {
    parsed = raw ? JSON.parse(raw) : null;
  } catch (error) {
    parsed = raw;
  }

  if (!response.ok) {
    const message =
      parsed && typeof parsed === "object" && parsed.error
        ? parsed.error
        : `Request failed (${response.status}).`;
    throw new Error(message);
  }

  return parsed;
}

function isRole(role) {
  return state.me && state.me.role === role;
}

function hasAction(actionType) {
  return !!state.workspace?.enabledActions?.includes(actionType);
}

function escapeHtml(value) {
  const text = value == null ? "" : String(value);
  return text
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

function toCellText(value) {
  if (value == null || value === "") {
    return "-";
  }
  if (Array.isArray(value)) {
    return value.join(", ");
  }
  if (typeof value === "object") {
    return Object.entries(value)
      .map(([key, inner]) => `${key}: ${inner}`)
      .join(" | ");
  }
  return String(value);
}

function formatRole(role) {
  const map = {
    STUDENT: "Student",
    LECTURER: "Lecturer",
    ASSISTANT: "Assistant",
    ADMIN: "Administrator",
    STUSLU: "Student Office",
  };
  return map[role] || role;
}

function statusBadge(status) {
  const normalized = String(status || "").toUpperCase();
  let cls = "ok";
  if (normalized === "PENDING") {
    cls = "warn";
  }
  if (normalized === "DENIED" || normalized === "REJECTED") {
    cls = "error";
  }
  return `<span class="badge ${cls}">${escapeHtml(status)}</span>`;
}

function setStatus(message, type = "ok") {
  state.status = { message, type };
  renderStatus();
}

function renderStatus() {
  if (!state.status) {
    elements.statusArea.innerHTML = "";
    return;
  }
  elements.statusArea.innerHTML = `<div class="status-toast ${state.status.type}">${escapeHtml(
    state.status.message
  )}</div>`;
}

function clearStatus() {
  state.status = null;
  renderStatus();
}

function tableState(tableId, defaultSortKey) {
  if (!state.tableState[tableId]) {
    state.tableState[tableId] = {
      query: "",
      sortKey: defaultSortKey,
      direction: "asc",
      page: 1,
      pageSize: 6,
    };
  }
  return state.tableState[tableId];
}

function tableModule(tableId, title, columns, rows, options = {}) {
  const table = tableState(tableId, columns[0]?.key || "");
  const query = table.query.trim().toLowerCase();

  const filteredRows = (rows || []).filter((row) => {
    if (!query) {
      return true;
    }
    const joined = columns
      .map((column) => {
        const value = column.sortValue ? column.sortValue(row) : row[column.key];
        return toCellText(value);
      })
      .join(" ")
      .toLowerCase();
    return joined.includes(query);
  });

  const sortedRows = [...filteredRows].sort((left, right) => {
    const selectedColumn = columns.find((column) => column.key === table.sortKey) || columns[0];
    const leftValue = selectedColumn.sortValue ? selectedColumn.sortValue(left) : left[selectedColumn.key];
    const rightValue =
      selectedColumn.sortValue ? selectedColumn.sortValue(right) : right[selectedColumn.key];

    const leftText = toCellText(leftValue).toLowerCase();
    const rightText = toCellText(rightValue).toLowerCase();

    if (leftText < rightText) {
      return table.direction === "asc" ? -1 : 1;
    }
    if (leftText > rightText) {
      return table.direction === "asc" ? 1 : -1;
    }
    return 0;
  });

  const totalPages = Math.max(1, Math.ceil(sortedRows.length / table.pageSize));
  if (table.page > totalPages) {
    table.page = totalPages;
  }

  const start = (table.page - 1) * table.pageSize;
  const pagedRows = sortedRows.slice(start, start + table.pageSize);

  const headerHtml = columns
    .map((column) => {
      const marker =
        table.sortKey === column.key ? (table.direction === "asc" ? " ▲" : " ▼") : "";
      return `<th><button type="button" data-table-control="sort" data-table-id="${tableId}" data-sort-key="${column.key}">${escapeHtml(
        column.label
      )}${marker}</button></th>`;
    })
    .join("");

  const rowsHtml =
    pagedRows.length === 0
      ? `<tr><td colspan="${columns.length}">${escapeHtml(
          options.emptyMessage || "No records available."
        )}</td></tr>`
      : pagedRows
          .map((row) => {
            const cells = columns
              .map((column) => {
                const rendered = column.render
                  ? column.render(row)
                  : escapeHtml(toCellText(row[column.key]));
                return `<td>${rendered}</td>`;
              })
              .join("");
            return `<tr>${cells}</tr>`;
          })
          .join("");

  return `
    <article class="module ${options.span || "span-12"}">
      <h3>${escapeHtml(title)}</h3>
      ${options.lead ? `<p class="module-lead">${escapeHtml(options.lead)}</p>` : ""}
      <div class="table-shell">
        <div class="table-controls">
          <input
            type="text"
            placeholder="${escapeHtml(options.searchPlaceholder || "Search")}" 
            value="${escapeHtml(table.query)}"
            data-table-control="search"
            data-table-id="${tableId}"
          />
          <p class="small-note">${sortedRows.length} item(s)</p>
        </div>
        <div class="table-scroll">
          <table>
            <thead><tr>${headerHtml}</tr></thead>
            <tbody>${rowsHtml}</tbody>
          </table>
        </div>
        <div class="table-pagination">
          <button class="btn btn-ghost" type="button" data-table-control="page" data-table-id="${tableId}" data-direction="prev" ${
    table.page <= 1 ? "disabled" : ""
  }>Previous</button>
          <span>Page ${table.page} / ${totalPages}</span>
          <button class="btn btn-ghost" type="button" data-table-control="page" data-table-id="${tableId}" data-direction="next" ${
    table.page >= totalPages ? "disabled" : ""
  }>Next</button>
        </div>
      </div>
    </article>`;
}

function optionTags(values, selectedValue = "") {
  return values
    .map((value) => {
      const selected = String(value.value) === String(selectedValue) ? "selected" : "";
      return `<option value="${escapeHtml(value.value)}" ${selected}>${escapeHtml(value.label)}</option>`;
    })
    .join("");
}

function renderNavigation() {
  const navigation = state.workspace?.navigation || [];
  const enabledItems = navigation.filter((item) => item.enabled);
  if (!enabledItems.find((item) => item.id === state.activeSection)) {
    state.activeSection = enabledItems[0]?.id || "dashboard";
  }

  elements.mainNav.innerHTML = enabledItems
    .map(
      (item) => `
      <button class="nav-btn ${state.activeSection === item.id ? "active" : ""}" 
        type="button" data-nav-target="${item.id}">
        ${escapeHtml(item.label)}
      </button>`
    )
    .join("");

  elements.mainNav.querySelectorAll("[data-nav-target]").forEach((button) => {
    button.addEventListener("click", () => {
      state.activeSection = button.dataset.navTarget;
      clearStatus();
      renderPortal();
    });
  });
}

function visibleSectionElement() {
  return elements.sections[state.activeSection];
}

function hideAllSections() {
  Object.values(elements.sections).forEach((section) => section.classList.add("hidden"));
}

function renderPortal() {
  if (!state.workspace || !state.me) {
    return;
  }

  elements.portalKicker.textContent = state.workspace.branding;
  elements.portalTitle.textContent = "FERKO Academic Portal";
  elements.portalSubtitle.textContent = state.workspace.subtitle;
  elements.sessionPill.textContent = `${state.me.fullName} · ${formatRole(state.me.role)}`;

  renderNavigation();
  hideAllSections();

  switch (state.activeSection) {
    case "dashboard":
      renderDashboardSection();
      break;
    case "courses":
      renderCoursesSection();
      break;
    case "calendar":
      renderCalendarSection();
      break;
    case "points":
      renderPointsSection();
      break;
    case "exams":
      renderExamsSection();
      break;
    case "exchange":
      renderExchangeSection();
      break;
    case "administration":
      renderAdministrationSection();
      break;
    default:
      state.activeSection = "dashboard";
      renderDashboardSection();
  }

  renderStatus();
  bindSectionInteractions();
}

function renderDashboardSection() {
  const section = elements.sections.dashboard;
  section.classList.remove("hidden");

  const cards = (state.workspace.dashboardCards || [])
    .map(
      (card) => `
      <article class="stat-card">
        <p class="small-note">${escapeHtml(card.label)}</p>
        <p class="value">${escapeHtml(card.value)}</p>
        <p class="hint">${escapeHtml(card.hint)}</p>
      </article>`
    )
    .join("");

  const myGroupRows = [];
  if (isRole("STUDENT") && state.workspace.students?.[0]) {
    const groups = state.workspace.students[0].groups || {};
    Object.entries(groups).forEach(([courseCode, group]) => {
      myGroupRows.push({ courseCode, group });
    });
  }

  section.innerHTML = `
    <div class="section-grid">
      <article class="module span-12">
        <h3>Role-based dashboard</h3>
        <p class="module-lead">
          Active semester: ${escapeHtml(state.workspace.currentSemester.code)} · ${escapeHtml(
    state.workspace.currentSemester.academicYear
  )} (${escapeHtml(state.workspace.currentSemester.term)}).
        </p>
        <div class="cards-grid">${cards}</div>
      </article>

      ${tableModule(
        "dashboard-my-courses",
        isRole("STUDENT") ? "Enrolled courses" : "Courses in your scope",
        [
          { key: "code", label: "Code" },
          { key: "name", label: "Course" },
          { key: "lecturer", label: "Lecturer" },
          { key: "assistant", label: "Assistant" },
          { key: "semesterCode", label: "Semester" },
        ],
        state.workspace.myCourses,
        { span: "span-8", searchPlaceholder: "Search course by code or name" }
      )}

      ${tableModule(
        "dashboard-activity",
        "Recent academic activity",
        [
          { key: "occurredAt", label: "Time" },
          { key: "actor", label: "Actor" },
          { key: "action", label: "Action" },
          { key: "details", label: "Details" },
        ],
        state.workspace.recentActivity,
        { span: "span-4", searchPlaceholder: "Search activity" }
      )}

      ${
        myGroupRows.length > 0
          ? tableModule(
              "dashboard-groups",
              "My group assignments",
              [
                { key: "courseCode", label: "Course" },
                { key: "group", label: "Group" },
              ],
              myGroupRows,
              { span: "span-6", searchPlaceholder: "Search groups" }
            )
          : ""
      }

      ${
        isRole("STUDENT")
          ? tableModule(
              "dashboard-grade-overview",
              "Grade components overview",
              [
                { key: "courseCode", label: "Course" },
                { key: "collectedPoints", label: "Collected" },
                { key: "maxPoints", label: "Maximum" },
                { key: "percentage", label: "Percent" },
                { key: "finalGrade", label: "Final grade" },
                {
                  key: "fullyPublished",
                  label: "Published",
                  render: (row) => statusBadge(row.fullyPublished ? "PUBLISHED" : "DRAFT"),
                },
              ],
              state.workspace.gradingOverview,
              { span: "span-6", searchPlaceholder: "Search grading data" }
            )
          : ""
      }
    </div>`;
}

function renderCoursesSection() {
  const section = elements.sections.courses;
  section.classList.remove("hidden");

  const semesterOptions = (state.workspace.semesters || []).map((semester) => ({
    value: semester.code,
    label: `${semester.code} (${semester.term})`,
  }));
  const courseOptions = (state.workspace.courses || []).map((course) => ({
    value: course.code,
    label: `${course.code} · ${course.name}`,
  }));

  const actionPanels = [];

  if (hasAction("CREATE_COURSE")) {
    actionPanels.push(`
      <article class="module span-6">
        <h3>Course management</h3>
        <p class="module-lead">Create a course and assign initial staff context.</p>
        <form class="inline-form" data-action-form data-action-type="CREATE_COURSE">
          <label>Course code<input name="code" type="text" required /></label>
          <label>Course name<input name="name" type="text" required /></label>
          <label>ECTS<input name="ects" type="number" min="1" max="15" value="6" /></label>
          <label>Semester
            <select name="semesterCode">${optionTags(semesterOptions, state.workspace.currentSemester.code)}</select>
          </label>
          <label>Lecturer username<input name="lecturerUsername" type="text" value="lecturer.marko" /></label>
          <label>Assistant username<input name="assistantUsername" type="text" value="assistant.iva" /></label>
          <button class="btn btn-primary" type="submit">Create course</button>
        </form>
      </article>
    `);

    actionPanels.push(`
      <article class="module span-6">
        <h3>Staff and groups</h3>
        <p class="module-lead">Assign course staff and define lecture/lab groups.</p>
        <form class="inline-form" data-action-form data-action-type="ASSIGN_STAFF">
          <label>Course
            <select name="courseCode">${optionTags(courseOptions)}</select>
          </label>
          <label>Lecturer username<input name="lecturerUsername" type="text" value="lecturer.marko" /></label>
          <label>Assistant username<input name="assistantUsername" type="text" value="assistant.iva" /></label>
          <button class="btn btn-ghost" type="submit">Assign staff</button>
        </form>
        <form class="inline-form" data-action-form data-action-type="DEFINE_GROUP">
          <label>Course
            <select name="courseCode">${optionTags(courseOptions)}</select>
          </label>
          <label>Group code<input name="groupCode" type="text" required /></label>
          <label>Type
            <select name="type">
              <option value="LECTURE">LECTURE</option>
              <option value="LAB">LAB</option>
            </select>
          </label>
          <label>Category
            <select name="category">
              <option value="regular">regular</option>
              <option value="klasicno">klasicno</option>
              <option value="demosi">demosi</option>
            </select>
          </label>
          <label>Capacity<input name="capacity" type="number" min="1" value="30" /></label>
          <button class="btn btn-primary" type="submit">Define group</button>
        </form>
      </article>
    `);
  } else if (hasAction("DEFINE_GROUP")) {
    actionPanels.push(`
      <article class="module span-6">
        <h3>Define groups</h3>
        <p class="module-lead">Extend group structure for your course context.</p>
        <form class="inline-form" data-action-form data-action-type="DEFINE_GROUP">
          <label>Course
            <select name="courseCode">${optionTags(courseOptions)}</select>
          </label>
          <label>Group code<input name="groupCode" type="text" required /></label>
          <label>Type
            <select name="type">
              <option value="LECTURE">LECTURE</option>
              <option value="LAB">LAB</option>
            </select>
          </label>
          <label>Category
            <select name="category">
              <option value="regular">regular</option>
              <option value="klasicno">klasicno</option>
              <option value="demosi">demosi</option>
            </select>
          </label>
          <label>Capacity<input name="capacity" type="number" min="1" value="24" /></label>
          <button class="btn btn-primary" type="submit">Save group</button>
        </form>
      </article>
    `);
  }

  if (hasAction("ENROLL_STUDENT") || hasAction("ASSIGN_STUDENT_GROUP")) {
    actionPanels.push(`
      <article class="module span-6">
        <h3>Student office enrollment tools</h3>
        <p class="module-lead">Manage enrollment and group assignment using JMBAG identity.</p>
        ${
          hasAction("ENROLL_STUDENT")
            ? `<form class="inline-form" data-action-form data-action-type="ENROLL_STUDENT">
                <label>Student JMBAG<input name="studentJmbag" type="text" required /></label>
                <label>Course
                  <select name="courseCode">${optionTags(courseOptions)}</select>
                </label>
                <button class="btn btn-primary" type="submit">Enroll student</button>
              </form>`
            : ""
        }
        ${
          hasAction("ASSIGN_STUDENT_GROUP")
            ? `<form class="inline-form" data-action-form data-action-type="ASSIGN_STUDENT_GROUP">
                <label>Student JMBAG<input name="studentJmbag" type="text" required /></label>
                <label>Course
                  <select name="courseCode">${optionTags(courseOptions)}</select>
                </label>
                <label>Group code<input name="groupCode" type="text" required /></label>
                <button class="btn btn-ghost" type="submit">Assign group</button>
              </form>`
            : ""
        }
      </article>
    `);
  }

  section.innerHTML = `
    <div class="section-grid">
      ${actionPanels.join("")}
      ${tableModule(
        "courses-all",
        "Course catalog",
        [
          { key: "code", label: "Code" },
          { key: "name", label: "Course" },
          { key: "semesterCode", label: "Semester" },
          { key: "lecturer", label: "Lecturer" },
          { key: "assistant", label: "Assistant" },
          {
            key: "groups",
            label: "Groups",
            render: (row) =>
              escapeHtml((row.groups || []).map((group) => `${group.groupCode} (${group.category})`).join(", ")),
            sortValue: (row) => (row.groups || []).length,
          },
        ],
        state.workspace.courses,
        {
          span: "span-12",
          searchPlaceholder: "Search by course code, title, staff or group",
          lead: "Real FERKO workflows: course creation, staff assignment, and group definition.",
        }
      )}

      ${tableModule(
        "courses-students",
        "Students and enrollments",
        [
          { key: "jmbag", label: "JMBAG" },
          { key: "fullName", label: "Student" },
          { key: "studyProgram", label: "Study" },
          { key: "yearOfStudy", label: "Year" },
          { key: "enrolledCourses", label: "Courses" },
          { key: "groups", label: "Groups" },
        ],
        state.workspace.students,
        { span: "span-12", searchPlaceholder: "Search by JMBAG, name, course, or group" }
      )}
    </div>`;
}

function renderCalendarSection() {
  const section = elements.sections.calendar;
  section.classList.remove("hidden");

  const syncButtons = hasAction("SYNC_LECTURES")
    ? `
      <div class="inline-row">
        <button class="btn btn-primary" type="button" data-action-button data-action-type="SYNC_LECTURES">Sync lectures</button>
        <button class="btn btn-ghost" type="button" data-action-button data-action-type="SYNC_LABS">Sync laboratories</button>
        <button class="btn btn-ghost" type="button" data-action-button data-action-type="SYNC_ROOMS">Sync rooms</button>
      </div>
      <p class="small-note">Synchronization includes timetable and room allocations.</p>`
    : "";

  section.innerHTML = `
    <div class="section-grid">
      <article class="module span-12">
        <h3>Academic calendar and schedule synchronization</h3>
        <p class="module-lead">
          Filtered per role and group context. Parallel laboratory events are shown separately for
          klasicno and demosi categories.
        </p>
        ${syncButtons}
      </article>

      ${tableModule(
        "calendar-lectures",
        "Lecture schedule",
        [
          { key: "courseCode", label: "Course" },
          { key: "weekday", label: "Weekday" },
          { key: "startsAt", label: "Start" },
          { key: "endsAt", label: "End" },
          { key: "groupCode", label: "Group" },
          { key: "room", label: "Room" },
          { key: "instructor", label: "Instructor" },
        ],
        state.workspace.lectureSchedule,
        { span: "span-6", searchPlaceholder: "Search lectures" }
      )}

      ${tableModule(
        "calendar-labs",
        "Laboratory schedule",
        [
          { key: "courseCode", label: "Course" },
          { key: "weekday", label: "Weekday" },
          { key: "startsAt", label: "Start" },
          { key: "endsAt", label: "End" },
          { key: "groupCode", label: "Group" },
          { key: "category", label: "Category" },
          { key: "room", label: "Room" },
        ],
        state.workspace.labSchedule,
        { span: "span-6", searchPlaceholder: "Search labs" }
      )}
    </div>`;
}

function renderPointsSection() {
  const section = elements.sections.points;
  section.classList.remove("hidden");

  const courseOptions = (state.workspace.courses || []).map((course) => ({
    value: course.code,
    label: `${course.code} · ${course.name}`,
  }));

  const controls = [];
  if (hasAction("ENTER_POINTS")) {
    controls.push(`
      <article class="module span-6">
        <h3>Enter points</h3>
        <p class="module-lead">Supports multiple grading components and flexible scoring.</p>
        <form class="inline-form" data-action-form data-action-type="ENTER_POINTS">
          <label>Course
            <select name="courseCode">${optionTags(courseOptions)}</select>
          </label>
          <label>Student JMBAG<input name="studentJmbag" type="text" required /></label>
          <label>Component<input name="component" type="text" value="Midterm" required /></label>
          <label>Points<input name="points" type="number" min="0" step="0.5" required /></label>
          <label>Max points<input name="maxPoints" type="number" min="1" step="0.5" value="40" required /></label>
          <button class="btn btn-primary" type="submit">Save points</button>
        </form>
      </article>
    `);
  }

  if (hasAction("PUBLISH_POINTS")) {
    controls.push(`
      <article class="module span-6">
        <h3>Publish results</h3>
        <p class="module-lead">Publish grading data per course once validation is complete.</p>
        <form class="inline-form" data-action-form data-action-type="PUBLISH_POINTS">
          <label>Course
            <select name="courseCode">${optionTags(courseOptions)}</select>
          </label>
          <button class="btn btn-warning" type="submit">Publish points</button>
        </form>
      </article>
    `);
  }

  section.innerHTML = `
    <div class="section-grid">
      ${controls.join("")}

      ${tableModule(
        "points-entries",
        "Points and grading components",
        [
          { key: "courseCode", label: "Course" },
          { key: "studentJmbag", label: "JMBAG" },
          { key: "studentName", label: "Student" },
          { key: "component", label: "Component" },
          { key: "points", label: "Points" },
          { key: "maxPoints", label: "Max" },
          { key: "percentage", label: "%" },
          {
            key: "published",
            label: "Published",
            render: (row) => statusBadge(row.published ? "PUBLISHED" : "DRAFT"),
            sortValue: (row) => (row.published ? "1" : "0"),
          },
          { key: "enteredBy", label: "Entered by" },
        ],
        state.workspace.points,
        { span: "span-12", searchPlaceholder: "Search points by course, student, component" }
      )}

      ${tableModule(
        "points-overview",
        "Final grading overview",
        [
          { key: "courseCode", label: "Course" },
          { key: "studentJmbag", label: "JMBAG" },
          { key: "studentName", label: "Student" },
          { key: "collectedPoints", label: "Collected" },
          { key: "maxPoints", label: "Max" },
          { key: "percentage", label: "%" },
          { key: "finalGrade", label: "Final grade" },
          {
            key: "fullyPublished",
            label: "Status",
            render: (row) => statusBadge(row.fullyPublished ? "PUBLISHED" : "DRAFT"),
            sortValue: (row) => (row.fullyPublished ? "1" : "0"),
          },
        ],
        state.workspace.gradingOverview,
        { span: "span-12", searchPlaceholder: "Search final grade overview" }
      )}
    </div>`;
}

function renderExamsSection() {
  const section = elements.sections.exams;
  section.classList.remove("hidden");

  const courseOptions = (state.workspace.courses || []).map((course) => ({
    value: course.code,
    label: `${course.code} · ${course.name}`,
  }));
  const examOptions = (state.workspace.exams || []).map((exam) => ({
    value: exam.id,
    label: `#${exam.id} · ${exam.courseCode} · ${exam.title}`,
  }));

  section.innerHTML = `
    <div class="section-grid">
      ${
        hasAction("CREATE_EXAM")
          ? `<article class="module span-6">
              <h3>Create exam schedule</h3>
              <p class="module-lead">Define room assignment and capacity for exam windows.</p>
              <form class="inline-form" data-action-form data-action-type="CREATE_EXAM">
                <label>Course
                  <select name="courseCode">${optionTags(courseOptions)}</select>
                </label>
                <label>Title<input name="title" type="text" value="Regular exam term" required /></label>
                <label>Date and time
                  <input name="dateTime" type="datetime-local" required />
                </label>
                <label>Room<input name="room" type="text" value="A-111" required /></label>
                <label>Capacity<input name="capacity" type="number" min="1" value="120" /></label>
                <button class="btn btn-primary" type="submit">Create exam</button>
              </form>
            </article>`
          : ""
      }

      ${
        hasAction("PUBLISH_EXAM_RESULTS")
          ? `<article class="module span-6">
              <h3>Publish exam results</h3>
              <p class="module-lead">Publish exam output once grading and room allocation are complete.</p>
              <form class="inline-form" data-action-form data-action-type="PUBLISH_EXAM_RESULTS">
                <label>Exam
                  <select name="examId">${optionTags(examOptions)}</select>
                </label>
                <button class="btn btn-warning" type="submit">Publish results</button>
              </form>
            </article>`
          : ""
      }

      ${tableModule(
        "exams-table",
        "Exam organization",
        [
          { key: "id", label: "Exam ID" },
          { key: "courseCode", label: "Course" },
          { key: "title", label: "Title" },
          { key: "dateTime", label: "Date/time" },
          { key: "room", label: "Room" },
          { key: "allocatedStudents", label: "Allocated" },
          { key: "capacity", label: "Capacity" },
          {
            key: "published",
            label: "Status",
            render: (row) => statusBadge(row.published ? "PUBLISHED" : "DRAFT"),
            sortValue: (row) => (row.published ? "1" : "0"),
          },
        ],
        state.workspace.exams,
        { span: "span-12", searchPlaceholder: "Search exams by course, room, or status" }
      )}
    </div>`;
}

function renderExchangeSection() {
  const section = elements.sections.exchange;
  section.classList.remove("hidden");

  const courseOptions = (state.workspace.myCourses || state.workspace.courses || []).map((course) => ({
    value: course.code,
    label: `${course.code} · ${course.name}`,
  }));
  const exchangeOptions = (state.workspace.groupExchanges || []).map((exchange) => ({
    value: exchange.id,
    label: `#${exchange.id} · ${exchange.courseCode} · ${exchange.studentName}`,
  }));

  section.innerHTML = `
    <div class="section-grid">
      ${
        hasAction("REQUEST_GROUP_EXCHANGE")
          ? `<article class="module span-6">
              <h3>Request group exchange</h3>
              <p class="module-lead">Student workflow for requesting lab group swaps.</p>
              <form class="inline-form" data-action-form data-action-type="REQUEST_GROUP_EXCHANGE">
                <label>Course
                  <select name="courseCode">${optionTags(courseOptions)}</select>
                </label>
                <label>From group<input name="fromGroup" type="text" required /></label>
                <label>To group<input name="toGroup" type="text" required /></label>
                <label>Reason<textarea name="reason"></textarea></label>
                <button class="btn btn-primary" type="submit">Submit request</button>
              </form>
            </article>`
          : ""
      }

      ${
        hasAction("DECIDE_GROUP_EXCHANGE")
          ? `<article class="module span-6">
              <h3>Exchange decision workflow</h3>
              <p class="module-lead">Administrative approval/rejection for group exchange requests.</p>
              <form class="inline-form" data-action-form data-action-type="DECIDE_GROUP_EXCHANGE">
                <label>Request
                  <select name="exchangeId">${optionTags(exchangeOptions)}</select>
                </label>
                <label>Decision
                  <select name="decision">
                    <option value="APPROVED">APPROVED</option>
                    <option value="REJECTED">REJECTED</option>
                  </select>
                </label>
                <button class="btn btn-warning" type="submit">Save decision</button>
              </form>
            </article>`
          : ""
      }

      ${tableModule(
        "exchange-table",
        "Group exchange system",
        [
          { key: "id", label: "Request ID" },
          { key: "courseCode", label: "Course" },
          { key: "studentJmbag", label: "JMBAG" },
          { key: "studentName", label: "Student" },
          { key: "fromGroup", label: "From" },
          { key: "toGroup", label: "To" },
          {
            key: "status",
            label: "Status",
            render: (row) => statusBadge(row.status),
          },
          { key: "decidedBy", label: "Decided by" },
          { key: "reason", label: "Reason" },
        ],
        state.workspace.groupExchanges,
        { span: "span-12", searchPlaceholder: "Search exchange requests" }
      )}
    </div>`;
}

function renderAdministrationSection() {
  const section = elements.sections.administration;
  section.classList.remove("hidden");

  const semesterOptions = (state.workspace.semesters || []).map((semester) => ({
    value: semester.code,
    label: `${semester.code} (${semester.term})`,
  }));

  const courseOptions = (state.workspace.courses || []).map((course) => ({
    value: course.code,
    label: `${course.code} · ${course.name}`,
  }));

  section.innerHTML = `
    <div class="section-grid">
      ${
        hasAction("CREATE_SEMESTER") || hasAction("ACTIVATE_SEMESTER")
          ? `<article class="module span-6">
              <h3>Semester lifecycle management</h3>
              <p class="module-lead">Create semesters and define the active semester for all workflows.</p>
              ${
                hasAction("CREATE_SEMESTER")
                  ? `<form class="inline-form" data-action-form data-action-type="CREATE_SEMESTER">
                      <label>Semester code<input name="code" type="text" placeholder="2026L" required /></label>
                      <label>Academic year<input name="academicYear" type="text" placeholder="2025/2026" required /></label>
                      <label>Term
                        <select name="term">
                          <option value="winter">winter</option>
                          <option value="summer">summer</option>
                        </select>
                      </label>
                      <label>Start date<input name="startDate" type="date" required /></label>
                      <label>End date<input name="endDate" type="date" required /></label>
                      <button class="btn btn-primary" type="submit">Create semester</button>
                    </form>`
                  : ""
              }
              ${
                hasAction("ACTIVATE_SEMESTER")
                  ? `<form class="inline-form" data-action-form data-action-type="ACTIVATE_SEMESTER">
                      <label>Activate semester
                        <select name="code">${optionTags(semesterOptions, state.workspace.currentSemester.code)}</select>
                      </label>
                      <button class="btn btn-warning" type="submit">Set active semester</button>
                    </form>`
                  : ""
              }
            </article>`
          : ""
      }

      ${
        hasAction("SYNC_STUDENTS")
          ? `<article class="module span-6">
              <h3>Synchronization controls</h3>
              <p class="module-lead">
                Run student/room/lecture/laboratory synchronization directly from administration.
              </p>
              <div class="inline-row">
                <button class="btn btn-primary" type="button" data-action-button data-action-type="SYNC_STUDENTS">Sync students</button>
                <button class="btn btn-ghost" type="button" data-action-button data-action-type="SYNC_ROOMS">Sync rooms</button>
                <button class="btn btn-ghost" type="button" data-action-button data-action-type="SYNC_LECTURES">Sync lectures</button>
                <button class="btn btn-ghost" type="button" data-action-button data-action-type="SYNC_LABS">Sync labs</button>
              </div>
              <p class="small-note">These operations mirror FERKO administrative synchronization workflows.</p>
            </article>`
          : ""
      }

      ${
        hasAction("IMPORT_STUDENTS")
          ? `<article class="module span-6">
              <h3>Bulk student import</h3>
              <p class="module-lead">
                One row per student, format: JMBAG, Full Name, Year. Example: 0036501222, Petra Novak, 1
              </p>
              <form class="inline-form" data-action-form data-action-type="IMPORT_STUDENTS">
                <label>Import lines
                  <textarea name="studentRows" placeholder="0036501222, Petra Novak, 1\n0036501223, Nika Simic, 2"></textarea>
                </label>
                <button class="btn btn-primary" type="submit">Import students</button>
              </form>
            </article>`
          : ""
      }

      ${
        hasAction("ENROLL_STUDENT")
          ? `<article class="module span-6">
              <h3>Student office enrollment management</h3>
              <p class="module-lead">Administrative enrollment and group assignment tools.</p>
              <form class="inline-form" data-action-form data-action-type="ENROLL_STUDENT">
                <label>Student JMBAG<input name="studentJmbag" type="text" required /></label>
                <label>Course
                  <select name="courseCode">${optionTags(courseOptions)}</select>
                </label>
                <button class="btn btn-primary" type="submit">Enroll student</button>
              </form>
              <form class="inline-form" data-action-form data-action-type="ASSIGN_STUDENT_GROUP">
                <label>Student JMBAG<input name="studentJmbag" type="text" required /></label>
                <label>Course
                  <select name="courseCode">${optionTags(courseOptions)}</select>
                </label>
                <label>Group code<input name="groupCode" type="text" required /></label>
                <button class="btn btn-ghost" type="submit">Assign group</button>
              </form>
            </article>`
          : ""
      }

      ${tableModule(
        "admin-semesters",
        "Semester governance",
        [
          { key: "code", label: "Code" },
          { key: "academicYear", label: "Academic year" },
          { key: "term", label: "Term" },
          { key: "startDate", label: "Start" },
          { key: "endDate", label: "End" },
          {
            key: "active",
            label: "Status",
            render: (row) => statusBadge(row.active ? "ACTIVE" : "INACTIVE"),
            sortValue: (row) => (row.active ? "1" : "0"),
          },
        ],
        state.workspace.semesters,
        { span: "span-12", searchPlaceholder: "Search semester lifecycle" }
      )}

      ${tableModule(
        "admin-sync",
        "Synchronization status",
        [
          { key: "operation", label: "Operation" },
          { key: "lastRunAt", label: "Last run" },
          {
            key: "status",
            label: "Status",
            render: (row) => statusBadge(row.status),
          },
        ],
        state.workspace.syncOperations,
        { span: "span-6", searchPlaceholder: "Search sync operations" }
      )}

      ${tableModule(
        "admin-permissions",
        "Permission management",
        [
          { key: "courseCode", label: "Course" },
          { key: "lecturer", label: "Lecturer" },
          { key: "assistant", label: "Assistant" },
          { key: "capabilities", label: "Capabilities" },
        ],
        state.workspace.permissions,
        { span: "span-6", searchPlaceholder: "Search permissions" }
      )}

      ${tableModule(
        "admin-users",
        "User management",
        [
          { key: "userId", label: "User ID" },
          { key: "username", label: "Username" },
          { key: "fullName", label: "Name" },
          { key: "role", label: "Role" },
          { key: "jmbag", label: "JMBAG" },
        ],
        state.workspace.users,
        { span: "span-12", searchPlaceholder: "Search users by role, name, or username" }
      )}
    </div>`;
}

function bindSectionInteractions() {
  const section = visibleSectionElement();
  if (!section) {
    return;
  }

  section.querySelectorAll("[data-table-control='search']").forEach((input) => {
    input.addEventListener("input", (event) => {
      const tableId = event.target.dataset.tableId;
      const config = tableState(tableId, "");
      config.query = event.target.value;
      config.page = 1;
      renderPortal();
    });
  });

  section.querySelectorAll("[data-table-control='sort']").forEach((button) => {
    button.addEventListener("click", () => {
      const tableId = button.dataset.tableId;
      const sortKey = button.dataset.sortKey;
      const config = tableState(tableId, sortKey);
      if (config.sortKey === sortKey) {
        config.direction = config.direction === "asc" ? "desc" : "asc";
      } else {
        config.sortKey = sortKey;
        config.direction = "asc";
      }
      renderPortal();
    });
  });

  section.querySelectorAll("[data-table-control='page']").forEach((button) => {
    button.addEventListener("click", () => {
      const tableId = button.dataset.tableId;
      const direction = button.dataset.direction;
      const config = tableState(tableId, "");
      config.page = direction === "next" ? config.page + 1 : Math.max(1, config.page - 1);
      renderPortal();
    });
  });

  section.querySelectorAll("[data-action-form]").forEach((form) => {
    form.addEventListener("submit", async (event) => {
      event.preventDefault();
      const actionType = form.dataset.actionType;
      const payload = buildPayloadFromForm(form, actionType);
      await runAction(actionType, payload);
    });
  });

  section.querySelectorAll("[data-action-button]").forEach((button) => {
    button.addEventListener("click", async () => {
      await runAction(button.dataset.actionType, {});
    });
  });
}

function buildPayloadFromForm(form, actionType) {
  const data = Object.fromEntries(new FormData(form).entries());

  if (actionType === "IMPORT_STUDENTS") {
    const rows = String(data.studentRows || "")
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter((line) => line.length > 0);

    return {
      students: rows.map((line) => {
        const parts = line.split(",").map((part) => part.trim());
        return {
          jmbag: parts[0] || "",
          fullName: parts[1] || "",
          yearOfStudy: Number(parts[2] || 1),
        };
      }),
    };
  }

  if (actionType === "CREATE_EXAM" && data.dateTime) {
    data.dateTime = `${data.dateTime}:00`;
  }

  const numericSuffixes = ["Id", "ects", "capacity", "points", "maxPoints", "yearOfStudy", "examId"];
  Object.keys(data).forEach((key) => {
    const value = data[key];
    if (value === "") {
      return;
    }

    if (numericSuffixes.some((suffix) => key.endsWith(suffix))) {
      const parsed = Number(value);
      if (!Number.isNaN(parsed)) {
        data[key] = parsed;
      }
    }
  });

  return data;
}

async function runAction(actionType, payload) {
  try {
    const response = await api("/api/v1/portal/actions", {
      method: "POST",
      body: {
        type: actionType,
        payload,
      },
    });

    state.workspace = response.workspace;
    state.me = state.workspace.me;
    setStatus(response.message, "ok");
    renderPortal();
  } catch (error) {
    setStatus(error.message, "error");
  }
}

function showPortal() {
  elements.loginView.classList.add("hidden");
  elements.portalView.classList.remove("hidden");
}

function showLogin() {
  elements.portalView.classList.add("hidden");
  elements.loginView.classList.remove("hidden");
}

async function loadWorkspace() {
  state.workspace = await api("/api/v1/portal/workspace");
  state.me = state.workspace.me;
}

async function tryRestoreSession() {
  try {
    const me = await api("/api/v1/portal/auth/me");
    state.me = me;
    state.activeSection = me.redirectSection || "dashboard";
    await loadWorkspace();
    showPortal();
    renderPortal();
  } catch (error) {
    state.me = null;
    state.workspace = null;
    showLogin();
  }
}

async function loadDemoAccounts() {
  try {
    const accounts = await api("/api/v1/portal/public/demo-accounts");
    renderCredentialCards(accounts);
  } catch (error) {
    renderCredentialCards(STATIC_DEMO_ACCOUNTS);
  }
}

function renderCredentialCards(accounts) {
  const rows = accounts && accounts.length > 0 ? accounts : STATIC_DEMO_ACCOUNTS;
  elements.credentialCards.innerHTML = rows
    .map(
      (account) => `
      <article class="credential-card">
        <h3>${escapeHtml(account.fullName)} · ${escapeHtml(formatRole(account.role))}</h3>
        <p class="credential-meta">username: <strong>${escapeHtml(account.username)}</strong></p>
        <p class="credential-meta">password: <strong>${escapeHtml(account.password)}</strong></p>
        <button class="btn btn-ghost" type="button" data-fill-login data-username="${escapeHtml(
          account.username
        )}" data-password="${escapeHtml(account.password)}">Use credentials</button>
      </article>
    `
    )
    .join("");

  elements.credentialCards.querySelectorAll("[data-fill-login]").forEach((button) => {
    button.addEventListener("click", () => {
      elements.loginUsername.value = button.dataset.username;
      elements.loginPassword.value = button.dataset.password;
      elements.loginUsername.focus();
    });
  });
}

async function handleLoginSubmit(event) {
  event.preventDefault();
  elements.loginError.textContent = "";

  const username = elements.loginUsername.value.trim();
  const password = elements.loginPassword.value.trim();
  if (!username || !password) {
    elements.loginError.textContent = "Username and password are required.";
    return;
  }

  try {
    const me = await api("/api/v1/portal/auth/login", {
      method: "POST",
      body: { username, password },
    });
    state.me = me;
    state.activeSection = me.redirectSection || "dashboard";
    await loadWorkspace();
    clearStatus();
    showPortal();
    renderPortal();
  } catch (error) {
    elements.loginError.textContent = error.message;
  }
}

async function handleLogout() {
  try {
    await api("/api/v1/portal/auth/logout", { method: "POST" });
  } catch (error) {
    // Ignore logout failures and clear local state regardless.
  }

  state.me = null;
  state.workspace = null;
  state.tableState = {};
  clearStatus();
  elements.loginForm.reset();
  showLogin();
}

function bindStaticEvents() {
  elements.loginForm.addEventListener("submit", handleLoginSubmit);
  elements.logoutButton.addEventListener("click", handleLogout);
}

async function boot() {
  bindStaticEvents();
  await loadDemoAccounts();
  await tryRestoreSession();
}

boot();
