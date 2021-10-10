package guru.springframework.services;

import guru.springframework.commands_DTOs.IngredientCommand;


public interface IngredientService {

    IngredientCommand findByRecipeIdAndIngredientId(Long recipeId, Long ingredientId);
}
