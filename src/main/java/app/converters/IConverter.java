package app.converters;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface IConverter<S, T> {

    T convert(S source);

    default List<T> convertList(Collection<S> sources) {
        if (sources == null || sources.isEmpty()) {
            return List.of();
        }
        return sources.stream()
                .map(x -> convert(x))
                .filter(x -> Objects.nonNull(x))
                .collect(Collectors.toList());
    }

}
