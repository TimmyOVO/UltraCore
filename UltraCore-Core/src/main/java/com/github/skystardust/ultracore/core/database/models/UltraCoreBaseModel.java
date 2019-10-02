package com.github.skystardust.ultracore.core.database.models;

import io.ebean.EbeanServer;
import io.ebean.Model;
import io.ebean.bean.EntityBean;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class UltraCoreBaseModel extends Model {

    public abstract EbeanServer modelEbeanServer();

    public void markAsDirty() {
        modelEbeanServer().markAsDirty(this);
    }

    public void markPropertyUnset(String propertyName) {
        ((EntityBean) this)._ebean_getIntercept().setPropertyLoaded(propertyName, false);
    }

    public void save() {
        modelEbeanServer().save(this);
    }

    public void flush() {
        modelEbeanServer().flush();
    }

    public void update() {
        modelEbeanServer().update(this);
    }

    public void insert() {
        modelEbeanServer().insert(this);
    }

    public boolean delete() {
        return modelEbeanServer().delete(this);
    }

    public boolean deletePermanent() {
        return modelEbeanServer().deletePermanent(this);
    }

    public void refresh() {
        modelEbeanServer().refresh(this);
    }
}
