package api.response.apiResponse.entities.concretes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Table(name="addresses")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;
    @Column(name = "totalCount")
    private long totalCount;
    @Column(name = "count")
    private int count;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    List<Model> models;
    @Column(name = "page")
    private int page;
    @Column(name = "pageCount")
    private long pageCount;
}
