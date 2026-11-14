package com.proftest.server.util;

import com.proftest.server.config.ServerConfig;
import com.proftest.server.model.*;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Паттерн Singleton — единственный экземпляр SessionFactory Hibernate.
 */
public class HibernateUtil {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static volatile HibernateUtil instance;
    private final SessionFactory sessionFactory;

    private HibernateUtil() {
        try {
            ServerConfig config = ServerConfig.getInstance();
            Configuration configuration = new Configuration();
            configuration.setProperty("hibernate.connection.driver_class", "com.mysql.cj.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url", config.getDbUrl());
            configuration.setProperty("hibernate.connection.username", config.getDbUser());
            configuration.setProperty("hibernate.connection.password", config.getDbPassword());
            configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
            configuration.setProperty("hibernate.show_sql", config.isShowSql() ? "true" : "false");
            configuration.setProperty("hibernate.hbm2ddl.auto", "validate");
            configuration.setProperty("hibernate.current_session_context_class", "thread");
            configuration.setProperty("hibernate.connection.pool_size", "10");
            configuration.setProperty("hibernate.connection.CharSet", "UTF-8");
            configuration.setProperty("hibernate.connection.characterEncoding", "UTF-8");
            configuration.setProperty("hibernate.connection.useUnicode", "true");

            configuration.addAnnotatedClass(Person.class);
            configuration.addAnnotatedClass(User.class);
            configuration.addAnnotatedClass(Role.class);
            configuration.addAnnotatedClass(Skill.class);
            configuration.addAnnotatedClass(Question.class);
            configuration.addAnnotatedClass(QuestionOption.class);
            configuration.addAnnotatedClass(Test.class);
            configuration.addAnnotatedClass(Assignment.class);
            configuration.addAnnotatedClass(TestAttempt.class);
            configuration.addAnnotatedClass(UserAnswer.class);

            sessionFactory = configuration.buildSessionFactory();
            logger.info("Hibernate SessionFactory initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize Hibernate", e);
            throw new RuntimeException("Hibernate initialization failed", e);
        }
    }

    public static HibernateUtil getInstance() {
        if (instance == null) {
            synchronized (HibernateUtil.class) {
                if (instance == null) {
                    instance = new HibernateUtil();
                }
            }
        }
        return instance;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            logger.info("Hibernate SessionFactory closed");
        }
    }
}
