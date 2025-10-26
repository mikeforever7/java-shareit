package ru.practicum.shareit.item.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-controllers.
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "items")
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column
    private String name;
    @Column
    private String description;
    @Column
    private Boolean available;

    @OneToMany(mappedBy = "item", fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Comment> comments = new ArrayList<>();
}