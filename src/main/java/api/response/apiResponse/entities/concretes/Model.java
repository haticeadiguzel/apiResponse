package api.response.apiResponse.entities.concretes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Table(name="models")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@ToString
public class Model {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "modelId")
    private int modelId;
    @Column(name = "id")
    private int id;
    @Column(name = "url")
    private String url;
    @Column(name = "type")
    private String type;
    @Column(name = "\"desc\"")
    private String desc;
    @Column(name = "source")
    private String source;
    @Column(name = "date")
    private String date;
    @Column(name = "criticalityLevel")
    private int criticality_level;
    @Column(name = "connectionType")
    private String connectiontype;
}
