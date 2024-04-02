package com.example.scrapper.validators;

import br.com.fluentvalidator.AbstractValidator;
import com.example.scrapper.domain.models.CrossPromotion;

import static br.com.fluentvalidator.predicate.StringPredicate.*;
import static br.com.fluentvalidator.predicate.LogicalPredicate.not;


public class CrossPromotionValidator extends AbstractValidator<CrossPromotion> {
    @Override
    public void rules() {
        ruleFor(CrossPromotion::getInviteLink)
                .must(stringContains("https://t.me/"))
                .when(not(stringEmptyOrNull()))
                .withMessage("Incorrect telegram invite link!");
    }
}