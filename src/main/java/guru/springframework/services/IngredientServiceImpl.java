package guru.springframework.services;

import guru.springframework.commands_DTOs.IngredientCommand;
import guru.springframework.converters.IngredientCommandToIngredient;
import guru.springframework.converters.IngredientToIngredientCommand;
import guru.springframework.domain.Ingredient;
import guru.springframework.domain.Recipe;
import guru.springframework.repositories.RecipeRepository;
import guru.springframework.repositories.UnitOfMeasureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final IngredientCommandToIngredient ingredientCommandToIngredient;
    private final RecipeRepository recipeRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand,
                                 IngredientCommandToIngredient ingredientCommandToIngredient,
                                 RecipeRepository recipeRepository, UnitOfMeasureRepository unitOfMeasureRepository) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.ingredientCommandToIngredient = ingredientCommandToIngredient;
        this.recipeRepository = recipeRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Override
    public IngredientCommand findByRecipeIdAndIngredientId(Long recipeId, Long ingredientId) {

        Optional<Recipe> recipeOptional = recipeRepository.findById(recipeId);

        //Getting recipe:
        if (!recipeOptional.isPresent()){
            //todo impl error handling
            log.error("recipe id not found. Id: " + recipeId);
        }

        Recipe recipe = recipeOptional.get();

        //Getting ingredient from recipe found:
        /*NOTES:
        Stream Filter(Predicate predicate): Intermediate Operation:  returns a stream consisting of the elements of this stream that match the given predicate
        Stream map(Function mapper) returns a stream consisting of the results of applying the given function to the elements of this stream
        Stream findFirst() returns an Optional (a container object) describing the first element of this stream*/
        Optional<IngredientCommand> ingredientCommandOptional = recipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getId().equals(ingredientId))
                .map(ingredient -> ingredientToIngredientCommand.convert(ingredient))
                .findFirst();

        if(!ingredientCommandOptional.isPresent()){
            //todo impl error handling
            log.error("Ingredient id not found: " + ingredientId);
        }

        return ingredientCommandOptional.get();
    }

    @Override
    @Transactional
    public IngredientCommand saveIngredientCommand(IngredientCommand command) {
        //Look for the Recipe of this particular ingredient
        Optional<Recipe> recipeOptional = recipeRepository.findById(command.getRecipeId());

        //if recipe is not found in repository
        if(!recipeOptional.isPresent()){

            //todo toss error if not found!
            log.error("Recipe not found for id: " + command.getRecipeId());
            return new IngredientCommand();
        } else {
            //if recipe was found, lets create an Optional containing this Ingredient
            Recipe recipe = recipeOptional.get();

            Optional<Ingredient> ingredientOptional = recipe.getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(command.getId()))
                    .findFirst();

            //if ingredient is found in this Recipe, we need to update it
            if(ingredientOptional.isPresent()){
                Ingredient ingredientFound = ingredientOptional.get();
                ingredientFound.setDescription(command.getDescription());
                ingredientFound.setAmount(command.getAmount());
                ingredientFound.setUom(unitOfMeasureRepository
                        .findById(command.getUom().getId())
                        .orElseThrow(() -> new RuntimeException("UOM NOT FOUND"))); //todo address this
            } else {
                //add new Ingredient to this recipe
                Ingredient ingredient = ingredientCommandToIngredient.convert(command);
                ingredient.setRecipe(recipe);
                recipe.addIngredient(ingredient);
            }

            //Persist recipe, with this new or updated ingredient
            Recipe savedRecipe = recipeRepository.save(recipe);

            //Creating Optional containing this Ingredient
            Optional<Ingredient> savedIngredientOptional = savedRecipe.getIngredients().stream()
                    .filter(recipeIngredients -> recipeIngredients.getId().equals(command.getId()))
                    .findFirst();

            //If ingredient is not found by Id, check by description (a gap because Ingredients can be repeated)
            if(!savedIngredientOptional.isPresent()){
                //not totally safe... But best guess
                savedIngredientOptional = savedRecipe.getIngredients().stream()
                        .filter(recipeIngredients -> recipeIngredients.getDescription().equals(command.getDescription()))
                        .filter(recipeIngredients -> recipeIngredients.getAmount().equals(command.getAmount()))
                        .filter(recipeIngredients -> recipeIngredients.getUom().getId().equals(command.getUom().getId()))
                        .findFirst();
            }

            //todo check for fail

            //If ingredient is found: return IngredientCommand to controller layer
            return ingredientToIngredientCommand.convert(savedIngredientOptional.get());
        }

    }

    @Override
    public void deleteById(Long recipeId, Long idToDelete) {

        log.debug("Deleting ingredient: " + recipeId + ":" + idToDelete);

        //Getting recipe
        Optional<Recipe> recipeOptional = recipeRepository.findById(recipeId);

        //If recipe was found
        if(recipeOptional.isPresent()){
            Recipe recipe = recipeOptional.get();
            log.debug("found recipe");
            //Getting ingredient:
            Optional<Ingredient> ingredientOptional = recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(idToDelete))
                    .findFirst();

            if(ingredientOptional.isPresent()){
                log.debug("found Ingredient");
                Ingredient ingredientToDelete = ingredientOptional.get();
                ingredientToDelete.setRecipe(null); //Is this necessary?
                recipe.getIngredients().remove(ingredientOptional.get());
                recipeRepository.save(recipe);
            }
        } else {
            log.debug("Recipe Id Not found. Id:" + recipeId);
        }
    }
}
