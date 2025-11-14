package app.daos;

import java.util.List;
import java.util.Set;

public interface ICRUD <T> {
    T persist(T entity);
    Set<T>persistList(List<T> entities);
    T findById(Long id);
    Set<T> retrieveAll();
    T update(T entity);
    boolean delete(Long id);
}
