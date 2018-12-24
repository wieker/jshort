package com.example.jshort.services;

/*
    Author: Kirill Abramovich
*/

import com.example.jshort.entities.StatRecord;
import com.example.jshort.entities.UrlRecord;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.transaction.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;

@Service
@Scope(value = SCOPE_SINGLETON)
public class StatService {

    private ConcurrentLinkedQueue<Long> clickedEntities = new ConcurrentLinkedQueue<Long>();

    @Autowired
    @Qualifier("sessionFactory")
    private SessionFactory sessionFactory;

    @Async
    @Transactional
    public void logAsyncStat(Long urlId) {
        clickedEntities.add(urlId);
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void reportCurrentTime() {
        final ArrayList<Integer> counter = new ArrayList<Integer>();
        int size = clickedEntities.size();
        counter.add(size);
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (int i = 0; i < counter.get(0); i ++) {
                    clickedEntities.poll();
                }
            }

            @Override
            public void flush() {

            }
        });
        DateTime roundedDate = getRoundedDate();
        Iterator<Long> iterator = clickedEntities.iterator();
        for (int i = 0; i < size; i ++) {
            Long id = iterator.next();
            UrlRecord urlRecord = sessionFactory.getCurrentSession().find(UrlRecord.class, id);
            List<StatRecord> statRecords = sessionFactory.getCurrentSession()
                    .createQuery("from StatRecord where url_record_id = :urlId and dateTime = :dateTime")
                    .setParameter("urlId", id)
                    .setParameter("dateTime", roundedDate)
                    .getResultList();
            StatRecord statRecord;
            if (statRecords.size() == 0) {
                statRecord = new StatRecord();
                statRecord.setUrlRecord(urlRecord);
                statRecord.setCounter(1L);
                statRecord.setDateTime(roundedDate);
            } else {
                statRecord = statRecords.get(0);
                statRecord.setCounter(statRecord.getCounter() + 1);
            }

            if (statRecord.getId() == null) {
                sessionFactory.getCurrentSession().persist(statRecord);
            } else {
                sessionFactory.getCurrentSession().merge(statRecord);
            }
        }
    }

    DateTime getRoundedDate() {
        DateTime currentDateTime = new DateTime();
        int granularity = 30;
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(currentDateTime.toDate());
        calendar.set(Calendar.MINUTE, calendar.get(Calendar.MINUTE) / granularity * granularity);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return new DateTime(calendar.getTime());
    }

    @Transactional
    public Long getStatistics(String shortUrl, DateTime from, DateTime to) {
        String query = "select sum(s.counter) from StatRecord s where s.urlRecord.shortUrl = :shortUrl"
                + ((from != null) ? " and dateTime > :fromDate" : "")
                + ((to != null) ? " and dateTime < :toDate" : "");
        Query hibernateQuery = sessionFactory.getCurrentSession()
                .createQuery(query)
                .setParameter("shortUrl", shortUrl);
        if (from != null) {
            hibernateQuery.setParameter("fromDate", from);
        }
        if (to != null) {
            hibernateQuery.setParameter("toDate", to);
        }
        Long result = (Long) hibernateQuery.getSingleResult();
        return result != null ? result : 0;
    }

}
