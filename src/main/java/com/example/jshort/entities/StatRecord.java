package com.example.jshort.entities;

/*
    Author: Kirill Abramovich
*/

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;

@Entity
@Table(name = "stat_records")
public class StatRecord {

    @Id
    @Column(name="id")
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(targetEntity = UrlRecord.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "url_record_id")
    private UrlRecord urlRecord;

    @Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime dateTime;

    private Long counter;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UrlRecord getUrlRecord() {
        return urlRecord;
    }

    public void setUrlRecord(UrlRecord urlRecord) {
        this.urlRecord = urlRecord;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(DateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Long getCounter() {
        return counter;
    }

    public void setCounter(Long counter) {
        this.counter = counter;
    }
}
