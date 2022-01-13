package org.precise.raproto;

public interface DatabaseObserver {
    void registerDatabaseObserver(DatabaseObserver dbObserver);
    void unregisterDatabaseObserver(DatabaseObserver dbObserver);
    void alertStatusChange();

}
