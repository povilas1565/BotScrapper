package com.example.scrapper.services;

import br.com.fluentvalidator.AbstractValidator;
import br.com.fluentvalidator.context.Error;

public interface ValidationService {

    /**
     * Validate and throw exception if data invalid
     *
     * @param validator Validator
     * @param data      Validating object
     * @param <TData>   Validating object type
     */
    default <TData> void validateAndThrowIfInvalid(AbstractValidator<TData> validator, TData data) {
        var validationResult = validator.validate(data);

        var message = validationResult.getErrors().stream()
                .map(Error::getMessage)
                .reduce((s1, s2) -> s1 + "\n" + s2);

        if (message.isPresent())
            throw new IllegalStateException(message.get());
    }
}
