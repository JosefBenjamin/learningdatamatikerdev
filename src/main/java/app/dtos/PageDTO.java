package app.dtos;

import java.util.List;

public record PageDTO<T>(
        List<T> content,
        int page,
        int limit,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageDTO<T> of(List<T> content, int page, int limit, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / limit);
        boolean hasNext = page < totalPages - 1;
        boolean hasPrevious = page > 0;
        return new PageDTO<>(content, page, limit, totalElements, totalPages, hasNext, hasPrevious);
    }
}
