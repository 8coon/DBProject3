package coon.models;


import coon.models.data.ForumData;
import coon.models.data.ThreadData;
import coon.models.data.UserData;
import coon.models.data.VoiceData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Repository
@Transactional
public class Threads {

    private JdbcTemplate jdbc;
    private Users users;
    private Forums forums;


    @Autowired
    public Threads(JdbcTemplate jdbc, Users users, Forums forums) {
        this.jdbc = jdbc;
        this.users = users;
        this.forums = forums;
    }


    public ThreadData create(ThreadData thread, ForumData forum, UserData author) {
        thread.setAuthor(author.getNickname());
        thread.setForum(forum.getSlug());

        forum = this.forums.get(forum.getSlug());
        forum.setThreads(forum.getThreads() + 1);
        this.forums.set(forum);
        this.forums.addMember(forum.getSlug(), author.getNickname());

        if (thread.getCreated() == null) {
            thread.setCreated(LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            ));
        }

        return this.jdbc.queryForObject(
                "INSERT INTO Threads" +
                        "(author, created, forum, message, title, slug)" +
                        " VALUES (?, ?::TIMESTAMPTZ, ?, ?, ?, ?)" +
                        "RETURNING *",
                new ThreadData(),
                thread.getAuthor(), thread.getCreated(), thread.getForum(), thread.getMessage(), thread.getTitle(),
                thread.getSlug()
        );
    }


    public ThreadData get(int id) {
        return this.jdbc.queryForObject(
                "SELECT * FROM Threads WHERE id = ? LIMIT 1",
                new ThreadData(),
                id
        );
    }


    public ThreadData withSlug(String slug) {
        return this.jdbc.queryForObject(
                "SELECT * FROM Threads WHERE lower(slug) = lower(?) LIMIT 1",
                new ThreadData(),
                slug
        );
    }


    public List<ThreadData> all(String forum, String since, int limit, boolean desc) {
        return this.jdbc.query(
                "SELECT * FROM Threads " +
                        "WHERE lower(forum) = lower(?) AND " +
                        (since != null
                                ?
                                    (desc
                                            ?
                                                "created <= ?::TIMESTAMPTZ ORDER BY created DESC"
                                            :
                                                "created >= ?::TIMESTAMPTZ ORDER BY created ASC"
                                    )
                                :
                                    (desc
                                            ?
                                                "?::TEXT IS NULL ORDER BY created DESC"
                                            :
                                                "?::TEXT IS NULL ORDER BY created ASC"
                                    )
                        ) +
                        " LIMIT ?",
                new ThreadData(),
                forum, since, limit
        );
    }


    public ThreadData resolve(String slugOrId) {
        try {
            int id = Integer.valueOf(slugOrId);
            return this.get(id);
        } catch (NumberFormatException e1) {
            return this.withSlug(slugOrId);
        }
    }


    public ThreadData updateVotes(ThreadData thread) {
        this.jdbc.update(
                "UPDATE Threads SET votes = ? WHERE id = ?",
                thread.getVotes(), thread.getId()
        );

        return thread;
    }


    public ThreadData vote(UserData author, ThreadData thread, int voice) {
        try {
            VoiceData lastVoice = this.jdbc.queryForObject(
                    "SELECT * FROM Votes WHERE lower(author) = lower(?) AND thread = ? LIMIT 1",
                    new VoiceData(),
                    author.getNickname(), thread.getId()
            );

            thread.setVotes(thread.getVotes() + voice - lastVoice.getVoice());
            this.updateVotes(thread);

            this.jdbc.update(
                    "UPDATE Votes SET voice = ? WHERE lower(author) = lower(?) AND thread = ?",
                    voice, author.getNickname(), thread.getId()
            );
        } catch (EmptyResultDataAccessException e) {
            thread.setVotes(thread.getVotes() + voice);
            this.updateVotes(thread);

            this.jdbc.update(
                    "INSERT INTO Votes (author, thread, voice) VALUES (?, ?, ?)",
                    author.getNickname(), thread.getId(), voice
            );
        }

        return thread;
    }


    public ThreadData set(ThreadData thread, ThreadData data) {
        ThreadData newThread = thread.merge(data);

        return this.jdbc.queryForObject(
                "UPDATE Threads SET " +
                        "message = ?, title = ?" +
                        " WHERE id = ? RETURNING *",
                new ThreadData(),
                newThread.getMessage(), newThread.getTitle(), newThread.getId()
        );
    }


}
