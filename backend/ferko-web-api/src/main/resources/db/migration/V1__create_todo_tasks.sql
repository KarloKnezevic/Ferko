create sequence if not exists todo_task_seq start with 1 increment by 1;

create table if not exists todo_tasks (
    id bigint primary key,
    owner_id bigint not null,
    assignee_id bigint not null,
    title varchar(100) not null,
    description varchar(1000) not null default '',
    deadline timestamp not null,
    priority varchar(32) not null,
    status varchar(32) not null
);

create index if not exists idx_todo_tasks_owner_id on todo_tasks(owner_id);
create index if not exists idx_todo_tasks_assignee_id on todo_tasks(assignee_id);
create index if not exists idx_todo_tasks_deadline on todo_tasks(deadline);
