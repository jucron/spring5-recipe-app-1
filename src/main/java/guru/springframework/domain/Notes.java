package guru.springframework.domain;

import javax.persistence.*;

@Entity
public class Notes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne //Not specifying a type, makes the Recipe as OWNER.
    private Recipe recipe;

    @Lob //Large-objects : makes this field to be a large String object (CLOB - character-large-object)
    private String recipeNotes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public String getRecipeNotes() {
        return recipeNotes;
    }

    public void setRecipeNotes(String recipeNotes) {
        this.recipeNotes = recipeNotes;
    }
}
