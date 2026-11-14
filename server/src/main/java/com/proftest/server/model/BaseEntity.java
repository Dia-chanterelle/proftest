package com.proftest.server.model;

import java.io.Serializable;

/**
 * Базовый абстрактный класс для всех сущностей.
 * Обеспечивает общий контракт через абстрактный метод getId().
 */
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    public abstract Long getId();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseEntity that = (BaseEntity) o;
        if (getId() == null || that.getId() == null) return false;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
