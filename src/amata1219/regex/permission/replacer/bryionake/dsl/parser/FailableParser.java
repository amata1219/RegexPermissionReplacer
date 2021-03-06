package amata1219.regex.permission.replacer.bryionake.dsl.parser;

import amata1219.regex.permission.replacer.bryionake.adt.Either;

import java.util.function.Function;

public interface FailableParser<T> {

    Either<String, T> tryParse(String arg);

    default <U> FailableParser<U> append(Function<T, Either<String, U>> mapper) {
        return arg -> tryParse(arg).flatMap(mapper);
    }

}
