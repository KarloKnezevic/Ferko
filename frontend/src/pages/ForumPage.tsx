import { useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api/client';
import { useI18n } from '../i18n';
import type { ForumPost } from '../api/types';

function PostBlock({
  post,
  replies,
  courseId,
}: {
  post: ForumPost;
  replies: ForumPost[];
  courseId: number;
}) {
  const { t } = useI18n();
  const queryClient = useQueryClient();
  const [replyOpen, setReplyOpen] = useState(false);
  const [text, setText] = useState('');
  const reply = useMutation({
    mutationFn: () => api.postForum(courseId, { parentId: post.id, body: text }),
    onSuccess: () => {
      setText('');
      setReplyOpen(false);
      queryClient.invalidateQueries({ queryKey: ['forum', courseId] });
    },
  });

  return (
    <div className="card">
      <div className="muted" style={{ fontSize: 12 }}>
        {post.authorName} · {new Date(post.createdAt).toLocaleString('hr-HR')}
      </div>
      <p style={{ whiteSpace: 'pre-wrap', margin: '6px 0' }}>{post.body}</p>

      {replies.map((r) => (
        <div key={r.id} className="forum-reply">
          <div className="muted" style={{ fontSize: 12 }}>
            {r.authorName} · {new Date(r.createdAt).toLocaleString('hr-HR')}
          </div>
          <p style={{ whiteSpace: 'pre-wrap', margin: '4px 0' }}>{r.body}</p>
        </div>
      ))}

      {!replyOpen ? (
        <button className="ghost" onClick={() => setReplyOpen(true)}>
          {t('forum.reply')}
        </button>
      ) : (
        <div style={{ marginTop: 8 }}>
          <textarea
            rows={2}
            value={text}
            onChange={(e) => setText(e.target.value)}
            style={{ width: '100%', padding: 8, borderRadius: 6, border: '1px solid var(--border)' }}
          />
          <button
            style={{ marginTop: 6 }}
            disabled={!text.trim() || reply.isPending}
            onClick={() => reply.mutate()}
          >
            {t('forum.send')}
          </button>
        </div>
      )}
    </div>
  );
}

export function ForumPage() {
  const { id } = useParams();
  const courseId = Number(id);
  const { t } = useI18n();
  const queryClient = useQueryClient();

  const forum = useQuery({ queryKey: ['forum', courseId], queryFn: () => api.courseForum(courseId) });

  const [text, setText] = useState('');
  const ask = useMutation({
    mutationFn: () => api.postForum(courseId, { parentId: null, body: text }),
    onSuccess: () => {
      setText('');
      queryClient.invalidateQueries({ queryKey: ['forum', courseId] });
    },
  });

  const posts = forum.data ?? [];
  const topLevel = posts.filter((p) => p.parentId == null);
  const repliesByParent: Record<number, ForumPost[]> = {};
  for (const p of posts) {
    if (p.parentId != null) (repliesByParent[p.parentId] ??= []).push(p);
  }

  return (
    <div>
      <div className="breadcrumb">
        <Link to="/kolegiji">{t('nav.courses')}</Link> ›{' '}
        <Link to={`/kolegiji/${courseId}`}>Kolegij</Link> › {t('forum.title')}
      </div>
      <h1>{t('forum.title')}</h1>

      <div className="card">
        <h2>{t('forum.ask')}</h2>
        <textarea
          rows={3}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder={t('forum.placeholder')}
          style={{ width: '100%', padding: 10, borderRadius: 6, border: '1px solid var(--border)' }}
        />
        <button style={{ marginTop: 10 }} disabled={!text.trim() || ask.isPending} onClick={() => ask.mutate()}>
          {t('forum.send')}
        </button>
      </div>

      {topLevel.length === 0 && <p className="muted">{t('forum.empty')}</p>}
      {topLevel.map((p) => (
        <PostBlock key={p.id} post={p} replies={repliesByParent[p.id] ?? []} courseId={courseId} />
      ))}
    </div>
  );
}
