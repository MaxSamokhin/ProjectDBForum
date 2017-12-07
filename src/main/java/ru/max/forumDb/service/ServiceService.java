package ru.max.forumDb.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ServiceService {

    private final JdbcTemplate jdbcTmp;

    public ServiceService(JdbcTemplate jdbcTmp) {
        this.jdbcTmp = jdbcTmp;
    }

    void clear() {
        jdbcTmp.update(ServiceRequest.truncateAll);
    }

    SeviceModel status() {

        return new SeviceModel(
                jdbcTmp.queryForObject(ServiceRequest.countUsers, Integer.class),
                jdbcTmp.queryForObject(ServiceRequest.countForum, Integer.class),
                jdbcTmp.queryForObject(ServiceRequest.countThread, Integer.class),
                jdbcTmp.queryForObject(ServiceRequest.countPosts, Integer.class));
    }
}

