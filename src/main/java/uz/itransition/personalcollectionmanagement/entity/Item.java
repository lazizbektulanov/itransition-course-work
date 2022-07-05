package uz.itransition.personalcollectionmanagement.entity;

import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import uz.itransition.personalcollectionmanagement.entity.template.AbsEntity;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "items")
public class Item extends AbsEntity {

    @Column(nullable = false)
    private String name;

    private String imgUrl;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToMany(fetch = FetchType.EAGER)
    private List<Tag> tags;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User createdBy;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Collection collection;


    public Item(String name, List<Tag> tags, User createdBy, Collection collection) {
        this.name = name;
        this.tags = tags;
        this.createdBy = createdBy;
        this.collection = collection;
    }

}
