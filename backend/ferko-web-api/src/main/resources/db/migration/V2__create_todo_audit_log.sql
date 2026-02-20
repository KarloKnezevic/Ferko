create sequence if not exists todo_audit_log_seq start with 1 increment by 1;

create table if not exists todo_audit_log (
  id bigint default nextval('todo_audit_log_seq') primary key,
  occurred_at timestamp not null default current_timestamp,
  action varchar(32) not null,
  outcome varchar(32) not null,
  actor_user_id bigint,
  task_id bigint,
  details varchar(1000) not null
);

create index if not exists idx_todo_audit_log_task_id on todo_audit_log(task_id);
create index if not exists idx_todo_audit_log_occurred_at on todo_audit_log(occurred_at);
