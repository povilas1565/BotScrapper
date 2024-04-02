package com.example.scrapper.transformers;

/**
 * Base transformer
 *
 * @param <E> Entity type
 * @param <D> Dto type
 *
 * @author Ilya Viaznin
 */
public interface Transformer<E, D> {

    D entityToDto(E entity);

    E dtoToEntity(D dto);
}
