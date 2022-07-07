package org.javaee7.jpa.dynamicnamedquery.entity;

import static jakarta.persistence.GenerationType.AUTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * A very simple JPA entity that will be used for testing
 * 
 * @author Arjan Tijms
 *
 */
@Entity
public class TestEntity {

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;
    @Column(name = "val")
    private String value;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
