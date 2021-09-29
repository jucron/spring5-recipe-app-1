package guru.springframework.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude = {"recipe"})
@Entity
public class Notes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne //Not specifying a type, makes the Recipe as OWNER.
    private Recipe recipe;

    @Lob //Large-objects : makes this field to be a large String object (CLOB - character-large-object)
    private String recipeNotes;

}
