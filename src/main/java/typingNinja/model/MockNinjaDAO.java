package typingNinja.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MockNinjaDAO implements INinjaContactDAO {

    private static final ArrayList<NinjaUser> NinjaUsers = new ArrayList<>();
    private static int autoIncrementedID = 0;

    @Override
    public void addNinjaUser(NinjaUser ninjaUser) {
        ninjaUser.setId(autoIncrementedID);
        autoIncrementedID++;
        NinjaUsers.add(ninjaUser);
    }

    @Override
    public void updateNinjaUser(NinjaUser ninjaUser) {
        for (int i = 0; i < NinjaUsers.size(); i++) {
            if (NinjaUsers.get(i).getId() == ninjaUser.getId()) {
                NinjaUsers.set(i, ninjaUser);
                break;
            }
        }
    }

    @Override
    public void deleteNinjaUser(NinjaUser ninjaUser) {
        NinjaUsers.remove(ninjaUser);
    }

    @Override
    public NinjaUser getNinjaUser(String userName) {
        for (NinjaUser ninjaUser : NinjaUsers) {
            if (Objects.equals(ninjaUser.getUserName(), userName)) {
                return ninjaUser;
            }
        }
        return null;
    }

    @Override
    public List<NinjaUser> getAllNinjas() {
        return new ArrayList<>(NinjaUsers);
    }

    public static void clearAll() {
        NinjaUsers.clear();
        autoIncrementedID = 0;
    }

    @Override
    public void safeInitUserData(int userId) {
    }

    @Override
    public void recalcUserStatistics(int userId) {
    }
}
