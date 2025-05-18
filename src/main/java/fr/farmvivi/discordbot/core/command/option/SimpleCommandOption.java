package fr.farmvivi.discordbot.core.command.option;

import fr.farmvivi.discordbot.core.api.command.option.CommandOption;
import fr.farmvivi.discordbot.core.api.command.option.OptionChoice;
import fr.farmvivi.discordbot.core.api.command.option.OptionType2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Implementation of CommandOption with typed value.
 *
 * @param <T> the type of the option value
 */
public record SimpleCommandOption<T>(
        String name,
        String description,
        OptionType2 type,
        boolean required,
        List<OptionChoice<T>> choices,
        Predicate<T> validator,
        Function<String, List<OptionChoice<T>>> autocompleteProvider,
        Number minValue,
        Number maxValue,
        Integer minLength,
        Integer maxLength
) implements CommandOption<T> {

    /**
     * Creates a new SimpleCommandOption with the provided parameters.
     */
    public SimpleCommandOption {
        // Ensure immutable collections
        choices = choices != null ? List.copyOf(choices) : List.of();
        
        // Validate required fields
        Objects.requireNonNull(name, "Option name cannot be null");
        Objects.requireNonNull(description, "Option description cannot be null");
        Objects.requireNonNull(type, "Option type cannot be null");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public OptionType2 getType() {
        return type;
    }

    @Override
    public boolean isRequired() {
        return required;
    }

    @Override
    public List<OptionChoice<T>> getChoices() {
        return choices;
    }

    @Override
    public Predicate<T> getValidator() {
        return validator;
    }

    @Override
    public Function<String, List<OptionChoice<T>>> getAutocompleteProvider() {
        return autocompleteProvider;
    }

    @Override
    public Number getMinValue() {
        return minValue;
    }

    @Override
    public Number getMaxValue() {
        return maxValue;
    }

    @Override
    public Integer getMinLength() {
        return minLength;
    }

    @Override
    public Integer getMaxLength() {
        return maxLength;
    }

    /**
     * Builder for creating SimpleCommandOption instances.
     *
     * @param <T> the type of the option value
     */
    public static class Builder<T> {
        private String name;
        private String description;
        private OptionType2 type;
        private boolean required;
        private final List<OptionChoice<T>> choices = new ArrayList<>();
        private Predicate<T> validator;
        private Function<String, List<OptionChoice<T>>> autocompleteProvider;
        private Number minValue;
        private Number maxValue;
        private Integer minLength;
        private Integer maxLength;

        /**
         * Sets the name of the option.
         *
         * @param name the option name
         * @return this builder
         */
        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the description of the option.
         *
         * @param description the option description
         * @return this builder
         */
        public Builder<T> description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the type of the option.
         *
         * @param type the option type
         * @return this builder
         */
        public Builder<T> type(OptionType2 type) {
            this.type = type;
            return this;
        }

        /**
         * Sets whether the option is required.
         *
         * @param required true if the option is required
         * @return this builder
         */
        public Builder<T> required(boolean required) {
            this.required = required;
            return this;
        }

        /**
         * Adds a choice to the option.
         *
         * @param choice the choice to add
         * @return this builder
         */
        public Builder<T> choice(OptionChoice<T> choice) {
            this.choices.add(choice);
            return this;
        }

        /**
         * Sets the choices for the option.
         *
         * @param choices the choices to set
         * @return this builder
         */
        public Builder<T> choices(List<OptionChoice<T>> choices) {
            this.choices.clear();
            this.choices.addAll(choices);
            return this;
        }

        /**
         * Sets the validator for the option.
         *
         * @param validator the validator to set
         * @return this builder
         */
        public Builder<T> validator(Predicate<T> validator) {
            this.validator = validator;
            return this;
        }

        /**
         * Sets the autocomplete provider for the option.
         *
         * @param autocompleteProvider the autocomplete provider to set
         * @return this builder
         */
        public Builder<T> autocompleteProvider(Function<String, List<OptionChoice<T>>> autocompleteProvider) {
            this.autocompleteProvider = autocompleteProvider;
            return this;
        }

        /**
         * Sets the minimum value for the option.
         *
         * @param minValue the minimum value to set
         * @return this builder
         */
        public Builder<T> minValue(Number minValue) {
            this.minValue = minValue;
            return this;
        }

        /**
         * Sets the maximum value for the option.
         *
         * @param maxValue the maximum value to set
         * @return this builder
         */
        public Builder<T> maxValue(Number maxValue) {
            this.maxValue = maxValue;
            return this;
        }

        /**
         * Sets the minimum length for the option.
         *
         * @param minLength the minimum length to set
         * @return this builder
         */
        public Builder<T> minLength(Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        /**
         * Sets the maximum length for the option.
         *
         * @param maxLength the maximum length to set
         * @return this builder
         */
        public Builder<T> maxLength(Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        /**
         * Builds the option.
         *
         * @return the built option
         */
        public SimpleCommandOption<T> build() {
            if (name == null) {
                throw new IllegalArgumentException("Option name is required");
            }
            if (description == null) {
                throw new IllegalArgumentException("Option description is required");
            }
            if (type == null) {
                throw new IllegalArgumentException("Option type is required");
            }

            return new SimpleCommandOption<>(
                    name, description, type, required, choices, validator,
                    autocompleteProvider, minValue, maxValue, minLength, maxLength
            );
        }
    }
}
