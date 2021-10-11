package guru.springframework.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private Integer prepTime;
    private Integer cookTime;
    private Integer servings;
    private String source;
    private String url;

    @Lob
    private String directions;

    //"mappedBy" is saying that the Class containing the field 'recipe' (called Ingredient)
    // will hold the foreign key. It is basically saying the foreign key will be allocated in
    // the other Entity of the relationship. This also avoids Hibernate to create an extra
    // joint-table unnecessarily.
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "recipe")
    private Set<Ingredient> ingredients = new HashSet<>();

    //Large-objects : makes this field to be a large Byte object (BLOB - Binary-large-object)
    //Lob annotation makes this data to store more than 255 characters
    @Lob
    private Byte[] image;

    @Enumerated(value = EnumType.STRING) //if you use 'ORDINAL', it will apply numbers to each enumeration (ex 1,2,3)
    private Difficulty difficulty;

    @OneToOne(cascade = CascadeType.ALL) //Owner.
    private Notes notes;

    @ManyToMany
    //NOTE THAT IF WE DON'T DO THIS BELOW, HIBERNATE WILL GENERATE A JOINT TABLE AUTOMATICALLY
    //Joint tables are automatically built in '...Many...' annotations, unless you select 'mapped by'
    @JoinTable(name = "recipe_category",
        joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    // GETTERS AND SETTERS (DELETED) ARE TAKEN CARED BY LOMBOK:

    //Refactoring setters and getters to apply bi-directional relationship (Helper Method)
    //It's important to encapsulate the logic in one spot
    //Note that LOMBOK cannot override these, since they are custom implemented.
    public void setNotes(Notes notes) {
        if (notes != null) {
            this.notes = notes;
            notes.setRecipe(this);
        }
    }

    public Recipe addIngredient(Ingredient ingredient){
        ingredient.setRecipe(this);
        this.ingredients.add(ingredient);
        return this;
    }



}
