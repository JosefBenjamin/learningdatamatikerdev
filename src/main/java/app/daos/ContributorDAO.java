package app.daos;

import app.entities.Contributor;

import java.util.List;
import java.util.Set;

public class ContributorDAO implements ICRUD<Contributor> {
    @Override
    public Contributor persist(Contributor entity) {
        return null;
    }

    @Override
    public Set<Contributor> persistList(List<Contributor> entities) {
        return Set.of();
    }

    @Override
    public Contributor findById(Long id) {
        return null;
    }

    @Override
    public Set<Contributor> retrieveAll() {
        return Set.of();
    }

    @Override
    public Contributor update(Contributor entity) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
