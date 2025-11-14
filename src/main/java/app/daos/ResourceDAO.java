package app.daos;

import app.entities.Resource;

import java.util.List;
import java.util.Set;

public class ResourceDAO implements ICRUD<Resource> {
    @Override
    public Resource persist(Resource entity) {
        return null;
    }

    @Override
    public Set<Resource> persistList(List<Resource> entities) {
        return Set.of();
    }

    @Override
    public Resource findById(Long id) {
        return null;
    }

    @Override
    public Set<Resource> retrieveAll() {
        return Set.of();
    }

    @Override
    public Resource update(Resource entity) {
        return null;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }
}
