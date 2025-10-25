package typingNinja.model;

import java.util.List;

/**
 * Interface for managing NinjaUser data in a data store.
 */
public interface INinjaContactDAO {

    /**
     * Adds a new NinjaUser to the data store.
     *
     * @param ninjaUser the NinjaUser object to be added
     */
    void addNinjaUser(NinjaUser ninjaUser);

    /**
     * Updates an existing NinjaUser in the data store.
     *
     * @param ninjaUser the NinjaUser object with updated information
     */
    void updateNinjaUser(NinjaUser ninjaUser);

    /**
     * Deletes a NinjaUser from the data store.
     *
     * @param ninjaUser the NinjaUser object to be deleted
     */
    void deleteNinjaUser(NinjaUser ninjaUser);

    /**
     * Retrieves a NinjaUser by their username.
     *
     * @param userName the username of the NinjaUser to retrieve
     * @return the NinjaUser object corresponding to the given username,
     *         or null if no such user exists
     */
    NinjaUser getNinjaUser(String userName);

    /**
     * Retrieves a list of all NinjaUsers in the data store.
     *
     * @return a List of all NinjaUser objects
     */
    List<NinjaUser> getAllNinjas();

    /**
     * Safely initializes user data for a given user ID.
     * This may include setting default values or ensuring required
     * data structures are present.
     *
     * @param userId the ID of the user whose data should be initialized
     */
    void safeInitUserData(int userId);

    /**
     * Recalculates statistics for a given user.
     * This could include metrics like score, level, or activity stats.
     *
     * @param userId the ID of the user whose statistics should be recalculated
     */
    void recalcUserStatistics(int userId);
}
