package com.proftest.common.dto;

import java.io.Serializable;

public class IdPayload implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    public IdPayload() {}
    public IdPayload(Long id) { this.id = id; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
