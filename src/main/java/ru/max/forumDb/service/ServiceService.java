package ru.max.forumDb.service;


import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class ServiceService {

    private final JdbcTemplate jdbcTmp;

    public ServiceService(JdbcTemplate jdbcTmp) {
        this.jdbcTmp = jdbcTmp;
    }

    public void clear() {
        String sql = "TRUNCATE Forum CASCADE; " +
                "TRUNCATE Posts CASCADE; " +
                "TRUNCATE Thread CASCADE; " +
                "TRUNCATE Users CASCADE; " +
                "TRUNCATE Vote CASCADE; ";

        jdbcTmp.update(sql);

    }

    public SeviceModel status() {

        String sqlCountUsers = "select count(*) from Users";
        String sqlCountForum = "select count(*) from Forum";
        String sqlCountThread = "select count(*) from Thread";
        String sqlCountPosts = "select count(*) from Posts";

        return new SeviceModel(
                jdbcTmp.queryForObject(sqlCountUsers, Integer.class),
                jdbcTmp.queryForObject(sqlCountForum, Integer.class),
                jdbcTmp.queryForObject(sqlCountThread, Integer.class),
                jdbcTmp.queryForObject(sqlCountPosts, Integer.class));
    }
}

