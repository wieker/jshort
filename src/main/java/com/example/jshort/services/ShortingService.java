package com.example.jshort.services;

import com.example.jshort.dto.ShortingRequest;
import com.example.jshort.entities.UrlRecord;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service("shortingService")
public class ShortingService {
    private static final int symbolBase = 62;

    @Autowired
    @Qualifier("sessionFactory")
    private SessionFactory sessionFactory;

    @Autowired
    private StatService statService;

    private UrlRecord getSavedIfExist(String longUrl) {
        Session session = sessionFactory.getCurrentSession();
        List<UrlRecord> records = (List<UrlRecord>) session.createQuery("select r from UrlRecord r where r.longUrl = :longUrl")
                .setParameter("longUrl", longUrl).getResultList();
        if (records.size() > 0) {
            return records.get(0);
        } else {
            UrlRecord record = new UrlRecord();
            record.setLongUrl(longUrl);
            session.persist(record);
            return record;
        }
    }

    private void update(UrlRecord urlRecord) {
        Session session = sessionFactory.getCurrentSession();
        session.merge(urlRecord);
    }

    private String generateUrlFromId(Long id) {
        StringBuilder builder = new StringBuilder();
        while (id > 0) {
            long symbol = id % symbolBase;
            id /= symbolBase;
            char value;
            if (symbol < 10) {
                value = (char) (48 + symbol);
            } else if (symbol < 36) {
                value = (char) (55 + symbol);
            } else {
                value = (char) (61 + symbol);
            }
            builder.append(value);
        }
        return builder.toString();
    }

    @Transactional
    public String saveInformation(ShortingRequest request) {
        UrlRecord urlRecord = getSavedIfExist(request.getLongUrl());
        if (urlRecord.getShortUrl() != null) {
            return urlRecord.getShortUrl();
        }
        urlRecord.setShortUrl(generateUrlFromId(urlRecord.getId()));
        update(urlRecord);
        return urlRecord.getShortUrl();
    }

    @Transactional
    public String getLongUrl(String shortUrl) {
        final List<UrlRecord> records = new ArrayList<UrlRecord>();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                statService.logAsyncStat(records.get(0).getId());
            }

            @Override
            public void flush() {

            }
        });
        Session session = sessionFactory.getCurrentSession();
        List<UrlRecord> resultFromDB = session
                .createQuery("from UrlRecord where shortUrl = :shortUrl")
                .setParameter("shortUrl", shortUrl)
                .getResultList();
        if (resultFromDB.size() == 0) {
            throw new NoResultException();
        }
        UrlRecord record = (UrlRecord) resultFromDB.get(0);
        records.add(record);
        return record.getLongUrl();
    }

}
